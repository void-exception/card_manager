package com.cardmanager.card.Service.Card;

import com.cardmanager.card.Error.ResourceNotFoundException;
import com.cardmanager.card.Model.Card.Card;
import com.cardmanager.card.Model.Card.CardRepository;
import com.cardmanager.card.Model.Card.CardStatus;
import com.cardmanager.card.Model.Statement.Statement;
import com.cardmanager.card.Model.Statement.StatementRepository;
import com.cardmanager.card.Service.User.UserContextService;
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
