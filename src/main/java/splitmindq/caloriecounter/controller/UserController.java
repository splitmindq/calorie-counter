package splitmindq.caloriecounter.controller;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.UserService;

/**
 * Контроллер для работы с пользователями.
 * Предоставляет API для создания, обновления, удаления и поиска пользователей.
 */
@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Получение списка всех пользователей.
     *
     * @return список всех пользователей
     */
    @GetMapping
    public List<User> findAllUsers() {
        return userService.findAllUsers();
    }

    /**
     * Сохранение нового пользователя.
     *
     * @param user объект пользователя, который нужно сохранить
     * @return сообщение о результате сохранения
     */
    @PostMapping("save_user")
    public ResponseEntity<String> saveUser(@RequestBody User user) {
        try {
            userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Поиск пользователя по его id.
     *
     * @param id пользователя, которого нужно найти
     * @return найденный пользователь
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        User user = userService.findUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("update_user/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {
        try {
            userService.updateUser(id, updatedUser);
            return ResponseEntity.status(HttpStatus.OK).body("User updated successfully.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Удаление пользователя по id.
     *
     * @param id id пользователя, которого нужно удалить
     */
    @DeleteMapping("delete_user/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}

//    @GetMapping("/filter")
//    public ResponseEntity<List<User>> findUsersByGender(@RequestParam(required = false) String gender) {
//        List<User> users = userService.findUsersByGender(gender);
//        if (users.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//        return ResponseEntity.ok(users);
//    }
//
//    @DeleteMapping("delete_user_by_email")
//    public void deleteUserByEmail(@RequestParam String email) {
//        userService.deleteUserByEmail(email);
//    }
// }


