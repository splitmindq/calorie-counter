package splitmindq.caloriecounter.service;

import java.util.List;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.model.User;

/**
 * Интерфейс для сервиса, управляющего пользователями.
 * Содержит методы для сохранения, поиска, обновления и удаления пользователей,
 * а также для фильтрации пользователей по полу.
 */
@Service
public interface UserService {
    List<User> findAllUsers();

    void createUser(User user);

    User findUserById(Long id);

    void updateUser(Long id, User updatedUser);

    boolean deleteUser(Long id);
}
