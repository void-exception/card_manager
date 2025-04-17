package com.cardmanager.card.controller;

import com.cardmanager.card.dto.card.CreateCardResponce;
import com.cardmanager.card.dto.transaction.TransactionResponse;
import com.cardmanager.card.service.card.CardService;
import com.cardmanager.card.service.card.LimitService;
import com.cardmanager.card.service.card.StatementService;
import com.cardmanager.card.service.card.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Карты", description = "Работа с банковскими картами пользователя")
@RestController
@RequestMapping("/api/cards")
public class CardsController {
    @Autowired
    private CardService cardService;
    @Autowired
    private LimitService limitService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private StatementService statementService;

    @Operation(summary = "Все карты пользователя", description = "Получения всех карт пользователя")
    @GetMapping
    public ResponseEntity<?> allCard() {
        return ResponseEntity.ok(cardService.getCards());
    }

    @Operation(summary = "Получить карту", description = "Получить данные карты по её Id")
    @GetMapping("/details")
    public ResponseEntity<?> currentCard(@RequestParam("card_id") @Parameter(description = "Id карты", required = true) Long id){
        return cardService.currentCard(id);
    }

    @Operation(summary = "Транзакции по карте", description = "Получить все транзакции по конкретной карте")
    @GetMapping("/transaction")
    public ResponseEntity<?> transaction(@RequestParam("card_id") @Parameter(description = "Id карты", required = true) Long id,
                                         @RequestParam(name = "page", defaultValue = "0") @Parameter(description = "Номер страницы") int page,
                                         @RequestParam(name = "size", defaultValue = "10") @Parameter(description = "Количество транзакций на одной страние") int size){
        return transactionService.getTransaction(id, page, size);
    }

    @Operation(summary = "Создать карту", description = "Создание новой карты пользователем")
    @PostMapping
    public ResponseEntity<?> createCards(@Valid  @RequestBody CreateCardResponce cardResponce) {
        return cardService.createdCard(cardResponce.number(), cardResponce.end(), cardResponce.balance());
    }

    @Operation(summary = "Заявка на блокировку карты", description = "Создание заявки на блокировку карты")
    @PostMapping("/block")
    public ResponseEntity<String> requestBlock(@RequestParam("card_id") @Parameter(description = "Id карты", required = true) Long cardID){
        return statementService.requestBlocking(cardID);
    }

    @Operation(summary = "Создание транзакции", description = "Создание новой транзакции между картами")
    @PostMapping("/transaction")
    public ResponseEntity<String> createTransaction(@Valid @RequestBody TransactionResponse transactionResponse){
        return transactionService.createTransaction(transactionResponse);
    }

    @Operation(summary = "Снять средства", description = "Снятие средств с карты")
    @PostMapping("/debiting")
    public ResponseEntity<String> debitingFunds(@RequestParam("card_id") @Parameter(description = "Id карты", required = true) Long id , @RequestParam("sum") @Parameter(description = "Сумма снятия средств", required = true) double sum){
        return limitService.debitingFunds(id, sum);
    }
}