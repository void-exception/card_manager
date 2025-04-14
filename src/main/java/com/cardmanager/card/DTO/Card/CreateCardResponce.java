package com.cardmanager.card.DTO.Card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.YearMonth;

@Schema(description = "Сущность создания карты")
public record CreateCardResponce(
        @Schema(description = "Номер карты", example = "4400223355887733")
        @NotBlank
        @Length(min=16)
        String number,

        @Schema(description = "Дата окончания работы карты", example = "2027-04")
        @NotNull
        YearMonth end,

        @Schema(description = "Баланс на карте")
        @NotNull
        double balance
) {
}
