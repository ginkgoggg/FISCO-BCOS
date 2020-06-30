/*
 * @CopyRight:
 * FISCO-BCOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FISCO-BCOS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FISCO-BCOS.  If not, see <http://www.gnu.org/licenses/>
 * (c) 2016-2020 fisco-dev contributors.
 */
/**
 * @brief : implementation of VRF based rPBFTEngine
 * @file: VRFBasedrPBFTEngine.cpp
 * @author: yujiechen
 * @date: 2020-06-04
 */
#include "VRFBasedrPBFTEngine.h"
#include "Common.h"

using namespace dev::consensus;

void VRFBasedrPBFTEngine::setShouldRotateSealers(bool _shouldRotateSealers)
{
    m_shouldRotateSealers.store(_shouldRotateSealers);
}

// Working sealer is elected as leader in turn
IDXTYPE VRFBasedrPBFTEngine::selectLeader() const
{
    int64_t selectedIdx = ((m_view + m_highestBlock.number()) % m_workingSealersNum);
    dev::h512 selectedNodeId;
    {
        ReadGuard l(x_chosedSealerList);
        selectedNodeId = (*m_chosedSealerList)[selectedIdx];
    }
    {
        ReadGuard l(x_nodeID2Index);
        return (*m_nodeID2Index)[selectedNodeId];
    }
}

void VRFBasedrPBFTEngine::updateConsensusNodeList()
{
    ConsensusEngineBase::updateConsensusNodeList();
    // update chosedConsensusNode and x_chosedSealerList
    auto workingSealers = m_blockChain->workingSealerList();
    std::sort(workingSealers.begin(), workingSealers.end());

    UpgradableGuard l(x_chosedSealerList);
    bool chosedSealerListUpdated = false;
    if (workingSealers != *m_chosedSealerList)
    {
        chosedSealerListUpdated = true;
        UpgradeGuard ul(l);
        *m_chosedSealerList = workingSealers;
        m_workingSealersNum = workingSealers.size();
        std::string workingSealersStr;
        // TODO: remove this
        for (auto const& node : workingSealers)
        {
            workingSealersStr += node.abridged() + ",";
        }
        VRFRPBFTEngine_LOG(INFO) << LOG_DESC("updateConsensusNodeList: update working sealers")
                                 << LOG_KV("updatedWorkingSealers", m_workingSealersNum)
                                 << LOG_KV("workingSealers", workingSealersStr);
    }
    // update m_chosedConsensusNodes
    if (chosedSealerListUpdated)
    {
        auto lastEpocWorkingSealer = *m_chosedConsensusNodes;
        {
            WriteGuard l(x_chosedConsensusNodes);
            m_chosedConsensusNodes->clear();
            for (auto const& _node : *m_chosedSealerList)
            {
                m_chosedConsensusNodes->insert(_node);
            }
        }
        resetLocatedInConsensusNodes();
        forwardRemaingTxs(lastEpocWorkingSealer);
    }
    // the working sealers or the sealers have been updated
    if (chosedSealerListUpdated || m_sealerListUpdated)
    {
        // update consensusInfo when send block status by tree-topology
        updateConsensusInfo();
    }
    if (m_sealerListUpdated)
    {
        ReadGuard l(m_sealerListMutex);
        m_sealersNum = m_sealerList.size();
        WriteGuard lock(x_nodeID2Index);
        m_nodeID2Index->clear();
        IDXTYPE nodeIndex = 0;
        for (auto const& node : m_sealerList)
        {
            m_nodeID2Index->insert(std::make_pair(node, nodeIndex));
            nodeIndex++;
        }
    }
}

void VRFBasedrPBFTEngine::forwardRemaingTxs(std::set<dev::h512> const& _lastEpochWorkingSealers)
{
    // the node is one working sealer of the last epoch
    // while not the working sealer of this epoch
    auto nodeId = m_keyPair.pub();
    if (!_lastEpochWorkingSealers.count(nodeId) || m_chosedConsensusNodes->count(nodeId))
    {
        return;
    }
    // get the working sealers rotated in, and forward the remaining transactions to the rotated-in
    // working sealers
    for (auto const& workingSealer : *m_chosedConsensusNodes)
    {
        if (!_lastEpochWorkingSealers.count(workingSealer))
        {
            m_blockSync->noteForwardRemainTxs(workingSealer);
            RPBFTENGINE_LOG(DEBUG)
                << LOG_DESC("noteForwardRemainTxs after the node rotating out")
                << LOG_KV("idx", m_idx) << LOG_KV("chosedOutWorkingSealer", m_idx)
                << LOG_KV("chosedInWorkingSealer", workingSealer.abridged());
        }
    }
}

