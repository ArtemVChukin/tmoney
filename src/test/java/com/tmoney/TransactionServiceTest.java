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
        Account debitAccount = new Account();
        debitAccount.setNumber(debit);
        debitAccount.setName("debit name");
        debitAccount.setBalance(new BigDecimal(50));

        String credit = "credit";
        Account creditAccount = new Account();
        creditAccount.setNumber(credit);
        creditAccount.setName("credit name");
        creditAccount.setBalance(new BigDecimal(2));

        accountService = mock(AccountService.class);
        when(accountService.get(debit)).thenReturn(debitAccount);
        when(accountService.get(credit)).thenReturn(creditAccount);
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
        Account firstAccount = new Account();
        firstAccount.setNumber(first);
        firstAccount.setName("credit name");
        firstAccount.setBalance(new BigDecimal(2));
        when(accountService.get(first)).thenReturn(firstAccount);

        List<Transaction> result = new ArrayList<>();

        Transaction transaction1 = makeCopy(transaction);
        transaction1.setCredit(first);
        result.add(transaction1);
        transactionService.add(transaction1);

        Transaction transaction2 = makeCopy(transaction);
        transaction2.setDebit(first);
        result.add(transaction2);
        transactionService.add(transaction2);
        assertArrayEquals(result.toArray(), transactionService.getByAccount(first).toArray());
    }

    private Transaction makeCopy(Transaction transaction) {
        Transaction trans = new Transaction();
        trans.setCredit(transaction.getCredit());
        trans.setDebit(transaction.getDebit());
        trans.setAmount(transaction.getAmount());
        return trans;
    }
}
