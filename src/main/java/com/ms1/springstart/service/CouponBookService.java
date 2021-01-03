package com.ms1.springstart.service;

import com.ms1.springstart.DTO.CouponUseType;
import com.ms1.springstart.book.CouponBook;
import com.ms1.springstart.book.CouponBookRepository;
import lombok.AllArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
//@RequiredArgsConstructor //에러나서 주석 처리함
public class CouponBookService {

    private CouponBookRepository couponBookRepository;

    /*
        쿠폰 생성
     */
    @Transactional
    public List<CouponBook> createCouponBook(int count) {
        List<CouponBook> coupones = new ArrayList<>();

        for(int i = 0 ; i < count ; i++){
            String random1 = RandomString.make(5);
            String random2 = RandomString.make(5);
            String random3 = RandomString.make(8);
            String couponid = random1 + "-" + random2 + "-" + random3;

            coupones.add(new CouponBook(couponid, "N"));
        }
        return couponBookRepository.saveAll(coupones);
    }

    /*
        쿠폰 목록 조회 (전체)
     */
    public List<CouponBook> findAll() {
        return couponBookRepository.findAll();
    }

    /*
        사용자에게 지급된 쿠폰 목록 조회
     */
    public List<String> findIssuedCoupon() {
        return couponBookRepository.findForIssuedCouponList();
    }

    /*
        쿠폰 업데이트 (지급 / 사용 / 사용 취소)
     */
    @Transactional  //(rollbackFor = Exception.class)
    public String update(CouponUseType type, CouponBook coupon) {
        int result = 0;
        int randomCount = 1;

        CouponBook couponBook = null;
        System.out.println("service >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
        if (type != null) {
            List<String> coupones = null;

            switch (type) {
                case issue:
                    // 랜덤으로 쿠폰 1개 추출, 지급
                    randomCount = 1;
                    List<String> couponList = couponBookRepository.findForCouponByIssueYn("N");
                    List<String> extraction = getRandomCoupon(couponList, randomCount);

                    if (extraction.size() > 0) {
                        coupon.setCoupon_id(extraction.get(0));
                        couponBook = updateCoupon(type, coupon);
                    }
                    break;
                case use:
                case cancel:
                    // 지급된 쿠폰이 맞는지 체크
                    String couponId = coupon.getCoupon_id();
                    String isseudCouponIdList = "";
                    String param1 = "";
                    String param2 = "";

                    if (type.equals(CouponUseType.use)) {
                        param1 = "N";
                        param2 = "C";
                    } else {
                        param1 = "Y";
                    }

                    isseudCouponIdList = couponBookRepository.checkValidationCheckCouponId(couponId, param1, param2);

                    System.out.println("1111111111");
                    if (isseudCouponIdList != null && !isseudCouponIdList.isEmpty()) {
                        System.out.println("22222222");
                        couponBook = updateCoupon(type, coupon);
                    } else {
                        System.out.println("33333");
                    }
                    break;
                default:
                    break;
            }
        }
        System.out.println("4444444");
        if (couponBook != null && !couponBook.getCoupon_id().isEmpty()) {
            System.out.println("555555");
            return couponBook.getCoupon_id();
        } else {
            return null;
        }
    }

    /*
        쿠폰 번호 랜덤 추출
     */
    public List<String> getRandomCoupon(List<String> extraction, int numberOfQuestions) {
        List<String> randomCoupon = new ArrayList<>();
        List<String> copy = new ArrayList<>(extraction);

        SecureRandom rand = new SecureRandom();
        for (int i = 0; i < Math.min(numberOfQuestions, extraction.size()); i++) {
            randomCoupon.add(copy.remove(rand.nextInt(copy.size())));
        }
        return randomCoupon;
    }

    /*
        쿠폰 정보 업데이트
    * */
    public CouponBook updateCoupon(CouponUseType type, CouponBook coupon) {
        CouponBook extraction = couponBookRepository.findById(coupon.getCoupon_id()).get();

        if (extraction == null){
            return null;
        } else if (extraction.getCoupon_id().isEmpty()) {
            return null;
        }

        if (type != null) {
            switch (type) {
                case issue:
                    // 쿠폰 지급
                    extraction.setIssue_yn("Y"); // 발급여부
                    extraction.setIssue_date(Date.valueOf(LocalDate.now())); // 발행일자
                    extraction.setExpire_date(Date.valueOf(LocalDate.now().plusDays(5))); // 만료일자
                    extraction.setUse_tp_cd("N"); // 사용구분코드(N/Y/C)
                    break;
                case use:
                    // 쿠폰 사용
                    extraction.setUse_tp_cd("Y");
                    extraction.setUse_date(Date.valueOf(LocalDate.now())); // 사용일자
                    break;
                case cancel:
                    // 쿠폰 사용 취소
                    extraction.setUse_tp_cd("C");
                    extraction.setUse_date(Date.valueOf(LocalDate.now())); // 취소일자
                default:
                    break;
            }
        }
        return couponBookRepository.save(extraction);
    }

    /*
        당일 만료된 쿠폰 목록 조회
     */
    public List<String> findExpiredCouponToday() {
        return couponBookRepository.findForExpiredCouponList();
    }

    /*
        사용자에게 공지할 목적으로 만료 3일 전 쿠폰 목록 조회
     */
    public List<String> findExpiredCouponForNotice() {
        return couponBookRepository.findForExpiredCouponForNotice();
    }
}