void VRFBasedrPBFTEngine::resetConfig()
{
    PBFTEngine::resetConfig();
    // Reach consensus on new blocks, update m_shouldRotateSealers to false
    if (true == m_shouldRotateSealers.load())
    {
        m_shouldRotateSealers.store(false);
    }
    // update m_f
    m_f = (m_workingSealersNum - 1) / 3;

    // update according to epoch_blocks_num
    updateRotatingInterval();
    // update according to epoch_sealers_num
    bool epochUpdated = updateEpochSize();
    // first setup
    if (m_blockChain->number() == 0)
    {
        return;
    }

    // the number of workingSealers is smaller than epochSize,
    // trigger rotate in case of the number of working sealers has been decreased to 0
    auto epochSize = std::min(m_epochSize.load(), m_sealersNum.load());
    if (m_workingSealersNum.load() < epochSize)
    {
        m_shouldRotateSealers.store(true);
    }
    // After the last batch of workingsealers agreed on m_rotatingInterval blocks,
    // set m_shouldRotateSealers to true to notify VRFBasedrPBFTSealer to update workingSealer
    if (epochUpdated ||
        (0 == (m_blockChain->number() - m_rotatingIntervalEnableNumber) % m_rotatingInterval))
    {
        m_shouldRotateSealers.store(true);
    }
    VRFRPBFTEngine_LOG(DEBUG) << LOG_DESC("resetConfig") << LOG_KV("blkNum", m_blockChain->number())
                              << LOG_KV("shouldRotateSealers", m_shouldRotateSealers)
                              << LOG_KV("workingSealersNum", m_workingSealersNum)
                              << LOG_KV("epochBlockNum", m_rotatingInterval)
                              << LOG_KV("configuredEpochSealers", m_epochSize);
}

void VRFBasedrPBFTEngine::updateConsensusInfo()
{
    ReadGuard l(x_chosedSealerList);
    // update consensus node info for syncing block
    if (m_blockSync->syncTreeRouterEnabled())
    {
        // get all the node list of this group
        auto nodeList = m_blockChain->sealerList() + m_blockChain->observerList();
        std::sort(nodeList.begin(), nodeList.end());
        m_blockSync->updateConsensusNodeInfo(*m_chosedSealerList, nodeList);
    }
    // update consensus node info for broadcasting transactions
    if (m_treeRouter)
    {
        m_treeRouter->updateConsensusNodeInfo(*m_chosedSealerList);
    }
    VRFRPBFTEngine_LOG(INFO) << LOG_DESC("updateConsensusInfo")
                             << LOG_KV("chosedSealerListSize", m_chosedSealerList->size());
}

// When you need to rotate nodes, check the node rotation transaction
// (the last transaction in the block, the sender must be the leader)
void VRFBasedrPBFTEngine::checkTransactionsValid(
    dev::eth::Block::Ptr _block, PrepareReq::Ptr _prepareReq)
{
    if (!m_shouldRotateSealers.load())
    {
        return;
    }
    // check the node rotation transaction
    auto transactionSize = _block->getTransactionSize();
    // empty block
    if (transactionSize == 0)
    {
        return;
    }
    VRFRPBFTEngine_LOG(DEBUG) << LOG_DESC("checkTransactionsValid")
                              << LOG_KV("blkNum", _prepareReq->height)
                              << LOG_KV("hash", _prepareReq->block_hash.abridged())
                              << LOG_KV("leaderIdx", _prepareReq->idx)
                              << LOG_KV("nodeIdx", nodeIdx()) << LOG_KV("txSize", transactionSize);
    auto nodeRotatingTx = (*(_block->transactions()))[transactionSize - 1];
    auto leaderNodeID = RotatingPBFTEngine::getSealerByIndex(_prepareReq->idx);
    if (leaderNodeID == dev::h512())
    {
        VRFRPBFTEngine_LOG(WARNING)
            << LOG_DESC("checkTransactionsValid failed for invalid preprareReq generator");
        BOOST_THROW_EXCEPTION(
            InvalidLeader() << errinfo_comment(
                "Invalid Leader " + std::to_string(_prepareReq->idx) + ": NodeID not found!"));
    }
    // get the account of the leader
    Address leaderAccount = toAddress(leaderNodeID);
    // check the sender of the nodeRotatingTx
    if (nodeRotatingTx->sender() != leaderAccount)
    {
        VRFRPBFTEngine_LOG(WARNING) << LOG_DESC("checkTransactionsValid failed")
                                    << LOG_KV("expectedSender", leaderAccount.hexPrefixed())
                                    << LOG_KV("sender", nodeRotatingTx->sender().hexPrefixed());

        BOOST_THROW_EXCEPTION(InvalidNodeRotationTx() << errinfo_comment(
                                  "Invalid node rotation transaction, expected sender: " +
                                  leaderAccount.hexPrefixed() +
                                  ", current sender:" + nodeRotatingTx->sender().hexPrefixed()));
    }
}