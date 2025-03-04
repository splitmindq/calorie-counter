package splitmindq.caloriecounter.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
        if (userService.findUserByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Поиск пользователя по его email.
     *
     * @param id email пользователя, которого нужно найти
     * @return найденный пользователь
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Integer id) {
        User user = userService.findUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * Обновление данных пользователя.
     *
     * @param user объект пользователя с обновленными данными
     * @return обновленный пользователь
     */
    @PutMapping("update_user")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        User userToUpdate = userService.updateUser(user);
        if (userToUpdate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(userToUpdate);
    }

    /**
     * Удаление пользователя по email.
     *
     * @param id email пользователя, которого нужно удалить
     */
    @DeleteMapping("delete_user/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }

    /**
     * Поиск пользователей по полу.
     *
     * @param gender пол, по которому осуществляется фильтрация
     * @return список пользователей, соответствующих фильтру
     */
    @GetMapping("/filter")
    public ResponseEntity<List<User>> findUsersByGender(@RequestParam(required = false) String gender) {
        List<User> users = userService.findUsersByGender(gender);
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(users);
    }
}


