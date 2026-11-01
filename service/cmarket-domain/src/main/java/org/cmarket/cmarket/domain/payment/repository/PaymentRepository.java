package org.cmarket.cmarket.domain.payment.repository;

import jakarta.persistence.LockModeType;
import org.cmarket.cmarket.domain.payment.model.Payment;
import org.cmarket.cmarket.domain.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByProductIdAndStatus(Long productId, PaymentStatus status);

    /**
     * 확정할 때 쓴다. 같은 주문이 동시에 두 번 확정되지 않게 행을 잠근다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.orderId = :orderId")
    Optional<Payment> findByOrderIdForUpdate(@Param("orderId") String orderId);
}
