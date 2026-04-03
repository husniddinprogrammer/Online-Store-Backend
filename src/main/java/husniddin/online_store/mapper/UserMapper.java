package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.UserResponse;
import husniddin.online_store.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
