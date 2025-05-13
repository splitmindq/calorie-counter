package splitmindq.caloriecounter.service.impl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import splitmindq.caloriecounter.cache.DailyIntakeCache;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.UserServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DailyIntakeRepository dailyIntakeRepository;

    @Mock
    private DailyIntakeCache dailyIntakeCache;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user1 = new User();
        User user2 = new User();
        List<User> expectedUsers = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.findAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
        verify(userRepository).findAll();
    }

    @Test
    void createUser_ShouldSaveUser() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("test@example.com");

        // Act
        userService.createUser(newUser);

        // Assert
        verify(userRepository).save(newUser);
    }

    @Test
    void findUserById_ShouldReturnUser_WhenExists() {
        // Arrange
        User expectedUser = new User();
        expectedUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = userService.findUserById(1L);

        // Assert
        assertEquals(expectedUser, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void findUserById_ShouldReturnNull_WhenNotExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        User result = userService.findUserById(1L);

        // Assert
        assertNull(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void deleteUser_ShouldReturnTrueAndDelete_WhenUserExists() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        boolean result = userService.deleteUser(1L);

        // Assert
        assertTrue(result);
        verify(dailyIntakeRepository).deleteAllDailyIntakeFoodByUserId(1L);
        verify(dailyIntakeRepository).deleteAllByUserId(1L);
        verify(dailyIntakeCache).evictAllUserData("user@example.com");
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldReturnFalse_WhenUserNotExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.deleteUser(1L);

        // Assert
        assertFalse(result);
        verify(dailyIntakeRepository, never()).deleteAllDailyIntakeFoodByUserId(anyLong());
        verify(dailyIntakeRepository, never()).deleteAllByUserId(anyLong());
        verify(dailyIntakeCache, never()).evictAllUserData(anyString());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenValidData() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");
        updatedUser.setFirstName("New");
        updatedUser.setLastName("Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // Act
        userService.updateUser(1L, updatedUser);

        // Assert
        assertEquals("new@example.com", existingUser.getEmail());
        assertEquals("New", existingUser.getFirstName());
        assertEquals("Name", existingUser.getLastName());
        verify(dailyIntakeCache).evictAllUserData("old@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_ShouldThrow_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(1L, new User());
        });
    }

    @Test
    void updateUser_ShouldThrow_WhenEmailAlreadyInUse() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.updateUser(1L, updatedUser);
        });
    }

    @Test
    void updateUser_ShouldNotEvictCache_WhenEmailNotChanged() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("same@example.com");

        User updatedUser = new User();
        updatedUser.setEmail("same@example.com");
        updatedUser.setFirstName("NewName");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // Act
        userService.updateUser(1L, updatedUser);

        // Assert
        verify(dailyIntakeCache, never()).evictAllUserData(anyString());
        assertEquals("NewName", existingUser.getFirstName());
        verify(userRepository).save(existingUser);
    }
}