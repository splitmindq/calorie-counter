package splitmindq.caloriecounter.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.UserService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> findAllUsers() {

        return userService.findAllUsers();

    }

    @PostMapping("save_user")
    public String saveUser(@RequestBody User user) {
        userService.saveUser(user);
        return "Saved";
    }

    @GetMapping("/{email}")
    public User findByEmail(@PathVariable String email) {
        return userService.findUserByEmail(email);
    }

    @PutMapping("update_user")
    public User updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @DeleteMapping("delete_user/{email}")
    public void deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
    }

    @GetMapping("/filter")
    public List<User> findUsersByGender(@RequestParam(required = false) String gender) {

        return userService.findUsersByGender(gender);

    }


}