package splitmindq.caloriecounter.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
    public String saveUser(@RequestBody User user) {
    userService.saveUser(user);
    return "Saved";
    }

  /**
     * Поиск пользователя по его email.
     *
     * @param email email пользователя, которого нужно найти
     * @return найденный пользователь
     */
  @GetMapping("/{email}")
    public User findByEmail(@PathVariable String email) {
    return userService.findUserByEmail(email);
    }

  /**
     * Обновление данных пользователя.
     *
     * @param user объект пользователя с обновленными данными
     * @return обновленный пользователь
     */
  @PutMapping("update_user")
    public User updateUser(@RequestBody User user) {
    return userService.updateUser(user);
    }

  /**
     * Удаление пользователя по email.
     *
     * @param email email пользователя, которого нужно удалить
     */
  @DeleteMapping("delete_user/{email}")
    public void deleteUser(@PathVariable String email) {
    userService.deleteUser(email);
    }

  /**
     * Поиск пользователей по полу.
     *
     * @param gender пол, по которому осуществляется фильтрация
     * @return список пользователей, соответствующих фильтру
     */
  @GetMapping("/filter")
    public List<User> findUsersByGender(@RequestParam(required = false) String gender) {
    return userService.findUsersByGender(gender);
    }
}
