package com.application.consumer;

import com.application.TransactionPayload;
import com.application.UserCreatedPayload;
import com.application.WalletUpdatePayload;
import com.application.entity.Wallet;
import com.application.repository.WalletRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

@EnableKafka
@Configuration
public class WalletKafkaConsumer {

    @Autowired
    private KafkaTemplate<String, TransactionPayload> txnKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, WalletUpdatePayload> kafkaTemplate;
    private static String WALLET_TOPIC="WALLET_UPDATE";
    private static String TXN_TOPIC="TXN_COMP";
    @Autowired
    private WalletRepo walletRepo;
    private static Logger LOGGER= LoggerFactory.getLogger(WalletKafkaConsumer.class);

    @KafkaListener(topics="USER_CREATED",groupId="wallet-service",containerFactory = "userKafkaListenerContainerFactory")
    public void consumerUserCreated(UserCreatedPayload payload) throws ExecutionException, InterruptedException {
        LOGGER.info("Consumer from kafka;{}",payload);
        Wallet wallet = Wallet.builder()
                .balance(100.00)
                .userId(payload.getUserId())
                .email(payload.getEmail())
                .build();
        walletRepo.save(wallet);


        //sending data to notification service

        WalletUpdatePayload walletUpdatePayload = WalletUpdatePayload.builder()
                .email(payload.getEmail())
                .balance(wallet.getBalance())
                .build();

        ListenableFuture listenableFuture = kafkaTemplate.send(WALLET_TOPIC,walletUpdatePayload);

        LOGGER.info("Pushed to {}, kafka response : {}", WALLET_TOPIC, listenableFuture.get());

    }

    @KafkaListener(topics = "TXN_INIT",groupId = "wallet-service", containerFactory = "txnInitKafkaListenerContainerFactory")
    public void consumeFromTxnInit(TransactionPayload payload) throws ExecutionException, InterruptedException {
        //find user id in wallet the current one fromm which money is transfer
        Wallet fromWallet = walletRepo.findByUserId(payload.getFromUserId());
        //check if we have a sufficient amount or not
        if(fromWallet.getBalance() >= payload.getAmount()){
            //fetch the wallet  in which we have to transfer the money
            Wallet toWallet = walletRepo.findByUserId(payload.getToUserId());
            //update the balance after debited
            fromWallet.setBalance(fromWallet.getBalance() - payload.getAmount());
            //update the balance after credited
            toWallet.setBalance(toWallet.getBalance() + payload.getAmount());
            //save the updated balance in db
            walletRepo.save(fromWallet);
            walletRepo.save(toWallet);

            WalletUpdatePayload fromWalletUpdatePayload = WalletUpdatePayload.builder()
                    .email(fromWallet.getEmail())
                    .balance(fromWallet.getBalance())
                    .build();
            ListenableFuture listenableFuture = kafkaTemplate.send(WALLET_TOPIC,fromWalletUpdatePayload);
            LOGGER.info("Pushed to {}, kafka response : {}", WALLET_TOPIC, listenableFuture.get());

            WalletUpdatePayload toWalletUpdatePayload = WalletUpdatePayload.builder()
                    .email(toWallet.getEmail())
                    .balance(toWallet.getBalance())
                    .build();
            ListenableFuture listenableFuture2 = kafkaTemplate.send(WALLET_TOPIC,toWalletUpdatePayload);
            LOGGER.info("Pushed to {}, kafka response : {}", WALLET_TOPIC, listenableFuture2.get());

            payload.setStatus("SUCCESS");
            ListenableFuture listenableFuture3 = txnKafkaTemplate.send(TXN_TOPIC,payload);
            LOGGER.info("Pushed to {}, kafka response : {}", TXN_TOPIC, listenableFuture3.get());
            return;
        }
        //if not true transaction failed
        payload.setStatus("FAILED");
        ListenableFuture listenableFuture3 = txnKafkaTemplate.send(TXN_TOPIC,payload);
        LOGGER.info("Pushed to {}, kafka response : {}", TXN_TOPIC, listenableFuture3.get());
    }
}
