package com.chaintrade.productservice.mapper;

import com.chaintrade.core.commands.ReserveProductCommand;
import com.chaintrade.core.events.ProductReservedEvent;
import com.chaintrade.core.model.ProductRestModel;
import com.chaintrade.productservice.command.CreateProductCommand;
import com.chaintrade.productservice.command.UpdateProductCommand;
import com.chaintrade.productservice.command.rest.CreateProductRestModel;
import com.chaintrade.productservice.command.rest.UpdateProductRestModel;
import com.chaintrade.productservice.core.data.ProductEntity;
import com.chaintrade.productservice.core.data.ProductLookupEntity;
import com.chaintrade.productservice.core.events.ProductCreatedEvent;
import com.chaintrade.productservice.core.events.ProductUpdatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(imports = UUID.class)
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "productId", expression = "java(UUID.randomUUID())")
    CreateProductCommand toCreateCommand(CreateProductRestModel model);

    UpdateProductCommand toUpdateCommand(UpdateProductRestModel model);

    ProductCreatedEvent toCreatedEvent(CreateProductCommand createProductCommand);

    ProductUpdatedEvent toUpdateEvent(UpdateProductCommand updateProductCommand);

    ProductEntity toProductEntity(ProductCreatedEvent event);

    ProductEntity toProductEntity(ProductUpdatedEvent event);

    ProductRestModel toProductRestModel(ProductEntity entity);

    ProductLookupEntity toProductLookupEntity(ProductCreatedEvent event);

    ProductLookupEntity toProductLookupEntity(ProductUpdatedEvent event);

    ProductReservedEvent toEvent(ReserveProductCommand command);

    // ProductReservationCancelledEvent toEvent(CancelProductReservationCommand command);

}
