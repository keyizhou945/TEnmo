package com.techelevator.tenmo.service;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.model.Account;
import org.springframework.stereotype.Service;

/**
 * @ClassName AccountServiceImpl
 * @Description
 * @Author Keyi Zhou
 * @Date 2024/8/18 14:51
 **/
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao;

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Double getAccountBalance(Integer userId) throws Exception {
        Account account = accountDao.findByUserId(userId);
        if (account == null) {
            throw new Exception("could not find the account, userId: " + userId.toString());
        }
        return account.getBalance();
    }
}
