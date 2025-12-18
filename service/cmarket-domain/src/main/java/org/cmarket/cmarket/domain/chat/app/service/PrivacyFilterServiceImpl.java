package org.cmarket.cmarket.domain.chat.app.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 개인정보 필터링 서비스 구현체
 * 
 * 정규표현식을 사용하여 개인정보를 탐지합니다.
 */
@Service
public class PrivacyFilterServiceImpl implements PrivacyFilterService {
    
    // 전화번호 패턴 (010-1234-5678, 01012345678, 010 1234 5678 등)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "01[0-9][- ]?[0-9]{3,4}[- ]?[0-9]{4}"
    );
    
    // 이메일 패턴
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    
    // 계좌번호 패턴 (10~16자리 숫자, 하이픈 포함 가능)
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile(
            "\\d{2,6}[- ]?\\d{2,6}[- ]?\\d{2,6}[- ]?\\d{0,6}"
    );
    
    // 주민등록번호 패턴 (000000-0000000)
    private static final Pattern SSN_PATTERN = Pattern.compile(
            "\\d{6}[- ]?[1-4]\\d{6}"
    );
    
    @Override
    public boolean containsPrivateInfo(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }
        
        return PHONE_PATTERN.matcher(content).find()
                || EMAIL_PATTERN.matcher(content).find()
                || SSN_PATTERN.matcher(content).find()
                || isLikelyAccountNumber(content);
    }
    
    @Override
    public String getBlockReason(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        
        List<String> reasons = new ArrayList<>();
        
        if (PHONE_PATTERN.matcher(content).find()) {
            reasons.add("전화번호");
        }
        
        if (EMAIL_PATTERN.matcher(content).find()) {
            reasons.add("이메일 주소");
        }
        
        if (SSN_PATTERN.matcher(content).find()) {
            reasons.add("주민등록번호");
        }
        
        if (isLikelyAccountNumber(content)) {
            reasons.add("계좌번호");
        }
        
        if (reasons.isEmpty()) {
            return null;
        }
        
        return String.join(", ", reasons) + " 포함";
    }
    
    /**
     * 계좌번호 가능성 판단
     * 
     * 단순 숫자 나열과 구분하기 위해 추가 조건 검사:
     * - 10~16자리 숫자 (하이픈 제외)
     * - 하이픈이나 공백으로 구분된 형태
     */
    private boolean isLikelyAccountNumber(String content) {
        if (!ACCOUNT_PATTERN.matcher(content).find()) {
            return false;
        }
        
        // 계좌번호로 의심되는 문자열 추출
        java.util.regex.Matcher matcher = ACCOUNT_PATTERN.matcher(content);
        while (matcher.find()) {
            String matched = matcher.group();
            String digitsOnly = matched.replaceAll("[- ]", "");
            
            // 10~16자리 숫자이고, 하이픈이나 공백이 포함된 경우 계좌번호로 판단
            if (digitsOnly.length() >= 10 && digitsOnly.length() <= 16) {
                if (matched.contains("-") || matched.contains(" ")) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
