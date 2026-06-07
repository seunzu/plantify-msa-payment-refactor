package com.plantify.pay.point.domain;

import com.plantify.pay.global.exception.ApplicationException;
import com.plantify.pay.global.exception.errorcode.PointErrorCode;
import com.plantify.pay.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long pointId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long pointBalance;

    public Point init(Long userId) {
        this.userId = userId;
        this.pointBalance = 0L;
        return this;
    }

    public Point validatePoint(long use){
        if (use < 0) {
            throw new ApplicationException(PointErrorCode.INVALID_POINT_OPERATION);
        }
        if (this.pointBalance < use) {
            throw new ApplicationException(PointErrorCode.INSUFFICIENT_POINTS);
        }
        return this;
    }

    public Point addPoint(long newPoint){
        this.pointBalance += newPoint;
        return this;
    }

    public Point usePoint(long use){
        this.pointBalance -= use;
        return this;
    }
}
