package productservice.command.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import productservice.command.CreateProductCommand;
import productservice.mapper.ProductMapper;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductsCommandController {

    private final Environment environment;
    private final ProductMapper mapper;
    private final CommandGateway commandGateway;

    @PostMapping
    public ResponseEntity<UUID> createProduct(@Valid @RequestBody CreateProductRestModel createProductRestModel) {
        CreateProductCommand createProductCommand = mapper.toCreateCommand(createProductRestModel);

        UUID productId = commandGateway.sendAndWait(createProductCommand);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .pathSegment("{id}")
                .buildAndExpand(productId)
                .toUri();

        return ResponseEntity
                .created(location)
                .body(productId);
    }

    @PutMapping
    public String updateProduct() {
        return "Http PUT is handled";
    }

    @DeleteMapping
    public String deleteProduct() {
        return "Http DELETE is handled";
    }

}
