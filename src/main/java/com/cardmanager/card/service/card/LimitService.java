package com.cardmanager.card.service.card;

import com.cardmanager.card.dto.limit.LimitResponse;
import com.cardmanager.card.error.ResourceNotFoundException;
import com.cardmanager.card.model.card.Card;
import com.cardmanager.card.model.card.CardRepository;
import com.cardmanager.card.model.card.CardStatus;
import com.cardmanager.card.model.limit.Limit;
import com.cardmanager.card.model.limit.LimitRepository;
import com.cardmanager.card.service.user.UserContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LimitService {
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UserContextService userContextService;
    @Autowired
    private LimitRepository limitRepository;

    public ResponseEntity<String> debitingFunds(Long idCard, double sum){
        Card card = cardRepository.findById(idCard).orElseThrow(() -> new ResourceNotFoundException("Карта", idCard));
        if (!card.getUser().getId().equals(userContextService.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Карта не пренадлежит пользователю");
        }
        if (card.getStatus() != CardStatus.ACTIVE){
            return ResponseEntity.badRequest().body("Карта недоступна для проведения транзакций");
        }
        if (card.getBalance()<sum){
            return ResponseEntity.badRequest().body("На карте недостаточно средств");
        }
        Limit limit = limitRepository.findByCardAndEndLimitAfter(card, LocalDateTime.now()).orElse(null);
        if (limit != null){
            if (limit.getSum()<sum){
                return ResponseEntity.badRequest().body("Сумма списания превышает лимит");
            }
            limit.setSum(limit.getSum()-sum);
            limitRepository.save(limit);
        }
        card.setBalance(card.getBalance() -  sum);
        cardRepository.save(card);
        return ResponseEntity.ok("Деньги списаны");
    }

    public ResponseEntity<String> createLimit(LimitResponse limitResponce){
        Card card = cardRepository.findById(limitResponce.cardId()).orElseThrow(() -> new ResourceNotFoundException("Карта", limitResponce.cardId()));
        if (limitRepository.findByCardAndEndLimitAfter(card, LocalDateTime.now()).isPresent()){
            return ResponseEntity.badRequest().body("Лимит на карту уже есть");
        }
        Limit limit = new Limit(card, limitResponce.sum(), limitResponce.endLimit());
        limitRepository.save(limit);
        return ResponseEntity.ok("Лимит добавлен созданна");
    }
}
