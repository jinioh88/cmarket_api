package org.cmarket.cmarket.domain.map.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.cmarket.cmarket.domain.map.model.AnimalType;
import org.cmarket.cmarket.domain.map.model.HospitalDetail;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.domain.map.model.QHospitalDetail;
import org.cmarket.cmarket.domain.map.model.QPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

public class PlaceRepositoryCustomImpl implements PlaceRepositoryCustom {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final JPAQueryFactory queryFactory;
    private final QPlace place = QPlace.place;
    private final QHospitalDetail hospitalDetail = QHospitalDetail.hospitalDetail;

    public PlaceRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<Place> searchPlaces(
            PlaceCategory category,
            Double latitude,
            Double longitude,
            Double radius,
            Double minLatitude,
            Double maxLatitude,
            Double minLongitude,
            Double maxLongitude,
            Boolean isRecommended,
            Boolean is24Hours,
            Boolean isEmergencyAvailable,
            List<AnimalType> animalTypes,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(place.category.eq(category));
        builder.and(place.salesStatusCode.isNull().or(place.salesStatusCode.eq("01")));

        if (isRecommended != null) {
            builder.and(place.isRecommended.eq(isRecommended));
        }

        if (category == PlaceCategory.HOSPITAL) {
            if (is24Hours != null) {
                builder.and(hospitalDetail.is24Hours.eq(is24Hours));
            }
            if (isEmergencyAvailable != null) {
                builder.and(hospitalDetail.isEmergencyAvailable.eq(isEmergencyAvailable));
            }
            if (animalTypes != null && !animalTypes.isEmpty()) {
                builder.and(hospitalDetail.animalTypes.any().in(animalTypes));
            }
        }

        boolean hasBounds = minLatitude != null && maxLatitude != null && minLongitude != null && maxLongitude != null;
        NumberExpression<Double> distanceExpression = null;

        if (hasBounds) {
            builder.and(place.latitude.between(minLatitude, maxLatitude));
            builder.and(place.longitude.between(minLongitude, maxLongitude));
        } else {
            distanceExpression = createDistanceExpression(latitude, longitude);
            builder.and(distanceExpression.loe(radius));
        }

        JPAQuery<Place> contentQuery = queryFactory
                .selectDistinct(place)
                .from(place)
                .leftJoin(hospitalDetail).on(hospitalDetail.placeId.eq(place.id))
                .where(builder);

        if (hasBounds) {
            contentQuery.orderBy(new OrderSpecifier<>(Order.ASC, place.id));
        } else {
            contentQuery.orderBy(
                    new OrderSpecifier<>(Order.ASC, distanceExpression),
                    new OrderSpecifier<>(Order.ASC, place.id)
            );
        }

        contentQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        JPAQuery<Long> countQuery = queryFactory
                .select(place.id.countDistinct())
                .from(place)
                .leftJoin(hospitalDetail).on(hospitalDetail.placeId.eq(place.id))
                .where(builder);

        List<Place> content = contentQuery.fetch();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Place> searchAdminPlaces(
            String keyword,
            PlaceCategory category,
            Boolean isRecommended,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim().toLowerCase();
            builder.and(
                    place.name.lower().contains(trimmedKeyword)
                            .or(place.address.lower().contains(trimmedKeyword))
            );
        }

        if (category != null) {
            builder.and(place.category.eq(category));
        }

        if (isRecommended != null) {
            builder.and(place.isRecommended.eq(isRecommended));
        }

        JPAQuery<Place> contentQuery = queryFactory
                .selectFrom(place)
                .where(builder)
                .orderBy(
                        new OrderSpecifier<>(Order.DESC, place.updatedAt),
                        new OrderSpecifier<>(Order.DESC, place.id)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        JPAQuery<Long> countQuery = queryFactory
                .select(place.count())
                .from(place)
                .where(builder);

        List<Place> content = contentQuery.fetch();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private NumberExpression<Double> createDistanceExpression(Double latitude, Double longitude) {
        return Expressions.numberTemplate(
                Double.class,
                "(" + EARTH_RADIUS_KM + " * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1}))))",
                Expressions.constant(latitude),
                place.latitude,
                place.longitude,
                Expressions.constant(longitude)
        );
    }
}
