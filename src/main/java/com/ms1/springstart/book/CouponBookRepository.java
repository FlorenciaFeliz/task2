package com.ms1.springstart.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CouponBookRepository extends JpaRepository<CouponBook, String> {

    /*
        사용자 지급 여부에 따른 쿠폰 목록 조회
     */
    @Query("select coupon_id from CouponBook where issue_yn = ?1")
    List<String> findForCouponByIssueYn(String issue_yn);

    /*
        사용자에게 지급된 쿠폰 목록 조회
     */
    @Query("select coupon_id from CouponBook where issue_yn = 'Y'")
    List<String> findForIssuedCouponList();

    /*
        쿠폰 사용 또는 취소하기 전 쿠폰 ID Validation Check
     */
    @Query("select coupon_id from CouponBook where coupon_id = ?1 and issue_yn = 'Y' and use_tp_cd in (?2, ?3)")
    String checkValidationCheckCouponId(String coupon_id, String use_tp_cd1, String use_tp_cd2);

    /*
        사용자에게 지급된 쿠폰 중, 당일 만료된 쿠폰 목록 조회
     */
    @Query("select coupon_id from CouponBook where issue_yn = 'Y' and expire_date = current_date")
    List<String> findForExpiredCouponList();

    /*
        사용자에게 발급된 쿠폰 중, 만료 3일 전 쿠폰 목록 조회
     */
    @Query("select concat(e.coupon_id, ' (만료일 : ', e.expire_date, ')') from CouponBook e where issue_yn = 'Y' and use_tp_cd <> 'Y' and expire_date <= DATEADD('DAY', 3, current_date) and expire_date >= current_date order by expire_date, issue_date")
    List<String> findForExpiredCouponForNotice();

}
