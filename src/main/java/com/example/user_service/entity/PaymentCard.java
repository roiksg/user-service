package com.example.user_service.entity;

import com.example.user_service.entity.enums.PaymentCardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_card")
public class PaymentCard {

    @Id
    @Column(columnDefinition = "uuid")
    @UuidGenerator(style = UuidGenerator.Style.TIME) // UUIDv7
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "number", nullable = false, unique = true, length = 16)
    private String number;

    private String holder;

    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "PAYMENT_CARD_TYPE")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PaymentCardType active = PaymentCardType.ACTIVE;
}
