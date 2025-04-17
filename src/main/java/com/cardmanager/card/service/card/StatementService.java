package com.cardmanager.card.service.card;

import com.cardmanager.card.error.ResourceNotFoundException;
import com.cardmanager.card.model.card.Card;
import com.cardmanager.card.model.card.CardRepository;
import com.cardmanager.card.model.card.CardStatus;
import com.cardmanager.card.model.statement.Statement;
import com.cardmanager.card.model.statement.StatementRepository;
import com.cardmanager.card.service.user.UserContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class StatementService {
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private StatementRepository statementRepository;
    @Autowired
    private UserContextService userContextService;

    public ResponseEntity<String> requestBlocking(Long idCard) {
        Card card = cardRepository.findById(idCard).orElseThrow(() -> new ResourceNotFoundException("Карта", idCard));
        if (!card.getUser().getId().equals(userContextService.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Карта не пренадлежит пользователю");
        }
        if (card.getStatus() == CardStatus.BLOCKED){
            return ResponseEntity.badRequest().body("Карта уже заблокированна");
        }
        if (!statementRepository.findByCardAndStatus(card, "PENDING").isEmpty()){
            return ResponseEntity.badRequest().body("Заявка уже созданна");
        }

        Statement statement = new Statement(card);
        statementRepository.save(statement);
        return ResponseEntity.ok("Заявка созданна");
    }
}
