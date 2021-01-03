package com.ms1.springstart;

import com.ms1.springstart.DTO.CouponUseType;
import com.ms1.springstart.book.CouponBook;
import com.ms1.springstart.book.CouponBookRepository;
import com.ms1.springstart.service.CouponBookService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
/*
    Test Code
 */
public class CouponBookTest {
    @Autowired
    CouponBookRepository couponBookRepository;

    @Autowired
    CouponBookService couponBookService;

    @AfterEach
    public void cleanup() {
        couponBookRepository.deleteAll();
    }

    @Order(1)
    @Test
    @Description("임의로 지정한 쿠폰 번호로 DB에 데이터를 insert하고 조회가 잘되는지 확인")
    public void TEST1_InsertCouponAndSelectCoupon() {
        // given
        // 쿠폰번호(id) 임의 지정 후 DB에 데이터가 잘 저장되고 조회되는지 확인
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
    @Description("임의로 지정한 개수만큼 쿠폰이 잘 생성되는지 확인")
    public void TEST2_CreateRandomCoupon() {
        // given
        // 지정한 개수만큼 쿠폰이 생성되어야 정상 (테스트 방법 : cnt = 개수 지정)
        int cnt = 3;  // 쿠폰 생성 개수
        couponBookService.createCouponBook(cnt);

        // when
        // 쿠폰 목록 조회 (All)
        List<CouponBook> couponList = couponBookRepository.findAll();

        // then
        // 생성된 쿠폰 개수 확인
        assertThat(couponList.size()).isEqualTo(cnt);
    }

    @Order(3)
    @Test
    @Description("사용자에게 지급된 또는 지급안된 쿠폰에 대해 사용 처리가 잘 되는지 안되는지 확인")
    public void TEST3_IssueRandomCouponAndUpdateUseY() {
        // 1. 쿠폰 생성
        TEST2_CreateRandomCoupon();

        // given
        // 사용자에게 지급되지 않은 쿠폰 번호를 입력받지 않은 경우(테스트 방법 : issue_yn = false), 쿠폰 사용 처리 안되어야 정상
        // 사용자에게 지급된 쿠폰 번호를 입력 받은 경우(테스트 방법 : issue_yn = true), 쿠폰 사용 처리 되어야 정상
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
                issuedCouponId = couponBookService.update(CouponUseType.issue, new CouponBook(""));
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
        // 쿠폰 사용 처리 확인
        assertThat(result_yn).isEqualTo(issue_yn);
    }

    @Order(4)
    @Test
    @Description("유효 / 유효하지 않은 쿠폰 번호를 입력받았을 경우, 쿠폰 사용처리가 잘되는지 확인")
    public void TEST4_ValidateCouponIdAndUpdateUseY() {
        String id = "";            // 쿠폰 번호
        String couponId = "";      // 사용 취소 처리된 쿠폰 번호
        Boolean result_yn = false; // 처리 결과

        // 1. 쿠폰 생성
        TEST2_CreateRandomCoupon();

        // given
        // 유효하지않은 쿠폰 번호(테스트 방법 : validateIdYn = false)를 입력받았을 경우, 쿠폰 사용 처리 안되어야 정상
        // 유효한 쿠폰 번호(테스트 방법 : validateIdYn = true)를 입력받았을 경우, 쿠폰 사용 처리 되어야 정상
        Boolean validateIdYn = false; // 유효한 쿠폰 번호인지 아닌지 선택 (true/false)

        if (validateIdYn) {
            // 랜덤 지급
            String issuedCouponId = couponBookService.update(CouponUseType.issue, new CouponBook(""));
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
        // 쿠폰 사용 처리 확인
        assertThat(result_yn).isEqualTo(validateIdYn);
    }

    @Order(5)
    @Test
    @Description("이미 사용 처리된 쿠폰에 대해 재사용 불가한지 확인")
    public void TEST5_CheckReusePossibility() {
        String randomCouponId = "";
        String usedCouponId = "";
        CouponBook coupon;

        // 1. 쿠폰 생성
        TEST2_CreateRandomCoupon();

        // 2. 랜덤 추출, 사용 처리
        // 랜덤 지급
        randomCouponId = couponBookService.update(CouponUseType.issue, new CouponBook(""));
        coupon = new CouponBook(randomCouponId);

        // 사용 처리
        usedCouponId = couponBookService.update(CouponUseType.valueOf("use"), coupon);

        // given
        // 앞에서 사용 처리된 쿠폰에 대해 다시 재사용 처리하기 위해 쿠폰 번호 재셋팅
        coupon = new CouponBook(usedCouponId);

        // when
        // 재사용 처리
        String reUsedCouponId = null;
        reUsedCouponId = couponBookService.update(CouponUseType.valueOf("use"), coupon);

//        System.out.println("랜덤 쿠폰 : " + randomCouponId);
//        System.out.println("사용 처리된 쿠폰 : " + usedCouponId);
//        System.out.println("재사용 처리된 쿠폰 : " + reUsedCouponId);

        // then
        // 이미 사용된 쿠폰은 재사용 안되는지 확인 (null이어야 성공)
        Assertions.assertNull(reUsedCouponId);
    }

    @Order(6)
    @Test
    @Description("취소된 쿠폰에 대해 사용 처리 가능한지 확인")
    public void TEST6_CheckUsePossibilityByCanceledCoupon() {
        String randomCouponId = "";
        String usedCouponId = "";
        String canceledCouponId = "";
        CouponBook coupon;

        // 1. 쿠폰 생성
        TEST2_CreateRandomCoupon();

        // 2. 랜덤 추출, 사용 처리
        // 랜덤 지급
        randomCouponId = couponBookService.update(CouponUseType.issue, new CouponBook(""));
        coupon = new CouponBook(randomCouponId);

        // 사용 처리
        usedCouponId = couponBookService.update(CouponUseType.valueOf("use"), coupon);
        coupon = new CouponBook(usedCouponId);

        // 사용 취소 처리
        canceledCouponId = couponBookService.update(CouponUseType.valueOf("cancel"), coupon);

        // 3. 앞에서 사용 취소된 쿠폰으로 재사용 가능한지 확인
        // given
        coupon = new CouponBook(canceledCouponId);

        // when
        // 쿠폰 재사용 처리
        String reUsedCouponId = null;
        reUsedCouponId = couponBookService.update(CouponUseType.valueOf("use"), coupon);

//        System.out.println("랜덤 쿠폰 : " + randomCouponId);
//        System.out.println("사용 처리된 쿠폰 : " + usedCouponId);
//        System.out.println("사용 취소 처리된 쿠폰 : " + canceledCouponId);
//        System.out.println("재사용 처리된 쿠폰 : " + reUsedCouponId);

        // then
        // 이미 사용된 쿠폰은 재사용 안되는지 확인 (not null이어야 성공)
        Assertions.assertNotNull(reUsedCouponId);
    }

    @Order(7)
    @Test
    @Description("취소된 쿠폰을 또 취소 가능한지 확인")
    public void TEST7_CheckCanclePossibilityByCanceledCoupon() {
        String randomCouponId = "";
        String usedCouponId = "";
        String canceledCouponId = "";
        CouponBook coupon;

        // 1. 쿠폰 생성
        TEST2_CreateRandomCoupon();

        // 2. 랜덤 추출, 사용 처리
        // 랜덤 지급
        randomCouponId = couponBookService.update(CouponUseType.issue, new CouponBook(""));
        coupon = new CouponBook(randomCouponId);

        // 사용 처리
        usedCouponId = couponBookService.update(CouponUseType.valueOf("use"), coupon);
        coupon = new CouponBook(usedCouponId);

        // 사용 취소 처리
        canceledCouponId = couponBookService.update(CouponUseType.valueOf("cancel"), coupon);

        // given
        // 앞에서 사용 취소된 쿠폰을 다시 재취소 시도
        coupon = new CouponBook(canceledCouponId);

        // when
        String reCanceledCouponId = null;
        reCanceledCouponId = couponBookService.update(CouponUseType.valueOf("cancel"), coupon);

        System.out.println("랜덤 쿠폰 : " + randomCouponId);
        System.out.println("사용 처리된 쿠폰 : " + usedCouponId);
        System.out.println("사용 취소 처리된 쿠폰 : " + canceledCouponId);
        System.out.println("재취소 처리된 쿠폰 : " + reCanceledCouponId);

        // then
        // 한 번 취소된 쿠폰은 재취소 안되는지 확인 (null이어야 성공)
        Assertions.assertNull(reCanceledCouponId);
    }

    @Order(8)
    @Test
    @Description("금일 만료되는 쿠폰 목록 조회 잘되는지 확인")
    public void TEST8_SelectExpiredCouponListToday() {
        String randomCouponId = "";
        CouponBook usedCoupon;
        CouponBook canceledCoupon;

        // 1. 랜덤 쿠폰 5개 생성 (
        int totalCnt = couponBookService.createCouponBook(5).size();

        // 2. 랜덤 추출, 1개 쿠폰은 사용 처리, 다른 1개 쿠폰은 최종 사용 취소 처리 (만료일은 두 쿠폰 모두 당일로 임의 셋팅)
        // 랜덤 추출, 사용자에게 지급 -> 사용 처리
        randomCouponId = couponBookService.update(CouponUseType.issue, new CouponBook(""));
        usedCoupon = new CouponBook(randomCouponId); // 랜덤 추출된 쿠폰

        usedCoupon.setCoupon_id(randomCouponId); // 쿠폰 번호
        usedCoupon.setIssue_yn("Y"); // 지급 여부
        usedCoupon.setIssue_date(Date.valueOf(LocalDate.now())); // 지급일자
        usedCoupon.setExpire_date(Date.valueOf(LocalDate.now())); // 만료일자 = sysdate
        usedCoupon.setUse_tp_cd("Y"); // 사용여부(N : 사용안함, Y : 사용, C : 사용취소)
        usedCoupon.setUse_date(Date.valueOf(LocalDate.now())); // 사용일자
        couponBookRepository.save(usedCoupon); // 사용 처리함

        // 랜덤 추출, 사용자에게 지급 -> 사용 처리 -> 사용 취소 처리 (만료일 임의로 금일로 지정)
        randomCouponId = couponBookService.update(CouponUseType.issue, new CouponBook(""));
        canceledCoupon = new CouponBook(randomCouponId); // 랜덤 추출된 쿠폰

        couponBookService.update(CouponUseType.valueOf("use"), canceledCoupon); // 사용 처리

        canceledCoupon.setCoupon_id(randomCouponId); // 쿠폰 번호
        canceledCoupon.setIssue_yn("Y"); // 지급 여부
        canceledCoupon.setIssue_date(Date.valueOf(LocalDate.now())); // 지급일자
        canceledCoupon.setExpire_date(Date.valueOf(LocalDate.now())); // 만료일자 = sysdate
        canceledCoupon.setUse_tp_cd("C"); // 사용여부(N : 사용안함, Y : 사용, C : 사용취소)
        canceledCoupon.setUse_date(Date.valueOf(LocalDate.now())); // 취소일자
        couponBookRepository.save(canceledCoupon); // 이미 사용된 쿠폰 다시 취소 처리함

        // given
        // none.

        // when
        // 금일 만료되는 쿠폰 목록 조회 (이미 사용된 쿠폰은 제외)
        List<String> couponList = couponBookRepository.findForExpiredCouponForNotice();

        System.out.println("생성된 쿠폰 건수 : " + totalCnt);
        System.out.println("사용 처리된 쿠폰 : " + usedCoupon.getCoupon_id());               // 만료일 금일 쿠폰번호번호
        System.out.println("사용 처리 후 다시 취소된 쿠폰 : " + canceledCoupon.getCoupon_id()); // 만료일 금일 쿠폰
        System.out.println("금일 만료되는 쿠폰 건수 : " + couponList.size());

        // then
        // 만료일이 금일이면서 아직 사용 안된 쿠폰 건수가 예상되는 값과 같은지 확인
        int estimatedValue = 1;
        assertThat(estimatedValue).isEqualTo(couponList.size());
    }

    @Order(9)
    @Test
    @Description("만료 3일 전 쿠폰 목록 조회 잘되는지 확인 (단, 만료일 이미 지난 쿠폰은 목록에서 제외하며 사용자가 사용한 쿠폰도 목록에서 제외함)")
    public void TEST9_SelectExpiredCouponForNoticeToUser() {
        String randomCouponId;     // 랜덤 추첨한 쿠폰 번호
        String issuedCouponId;     // 사용자에게 지급된 쿠폰 번호
        CouponBook usedCoupon;     // 사용 처리된 쿠폰
        CouponBook canceledCoupon; // 취소 처리된 쿠폰
        List<String> issuedCouponList = new ArrayList<>(); // 사용자에게 지급된 쿠폰 리스트
        int expNotUsedCoupon = 2;
        int expUsedCoupon = 2; // 오늘이 만료 3일 전이면서 사용된 쿠폰의 개수

        // 1. 랜덤 쿠폰 5개 생성
        int totalCnt = couponBookService.createCouponBook(5).size();

        // 2. [가정] 1번에서 생성된 쿠폰에서 랜덤으로 4개 쿠폰을 추출하여 사용자에게 지급하고 이 중 2개만 사용 처리함
        for(int j = 0 ; j < totalCnt-expNotUsedCoupon+1 ; j ++) {
            // 랜덤 추출 및 사용자에게 지급 (지급일 : sysdate, 만료일 : sysdate + 5days)
            issuedCouponId = couponBookService.update(CouponUseType.issue, new CouponBook(""));

            if (j >= expUsedCoupon) {
                continue;
            }

            // 사용 처리
            String str = couponBookService.update(CouponUseType.valueOf("use"), new CouponBook(issuedCouponId));
        }

        // 3. 사용자에게 지급된 쿠폰 목록 조회
        issuedCouponList = couponBookService.findIssuedCoupon();

        // 4. 쿠폰 전체 조회
        Object couponList = couponBookRepository.findAll(); // 전체 쿠폰 목록
        List<CouponBook> castedList = (List<CouponBook>) (List) couponList;

        // 5. 지급된 쿠폰에 대해 만료일을 모두 sysdate + 2days로 저장
        for(CouponBook cp : castedList){
            if (cp.getIssue_yn().equals("Y")) { // 지급 o
                if (cp.getUse_tp_cd().equals("Y")) { // 사용 o
                    // 지급된 쿠폰의 만료일을 모두 sysdate + 2days로 임의 변경
                    cp.setExpire_date(Date.valueOf(LocalDate.now().plusDays(2)));
                } else if (cp.getUse_tp_cd().equals("N")) { // 사용 X
                    cp.setExpire_date(Date.valueOf(LocalDate.now().plusDays(2)));
                }
            }
        }
        couponBookRepository.saveAll(castedList);

//        // 변경된 만료일 확인
//        Object allList = couponBookRepository.findAll(); // 전체 쿠폰 목록
//        List<CouponBook> castList = (List<CouponBook>) (List) couponList;
//
//        for(CouponBook c : castList){
//            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
//            System.out.println(c.getCoupon_id() + " / " + c.getIssue_yn() + " / " + c.getUse_tp_cd() + " / " + c.getExpire_date());
//        }

        // given
        // none.

        // when
        // 만료 3일 전, 쿠폰 목록 조회 (아직까지 사용 안된 쿠폰만 조회)
        List<String> noticeCouponList = couponBookRepository.findForExpiredCouponForNotice();

        System.out.println("생성된 쿠폰 건수 : " + totalCnt);
        System.out.println("사용자에게 지급된 쿠폰 건수 : " + issuedCouponList.size());
        System.out.println("만료일이 " + LocalDate.now().plusDays(2) + "이면서 이미 사용 처리된 쿠폰 건수 : " + expUsedCoupon);
        System.out.println("만료일이 " + LocalDate.now().plusDays(2) + "이면서 아직 사용 안된 쿠폰 건수 : " + expNotUsedCoupon);
        System.out.println("만료 3일 전이면서 아직까지 사용 안된 쿠폰 건수 : " + noticeCouponList.size());

        // then
        // 만료일이 3일 전인데 아직까지 사용 안된 쿠폰의 건수가 예상되는 값과 같은지 확인
        assertThat(expNotUsedCoupon).isEqualTo(noticeCouponList.size());
    }

}
