package com.techelevator.tenmo.service;

import com.techelevator.tenmo.model.BucksDto;
import com.techelevator.tenmo.model.TransferDto;

import java.util.List;

public interface TransferService {

    Integer sendBucks(BucksDto bucksDto) throws Exception;

    Integer requestBucks(BucksDto bucksDto) throws Exception;

    TransferDto getTransferByTransferId(Integer transferId);

    List<TransferDto> listHistoryByUserId(Integer userId);

    List<TransferDto> listPending(Integer userId);

    void approveTransfer(Integer transferId,Integer userId);

    void rejectTransfer(Integer transferId,Integer userId);
}
