package husniddin.online_store.service;

import husniddin.online_store.dto.request.ChangePasswordRequest;
import husniddin.online_store.dto.request.UpdateUserRequest;
import husniddin.online_store.dto.response.UserResponse;
import husniddin.online_store.entity.User;
import husniddin.online_store.enums.Role;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.exception.ForbiddenException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.UserMapper;
import husniddin.online_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        return userRepository.searchUsers(search, pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findUserById(id));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getSurname() != null) user.setSurname(request.getSurname());
        if (request.getBirthdayAt() != null) user.setBirthdayAt(request.getBirthdayAt());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        return userMapper.toResponse(userRepository.save(user));
    }

    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponse blockUser(Long id) {
        User user = findUserById(id);
        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenException("Cannot block SUPER_ADMIN");
        }
        user.setBlocked(true);
        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse unblockUser(Long id) {
        User user = findUserById(id);
        user.setBlocked(false);
        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse changeUserRole(Long id, Role role) {
        User user = findUserById(id);
        user.setRole(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        user.setDeleted(true);
        userRepository.save(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
