package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.PaymentResponse;
import husniddin.online_store.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toResponse(Payment payment);
}
