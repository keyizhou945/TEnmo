package com.techelevator.tenmo.model;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;


@Getter
public class BucksDto {

    @NotNull
    Integer fromUserId;

    @NotNull
    Integer toUserId;

    @NotNull
    Double amount;

}
