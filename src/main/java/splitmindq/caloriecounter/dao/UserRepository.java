package splitmindq.caloriecounter.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import splitmindq.caloriecounter.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}