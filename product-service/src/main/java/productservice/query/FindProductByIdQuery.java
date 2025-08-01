package productservice.query;

import lombok.Value;

import java.util.UUID;

@Value
public class FindProductByIdQuery {

    UUID productId;
}
