package com.cardmanager.card.Model.Statement;

import com.cardmanager.card.Model.Card.Card;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StatementRepository extends CrudRepository<Statement, Long> {
    void deleteByCard(Card card);
    List<Statement> findByCardAndStatus(Card card, String status);
}
