package com.tmoney;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class TransactionService {
    private final AtomicLong idSequence = new AtomicLong();
    private final ConcurrentMap<Long, Transaction> transactions = new ConcurrentHashMap<>();
    private final AccountService accountService;

    Transaction add(Transaction transaction) {
        if (transfer(transaction.getDebit(), transaction.getCredit(), transaction.getAmount())) {
            transaction.setId(idSequence.incrementAndGet());
            transactions.put(transaction.getId(), transaction);
            return transaction;
        }
        throw new InsufficientFundsException(String.format("Account %s have less amount than %s",
                transaction.getDebit(), transaction.getAmount()));
    }

    private boolean transfer(String debit, String credit, BigDecimal amount) {
        Account debitAccount = Objects.requireNonNull(accountService.get(debit), String.format("Account %s not found", debit));
        Account creditAccount = Objects.requireNonNull(accountService.get(credit), String.format("Account %s not found", credit));
        // avoid deadlock through sorting accounts by number
        Account first = debitAccount.getNumber().compareTo(creditAccount.getNumber()) > 0 ? debitAccount : creditAccount;
        Account second = (first == debitAccount) ? creditAccount : debitAccount;
        synchronized (first) {
            synchronized (second) {
                if (debitAccount.getBalance().compareTo(amount) >= 0) {
                    debitAccount.setBalance(debitAccount.getBalance().subtract(amount));
                    creditAccount.setBalance(creditAccount.getBalance().add(amount));
                    return true;
                }
            }
        }
        return false;
    }

    static class InsufficientFundsException extends RuntimeException {
        InsufficientFundsException(String message) {
            super(message);
        }
    }

    Transaction get(Long id) {
        return  Objects.requireNonNull(transactions.get(id), String.format("Transaction with id=%s does not exists", id));
    }

    Collection<Transaction> getAll() {
        return transactions.values();
    }

    Collection<Transaction> getByAccount(String account) {
        return account == null || account.isEmpty() ? getAll() :
                transactions.values().stream()
                        .filter(trans -> trans.getCredit().equals(account) || trans.getDebit().equals(account))
                        .collect(Collectors.toList());
    }

    void reset() {
        transactions.clear();
    }
}
