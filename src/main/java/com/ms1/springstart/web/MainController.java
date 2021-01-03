package com.ms1.springstart.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms1.springstart.DTO.CouponDTO;
import com.ms1.springstart.DTO.CouponUseType;
import com.ms1.springstart.book.CouponBook;
import com.ms1.springstart.service.CouponBookService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/couponbook")
/*
    CouponBook Main Controller
 */
public class MainController {

    @Autowired
    private CouponBookService couponBookService;

    /*
        [API 1] 랜덤 쿠폰 생성
     */
    @PostMapping("/create")
    public Map<String, String> createCouponBook(@RequestBody CouponDTO coupon) {
        List<CouponBook> couponBook = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        int cnt = coupon.getCnt();
        int resultCnt;

        if (cnt > 0) {
            resultCnt = couponBookService.createCouponBook(cnt).size();
            result.put("result_message", resultCnt + "건의 쿠폰이 생성되었습니다.");
            result.put("result_tag", "ok");
        } else {
            result.put("result_message", "쿠폰 생성 실패하였습니다. 0이상의 숫자를 입력하세요.");
            result.put("result_tag", "fail");
        }
        return result;
    }

    /*
        [API 2] 사용자에게 쿠폰 지급
     */
    @PutMapping("/issue")
    public void issueCoupon(HttpServletResponse response) throws Exception {
        List<CouponBook> couponBook = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        String coupon_id = "";

        coupon_id = couponBookService.update(CouponUseType.valueOf("issue"), new CouponBook(""));

        if (coupon_id != null && !coupon_id.isEmpty()) {
            map.put("result_message", "'" + coupon_id + "' " + "쿠폰이 지급되었습니다.");
            map.put("result_tag", "ok");
        } else {
            map.put("result_message", "쿠폰 지급이 실패하였습니다.");
            map.put("result_tag", "fail");
        }
        responseRestAPI(response, map);
    }

    /*
        [API 3] 사용자에게 지급된 쿠폰 목록 조회
     */
    @GetMapping("/list/issue")
    public void selectIssuedCoupon(HttpServletResponse response) throws Exception {
        List<String> couponBook = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        couponBook = couponBookService.findIssuedCoupon();
        System.out.println("지급된 쿠폰 조회 >>> " + couponBook);

        if (couponBook.size() > 0) {
            map.put("result_message", couponBook);
            map.put("result_tag", "ok");
        } else {
            map.put("result_message", "사용자에게 지급된 쿠폰이 없습니다.");
            map.put("result_tag", "no data found.");
        }
        responseRestAPI(response, map);
    }

    /*
        [API 4] 사용자에게 지급된 쿠폰 사용 처리 (재사용 불가)
     */
    @PutMapping("/use")
    public void useCoupon(@RequestBody CouponBook coupon, HttpServletResponse response) throws Exception{
        List<CouponBook> couponBook = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        String coupon_id = "";

        coupon_id = couponBookService.update(CouponUseType.valueOf("use"), coupon);
        System.out.println("사용 처리 method >>> " + coupon_id);

        if (coupon_id != null && !coupon_id.isEmpty()) {
            map.put("result_message", "'" + coupon_id + "' " + "쿠폰 사용 처리 되었습니다.");
            map.put("result_tag", "ok");
        } else {
            map.put("result_message", "쿠폰을 확인하세요. 번호가 잘못되었거나 이미 사용된 쿠폰입니다.");
            map.put("result_tag", "fail");
        }
        responseRestAPI(response, map);
    }

    /*
        [API 5] 사용자에게 지급된 쿠폰에 대해 사용 취소 처리 (취소된 쿠폰 재사용 가능)
     */
    @PutMapping("/cancel")
    public void cancelCoupon(@RequestBody CouponBook coupon, HttpServletResponse response) throws Exception {
        List<CouponBook> couponBook = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        String coupon_id = "";

        coupon_id = couponBookService.update(CouponUseType.valueOf("cancel"), coupon);

        if (coupon_id != null && !coupon_id.isEmpty()) {
            map.put("result_message", "'" + coupon_id + "' " + "쿠폰 사용 취소 되었습니다.");
            map.put("result_tag", "ok");
        } else {
            map.put("result_message", "쿠폰 번호를 확인하세요.");
            map.put("result_tag", "fail");
        }
        responseRestAPI(response, map);
    }

    /*
        [API 6] 사용자에게 지급된 쿠폰 중, 금일 만료된 쿠폰 목록 조회
     */
    @GetMapping("/list/expire")
    public void selectExpiredCouponToday(HttpServletResponse response) throws Exception {
        List<String> couponBook = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        couponBook = couponBookService.findExpiredCouponToday();

        if (couponBook.size() > 0) {
            map.put("result_message", couponBook);
            map.put("result_tag", "ok");
        } else {
            map.put("result_message", "당일 만료된 쿠폰이 없습니다.");
            map.put("result_tag", "no data found.");
        }
        responseRestAPI(response, map);
    }

    /*
        [API 7] 사용자에게 지급된 쿠폰 중, 만료 3일 전인 쿠폰 목록 조
     */
    @GetMapping("/list/expire/notice")
    public void selectExpiredCouponForNotice(HttpServletResponse response) throws Exception {
        List<String> couponBook = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        String str = "";

        couponBook = couponBookService.findExpiredCouponForNotice();

        if (couponBook.size() > 0) {
            map.put("result_message", couponBook);
            map.put("result_tag", "ok");
        } else {
            map.put("result_message", "만료 3일 전 사용 안된 쿠폰이 없습니다.");
            map.put("result_tag", "no data found.");
        }
        responseRestAPI(response, map);
    }

    /*
        Http 응답 메세지 설정
     */
    public void responseRestAPI(HttpServletResponse response, Map<String, Object> map) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = mapper.writeValueAsString(map);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(jsonStr);
    }


}
