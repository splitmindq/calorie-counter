package splitmindq.caloriecounter.service.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.UserService;

@Service
@AllArgsConstructor
@Primary
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findUserById(id);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> findUsersByGender(String gender) {
        return userRepository.findUserByGender(gender);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public void deleteUserByEmail(String email) {
        userRepository.deleteUserByEmail(email);
    }
}