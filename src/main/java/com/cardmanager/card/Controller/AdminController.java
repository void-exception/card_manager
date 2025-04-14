package com.cardmanager.card.Controller;

import com.cardmanager.card.DTO.Limit.LimitResponse;
import com.cardmanager.card.DTO.Statement.StatementDTO;
import com.cardmanager.card.DTO.Statement.StatementResponse;
import com.cardmanager.card.DTO.User.UserDTO;
import com.cardmanager.card.Error.ResourceNotFoundException;
import com.cardmanager.card.Model.Card.Card;
import com.cardmanager.card.Model.Card.CardRepository;
import com.cardmanager.card.Model.Card.CardStatus;
import com.cardmanager.card.Model.Statement.Statement;
import com.cardmanager.card.Model.Statement.StatementRepository;
import com.cardmanager.card.Security.Model.User;
import com.cardmanager.card.Security.Model.UserRepository;
import com.cardmanager.card.Service.Card.CardService;
import com.cardmanager.card.Service.Card.LimitService;
import com.cardmanager.card.Service.User.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.StreamSupport;

@Tag(name = "Админ", description = "Работа с функционалом домтупным только админу")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private StatementRepository statementRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardService cardService;
    @Autowired
    private LimitService limitService;
    @Autowired
    private UserService userService;

    @Operation(summary = "Все карты", description = "Получения всех карт")
    @GetMapping("/cards")
    public ResponseEntity<Page<Card>> getCards(@RequestParam(defaultValue = "0") @Parameter(description = "Номер страницы") int page,
                                               @RequestParam(defaultValue = "10") @Parameter(description = "Количество элементов на странице") int size,
                                               @RequestParam(required = false) @Parameter(description = "Статус карты") CardStatus status){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Card> cardPage;
        if (status==null){
            cardPage = cardRepository.findAll(pageable);
        }
        else {
            cardPage = cardRepository.findAllByStatus(status, pageable);
        }
        return ResponseEntity.ok(cardPage);
    }

    @Operation(summary = "Все заявки", description = "Получения всех заявок")
    @GetMapping("/statement")
    public ResponseEntity<?> allStatement() {
        Iterable<Statement> statements = statementRepository.findAll();
        List<StatementDTO> statementDTOs = StreamSupport.stream(statements.spliterator(), false)
                .map(StatementDTO::new)
                .toList();
        return ResponseEntity.ok(statementDTOs);
    }

    @Operation(summary = "Все пользователи", description = "Получения всех пользователей")
    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(defaultValue = "0") @Parameter(description = "Номер страницы") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Количество элементов на странице") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserDTO> userDTOPage = userPage.map(UserDTO::new);
        return ResponseEntity.ok(userDTOPage);
    }

    @Operation(summary = "Конкретный пользователь", description = "Получения данных о конкретном пользователе")
    @GetMapping("/user/details")
    public ResponseEntity<?> currentUser(@RequestParam("email") @Parameter(description = "Email пользователя", required = true) String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Пользователь", email));
        return ResponseEntity.ok(new UserDTO(user));
    }

    @Operation(summary = "Решение по заявке", description = "Решение по заявке о блокировке карты")
    @PostMapping("/statement")
    public ResponseEntity<String> decisionOnStatement(@Valid @RequestBody StatementResponse statementResponse){
        Statement statement = statementRepository.findById(statementResponse.idStatement()).orElseThrow(() -> new ResourceNotFoundException("Заявка", statementResponse.idStatement()));
        if (statementResponse.newStatus().equals("rejection")) {
            statement.setStatus("REJECTION");
        } else if (statementResponse.newStatus().equals("approved")) {
                statement.setStatus("APPROVED");
                Card card = statement.getCard();
                card.setStatus(CardStatus.BLOCKED);
                cardRepository.save(card);
        }
        statementRepository.save(statement);
        return ResponseEntity.ok("Статус присвоен");
    }

    @Operation(summary = "Заблокировать карту", description = "Заблокировать конкретную карту")
    @PostMapping("/card/blocking")
    public ResponseEntity<String> blockCard(@RequestParam @Parameter(description = "Id карты", required = true) Long id){
        return cardService.blockCard(id);
    }

    @Operation(summary = "Активировать карту", description = "Активировать конкретную карту")
    @PostMapping("/card/activation")
    public ResponseEntity<String> activeCard(@RequestParam @Parameter(description = "Id карты", required = true) Long id){
        return cardService.activeCard(id);
    }

    @Operation(summary = "Добавить лимит для карты", description = "Добавить лимит для конкретной карты")
    @PostMapping("/limit")
    public ResponseEntity<String> createLimit(@Valid @RequestBody LimitResponse limitResponse){
        return limitService.createLimit(limitResponse);
    }

    @Operation(summary = "Удалить пользователя", description = "Удалить конкретного пользователя")
    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam @Parameter(description = "Email пользователя", required = true) String email){
        return userService.deleteUser(email);
    }

    @Operation(summary = "Удалить карту", description = "Удалить конкретную карту")
    @DeleteMapping("/card/delete")
    public ResponseEntity<String> deleteCard(@RequestParam @Parameter(description = "Id карты", required = true) Long id){
        return cardService.deleteCard(id);
    }
}
