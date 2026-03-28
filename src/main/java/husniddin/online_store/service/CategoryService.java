package husniddin.online_store.service;

import husniddin.online_store.dto.request.CategoryRequest;
import husniddin.online_store.dto.response.CategoryResponse;
import husniddin.online_store.entity.Category;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.CategoryMapper;
import husniddin.online_store.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    @Cacheable("categories")
    public Page<CategoryResponse> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(findById(id));
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse create(CategoryRequest request, MultipartFile image) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category already exists: " + request.getName());
        }
        String imageLink = hasFile(image) ? fileStorageService.store(image, "categories") : null;
        Category category = Category.builder()
                .name(request.getName())
                .imageLink(imageLink)
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse update(Long id, CategoryRequest request, MultipartFile image) {
        Category category = findById(id);
        category.setName(request.getName());
        if (hasFile(image)) {
            category.setImageLink(fileStorageService.store(image, "categories"));
        }
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id) {
        Category category = findById(id);
        category.setDeleted(true);
        categoryRepository.save(category);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}
