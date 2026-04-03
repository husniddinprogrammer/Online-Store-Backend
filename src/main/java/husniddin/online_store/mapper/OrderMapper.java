package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.OrderItemResponse;
import husniddin.online_store.dto.response.OrderResponse;
import husniddin.online_store.entity.Order;
import husniddin.online_store.entity.OrderItem;
import husniddin.online_store.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(order.getUser().getName() + ' ' + order.getUser().getSurname())")
    @Mapping(target = "items", ignore = true)
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", expression = "java(resolveProductName(item))")
    @Mapping(target = "productImageLink", ignore = true)
    @Mapping(target = "totalPrice", expression = "java(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    OrderItemResponse toItemResponse(OrderItem item);

    default String resolveProductName(OrderItem item) {
        Product product = item.getProduct();
        if (product != null) {
            return product.getName();
        }
        Long productId = item.getProductId();
        return productId != null ? "Deleted product #" + productId : "Deleted product";
    }
}
