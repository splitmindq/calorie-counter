package splitmindq.caloriecounter.dao;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import splitmindq.caloriecounter.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}