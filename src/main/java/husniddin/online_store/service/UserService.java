package husniddin.online_store.service;

import husniddin.online_store.dto.request.BalanceTopUpRequest;
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

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns users visible to the caller based on their role:
     *   SUPER_ADMIN → all except other SUPER_ADMINs
     *   ADMIN       → only CUSTOMER accounts
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        List<Role> excluded = switch (getCurrentRole()) {
            case SUPER_ADMIN -> List.of(Role.SUPER_ADMIN);
            default          -> List.of(Role.SUPER_ADMIN, Role.ADMIN, Role.DELIVERY);
        };
        return userRepository.searchUsersExcluding(search, excluded, pageable)
                .map(userMapper::toResponse);
    }

    /**
     * SUPER_ADMIN can view any non-SUPER_ADMIN account.
     * ADMIN can only view CUSTOMER accounts.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User target = findUserById(id);
        validateVisibility(target);
        return userMapper.toResponse(target);
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

        if (request.getName() != null)        user.setName(request.getName());
        if (request.getSurname() != null)     user.setSurname(request.getSurname());
        if (request.getBirthdayAt() != null)  user.setBirthdayAt(request.getBirthdayAt());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse topUpBalance(BalanceTopUpRequest request) {
        String email = getCurrentEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setBalance(user.getBalance().add(request.getAmount()));
        log.info("Balance topped up by {} for user {}", request.getAmount(), email);
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

    /** ADMIN can only block CUSTOMER accounts. SUPER_ADMIN cannot be blocked. */
    public UserResponse blockUser(Long id) {
        User target = findUserById(id);
        guardModification(target);
        target.setBlocked(true);
        log.info("User {} blocked by {}", target.getEmail(), getCurrentEmail());
        return userMapper.toResponse(userRepository.save(target));
    }

    /** ADMIN can only unblock CUSTOMER accounts. SUPER_ADMIN cannot be unblocked via API. */
    public UserResponse unblockUser(Long id) {
        User target = findUserById(id);
        guardModification(target);
        target.setBlocked(false);
        return userMapper.toResponse(userRepository.save(target));
    }

    /**
     * Only SUPER_ADMIN can call this (enforced by controller @PreAuthorize).
     * Service layer also prevents assigning SUPER_ADMIN role or targeting a SUPER_ADMIN.
     */
    public UserResponse changeUserRole(Long id, Role newRole) {
        User target = findUserById(id);
        guardSuperAdminTarget(target);
        if (newRole == Role.SUPER_ADMIN) {
            throw new ForbiddenException("Cannot assign SUPER_ADMIN role via API");
        }
        target.setRole(newRole);
        log.info("Role of user {} changed to {} by {}", target.getEmail(), newRole, getCurrentEmail());
        return userMapper.toResponse(userRepository.save(target));
    }

    /**
     * Only SUPER_ADMIN can call this (enforced by controller @PreAuthorize).
     * Service layer also prevents deleting a SUPER_ADMIN.
     */
    public void deleteUser(Long id) {
        User target = findUserById(id);
        guardSuperAdminTarget(target);
        target.setDeleted(true);
        log.info("User {} soft-deleted by {}", target.getEmail(), getCurrentEmail());
        userRepository.save(target);
    }

    // ── Package-internal helpers ──────────────────────────────────────────────

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // ── Private guards ────────────────────────────────────────────────────────

    /**
     * Ensures the caller can see this user account.
     * ADMIN cannot view ADMIN, DELIVERY, or SUPER_ADMIN accounts.
     */
    private void validateVisibility(User target) {
        Role callerRole = getCurrentRole();
        if (callerRole == Role.ADMIN && target.getRole() != Role.CUSTOMER) {
            throw new ForbiddenException("ADMIN can only view CUSTOMER accounts");
        }
        if (target.getRole() == Role.SUPER_ADMIN && callerRole != Role.SUPER_ADMIN) {
            throw new ForbiddenException("Access denied");
        }
    }

    /**
     * Full modification guard — prevents targeting SUPER_ADMIN and enforces
     * ADMIN's scope to CUSTOMER-only accounts.
     */
    private void guardModification(User target) {
        guardSuperAdminTarget(target);
        Role callerRole = getCurrentRole();
        if (callerRole == Role.ADMIN && target.getRole() != Role.CUSTOMER) {
            throw new ForbiddenException("ADMIN can only manage CUSTOMER accounts");
        }
    }

    /** Rejects any operation that targets a SUPER_ADMIN. */
    private void guardSuperAdminTarget(User target) {
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new ForbiddenException("SUPER_ADMIN accounts cannot be modified");
        }
    }

    private Role getCurrentRole() {
        String email = getCurrentEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getRole();
    }

    private String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
