package com.cardmanager.card.Model.Transaction;

import com.cardmanager.card.Model.Card.Card;
import com.cardmanager.card.Security.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.fromCard = :card OR t.toCard = :card")
    Page<Transaction> findAllByCardInvolved(@Param("card") Card card, Pageable pageable);
    void deleteByUser(User user);
    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.fromCard = :card OR t.toCard = :card")
    void deleteAllByCardInvolved(@Param("card") Card card);

}
