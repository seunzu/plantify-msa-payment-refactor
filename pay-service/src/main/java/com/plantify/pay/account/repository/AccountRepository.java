package com.plantify.pay.account.repository;

import com.plantify.pay.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByAccountId(Long accountId);
    long countByPayUserId(Long userId);
    boolean existsByAccountNum(String accountNum);
    List<Account> findByPayUserId(Long userId);
    Optional<Account> findFirstByPayUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Account> findByAccountIdAndPayUserId(Long accountId, Long userId);
}
