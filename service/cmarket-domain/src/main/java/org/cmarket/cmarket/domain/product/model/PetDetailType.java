package org.cmarket.cmarket.domain.product.model;

/**
 * 반려동물 상세 종류 Enum
 * 
 * 포유류: 강아지, 고양이, 토끼, 햄스터, 기니피그, 페럿, 친칠라, 고슴도치
 * 조류: 잉꼬, 앵무새, 카나리아, 모란앵무
 * 파충류: 도마뱀, 뱀, 거북이, 게코
 * 수생동물: 금붕어, 열대어, 체리새우, 달팽이
 * 곤충/절지동물: 귀뚜라미, 사마귀, 딱정벌레, 거미
 */
public enum PetDetailType {
    // 포유류
    DOG,
    CAT,
    RABBIT,
    HAMSTER,
    GUINEA_PIG,
    FERRET,
    CHINCHILLA,
    HEDGEHOG,
    
    // 조류
    BUDGERIGAR,
    PARROT,
    CANARY,
    LOVEBIRD,
    
    // 파충류
    LIZARD,
    SNAKE,
    TURTLE,
    GECKO,
    
    // 수생동물
    GOLDFISH,
    TROPICAL_FISH,
    CHERRY_SHRIMP,
    SNAIL,
    
    // 곤충/절지동물
    CRICKET,
    MANTIS,
    BEETLE,
    SPIDER
}

