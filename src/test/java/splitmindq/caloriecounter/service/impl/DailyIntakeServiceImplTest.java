package splitmindq.caloriecounter.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import splitmindq.caloriecounter.cache.DailyIntakeCache;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dao.FoodRepository;
import splitmindq.caloriecounter.dao.UserRepository;
import splitmindq.caloriecounter.dto.DailyNutritionDto;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.*;
import splitmindq.caloriecounter.requests.DailyIntakeRequest;
import splitmindq.caloriecounter.requests.UpdateDailyIntakeRequest;
import splitmindq.caloriecounter.service.DailyIntakeServiceImpl;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyIntakeServiceImplAdditionalTest {

    @Mock
    private DailyIntakeRepository dailyIntakeRepository;
    @Mock
    private FoodRepository foodRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DailyIntakeCache dailyIntakeCache;

    @InjectMocks
    private DailyIntakeServiceImpl dailyIntakeService;

    private User user;
    private Food food;
    private DailyIntake intake;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");

        food = new Food();
        food.setId(10L);
        food.setCalories(100.0);
        food.setProtein(10.0);
        food.setFats(5.0);
        food.setCarbs(20.0);

        intake = new DailyIntake();
        intake.setId(1L);
        intake.setUser(user);
        intake.setCreationDate(LocalDate.of(2025, 4, 22));
        intake.setDailyIntakeFoods(new ArrayList<>());

        date = LocalDate.of(2025, 4, 22);
    }

    @Test
    void getAllDailyIntakes_WhenIntakesExist_ShouldReturnList() {
        // Arrange
        List<DailyIntake> intakes = List.of(intake);
        when(dailyIntakeRepository.findAll()).thenReturn(intakes);

        // Act
        List<DailyIntake> result = dailyIntakeService.getAllDailyIntakes();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(intake);
        verify(dailyIntakeRepository).findAll();
    }

    @Test
    void getAllDailyIntakes_WhenNoIntakes_ShouldReturnEmptyList() {
        // Arrange
        when(dailyIntakeRepository.findAll()).thenReturn(List.of());

        // Act
        List<DailyIntake> result = dailyIntakeService.getAllDailyIntakes();

        // Assert
        assertThat(result).isEmpty();
        verify(dailyIntakeRepository).findAll();
    }

    @Test
    void getDailyIntakeById_WhenExists_ShouldReturnIntake() {
        // Arrange
        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.of(intake));

        // Act
        DailyIntake result = dailyIntakeService.getDailyIntakeById(1L);

        // Assert
        assertThat(result).isEqualTo(intake);
        verify(dailyIntakeRepository).findById(1L);
    }

    @Test
    void getDailyIntakeById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dailyIntakeService.getDailyIntakeById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("DailyIntake not found with id: 1");
    }

    @Test
    void addFoodToDailyIntake_WhenEntryExists_ShouldUpdateWeight() {
        // Arrange
        DailyIntakeFood existingEntry = new DailyIntakeFood();
        existingEntry.setFood(food);
        existingEntry.setWeight(100.0);
        intake.getDailyIntakeFoods().add(existingEntry);

        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.of(intake));
        when(foodRepository.findById(10L)).thenReturn(Optional.of(food));
        when(dailyIntakeRepository.save(any(DailyIntake.class))).thenReturn(intake);
        doNothing().when(dailyIntakeCache).evictIntakesWithDate(anyString(), any(LocalDate.class));
        doNothing().when(dailyIntakeCache).evictNutritionData(anyString(), any(LocalDate.class));

        // Act
        DailyIntake result = dailyIntakeService.addFoodToDailyIntake(1L, 10L, 50.0);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDailyIntakeFoods().get(0).getWeight()).isEqualTo(150.0);
        verify(dailyIntakeCache).evictIntakesWithDate("test@email.com", date);
        verify(dailyIntakeCache).evictNutritionData("test@email.com", date);
        verify(dailyIntakeRepository).save(intake);
    }

    @Test
    void addFoodToDailyIntake_WhenEntryNotExists_ShouldAddNewEntry() {
        // Arrange
        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.of(intake));
        when(foodRepository.findById(10L)).thenReturn(Optional.of(food));
        when(dailyIntakeRepository.save(any(DailyIntake.class))).thenReturn(intake);
        doNothing().when(dailyIntakeCache).evictIntakesWithDate(anyString(), any(LocalDate.class));
        doNothing().when(dailyIntakeCache).evictNutritionData(anyString(), any(LocalDate.class));

        // Act
        DailyIntake result = dailyIntakeService.addFoodToDailyIntake(1L, 10L, 50.0);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDailyIntakeFoods()).hasSize(1);
        assertThat(result.getDailyIntakeFoods().get(0).getWeight()).isEqualTo(50.0);
        assertThat(result.getDailyIntakeFoods().get(0).getFood()).isEqualTo(food);
        verify(dailyIntakeCache).evictIntakesWithDate("test@email.com", date);
        verify(dailyIntakeCache).evictNutritionData("test@email.com", date);
        verify(dailyIntakeRepository).save(intake);
    }

    @Test
    void addFoodToDailyIntake_WhenIntakeNotFound_ShouldThrowException() {
        // Arrange
        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dailyIntakeService.addFoodToDailyIntake(1L, 10L, 50.0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("DailyIntake not found");
    }

    @Test
    void addFoodToDailyIntake_WhenFoodNotFound_ShouldThrowException() {
        // Arrange
        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.of(intake));
        when(foodRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dailyIntakeService.addFoodToDailyIntake(1L, 10L, 50.0))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Food not found");
    }

    @Test
    void deleteDailyIntake_WhenExists_ShouldEvictCacheAndReturnTrue() {
        // Arrange
        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.of(intake));
        doNothing().when(dailyIntakeCache).evictIntakesWithDate(anyString(), any(LocalDate.class));
        doNothing().when(dailyIntakeCache).evictNutritionData(anyString(), any(LocalDate.class));

        // Act
        boolean result = dailyIntakeService.deleteDailyIntake(1L);

        // Assert
        assertThat(result).isTrue();
        verify(dailyIntakeCache).evictIntakesWithDate("test@email.com", date);
        verify(dailyIntakeCache).evictNutritionData("test@email.com", date);
        verify(dailyIntakeRepository).delete(intake);
    }

    @Test
    void deleteDailyIntake_WhenNotExists_ShouldReturnFalse() {
        // Arrange
        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = dailyIntakeService.deleteDailyIntake(1L);

        // Assert
        assertThat(result).isFalse();
        verifyNoInteractions(dailyIntakeCache);
        verify(dailyIntakeRepository, never()).delete(any());
    }

    @Test
    void createDailyIntake_WhenValidRequest_ShouldCreateAndSaveIntake() {
        // Arrange
        DailyIntakeRequest request = new DailyIntakeRequest();
        request.setUserId(1L);
        request.setFoodEntries(List.of(new FoodEntry(10L, 200.0)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(foodRepository.findById(10L)).thenReturn(Optional.of(food));
        when(dailyIntakeRepository.save(any(DailyIntake.class))).thenReturn(intake);

        // Act
        DailyIntake result = dailyIntakeService.createDailyIntake(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getDailyIntakeFoods()).hasSize(1);
        assertThat(result.getDailyIntakeFoods().get(0).getFood()).isEqualTo(food);
        assertThat(result.getDailyIntakeFoods().get(0).getWeight()).isEqualTo(200.0);
        verify(dailyIntakeRepository).save(any(DailyIntake.class));
    }

    @Test
    void createDailyIntake_WhenDuplicateFoodEntries_ShouldMergeWeights() {
        // Arrange
        DailyIntakeRequest request = new DailyIntakeRequest();
        request.setUserId(1L);
        request.setFoodEntries(List.of(
                new FoodEntry(10L, 200.0),
                new FoodEntry(10L, 300.0)
        ));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(foodRepository.findById(10L)).thenReturn(Optional.of(food));
        when(dailyIntakeRepository.save(any(DailyIntake.class))).thenReturn(intake);

        // Act
        DailyIntake result = dailyIntakeService.createDailyIntake(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDailyIntakeFoods()).hasSize(1);
        assertThat(result.getDailyIntakeFoods().get(0).getWeight()).isEqualTo(500.0);
        verify(dailyIntakeRepository).save(any(DailyIntake.class));
    }

    @Test
    void createDailyIntake_WhenFoodNotFound_ShouldThrowException() {
        // Arrange
        DailyIntakeRequest request = new DailyIntakeRequest();
        request.setUserId(1L);
        request.setFoodEntries(List.of(new FoodEntry(10L, 200.0)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(foodRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dailyIntakeService.createDailyIntake(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Food not found");
    }

    @Test
    void getDailyNutrition_WhenCachedDataExists_ShouldReturnCachedData() {
        // Arrange
        Map<String, Double> nutrition = new HashMap<>();
        nutrition.put("calories", 1000.0);
        nutrition.put("protein", 50.0);
        nutrition.put("fats", 30.0);
        nutrition.put("carbs", 120.0);

        when(dailyIntakeCache.getNutritionData("test@email.com", date)).thenReturn(Optional.of(nutrition));

        // Act
        DailyNutritionDto result = dailyIntakeService.getDailyNutrition("test@email.com", date);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCalories()).isEqualTo(1000.0);
        assertThat(result.getProtein()).isEqualTo(50.0);
        assertThat(result.getFats()).isEqualTo(30.0);
        assertThat(result.getCarbs()).isEqualTo(120.0);
        verify(dailyIntakeCache).getNutritionData("test@email.com", date);
        verifyNoInteractions(dailyIntakeRepository);
    }

    @Test
    void getDailyNutrition_WhenNotCached_ShouldCalculateAndCache() {
        // Arrange
        Map<String, Double> nutrition = new HashMap<>();
        nutrition.put("calories", 1000.0);
        nutrition.put("protein", 50.0);
        nutrition.put("fats", 30.0);
        nutrition.put("carbs", 120.0);

        when(dailyIntakeCache.getNutritionData("test@email.com", date)).thenReturn(Optional.empty());
        when(dailyIntakeRepository.calculateDailyNutrition("test@email.com", date))
                .thenReturn(Optional.of(nutrition));
        doNothing().when(dailyIntakeCache).putNutritionData(anyString(), any(LocalDate.class), anyMap());

        // Act
        DailyNutritionDto result = dailyIntakeService.getDailyNutrition("test@email.com", date);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCalories()).isEqualTo(1000.0);
        assertThat(result.getProtein()).isEqualTo(50.0);
        assertThat(result.getFats()).isEqualTo(30.0);
        assertThat(result.getCarbs()).isEqualTo(120.0);
        verify(dailyIntakeRepository).calculateDailyNutrition("test@email.com", date);
        verify(dailyIntakeCache).putNutritionData("test@email.com", date, nutrition);
    }

    @Test
    void getDailyNutrition_WhenNoData_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(dailyIntakeCache.getNutritionData("test@email.com", date)).thenReturn(Optional.empty());
        when(dailyIntakeRepository.calculateDailyNutrition("test@email.com", date)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dailyIntakeService.getDailyNutrition("test@email.com", date))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Nutrition data not found");
    }

    @Test
    void getUserIntakes_WithDateAndCachedData_ShouldReturnCachedIntakes() {
        // Arrange
        List<DailyIntake> intakes = List.of(intake);
        when(dailyIntakeCache.getIntakesWithDate("test@email.com", date)).thenReturn(Optional.of(intakes));

        // Act
        List<DailyIntake> result = dailyIntakeService.getUserIntakes("test@email.com", date);

        // Assert
        assertThat(result).isEqualTo(intakes);
        verify(dailyIntakeCache).getIntakesWithDate("test@email.com", date);
        verifyNoInteractions(dailyIntakeRepository);
    }

    @Test
    void getUserIntakes_WithDateAndNotCached_ShouldFetchAndCache() {
        // Arrange
        List<DailyIntake> intakes = List.of(intake);
        when(dailyIntakeCache.getIntakesWithDate("test@email.com", date)).thenReturn(Optional.empty());
        when(dailyIntakeRepository.findUserIntakesWithDate("test@email.com", date)).thenReturn(intakes);
        doNothing().when(dailyIntakeCache).putIntakesWithDate(anyString(), any(LocalDate.class), anyList());

        // Act
        List<DailyIntake> result = dailyIntakeService.getUserIntakes("test@email.com", date);

        // Assert
        assertThat(result).isEqualTo(intakes);
        verify(dailyIntakeRepository).findUserIntakesWithDate("test@email.com", date);
        verify(dailyIntakeCache).putIntakesWithDate("test@email.com", date, intakes);
    }

    @Test
    void getUserIntakes_WithoutDateAndCachedData_ShouldReturnCachedIntakes() {
        // Arrange
        List<DailyIntake> intakes = List.of(intake);
        when(dailyIntakeCache.getIntakesWithoutDate("test@email.com")).thenReturn(Optional.of(intakes));

        // Act
        List<DailyIntake> result = dailyIntakeService.getUserIntakes("test@email.com", null);

        // Assert
        assertThat(result).isEqualTo(intakes);
        verify(dailyIntakeCache).getIntakesWithoutDate("test@email.com");
        verifyNoInteractions(dailyIntakeRepository);
    }

    @Test
    void getUserIntakes_WithoutDateAndNotCached_ShouldFetchAndCache() {
        // Arrange
        List<DailyIntake> intakes = List.of(intake);
        when(dailyIntakeCache.getIntakesWithoutDate("test@email.com")).thenReturn(Optional.empty());
        when(dailyIntakeRepository.findUserIntakesWithoutDate("test@email.com")).thenReturn(intakes);
        doNothing().when(dailyIntakeCache).putIntakesWithoutDate(anyString(), anyList());

        // Act
        List<DailyIntake> result = dailyIntakeService.getUserIntakes("test@email.com", null);

        // Assert
        assertThat(result).isEqualTo(intakes);
        verify(dailyIntakeRepository).findUserIntakesWithoutDate("test@email.com");
        verify(dailyIntakeCache).putIntakesWithoutDate("test@email.com", intakes);
    }

    @Test
    void updateDailyIntake_WhenExistingFoodId_ShouldUpdateWeight() {
        // Arrange
        DailyIntakeFood existingEntry = new DailyIntakeFood();
        existingEntry.setFood(food);
        existingEntry.setWeight(100.0);
        intake.getDailyIntakeFoods().add(existingEntry);

        UpdateDailyIntakeRequest request = new UpdateDailyIntakeRequest();
        request.setFoodIds(List.of(10L));
        request.setWeights(List.of(200.0));

        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.of(intake));
        when(dailyIntakeRepository.save(any(DailyIntake.class))).thenReturn(intake);
        doNothing().when(dailyIntakeCache).evictIntakesWithDate(anyString(), any(LocalDate.class));
        doNothing().when(dailyIntakeCache).evictNutritionData(anyString(), any(LocalDate.class));

        // Act
        dailyIntakeService.updateDailyIntake(1L, request);

        // Assert
        assertThat(intake.getDailyIntakeFoods().get(0).getWeight()).isEqualTo(200.0);
        verify(dailyIntakeRepository).save(intake);
        verify(dailyIntakeCache).evictIntakesWithDate("test@email.com", date);
        verify(dailyIntakeCache).evictNutritionData("test@email.com", date);
    }

    @Test
    void updateDailyIntake_WhenFoodNotFound_ShouldThrowException() {
        // Arrange
        UpdateDailyIntakeRequest request = new UpdateDailyIntakeRequest();
        request.setFoodIds(List.of(10L));
        request.setWeights(List.of(200.0));

        when(dailyIntakeRepository.findById(1L)).thenReturn(Optional.of(intake));
        when(foodRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dailyIntakeService.updateDailyIntake(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Food not found");
    }
}