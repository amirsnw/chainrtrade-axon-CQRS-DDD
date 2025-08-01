package productservice.core.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;

import java.io.Serializable;
import java.sql.Types;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "product_lookup")
public class ProductLookupEntity implements Serializable {

    private static final long serialVersionUID = -203523508911497034L;

    @Id
    @EqualsAndHashCode.Include
    @JdbcTypeCode(Types.VARCHAR) //VARCHAR(255) instead of default binary
    private UUID productId;

    @Column(unique = true)
    private String title;

}
