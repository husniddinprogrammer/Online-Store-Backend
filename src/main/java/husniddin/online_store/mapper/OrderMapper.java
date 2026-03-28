package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.OrderItemResponse;
import husniddin.online_store.dto.response.OrderResponse;
import husniddin.online_store.entity.Order;
import husniddin.online_store.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {AddressMapper.class})
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(order.getUser().getName() + ' ' + order.getUser().getSurname())")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageLink", ignore = true)
    @Mapping(target = "totalPrice", expression = "java(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    OrderItemResponse toItemResponse(OrderItem item);
}
