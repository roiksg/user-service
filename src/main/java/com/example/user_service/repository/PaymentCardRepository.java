package com.example.user_service.repository;

import com.example.user_service.entity.PaymentCard;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID>,
    JpaSpecificationExecutor<PaymentCard> {

}
