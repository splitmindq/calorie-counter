package splitmindq.caloriecounter.service;

import jakarta.transaction.Transactional;
import java.util.List;

import lombok.AllArgsConstructor;
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
    void createUser(User user);

    /**
    * Находит пользователя по его адресу электронной почты.
    *
    * @param id электронная почта пользователя.
    * @return пользователь с указанным адресом электронной почты.
    */
    User findUserById(Long id);

    /**
    * Обновляет информацию о пользователе.
    *
    * @param  updatedUser,id пользователя с обновленными данными.
    */
    void updateUser(Long id, User updatedUser);

    /**
    * Удаляет пользователя по адресу электронной почты.
    *
    * @param id электронная почта пользователя, которого необходимо удалить.
    */
    void deleteUser(Long id);

    //    /**
    //    * Находит пользователей по полу.
    //    *
    //    * @param gender пол пользователей (может быть null, если не фильтровать по полу).
    //    * @return список пользователей, соответствующих указанному полу.
    //    */
    //    List<User> findUsersByGender(String gender);
    //
    //    User findUserByEmail(String email);
    //
    //    void deleteUserByEmail(String email);
}
