package husniddin.online_store.dto.response;

import husniddin.online_store.enums.Role;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthdayAt;
    private String phoneNumber;
    private BigDecimal balance;
    private boolean blocked;
    private Role role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private boolean emailVerified;
}
