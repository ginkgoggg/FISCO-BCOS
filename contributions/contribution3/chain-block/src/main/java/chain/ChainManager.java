package chain;

import com.nstl.common.exception.BusinessException;
import config.ChainConfig;
import enums.ContractType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author jie.zhong
 * @version 1.0
 * @date 2022/9/8 15:48
 */
@Slf4j
public class ChainManager {


    private Client client;

    private CryptoKeyPair keyPair;

    private ChainConfig chainConfig;

    public ChainManager(ChainConfig config) {
        if (ObjectUtils.isEmpty(config)) {
            throw new BusinessException(20000, "区块链配置异常");
        }
        this.chainConfig = config;
    }


    public Date insert(ContractType insertType, LinkedList<Object> params) throws Exception {
        ChainConfig.Contract contract = this.getContract(insertType);
        if(ObjectUtils.isEmpty(contract)){
            throw new BusinessException(20001,"合约不存在");
        }
        this.init(contract.getGroupId());
        AssembleTransactionProcessor transactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, keyPair, this.chainConfig.getAbiPath(), "");
        TransactionResponse transactionResponse = transactionProcessor.sendTransactionAndGetResponseByContractLoader(contract.getContractName(), contract.getContractAddress(), "insert", params);
        if(transactionResponse.getReturnCode() == 1){
            return new Date();
        }
        log.error("参数:{} 上链失败 原因:{}",params,transactionResponse.getReturnMessage());
        throw new BusinessException(20002,"上链失败");
    }

    public Object find(ContractType insertType,String key) throws Exception {
        ChainConfig.Contract contract = this.getContract(insertType);
        if(ObjectUtils.isEmpty(contract)){
            throw new BusinessException(20001,"合约不存在");
        }
        this.init(contract.getGroupId());
        AssembleTransactionProcessor transactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, keyPair,  this.chainConfig.getAbiPath(), "");
        CallResponse callResponse = transactionProcessor.sendCallByContractLoader(contract.getContractName(), contract.getContractAddress(), "select", Collections.singletonList(key));
        return callResponse.getReturnObject();
    }

    private ChainConfig.Contract getContract(ContractType insertType){
        return this.chainConfig.getContracts().get(insertType);
    }

    private void init(Integer groupId){
        BcosSDK sdk = BcosSDK.build(this.chainConfig.getConfigPath());
        this.client = sdk.getClient(groupId);
        this.keyPair = client.getCryptoSuite().createKeyPair();
    }

}
