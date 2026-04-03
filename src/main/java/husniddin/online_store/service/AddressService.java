package husniddin.online_store.service;

import husniddin.online_store.dto.request.AddressRequest;
import husniddin.online_store.dto.response.AddressResponse;
import husniddin.online_store.entity.Address;
import husniddin.online_store.entity.User;
import husniddin.online_store.exception.ForbiddenException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.AddressMapper;
import husniddin.online_store.repository.AddressRepository;
import husniddin.online_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses() {
        User user = getCurrentUser();
        return addressRepository.findByUserId(user.getId())
                .stream().map(addressMapper::toResponse).collect(java.util.stream.Collectors.toList());
    }

    public AddressResponse createAddress(AddressRequest request) {
        User user = getCurrentUser();
        Address address = Address.builder()
                .user(user)
                .regionType(request.getRegionType())
                .cityType(request.getCityType())
                .homeNumber(request.getHomeNumber())
                .roomNumber(request.getRoomNumber())
                .build();
        return addressMapper.toResponse(addressRepository.save(address));
    }

    public AddressResponse updateAddress(Long id, AddressRequest request) {
        Address address = findById(id);
        validateOwnership(address);
        address.setRegionType(request.getRegionType());
        address.setCityType(request.getCityType());
        address.setHomeNumber(request.getHomeNumber());
        address.setRoomNumber(request.getRoomNumber());
        return addressMapper.toResponse(addressRepository.save(address));
    }

    public void deleteAddress(Long id) {
        Address address = findById(id);
        validateOwnership(address);
        address.setDeleted(true);
        addressRepository.save(address);
    }

    private void validateOwnership(Address address) {
        User user = getCurrentUser();
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }
    }

    private Address findById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
