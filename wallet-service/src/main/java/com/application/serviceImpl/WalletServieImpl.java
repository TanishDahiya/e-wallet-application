package com.application.serviceImpl;

import com.application.entity.Wallet;
import com.application.repository.WalletRepo;
import com.application.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletServieImpl implements WalletService {

    @Autowired
    private WalletRepo walletRepo;
    @Override
    public Double getBalanceByUserId(Long userId) {
        Wallet wallet=walletRepo.findByUserId(userId);
        return wallet.getBalance();
    }
}
