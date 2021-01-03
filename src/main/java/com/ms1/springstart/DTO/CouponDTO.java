package com.ms1.springstart.DTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
/*
    Coupon DTO (In parameter)
 */
public class CouponDTO {
    private int cnt;
    private String coupon_id;
}