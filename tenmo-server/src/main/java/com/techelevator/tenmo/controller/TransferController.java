package com.techelevator.tenmo.controller;


import com.techelevator.tenmo.model.BucksDto;
import com.techelevator.tenmo.model.TransferDto;
import com.techelevator.tenmo.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }


    @PostMapping("/send")
    Integer sendBucks(@RequestBody @Valid BucksDto bucksDto) {
        try {
            return transferService.sendBucks( bucksDto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/request")
    Integer requestBucks(@RequestBody @Valid BucksDto bucksDto) {
        try {
            return transferService.requestBucks(bucksDto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("transfer history/{userId}")
    List<TransferDto> list(@PathVariable Integer userId) {
        try {
            return transferService.listHistoryByUserId(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/Pending Transfers/{userId}")
    List<TransferDto> listPending(@PathVariable Integer userId) {
        try {
            return transferService.listPending(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{transferId}")
    TransferDto getTransferByTransferId(@PathVariable Integer transferId) {
        try {
            return transferService.getTransferByTransferId(transferId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/approve/{transferId}")
    public void approveTransfer(@PathVariable Integer transferId, @RequestBody Integer userId) {
        try {
            transferService.approveTransfer(transferId, userId);
        } catch (Exception e) {
            String s = e.getMessage();
            System.out.println(s);
            String d = e.getLocalizedMessage();
            System.out.println(d);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/reject/{transferId}")
    public void rejectTransfer(@PathVariable Integer transferId, @RequestBody Integer userId) {
        try {
            transferService.rejectTransfer(transferId, userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
