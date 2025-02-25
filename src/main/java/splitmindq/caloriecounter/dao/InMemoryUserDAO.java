package splitmindq.caloriecounter.dao;

import org.springframework.stereotype.Repository;
import splitmindq.caloriecounter.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class InMemoryUserDAO {

    private final List<User> USERS = new ArrayList<User>();

    public List<User> findAllUsers() {
        return USERS;
    }

    public void saveUser(User user) {
        USERS.add(user);
    }


    public User findUserByEmail(String email) {
        return USERS.stream()
                .filter(element -> element.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    public User updateUser(User user) {
        var userIndex = IntStream.range(0, USERS.size())
                .filter(i -> USERS.get(i).getEmail().equals(user.getEmail()))
                .findFirst()
                .orElse(-1);

        if (userIndex != -1) {
            USERS.set(userIndex, user);
            return user;
        }
        return null;
    }


    public void deleteUser(String email) {
        var user = findUserByEmail(email);
        if (user != null) {
            USERS.remove(user);
        }
    }

    public List<User> findUsersByGender(String gender) {
        return USERS.stream()
                .filter(element -> element.getGender().equals(gender))
                .collect(Collectors.toList());

    }
}


