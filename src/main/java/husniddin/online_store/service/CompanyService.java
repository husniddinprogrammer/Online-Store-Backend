package husniddin.online_store.service;

import husniddin.online_store.dto.request.CompanyRequest;
import husniddin.online_store.dto.response.CompanyResponse;
import husniddin.online_store.entity.Company;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.CompanyMapper;
import husniddin.online_store.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAll(Pageable pageable) {
        return companyRepository.findAll(pageable).map(companyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getById(Long id) {
        return companyMapper.toResponse(findById(id));
    }

    public CompanyResponse create(CompanyRequest request, MultipartFile image) {
        if (companyRepository.existsByName(request.getName())) {
            throw new BadRequestException("Company already exists: " + request.getName());
        }
        String imageLink = hasFile(image) ? fileStorageService.store(image, "companies") : null;
        Company company = Company.builder()
                .name(request.getName())
                .imageLink(imageLink)
                .build();
        return companyMapper.toResponse(companyRepository.save(company));
    }

    public CompanyResponse update(Long id, CompanyRequest request, MultipartFile image) {
        Company company = findById(id);
        company.setName(request.getName());
        if (hasFile(image)) {
            company.setImageLink(fileStorageService.store(image, "companies"));
        }
        return companyMapper.toResponse(companyRepository.save(company));
    }

    public void delete(Long id) {
        Company company = findById(id);
        company.setDeleted(true);
        companyRepository.save(company);
    }

    private Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}
