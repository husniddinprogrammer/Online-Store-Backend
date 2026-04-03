package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.PosterResponse;
import husniddin.online_store.entity.Poster;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PosterMapper {

    PosterResponse toResponse(Poster poster);
}
