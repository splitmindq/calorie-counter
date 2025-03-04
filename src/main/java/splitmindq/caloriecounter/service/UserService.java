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
    /**
    * Находит всех пользователей.
    *
    * @return список всех пользователей.
    */
    List<User> findAllUsers();

    /**
    * Сохраняет пользователя.
    *
    * @param user объект пользователя, который необходимо сохранить.
    */
    void saveUser(User user);

    /**
    * Находит пользователя по его адресу электронной почты.
    *
    * @param id электронная почта пользователя.
    * @return пользователь с указанным адресом электронной почты.
    */
    User findUserById(Integer id);

    /**
    * Обновляет информацию о пользователе.
    *
    * @param user объект пользователя с обновленными данными.
    * @return обновленный объект пользователя.
    */
    User updateUser(User user);

    /**
    * Удаляет пользователя по адресу электронной почты.
    *
    * @param id электронная почта пользователя, которого необходимо удалить.
    */
    void deleteUser(Integer id);

    /**
    * Находит пользователей по полу.
    *
    * @param gender пол пользователей (может быть null, если не фильтровать по полу).
    * @return список пользователей, соответствующих указанному полу.
    */
    List<User> findUsersByGender(String gender);

    User findUserByEmail(String email);
}
