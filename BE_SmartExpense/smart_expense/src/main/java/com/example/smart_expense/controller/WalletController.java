package com.example.smart_expense.controller;

import com.example.smart_expense.model.Wallet;
import com.example.smart_expense.repository.TransactionRepository;
import com.example.smart_expense.repository.WalletRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletController(WalletRepository walletRepository,
                            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
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
     * Tính tổng số dư tất cả ví của người dùng.
     * GET /api/wallets/total-balance?userId=1
     */
    @GetMapping("/total-balance")
    public ResponseEntity<Map<String, BigDecimal>> getTotalBalance(@RequestParam Integer userId) {
        BigDecimal totalBalance = walletRepository.getTotalBalanceByUserId(userId);
        return ResponseEntity.ok(Map.of("totalBalance", totalBalance));
    }

    /**
     * Tạo ví mới.
     * POST /api/wallets
     */
    @PostMapping
    public ResponseEntity<?> createWallet(@RequestBody Wallet wallet) {
        if (wallet.getUserId() == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        if (isBlank(wallet.getName())) {
            return ResponseEntity.badRequest().body("wallet name is required");
        }
        if (isBlank(wallet.getType())) {
            return ResponseEntity.badRequest().body("wallet type is required");
        }
        if (wallet.getBalance() == null) {
            wallet.setBalance(BigDecimal.ZERO);
        }

        Wallet savedWallet = walletRepository.save(wallet);
        return ResponseEntity.ok(savedWallet);
    }

    /**
     * Sửa tên ví, loại ví và số dư ban đầu nếu ví chưa có giao dịch.
     * PUT /api/wallets/{walletId}?userId=1
     */
    @PutMapping("/{walletId}")
    public ResponseEntity<?> updateWallet(@PathVariable Integer walletId,
                                          @RequestParam Integer userId,
                                          @RequestBody Wallet walletRequest) {
        Optional<Wallet> existingWalletOpt = walletRepository.findByIdAndUserId(walletId, userId);
        if (existingWalletOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Wallet existingWallet = existingWalletOpt.get();
        if (walletRequest.getName() != null) {
            if (isBlank(walletRequest.getName())) {
                return ResponseEntity.badRequest().body("wallet name must not be blank");
            }
            existingWallet.setName(walletRequest.getName());
        }
        if (walletRequest.getType() != null) {
            if (isBlank(walletRequest.getType())) {
                return ResponseEntity.badRequest().body("wallet type must not be blank");
            }
            existingWallet.setType(walletRequest.getType());
        }
        if (walletRequest.getBalance() != null) {
            int transactionCount = transactionRepository.countByWalletIdAndUserId(walletId, userId);
            if (transactionCount > 0) {
                return ResponseEntity.badRequest()
                        .body("Cannot update initial balance because this wallet already has transactions");
            }
            existingWallet.setBalance(walletRequest.getBalance());
        }

        Wallet updatedWallet = walletRepository.update(existingWallet);
        return ResponseEntity.ok(updatedWallet);
    }

    /**
     * Xóa ví nếu ví chưa có giao dịch.
     * DELETE /api/wallets/{walletId}?userId=1
     */
    @DeleteMapping("/{walletId}")
    public ResponseEntity<?> deleteWallet(@PathVariable Integer walletId,
                                          @RequestParam Integer userId) {
        Optional<Wallet> existingWalletOpt = walletRepository.findByIdAndUserId(walletId, userId);
        if (existingWalletOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        int transactionCount = transactionRepository.countByWalletIdAndUserId(walletId, userId);
        if (transactionCount > 0) {
            return ResponseEntity.badRequest()
                    .body("Cannot delete wallet because this wallet already has transactions");
        }

        walletRepository.deleteByIdAndUserId(walletId, userId);
        return ResponseEntity.noContent().build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
