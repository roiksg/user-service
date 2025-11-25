package com.example.user_service.util.specification;

import com.example.user_service.entity.PaymentCard;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {
    public static Specification<PaymentCard> byUserId(UUID userId) {
        return (root, query, cb) ->
            userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<PaymentCard> numberContains(String number) {
        return (root, query, cb) ->
            (number == null || number.isBlank())
                ? null
                : cb.like(cb.lower(root.get("number")), "%" + number.toLowerCase() + "%");
    }

}
