package com.cardmanager.card.Service.Card;

import com.cardmanager.card.DTO.Transaction.TransactionDTO;
import com.cardmanager.card.DTO.Transaction.TransactionResponse;
import com.cardmanager.card.Error.ResourceNotFoundException;
import com.cardmanager.card.Model.Card.Card;
import com.cardmanager.card.Model.Card.CardRepository;
import com.cardmanager.card.Model.Card.CardStatus;
import com.cardmanager.card.Model.Transaction.Transaction;
import com.cardmanager.card.Model.Transaction.TransactionRepository;
import com.cardmanager.card.Security.Model.User;
import com.cardmanager.card.Service.User.UserContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UserContextService userContextService;
    @Autowired
    private TransactionRepository transactionRepository;


    public ResponseEntity<?> getTransaction(Long idCard, int page, int size){
        Card card = cardRepository.findById(idCard).orElseThrow(() -> new ResourceNotFoundException("Карта", idCard));
        User user = userContextService.getUser();
        if (!user.getRole().equals("ADMIN") && !card.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Карта не пренадлежит пользователю");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Transaction> transactionsPage = transactionRepository.findAllByCardInvolved(card, pageable);
        Page<TransactionDTO> transactionDTOPage = transactionsPage.map(transaction ->
                new TransactionDTO(
                        transaction.getId(),
                        transaction.getUser().getEmail(),
                        transaction.getFromCard().getId(),
                        transaction.getToCard().getId(),
                        transaction.getLocalDateTime(),
                        transaction.getSumTransaction()
                ));
        return ResponseEntity.ok(transactionDTOPage);
    }

    public ResponseEntity<String> createTransaction(TransactionResponse transactionRecord){
        if (transactionRecord.toCardId().equals(transactionRecord.fromCardId())){
            return ResponseEntity.badRequest().body("Карты одинаковые");
        }
        Card fromCard = cardRepository.findById(transactionRecord.fromCardId()).orElseThrow(() -> new ResourceNotFoundException("Карта", transactionRecord.fromCardId()));
        Card toCard = cardRepository.findById(transactionRecord.toCardId()).orElseThrow(() -> new ResourceNotFoundException("Карта", transactionRecord.toCardId()));
        List<Card> cards = cardRepository.findAllByUser(userContextService.getUser());
        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE){
            return ResponseEntity.badRequest().body("Карта недоступна для проведения транзакций");
        }
        if(!cards.contains(fromCard) || !cards.contains(toCard)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Карта не пренадлежит пользователю");
        }
        if(transactionRecord.sumTransaction() > fromCard.getBalance()){
            return ResponseEntity.badRequest().body("Сумма перевода превышает доступный баланс карты");
        }
        Transaction transaction = new Transaction(userContextService.getUser(), fromCard, toCard, transactionRecord.sumTransaction());
        transactionRepository.save(transaction);
        fromCard.setBalance(fromCard.getBalance() - transactionRecord.sumTransaction());
        toCard.setBalance(toCard.getBalance() + transactionRecord.sumTransaction());
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        return ResponseEntity.ok("Баланс обновлен");
    }
}
