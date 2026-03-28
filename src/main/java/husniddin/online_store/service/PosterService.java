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
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class PosterService {

    private final PosterRepository posterRepository;
    private final PosterMapper posterMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public Page<PosterResponse> getAll(Pageable pageable) {
        return posterRepository.findAll(pageable).map(posterMapper::toResponse);
    }

    public PosterResponse create(MultipartFile image, PosterRequest request) {
        String imageUrl = fileStorageService.storePoster(image);
        Poster poster = Poster.builder()
                .imageLink(imageUrl)
                .link(request.getLink())
                .build();
        return posterMapper.toResponse(posterRepository.save(poster));
    }

    public PosterResponse update(Long id, MultipartFile image, PosterRequest request) {
        Poster poster = findById(id);
        if (image != null && !image.isEmpty()) {
            poster.setImageLink(fileStorageService.storePoster(image));
        }
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
