package org.cmarket.cmarket.web.common.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Optional;

/**
 * 쿠키 관련 유틸리티 클래스
 * 
 * OAuth2 인증 요청을 쿠키에 저장하고 조회하기 위한 유틸리티 메서드를 제공합니다.
 */
public final class CookieUtils {
    
    private CookieUtils() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
    
    /**
     * 요청에서 특정 이름의 쿠키를 찾습니다.
     *
     * @param request 요청 객체
     * @param name 쿠키 이름
     * @return 쿠키 (없으면 Optional.empty())
     */
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * 쿠키를 추가합니다.
     *
     * @param response 응답 객체
     * @param name 쿠키 이름
     * @param value 쿠키 값
     * @param maxAge 쿠키 만료 시간 (초)
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        // 개발 환경에서는 false, 운영 환경에서는 true로 설정
        // cookie.setSecure(true);
        response.addCookie(cookie);
    }
    
    /**
     * 쿠키를 삭제합니다.
     *
     * @param request 요청 객체
     * @param response 응답 객체
     * @param name 삭제할 쿠키 이름
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }
    
    /**
     * 객체를 Base64로 직렬화합니다.
     *
     * @param object 직렬화할 객체
     * @return Base64 인코딩된 문자열
     */
    public static String serialize(Object object) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            return Base64.getUrlEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize object", e);
        }
    }
    
    /**
     * Base64 문자열을 객체로 역직렬화합니다.
     *
     * @param cookie 쿠키 객체
     * @param cls 역직렬화할 클래스 타입
     * @return 역직렬화된 객체
     */
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return cls.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to deserialize object", e);
        }
    }
}
