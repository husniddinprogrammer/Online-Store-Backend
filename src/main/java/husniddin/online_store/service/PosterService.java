package husniddin.online_store.service;

import husniddin.online_store.dto.request.PosterRequest;
import husniddin.online_store.dto.response.PosterResponse;
import husniddin.online_store.entity.Poster;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.PosterMapper;
import husniddin.online_store.repository.PosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PosterService {

    private final PosterRepository posterRepository;
    private final PosterMapper posterMapper;

    @Transactional(readOnly = true)
    public Page<PosterResponse> getAll(Pageable pageable) {
        return posterRepository.findAll(pageable).map(posterMapper::toResponse);
    }

    public PosterResponse create(PosterRequest request) {
        Poster poster = Poster.builder()
                .imageLink(request.getImageLink())
                .link(request.getLink())
                .build();
        return posterMapper.toResponse(posterRepository.save(poster));
    }

    public PosterResponse update(Long id, PosterRequest request) {
        Poster poster = findById(id);
        poster.setImageLink(request.getImageLink());
        poster.setLink(request.getLink());
        return posterMapper.toResponse(posterRepository.save(poster));
    }

    public PosterResponse click(Long id) {
        Poster poster = findById(id);
        poster.setClickQuantity(poster.getClickQuantity() + 1);
        return posterMapper.toResponse(posterRepository.save(poster));
    }

    public void delete(Long id) {
        Poster poster = findById(id);
        poster.setDeleted(true);
        posterRepository.save(poster);
    }

    private Poster findById(Long id) {
        return posterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poster", id));
    }
}
