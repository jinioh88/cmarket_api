package org.cmarket.cmarket.domain.chat.app.service;

/**
 * 개인정보 필터링 서비스 인터페이스
 * 
 * 채팅 메시지에서 개인정보를 탐지하는 서비스입니다.
 * 탐지 대상:
 * - 전화번호
 * - 이메일 주소
 * - 계좌번호
 * - 주민등록번호
 */
public interface PrivacyFilterService {
    
    /**
     * 개인정보 포함 여부 확인
     * 
     * @param content 검사할 메시지 내용
     * @return 개인정보가 포함되어 있으면 true
     */
    boolean containsPrivateInfo(String content);
    
    /**
     * 차단 사유 반환
     * 
     * @param content 검사할 메시지 내용
     * @return 차단 사유 (개인정보 없으면 null)
     */
    String getBlockReason(String content);
}
