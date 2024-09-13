package com.techelevator.tenmo.service;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.enums.TransferStatusEnum;
import com.techelevator.tenmo.enums.TransferTypeEnum;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.BucksDto;
import com.techelevator.tenmo.model.TransferDto;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class TransferServiceImpl implements TransferService {
    protected final TransferDao transferDao;
    private final AccountDao accountDao;
    public TransferServiceImpl(TransferDao transferDao, AccountDao accountDao) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;

    }

    @Override
    @Transactional
    /**
     * @Author Keyi Zhou
     * @Description
     *      User A sends a transfer to User B.
     *     Create a new transfer recode, the transfer status shows 'Approved'
     *     User A's balance decreases, and User B's balance increases.
     * @Date 2024/9/12 10:50
     * @Param bucksDto 
     * @return transferId
    **/
    public Integer sendBucks(BucksDto bucksDto) throws Exception {
        Account fromAccount = accountDao.findByUserId(bucksDto.getFromUserId());
        Account toAccount = accountDao.findByUserId(bucksDto.getToUserId());
        if (!isAccountAvailable(fromAccount, toAccount)) {
            throw new Exception("Account not available.");
        }
        if (bucksDto.getAmount() <= 0) {
            throw new Exception("could not send a zero or negative amount.");
        }
        if (fromAccount.getBalance() < bucksDto.getAmount()) {
            throw new Exception("don't have enough money.");
        }
        Integer transferId = transferDao.addTransfer(TransferTypeEnum.SEND.getTypeId(), TransferStatusEnum.APPROVED.getStatusId(), fromAccount.getAccountId(), toAccount.getAccountId(), bucksDto.getAmount());
        accountDao.updateBalanceByAccountId(toAccount.getAccountId(), toAccount.getBalance() + bucksDto.getAmount());
        accountDao.updateBalanceByAccountId(fromAccount.getAccountId(), fromAccount.getBalance() - bucksDto.getAmount());
        return transferId;
    }

    /**
     * @Author Keyi Zhou
     * @Description
     *      User A request a transfer from User B.
     *      Create a new transfer recode, the transfer status shows 'Pending',
     *      No account balance changes until the request is approved.
     * @Date 2024/9/12 11:20
     * @Param bucksDto
     * @return
    **/
    @Override
    public Integer requestBucks(BucksDto bucksDto) throws Exception {
        Account fromAccount = accountDao.findByUserId(bucksDto.getFromUserId());
        Account toAccount = accountDao.findByUserId(bucksDto.getToUserId());
        if (!isAccountAvailable(fromAccount, toAccount)) {
            throw new Exception("Account not available.");
        }
        if (bucksDto.getAmount() <= 0) {
            throw new Exception("could not request a zero or negative amount.");
        }
        return transferDao.addTransfer(TransferTypeEnum.REQUEST.getTypeId(), TransferStatusEnum.PENDING.getStatusId(), fromAccount.getAccountId(), toAccount.getAccountId(), bucksDto.getAmount());
    }

    @Override
    public TransferDto getTransferByTransferId(Integer transferId) {
        return transferDao.getTransferById(transferId);
    }

    @Override
    public List<TransferDto> listHistoryByUserId(Integer userId) {
        return transferDao.listHistoryByUserId(userId);
    }

    @Override
    public List<TransferDto> listPending(Integer userId) {
        return transferDao.listPendingByUserId(userId);
    }


    Boolean isAccountAvailable(Account fromAccount, Account toAccount) {
        if (fromAccount == null ) {
            return false;
        }
        if (toAccount == null) {
            return false;
        }
        if (fromAccount.equals(toAccount)) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public void approveTransfer(Integer transferId,Integer userId){

        TransferDto transferDto = searchAndVerityTransferInfo(transferId);
        Account accountFrom = searchAndCheckFromAccount( transferDto);
        Account accountTo = searchAndCheckToAccount(transferDto,userId);
        updateTransferAndAccount(transferDto, accountFrom, accountTo);

    }


    private void updateTransferAndAccount(TransferDto transferDto, Account accountFrom, Account accountTo) {
        transferDao.updateTransferStatus(transferDto.getTransferId(), TransferStatusEnum.APPROVED.getStatusId());
        accountDao.updateBalanceByAccountId(accountFrom.getAccountId(), accountFrom.getBalance() + transferDto.getAmount());
        accountDao.updateBalanceByAccountId(accountTo.getAccountId(), accountTo.getBalance() - transferDto.getAmount());
    }

    private TransferDto searchAndVerityTransferInfo(Integer transferId) {
        TransferDto transfer = transferDao.getTransferById(transferId);
        if (transfer == null) {
            throw new IllegalArgumentException("Transfer recode not exit: " + transferId);
        }

        boolean isPending = TransferStatusEnum.PENDING.getStatusId().equals(transfer.getTransferStatusId());
        if (!isPending) {
            throw new IllegalArgumentException("Transfer " + transferId + " : is not pending");
        }
        if (transfer.getAccountFrom().equals(transfer.getAccountTo())) {
            throw new IllegalArgumentException("Cannot transfer to yourself ");
        }
        if (transfer.getAmount() <= 0) {
            throw new IllegalArgumentException("Cannot process a zero or negative amount.");
        }
        return transfer;
    }

    private Account searchAndCheckFromAccount(TransferDto transferDto) {
        Account account = accountDao.findByAccountId(transferDto.getAccountFrom());
        if (account == null) {
            throw new IllegalArgumentException("Account not found : " + transferDto.getAccountFrom());
        }
        return account;
    }

    private Account searchAndCheckToAccount(TransferDto transferDto, Integer userId) {

        Account accountTo = accountDao.findByAccountId(transferDto.getAccountTo());
        if (accountTo == null) {
            throw new IllegalArgumentException("Account not found with id: " + transferDto.getAccountTo());
        }

        if (accountTo.getBalance() < transferDto.getAmount()) {
            throw new IllegalArgumentException("Insufficient Balance: ");
        }

        if (!accountTo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("No permission.");
        }

        return accountTo;
    }

    @Override
    public void rejectTransfer(Integer transferId,Integer userId){
        TransferDto transfer = searchAndVerityTransferInfo(transferId);
        verifyOperationPermissions(transfer, userId);
        transferDao.updateTransferStatus(transferId, TransferStatusEnum.REJECTED.getStatusId());
    }

    private void verifyOperationPermissions (TransferDto transferDto, Integer userId) {

        Account accountTo = accountDao.findByAccountId(transferDto.getAccountTo());
        if (accountTo == null) {
            throw new IllegalArgumentException("Account not found with id: " + transferDto.getAccountTo());
        }

        if (!accountTo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("No permission.");
        }
    }


}
