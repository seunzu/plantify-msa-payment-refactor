package com.plantify.pay.ledger.domain;

import java.io.Serializable;

public enum Status implements Serializable {

    CHARGE,
    PENDING,
    PAYMENT,
    REFUND,
    CANCELLATION,
    SUCCESS,
    FAILED
}