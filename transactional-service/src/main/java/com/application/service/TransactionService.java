package com.application.service;

import com.application.dto.TransactionRequest;

import java.util.concurrent.ExecutionException;

public interface TransactionService {

    public String getStatus(String txnId);

    public String initTransaction(TransactionRequest request) throws ExecutionException, InterruptedException;
}
