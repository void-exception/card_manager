package com.cardmanager.card.Model.Card;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус карты")
public enum CardStatus {
    ACTIVE, BLOCKED, EXPIRED;
}
