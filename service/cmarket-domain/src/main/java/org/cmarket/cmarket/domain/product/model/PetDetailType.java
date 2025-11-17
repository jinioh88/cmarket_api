package org.cmarket.cmarket.domain.product.model;

/**
 * 반려동물 상세 종류 Enum
 * 
 * 파충류: 뱀, 도마뱀, 거북이 등
 * 포유류: 강아지, 고양이, 햄스터, 토끼 등
 * 조류: 앵무새, 카나리아 등
 * 어류: 금붕어, 열대어 등
 * 양서류: 개구리, 도롱뇽 등
 * 기타: 기타 반려동물
 */
public enum PetDetailType {
    // 파충류
    SNAKE,
    LIZARD,
    TURTLE,
    REPTILE_ETC,
    
    // 포유류
    DOG,
    CAT,
    HAMSTER,
    RABBIT,
    GUINEA_PIG,
    MAMMAL_ETC,
    
    // 조류
    PARROT,
    CANARY,
    BIRD_ETC,
    
    // 어류
    GOLDFISH,
    TROPICAL_FISH,
    FISH_ETC,
    
    // 양서류
    FROG,
    SALAMANDER,
    AMPHIBIAN_ETC,
    
    // 기타
    ETC
}

