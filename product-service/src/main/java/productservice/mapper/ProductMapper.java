package productservice.mapper;

import com.chaintrade.core.commands.ReserveProductCommand;
import com.chaintrade.core.events.ProductReservedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import productservice.command.CreateProductCommand;
import productservice.command.rest.CreateProductRestModel;
import productservice.core.data.ProductEntity;
import productservice.core.data.ProductLookupEntity;
import productservice.core.events.ProductCreatedEvent;
import productservice.query.rest.ProductRestModel;

import java.util.UUID;

@Mapper(imports = UUID.class)
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "productId", expression = "java(UUID.randomUUID())")
    CreateProductCommand toCreateCommand(CreateProductRestModel model);

    ProductCreatedEvent toCreatedEvent(CreateProductCommand createProductCommand);

    ProductEntity toProductEntity(ProductCreatedEvent event);

    ProductRestModel toProductRestModel(ProductEntity entity);

    ProductLookupEntity toProductLookupEntity(ProductCreatedEvent event);

    ProductReservedEvent toEvent(ReserveProductCommand command);

    // ProductReservationCancelledEvent toEvent(CancelProductReservationCommand command);

}
