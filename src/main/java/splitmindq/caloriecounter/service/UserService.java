package splitmindq.caloriecounter.service;

import org.springframework.stereotype.Service;
import splitmindq.caloriecounter.model.User;

import java.util.List;

@Service
public interface UserService {

   List<User> findAllUsers();

   void saveUser(User user);

   User findUserByEmail(String email);

   User updateUser(User user);

   void deleteUser(String email);

   List<User> findUsersByGender(String gender);

}


