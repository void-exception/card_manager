package com.cardmanager.card.model.statement;

import com.cardmanager.card.model.card.Card;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StatementRepository extends CrudRepository<Statement, Long> {
    void deleteByCard(Card card);
    List<Statement> findByCardAndStatus(Card card, String status);
}
