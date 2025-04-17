package com.cardmanager.card.service.card;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardmanager.card.dto.card.CardDTO;
import com.cardmanager.card.error.ResourceNotFoundException;
import com.cardmanager.card.model.card.Card;
import com.cardmanager.card.model.card.CardRepository;
import com.cardmanager.card.model.card.CardStatus;
import com.cardmanager.card.model.statement.StatementRepository;
import com.cardmanager.card.model.transaction.TransactionRepository;
import com.cardmanager.card.security.model.User;
import com.cardmanager.card.service.user.UserContextService;

@Service
public class CardService {

    @Autowired
    private UserContextService userContextService;
    @Autowired
    private EncodeService encodeService;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private StatementRepository statementRepository;
    @Autowired
    private TransactionRepository transactionRepository;


    public ResponseEntity<?> currentCard(Long id){
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Карта", id));
        User user = userContextService.getUser();
        if (!user.getRole().equals("ADMIN") && !card.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Карта не пренадлежит пользователю");
        }
        CardDTO cardDTO = new CardDTO(card.getId(), encodeService.decryption(card.getCardNumber()),card.getUser().getName(), card.getUser().getEmail(), card.getEndDate(), card.getStatus(), card.getBalance());
        return ResponseEntity.ok(cardDTO);
    }

    public ResponseEntity<?> createdCard(String number, YearMonth end, double balance){
        try {
            String encryptNumber = encodeService.encryption(number);
            if (cardRepository.findByCardNumber(encryptNumber).isPresent()){
                return ResponseEntity.badRequest().body("Такая карта уже созданна");
            }
            CardStatus status = YearMonth.now().isBefore(end) ? CardStatus.ACTIVE : CardStatus.EXPIRED;
            Card card = new Card(encryptNumber, userContextService.getUser(), end, status, balance);
            cardRepository.save(card);
            return ResponseEntity.ok("Новая карта создана");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Неверные входные данные: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла внутренняя ошибка при создании карты" + e.getMessage());
        }
    }

    public List<CardDTO> getCards(){
        User user = userContextService.getUser();
        List<Card> cards = cardRepository.findAllByUser(user);
        return cards.stream()
                .map(card -> new CardDTO(
                        card.getId(),
                        encodeService.decryption(card.getCardNumber()),
                        card.getUser().getName(),
                        card.getUser().getEmail(),
                        card.getEndDate(),
                        card.getStatus(),
                        card.getBalance()
                )).collect(Collectors.toList());
    }

    @Transactional
    public ResponseEntity<String> deleteCard(Long id){
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Карта", id));
        statementRepository.deleteByCard(card);
        transactionRepository.deleteAllByCardInvolved(card);
        cardRepository.delete(card);
        return ResponseEntity.ok("Карта удаленнна");
    }

    public ResponseEntity<String> blockCard(Long id){
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Карта", id));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        return ResponseEntity.ok("Карта заблокированна");
    }

    public ResponseEntity<String> activeCard(Long idCard){
        Card card = cardRepository.findById(idCard).orElseThrow(() -> new ResourceNotFoundException("Карта", idCard));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        return ResponseEntity.ok("Карта активирована");
    }


}

