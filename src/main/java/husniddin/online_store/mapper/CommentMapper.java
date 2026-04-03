package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.CommentResponse;
import husniddin.online_store.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userSurname", source = "user.surname")
    CommentResponse toResponse(Comment comment);
}
