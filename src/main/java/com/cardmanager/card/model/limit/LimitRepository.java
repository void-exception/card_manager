package com.cardmanager.card.model.limit;

import com.cardmanager.card.model.card.Card;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDateTime;
import java.util.Optional;

public interface LimitRepository extends JpaRepository<Limit, Long> {
    Optional<Limit> findByCardAndEndLimitAfter(Card card, LocalDateTime now);
    void deleteByCard(Card card);
}
