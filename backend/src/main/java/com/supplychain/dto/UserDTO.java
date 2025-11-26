package com.supplychain.dto;

import com.supplychain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private User.UserRole role;
    private User.UserStatus status;
    private LocalDateTime createdAt;
}
