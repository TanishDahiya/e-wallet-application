package com.application.controller;

import com.application.dto.TransactionRequest;
import com.application.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/transaction-service")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/status/{txnId}")
    ResponseEntity<String> getStatus(@PathVariable String txnId){
        return ResponseEntity.ok(transactionService.getStatus(txnId));
    }

    @PostMapping("/doTransaction")
    ResponseEntity<String> doTransaction(@RequestBody TransactionRequest request) throws InterruptedException, ExecutionException {
        String txnId = transactionService.initTransaction(request);
        return ResponseEntity.accepted().body(txnId);
    }
}
