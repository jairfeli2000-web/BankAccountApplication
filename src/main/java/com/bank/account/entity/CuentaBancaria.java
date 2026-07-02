package com.bank.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * Entidad que representa una cuenta bancaria en el sistema.
 * Mapeada a la tabla {@code cuentas_bancarias}.
 */
@Entity
@Table(name = "cuentas_bancarias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaBancaria {

    /** Identificador único autogenerado */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del titular (1-100 caracteres) */
    @Column(nullable = false, length = 100)
    private String titular;

    /** Saldo actual de la cuenta. Inicia en 0 al crear */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    /** Fecha y hora de creación de la cuenta */
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Hook JPA: asigna valores por defecto antes de persistir.
     * Garantiza saldo cero y fecha de creación automática.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }
    }
}
