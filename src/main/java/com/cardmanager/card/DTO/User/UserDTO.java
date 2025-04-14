package com.cardmanager.card.DTO.User;

import com.cardmanager.card.Model.Card.Card;
import com.cardmanager.card.Security.Model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о пользователи")
public record UserDTO(
        @Schema(description = "Id пользователя")
        Long id,

        @Schema(description = "Имя пользователя")
        String name,

        @Schema(description = "Email пользователя")
        String email,

        @Schema(description = "Роль пользователя")
        String role
) {
    public UserDTO(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
