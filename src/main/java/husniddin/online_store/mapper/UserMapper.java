package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.UserResponse;
import husniddin.online_store.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toResponse(User user);
}
