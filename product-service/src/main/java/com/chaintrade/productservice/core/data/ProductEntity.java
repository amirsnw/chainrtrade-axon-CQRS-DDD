package com.chaintrade.productservice.core.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 2268008426323359015L;

    @Id
    @Column(unique = true)
    @EqualsAndHashCode.Include
    @JdbcTypeCode(Types.VARCHAR) //VARCHAR(255) instead of default binary
    private UUID productId;

    @Column(unique = true)
    private String title;

    private BigDecimal price;

    private Integer quantity;

}
