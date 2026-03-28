package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.NotificationResponse;
import husniddin.online_store.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
