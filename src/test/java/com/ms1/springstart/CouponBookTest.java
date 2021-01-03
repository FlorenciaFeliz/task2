package com.ms1.springstart;

import com.ms1.springstart.DTO.CouponUseType;
import com.ms1.springstart.book.CouponBook;
import com.ms1.springstart.book.CouponBookRepository;
import com.ms1.springstart.service.CouponBookService;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CouponBookTest {
    @Autowired
    CouponBookRepository couponBookRepository;

    @Autowired
    CouponBookService couponBookService;
//    @AfterEach
//    public void cleanup() { // 데이터 섞임 방지
//        couponBookRepository.deleteAll();
//    }

    @Order(1)
    @Test
    public void TEST1_InsertCouponAndSelectCoupon() {
        // given
        String id = "1234";   // 쿠폰번호 (임의 지정)
        String issueYn = "N"; // 지급여부 (임의 지정)

        // test
        System.out.println("[Start] 쿠폰 번호 : " + id);

        // 쿠폰 등록 (insert)
        couponBookRepository.save(CouponBook.builder()
                .coupon_id(id)
                .issue_yn(issueYn)
                .build());

        // when
        // 쿠폰 목록 조회 (All)
        List<CouponBook> postsList = couponBookRepository.findAll();

        // then
        // Validation Check
        CouponBook coupon = postsList.get(0);
        assertThat(coupon.getCoupon_id()).isEqualTo(id);
        assertThat(coupon.getIssue_yn()).isEqualTo(issueYn);
    }

    @Order(2)
    @Test
    public void TEST2_CreateRandomCoupon() {
        // given
        int cnt = 3;  // 쿠폰 생성 개수
        couponBookService.createCouponBook(cnt);

        // when
        // 쿠폰 목록 조회 (All)
        List<CouponBook> couponList = couponBookRepository.findAll();

        // then
        // 생성된 쿠폼 개수 확인
        assertThat(couponList.size()).isEqualTo(cnt);
    }

    @Order(3)
    @Test
    public void TEST3_IssueRandomCoupon() {
        // 1. 쿠폰 생성
        TEST2_CreateRandomCoupon();

        // given
        Boolean issue_yn = false; // 사용자 지급 여부

        Boolean result_yn = true; // 사용 처리 여부
        List<String> couponList = new ArrayList<>();
        String couponId = "";
        String issuedCouponId = "";

        // 2. 사용자 지급 여부에 따른 쿠폰 랜덤 추출 후 , 사용 처리
        if (issue_yn) {
            // Case 1 > 사용자에게 지급된 쿠폰 중에서 랜덤 추출 후, 사용 처리 (o)
            couponList = couponBookRepository.findForCouponByIssueYn("Y");

            if (couponList.size() == 0) {
                // 사용자 쿠폰 랜덤 지급
                issuedCouponId = couponBookService.update(CouponUseType.issue, new CouponBook("",""));
            }
        } else {
            // Case 2 > 사용자에게 지급 안된 쿠폰 중에서 랜덤 추출 후, 사용 처리 (x)
            couponList = couponBookRepository.findForCouponByIssueYn("N");
        }

        // 랜덤 추첨
        List<String> extraction =  new ArrayList<>();
        if (issuedCouponId.isEmpty()) {
            extraction = couponBookService.getRandomCoupon(couponList, 1);
        } else {
            extraction.add(new String(issuedCouponId));
        }

        // when
        // 쿠폰 사용 처리
        CouponBook coupon = new CouponBook(extraction.get(0), "");
        couponId = couponBookService.update(CouponUseType.use, coupon);

        if (couponId != null && !couponId.isEmpty()) {
            result_yn = true; // 사용 처리 (o)
        } else {
            result_yn = false; // 사용 처리 (x)
        }

        // then
        assertThat(result_yn).isEqualTo(issue_yn); // 지급된 쿠폰만 사용 처리 가
    }

    @Order(4)
    @Test
    public void TEST4_CreateRandomCoupon() {
        String id = "";            // 쿠폰 번호
        String couponId = "";      // 사용 취소 처리된 쿠폰 번호
        Boolean result_yn = false; // 처리 결과

        // 1. 쿠폰 생성
        TEST2_CreateRandomCoupon();

        // given
        Boolean validateIdYn = false; // 유효한 쿠폰 번호인지 아닌지 선택 (true/false)

        if (validateIdYn) {
            // 랜덤 지급
            String issuedCouponId = couponBookService.update(CouponUseType.issue, new CouponBook("",""));
            // 사용 처리
            String usedCouponId = couponBookRepository.checkValidationCheckCouponId(issuedCouponId, "Y", null);
            // 사용 처리된 쿠폰 번호 셋팅
            id = usedCouponId;
        } else {
            id = "1234"; // 잘못된 쿠폰 번호 (임의 지정)
        }

        // when
        // 쿠폰 사용/취소 처리
        CouponBook coupon = new CouponBook(id, "");
        couponId = couponBookService.update(CouponUseType.cancel, coupon);

        if (couponId != null && !couponId.isEmpty()) {
            result_yn = true; // 사용 처리 (o)
        } else {
            result_yn = false; // 사용 처리 (x)
        }

        // then
        // 생성된 쿠폼 개수 확인
        assertThat(result_yn).isEqualTo(validateIdYn);
    }
}
