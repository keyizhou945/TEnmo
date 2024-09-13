package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.TransferDto;

import java.util.List;

public interface TransferDao {

    Integer addTransfer(Integer transferTypeId, Integer transferStatusId, Integer accountFrom, Integer accountTo, Double amount);

    List<TransferDto> listHistoryByUserId(Integer userId);

    List<TransferDto> listPendingByUserId(Integer userId);

    TransferDto getTransferById(Integer transferId);

    void updateTransferStatus(Integer transferId, Integer newStatusId);

}
