package splitmindq.caloriecounter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.UserService;

@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех зарегистрированных пользователей")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение списка пользователей",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "204", description = "Список пользователей пуст")
    })
    public ResponseEntity<List<User>> findAllUsers() {
        List<User> users = userService.findAllUsers();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("save_user")
    @Operation(summary = "Создать нового пользователя",
            description = "Регистрирует нового пользователя в системе")
    public ResponseEntity<?> saveUser(
            @Parameter(description = "Данные пользователя", required = true,
                    content = @Content(schema = @Schema(implementation = User.class)))
            @Valid @RequestBody User user) {
        log.debug("Создание пользователя: {}", user.getEmail());

        try {
            userService.createUser(user);
            log.info("Пользователь создан успешно. ID: {}", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body("Пользователь успешно создан.");
        } catch (DataIntegrityViolationException e) {
            log.warn("Попытка создания пользователя с существующим email: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email уже используется.");
        } catch (Exception e) {
            log.warn("Произошла ошибка при создании пользователя с email: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Найти пользователя по ID",
            description = "Возвращает пользователя по указанному идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<User> findById(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {
        User user = userService.findUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("update_user/{id}")
    @Operation(summary = "Обновить пользователя",
            description = "Обновляет данные пользователя по указанному ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Email уже используется"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера при обновлении")
    })
    public ResponseEntity<String> updateUser(
            @Parameter(description = "ID пользователя для обновления", required = true)
            @PathVariable Long id,
            @Parameter(description = "Обновленные данные пользователя", required = true,
                    content = @Content(schema = @Schema(implementation = User.class)))
            @Valid @RequestBody User updatedUser) {
        try {
            userService.updateUser(id, updatedUser);
            log.info("Пользователь обновлен успешно. ID: {}", updatedUser.getId());
            return ResponseEntity.status(HttpStatus.OK).body("Пользователь успешно обновлен.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.info("Email пользователя уже занят. ID: {}", updatedUser.getId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email уже используется.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка: " + e.getMessage());
        }
    }

    @DeleteMapping("delete_user/{id}")
    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя по указанному ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера при удалении")
    })
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "ID пользователя для удаления", required = true)
            @PathVariable Long id) {
        boolean isDeleted = userService.deleteUser(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}