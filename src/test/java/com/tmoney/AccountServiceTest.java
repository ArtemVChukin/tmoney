package com.tmoney;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.Assert.*;

public class AccountServiceTest {
    private AccountService accountService;
    private Account account;

    @Before
    public void setUp() {
        accountService = new AccountService();
        account = new Account();
        account.setName("cool name");
        account.setBalance(new BigDecimal(18));
    }

    @Test
    public void add() {
        assertSame(account, accountService.add(account));
    }

    @Test
    public void change() {
        account.setNumber("account with number");
        accountService.add(makeCopy(account));
        account.setName("bad boy");
        accountService.change(account);
        assertTrue(isEquals(account, accountService.get(account.getNumber())));
    }

    @Test(expected = NullPointerException.class)
    public void delete() {
        accountService.add(makeCopy(account));
        accountService.delete(account.getNumber());
        fail();
    }

    @Test
    public void get() {
        accountService.add(account);
        assertEquals(account, accountService.get(account.getNumber()));
    }

    @Test
    public void getAll() {
        account.setNumber(null);
        assertEquals(0, accountService.getAll().size());
        accountService.add(makeCopy(account));
        accountService.add(makeCopy(account));
        accountService.add(makeCopy(account));
        assertEquals(3, accountService.getAll().size());
    }

    @Test
    public void getByName() {
        account.setNumber("account with number");
        accountService.add(makeCopy(account));
        assertTrue(isEquals(account, accountService.getByName(account.getName()).iterator().next()));
    }

    private Account makeCopy(Account account) {
        Account acc = new Account();
        acc.setName(account.getName());
        acc.setBalance(account.getBalance());
        acc.setNumber(account.getNumber());
        return acc;
    }

    private boolean isEquals(Account acc1, Account acc2) {
        return acc1 != null && acc2 != null &&
                Objects.equals(acc1.getNumber(), acc2.getNumber()) &&
                Objects.equals(acc1.getName(), acc2.getName()) &&
                Objects.equals(acc1.getBalance(), acc2.getBalance());
    }
}
