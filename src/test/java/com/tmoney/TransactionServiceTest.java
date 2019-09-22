package com.tmoney;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionServiceTest {
    private AccountService accountService;
    private TransactionService transactionService;
    private Transaction transaction;

    @Before
    public void setUp() {
        String debit = "debit";
        String credit = "credit";
        accountService = mock(AccountService.class);
        createAccount(debit, "debit name", "50");
        createAccount(credit, "credit name", "2");

        transactionService = new TransactionService(accountService);

        transaction = new Transaction();
        transaction.setDebit(debit);
        transaction.setCredit(credit);
        transaction.setAmount(new BigDecimal(3));
    }

    @Test
    public void add() {
        assertNotNull(transactionService.add(transaction).getId());
    }


    @Test(expected = TransactionService.InsufficientFundsException.class)
    public void addNot() {
        transaction.setAmount(new BigDecimal(1000));
        assertNotNull(transactionService.add(transaction).getId());
    }

    @Test
    public void get() {
        Transaction added = transactionService.add(transaction);
        assertEquals(transaction, transactionService.get(added.getId()));
    }

    @Test
    public void getAll() {
        assertEquals(0, transactionService.getAll().size());
        transactionService.add(makeCopy(transaction));
        transactionService.add(makeCopy(transaction));
        transactionService.add(makeCopy(transaction));
        assertEquals(3, transactionService.getAll().size());
    }

    @Test
    public void getByAccount() {
        String first = "first account";
        createAccount(first, "credit name", "2");

        List<Transaction> result = new ArrayList<>();

        Transaction transaction1 = makeCopy(transaction);
        transaction1.setCredit(first);
        result.add(transaction1);
        transactionService.add(transaction1);

        Transaction transaction2 = makeCopy(transaction);
        transaction2.setDebit(first);
        result.add(transaction2);
        transactionService.add(transaction2);

        Transaction transaction3 = makeCopy(transaction);
        transactionService.add(transaction3);

        assertArrayEquals(result.toArray(), transactionService.getByAccount(first).toArray());

        result.add(transaction3);
        assertArrayEquals(result.toArray(), transactionService.getByAccount("").toArray());
    }

    private void createAccount(String number, String name, String balance) {
        Account account = new Account();
        account.setNumber(number);
        account.setName(name);
        account.setBalance(new BigDecimal(balance));
        when(accountService.get(number)).thenReturn(account);
    }

    private Transaction makeCopy(Transaction transaction) {
        Transaction trans = new Transaction();
        trans.setCredit(transaction.getCredit());
        trans.setDebit(transaction.getDebit());
        trans.setAmount(transaction.getAmount());
        return trans;
    }
}
