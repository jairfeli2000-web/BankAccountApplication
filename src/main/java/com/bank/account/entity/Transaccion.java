package com.bank.account.entity;

import com.bank.account.enums.TipoTransaccion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que registra cada movimiento financiero (depósito o retiro).
 * Mapeada a la tabla {@code transacciones}.
 */
@Entity
@Table(name = "transacciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaccion {

    /** Identificador único autogenerado */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cuenta bancaria asociada a la transacción */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_bancaria_id", nullable = false)
    private CuentaBancaria cuentaBancaria;

    /** Tipo de operación: DEPOSITO o RETIRO */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransaccion tipo;

    /** Monto de la transacción (siempre positivo) */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    /** Fecha y hora en que se registró la transacción */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Asigna la fecha automáticamente si no fue provista */
    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
