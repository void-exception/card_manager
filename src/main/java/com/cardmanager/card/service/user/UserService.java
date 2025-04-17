package com.cardmanager.card.service.user;

import com.cardmanager.card.error.ResourceNotFoundException;
import com.cardmanager.card.model.card.Card;
import com.cardmanager.card.model.card.CardRepository;
import com.cardmanager.card.model.limit.LimitRepository;
import com.cardmanager.card.model.statement.StatementRepository;
import com.cardmanager.card.model.transaction.TransactionRepository;
import com.cardmanager.card.security.model.User;
import com.cardmanager.card.security.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private LimitRepository limitRepository;
    @Autowired
    private StatementRepository statementRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public ResponseEntity<String> deleteUser(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email", email));
        List<Card> cards = cardRepository.findAllByUser(user);
        for (Card card : cards) {
            limitRepository.deleteByCard(card);
            statementRepository.deleteByCard(card);
        }
        transactionRepository.deleteByUser(user);
        userRepository.delete(user);
        return ResponseEntity.ok("Пользователь и все его данные удалены");
    }
}
