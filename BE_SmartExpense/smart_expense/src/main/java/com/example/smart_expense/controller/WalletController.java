package com.example.smart_expense.controller;

import com.example.smart_expense.model.Wallet;
import com.example.smart_expense.repository.WalletRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletRepository walletRepository;

    public WalletController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    /**
     * Lấy danh sách ví của người dùng.
     * GET /api/wallets?userId=1
     */
    @GetMapping
    public ResponseEntity<List<Wallet>> getWallets(@RequestParam Integer userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return ResponseEntity.ok(wallets);
    }

    /**
     * Tạo ví mới.
     * POST /api/wallets
     */
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody Wallet wallet) {
        Wallet savedWallet = walletRepository.save(wallet);
        return ResponseEntity.ok(savedWallet);
    }
}
