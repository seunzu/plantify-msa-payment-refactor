package com.plantify.transaction.transaction.domain;

import java.io.Serializable;

public enum Status implements Serializable {

    PENDING,
    PAYMENT,
    REFUND,
    CANCELLATION,
    SUCCESS,
    FAILED
}