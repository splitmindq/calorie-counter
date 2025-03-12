package splitmindq.caloriecounter.service.impl;

import jakarta.transaction.Transactional;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.UserService;

@Service
@AllArgsConstructor
@Primary
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void createUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new DataIntegrityViolationException("Email is already in use.");
            }
        }
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setAge(updatedUser.getAge());
        existingUser.setGender(updatedUser.getGender());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setWeight(updatedUser.getWeight());
        existingUser.setHeight(updatedUser.getHeight());

        userRepository.save(existingUser);
    }
}
