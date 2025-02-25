package splitmindq.caloriecounter.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.dao.InMemoryUserDAO;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.UserService;

import java.util.List;

@Service
@AllArgsConstructor
public class InMemoryUsersServiceImpl implements UserService {

    private final InMemoryUserDAO repository;

    @Override
    public List<User> findAllUsers() {
        return repository.findAllUsers();
    }

    @Override
    public void saveUser(User user) {
        repository.saveUser(user);
    }

    @Override
    public User findUserByEmail(String email) {
        return repository.findUserByEmail(email);
    }

    @Override
    public User updateUser(User user) {
        return repository.updateUser(user);
    }

    @Override
    public void deleteUser(String email) {
        repository.deleteUser(email);
    }

    @Override
    public List<User> findUsersByGender(String gender) {
        return repository.findUsersByGender(gender);
    }
}
