package com.plantify.pay.settlement.application;

import com.plantify.pay.ledger.domain.Status;
import com.plantify.pay.global.util.UserInfoProvider;
import com.plantify.pay.settlement.dto.ExternalSettlementResponse;
import com.plantify.pay.settlement.dto.PaySettlementResponse;
import com.plantify.pay.settlement.domain.PaySettlement;
import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.SettlementErrorCode;
import com.plantify.pay.settlement.repository.PaySettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaySettlementQueryServiceImpl implements PaySettlementQueryService {

    private final PaySettlementRepository paySettlementRepository;
    private final UserInfoProvider userInfoProvider;

    @Override
    public Page<PaySettlementResponse> getAllPaySettlements(Pageable pageable) {
        Long userId = userInfoProvider.getUserInfo().userId();
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return paySettlementRepository.findByPayUserId(userId, sortedPageable)
                .map(PaySettlementResponse::from);
    }

    @Override
    public Page<PaySettlementResponse> getPaySettlementByStatus(Status status, Pageable pageable) {
        Long userId = userInfoProvider.getUserInfo().userId();
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return paySettlementRepository.findByStatusAndPayUserId(status, userId, sortedPageable)
                .map(PaySettlementResponse::from);
    }

    @Override
    public Long getPaySettlementAmount() {
        Long userId = userInfoProvider.getUserInfo().userId();
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        Long totalAmountByUserIdAndMonth = paySettlementRepository.getTotalAmountByUserIdAndMonth(userId, currentMonth, currentYear);
        return totalAmountByUserIdAndMonth != null ? totalAmountByUserIdAndMonth : 0;
    }

    @Override
    public ExternalSettlementResponse getSettlementByOrderId(String orderId) {
        PaySettlement paySettlement = paySettlementRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ApplicationException(SettlementErrorCode.PAY_SETTLEMENT_NOT_FOUND));
        return ExternalSettlementResponse.from(paySettlement);
    }
}
