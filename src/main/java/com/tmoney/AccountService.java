package com.tmoney;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

class AccountService {
    private static final String ACCOUNT_MASK = "408178100%015d";
    private static final String ACCOUNT_NOT_FOUND = "Account with number %s not found";
    private final AtomicLong accountSequence = new AtomicLong();
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

    private String generateNumber() {
        return String.format(ACCOUNT_MASK, accountSequence.incrementAndGet());
    }

    Account add(Account account) {
        if (account.getNumber() == null || account.getNumber().isEmpty()) {
            account.setNumber(generateNumber());
        }
        if (accounts.putIfAbsent(account.getNumber(), account) != null) {
            throw new AccountAlreadyOpenedException(String.format("Account with number %s have opened already", account.getNumber()));
        }
        return account;
    }

    static class AccountAlreadyOpenedException extends RuntimeException {
        AccountAlreadyOpenedException(String message) {
            super(message);
        }
    }

    void change(Account account) {
        if (!accounts.containsKey(account.getNumber())) {
            throw new NullPointerException(String.format(ACCOUNT_NOT_FOUND, account.getNumber()));
        }
        accounts.put(account.getNumber(), account);
    }

    void delete(String accountNumber) {
        if (null == accounts.remove(accountNumber)) {
            throw new NullPointerException(String.format(ACCOUNT_NOT_FOUND, accountNumber));
        }
    }

    Account get(String number) {
        return Objects.requireNonNull(accounts.get(number), String.format(ACCOUNT_NOT_FOUND, number));
    }

    Collection<Account> getByName(String name) {
        return (name == null || name.isEmpty()) ? getAll() : accounts.values().stream()
                .filter(acc -> acc.getName().equals(name))
                .collect(Collectors.toList());
    }

    Collection<Account> getAll() {
        return accounts.values();
    }

    void reset() {
        accounts.clear();
    }
}
