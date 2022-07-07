#pragma once
#include "../Block.h"
#include "../storage/Storage.h"
#include <concepts>
#include <ranges>

namespace bcos::concepts::ledger
{

template <class ArgType>
concept TransactionOrReceipt = bcos::concepts::transaction::Transaction<ArgType> ||
    bcos::concepts::receipt::TransactionReceipt<ArgType>;

// clang-format off
struct DataFlagBase {};
struct ALL: public DataFlagBase {};
struct HEADER: public DataFlagBase {};
struct TRANSACTIONS: public DataFlagBase {};
struct RECEIPTS: public DataFlagBase {};
struct NONCES: public DataFlagBase {};
// clang-format on

template <class FlagType>
concept DataFlag = std::derived_from<FlagType, DataFlagBase>;

struct TransactionCount
{
    uint64_t total = 0;
    uint64_t failed = 0;
    uint64_t blockNumber = 0;
};

// All method in ledger is uncacheed
template <class Impl>
class LedgerBase
{
public:
    template <DataFlag... flags>
    auto getBlock(bcos::concepts::block::BlockNumber auto blockNumber)
        -> bcos::concepts::block::Block auto
    {
        return impl().template impl_getBlock<flags...>(blockNumber);
    }

    void setBlock(bcos::concepts::block::Block auto block)
    {
        impl().impl_setBlock(std::move(block));
    }

    template <DataFlag flag>
    auto getTransactionsOrReceipts(std::ranges::range auto const& hashes) -> std::ranges::range auto
    {
        return impl().template impl_getTransactionsOrReceipts<flag>(hashes);
    }

    TransactionCount getTotalTransactionCount() { return impl().impl_getTotalTransactionCount(); }

    template <bcos::crypto::hasher::Hasher Hasher>
    void setTransactionsOrReceipts(std::ranges::range auto const& inputs) requires
        bcos::concepts::ledger::TransactionOrReceipt<std::ranges::range_value_t<decltype(inputs)>>
    {
        auto hashesRange = inputs | std::views::transform([](auto const& input) {
            return bcos::concepts::hash::calculate<Hasher>(input);
        });
        auto buffersRange = inputs | std::views::transform([](auto const& input) {
            std::vector<bcos::byte> buffer;
            bcos::concepts::serialize::encode(input, buffer);
            return buffer;
        });

        constexpr auto isTransaction =
            bcos::concepts::transaction::Transaction<std::ranges::range_value_t<decltype(inputs)>>;
        setTransactionOrReceiptBuffers<isTransaction>(hashesRange, std::move(buffersRange));
    }

    template <bool isTransaction>
    void setTransactionOrReceiptBuffers(
        std::ranges::range auto const& hashes, std::ranges::range auto buffers)
    {
        impl().template impl_setTransactionOrReceiptBuffers<isTransaction>(
            hashes, std::move(buffers));
    }

private:
    friend Impl;
    auto& impl() { return static_cast<Impl&>(*this); }
};

template <class Impl>
concept Ledger = std::derived_from<Impl, LedgerBase<Impl>> ||
    std::derived_from<typename Impl::element_type, LedgerBase<typename Impl::element_type>>;

}  // namespace bcos::concepts::ledger