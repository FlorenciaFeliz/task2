package com.ms1.springstart.book;

import lombok.*;
import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
/*
    CouponBook Entity
 */
public class CouponBook {

    @Id
    @Column(nullable = false, unique = true, length = 38)
    private String coupon_id;  // 쿠폰 ID

    @Column(length=1)
    private String issue_yn;  // 발행여부

    @Column
    private Date issue_date;  // 발행일자

    @Column
    private Date expire_date;  // 만료일자

    @Column(length=1)
    private String use_tp_cd;  // 사용구분코드(Y/N/C)

    @Column
    private Date use_date;  // 사용일자

    @Builder
    public CouponBook(String coupon_id, String issue_yn) {
        // 쿠폰정보
        this.coupon_id = coupon_id;
        this.issue_yn = "N";
        this.issue_date = null;
        this.expire_date = null;
        this.use_tp_cd = "N";
        this.use_date = null;
    }

    public CouponBook(String coupon_id) {
        this.coupon_id = coupon_id;
    }

    public CouponBook(String coupon_id, Date expire_date) {
        this.coupon_id = coupon_id;
        this.issue_yn = "Y";
        this.issue_date = Date.valueOf(LocalDate.now());
        this.expire_date = expire_date;
    }
}
