package com.plantify.pay.account.controller;

import com.plantify.pay.account.dto.AccountRequest;
import com.plantify.pay.account.dto.AccountResponse;
import com.plantify.pay.global.response.ApiResponse;
import com.plantify.pay.account.application.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pay/accounts")
public class AccountController {

    private final AccountService accountService;

    // 자신의 모든 계좌 조회
    @GetMapping
    public ApiResponse<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> response = accountService.getAllAccounts();
        return ApiResponse.ok(response);
    }

    // 자신의 특정 계좌 조회
    @GetMapping("/{accountId}")
    public ApiResponse<AccountResponse> getAccountByAccountId(@PathVariable Long accountId) {
        AccountResponse response = accountService.getAccountByAccountId(accountId);
        return ApiResponse.ok(response);
    }

    // 계좌 연결(등록)
    @PostMapping
    public ApiResponse<AccountResponse> createAccount(@RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ApiResponse.ok(response);
    }

    // 자신의 계좌 삭제
    @DeleteMapping("/{accountId}")
    public ApiResponse<Void> deleteAccountByAccountId(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ApiResponse.ok();
    }
}
