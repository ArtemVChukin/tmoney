package com.tmoney;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;

public class TransferLoadTest {

    @Test
    public void transferLoad() {
        AccountService accountService = new AccountService();
        TransactionService transactionService = new TransactionService(accountService);
        Account account = new Account();
        account.setName("random name");
        account.setBalance(new BigDecimal(20));

        List<Account> accounts = IntStream.range(0, 10)
                .mapToObj(i -> accountService.add(makeCopy(account)))
                .collect(Collectors.toList());
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Random r = new Random();
        List<Future<Transaction>> futures = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            futures.add(executor.submit(() -> {
                Transaction transaction = new Transaction();
                transaction.setDebit(accounts.get(r.nextInt(10)).getNumber());
                transaction.setCredit(accounts.get(r.nextInt(10)).getNumber());
                transaction.setAmount(new BigDecimal(1));
                return transactionService.add(transaction);
            }));
        }
        futures.forEach(future -> {
            try {
                assertNotNull(future.get().getId());
            } catch (InterruptedException | ExecutionException e) {
                if (!(e.getCause() instanceof TransactionService.InsufficientFundsException)) {
                    fail(e.getMessage());
                }
            }
        });
    }

    private Account makeCopy(Account account) {
        Account acc = new Account();
        acc.setName(account.getName());
        acc.setBalance(account.getBalance());
        acc.setNumber(account.getNumber());
        return acc;
    }

}
