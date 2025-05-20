package splitmindq.caloriecounter.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import splitmindq.caloriecounter.cache.DailyIntakeCache;
import splitmindq.caloriecounter.dao.DailyIntakeFoodRepository;
import splitmindq.caloriecounter.dao.FoodRepository;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.model.DailyIntakeFood;
import splitmindq.caloriecounter.model.Food;
import splitmindq.caloriecounter.model.User;
import splitmindq.caloriecounter.service.FoodServiceImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceImplTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private DailyIntakeFoodRepository dailyIntakeFoodRepository;

    @Mock
    private DailyIntakeCache dailyIntakeCache;

    @InjectMocks
    private FoodServiceImpl foodService;

    @Test
    void createFood_ShouldSaveFood() {
        Food food = new Food();
        food.setName("Test Food");

        foodService.createFood(food);

        verify(foodRepository).save(food);
    }

    @Test
    void getFoodById_ShouldReturnFood_WhenExists() {
        Food expectedFood = new Food();
        expectedFood.setId(1L);
        when(foodRepository.findById(1L)).thenReturn(Optional.of(expectedFood));

        Food result = foodService.getFoodById(1L);

        assertEquals(expectedFood, result);
    }

    @Test
    void getFoodById_ShouldReturnNull_WhenNotExists() {
        when(foodRepository.findById(1L)).thenReturn(Optional.empty());

        Food result = foodService.getFoodById(1L);

        assertNull(result);
    }

    @Test
    void getAllFood_ShouldReturnAllFoods() {
        List<Food> expectedFoods = Arrays.asList(new Food(), new Food());
        when(foodRepository.findAll()).thenReturn(expectedFoods);

        List<Food> result = foodService.getAllFood();

        assertEquals(2, result.size());
        assertEquals(expectedFoods, result);
    }

    @Test
    void updateFood_ShouldUpdateExistingFood() {
        Food existingFood = new Food();
        existingFood.setId(1L);
        existingFood.setName("Old Name");

        Food updatedFood = new Food();
        updatedFood.setName("New Name");
        updatedFood.setCalories(100);

        when(foodRepository.findById(1L)).thenReturn(Optional.of(existingFood));
        when(foodRepository.existsByName("New Name")).thenReturn(false);

        foodService.updateFood(1L, updatedFood);

        verify(foodRepository).save(existingFood);
        assertEquals("New Name", existingFood.getName());
        assertEquals(100, existingFood.getCalories());
    }

    @Test
    void updateFood_ShouldThrow_WhenFoodNotFound() {
        when(foodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            foodService.updateFood(1L, new Food());
        });
    }

    @Test
    void getFoodById_WhenExists_ShouldReturnFood() {
        Food expectedFood = new Food();
        expectedFood.setId(1L);
        when(foodRepository.findById(1L)).thenReturn(Optional.of(expectedFood));

        Food result = foodService.getFoodById(1L);

        assertEquals(expectedFood, result);
    }

    @Test
    void updateFood_ShouldThrow_WhenNameAlreadyExists() {
        Food existingFood = new Food();
        existingFood.setId(1L);
        existingFood.setName("Old Name");

        Food updatedFood = new Food();
        updatedFood.setName("Existing Name");

        when(foodRepository.findById(1L)).thenReturn(Optional.of(existingFood));
        when(foodRepository.existsByName("Existing Name")).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> {
            foodService.updateFood(1L, updatedFood);
        });
    }

    @Test
    void deleteFood_WhenNoRelations_ShouldJustDeleteFood() {
        // Настройка mock-объектов
        when(dailyIntakeFoodRepository.findByFoodId(1L)).thenReturn(List.of());

        // Выполнение тестируемого метода
        boolean result = foodService.deleteFood(1L);

        // Проверки
        assertTrue(result);

        // Проверка, что очистка кэша не вызывалась
        verifyNoInteractions(dailyIntakeCache);

        // Проверка удаления еды
        verify(foodRepository).deleteById(1L);
    }

    @Test
    void deleteFood_ShouldHandleEmptyRelations() {
        when(dailyIntakeFoodRepository.findByFoodId(1L)).thenReturn(List.of());

        boolean result = foodService.deleteFood(1L);

        assertTrue(result);
        verify(dailyIntakeCache, never()).evictIntakesWithDate(any(), any());
        verify(dailyIntakeCache, never()).evictNutritionData(any(), any());
        verify(dailyIntakeFoodRepository).deleteAll(List.of());
        verify(foodRepository).deleteById(1L);
    }

    @Test
    void updateFood_ShouldUpdate_WhenNameChangedAndNameDoesNotExist() {
        // Arrange
        Food existingFood = new Food();
        existingFood.setId(1L);
        existingFood.setName("Old Name");
        existingFood.setCalories(50);

        Food updatedFood = new Food();
        updatedFood.setName("New Unique Name");
        updatedFood.setCalories(100);

        when(foodRepository.findById(1L)).thenReturn(Optional.of(existingFood));
        when(foodRepository.existsByName("New Unique Name")).thenReturn(false); // New name does not exist
        when(foodRepository.save(any(Food.class))).thenReturn(existingFood);

        // Act
        foodService.updateFood(1L, updatedFood);

        // Assert
        verify(foodRepository).findById(1L);
        verify(foodRepository).existsByName("New Unique Name");
        verify(foodRepository).save(existingFood);
        assertEquals("New Unique Name", existingFood.getName());
        assertEquals(100, existingFood.getCalories());
    }

    @Test
    void deleteFood_WithRelations_ShouldDeleteRelationsAndEvictCache() {
        // Setup mock data
        Food food = new Food();
        food.setId(1L);

        DailyIntakeFood dailyIntakeFood = new DailyIntakeFood();
        DailyIntake dailyIntake = new DailyIntake();
        User user = new User();
        user.setEmail("test@example.com");
        dailyIntake.setUser(user);
        dailyIntake.setCreationDate(LocalDate.of(2023, 10, 15));
        dailyIntakeFood.setDailyIntake(dailyIntake);

        List<DailyIntakeFood> dailyIntakeFoods = List.of(dailyIntakeFood);

        // Mock repository and cache behavior
        when(dailyIntakeFoodRepository.findByFoodId(1L)).thenReturn(dailyIntakeFoods);

        // Execute the method
        boolean result = foodService.deleteFood(1L);

        // Assertions
        assertTrue(result);
        verify(dailyIntakeFoodRepository).findByFoodId(1L);
        verify(dailyIntakeFoodRepository).deleteAll(dailyIntakeFoods);
        verify(foodRepository).deleteById(1L);
        verify(dailyIntakeCache).evictIntakesWithDate("test@example.com", LocalDate.of(2023, 10, 15));
        verify(dailyIntakeCache).evictNutritionData("test@example.com", LocalDate.of(2023, 10, 15));
    }
    
}