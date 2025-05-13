package splitmindq.caloriecounter.service;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.cache.DailyIntakeCache;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.UserService;

@Service
@AllArgsConstructor
@Primary
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DailyIntakeRepository dailyIntakeRepository;
    private final DailyIntakeCache dailyIntakeCache;

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
    @Transactional
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    dailyIntakeRepository.deleteAllDailyIntakeFoodByUserId(user.getId());

                    dailyIntakeRepository.deleteAllByUserId(user.getId());

                    dailyIntakeCache.evictAllUserData(user.getEmail());

                    userRepository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public void updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            dailyIntakeCache.evictAllUserData(existingUser.getEmail());
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
