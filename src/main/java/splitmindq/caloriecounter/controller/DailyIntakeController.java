package splitmindq.caloriecounter.controller;

import ch.qos.logback.classic.Logger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import splitmindq.caloriecounter.dao.DailyIntakeRepository;
import splitmindq.caloriecounter.dto.DailyNutritionDto;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.DailyIntake;
import splitmindq.caloriecounter.requests.*;
import splitmindq.caloriecounter.service.DailyIntakeService;

@RestController
@RequestMapping("/api/v1/daily_intakes")
@AllArgsConstructor
@Validated
@Slf4j  // Автоматически создает переменную 'log'
@Tag(name = "Дневной рацион", description = "Операции для учета ежедневного потребления пищи")
public class DailyIntakeController {
    private final DailyIntakeRepository dailyIntakeRepository;
    private DailyIntakeService dailyIntakeService;

    @GetMapping
    @Operation(summary = "Получить все записи о дневном рационе",
            description = "Возвращает список всех записей о потреблении пищи")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка",
            content = @Content(schema = @Schema(implementation = DailyIntake.class)))
    @ApiResponse(responseCode = "204", description = "Список пуст")
    public ResponseEntity<List<DailyIntake>> getAll() {
        List<DailyIntake> dailyIntakeList = dailyIntakeService.getAllDailyIntakes();
        if (dailyIntakeList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(dailyIntakeList, HttpStatus.OK);
    }

    @GetMapping("/filter")
    @Operation(summary = "Фильтрация записей по пользователю и дате",
            description = "Возвращает записи о потреблении пищи для конкретного пользователя с возможностью фильтрации по дате")
    @ApiResponse(responseCode = "200", description = "Успешное получение отфильтрованного списка")
    public List<DailyIntake> getIntakesByUserAndDate(
            @RequestParam String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dailyIntakeService.getUserIntakes(email, date);
    }

    @GetMapping("/nutrition")
    @Operation(summary = "Получить дневную nutritional информацию",
            description = "Возвращает суммарную nutritional информацию за указанный день для пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение данных",
                    content = @Content(schema = @Schema(implementation = DailyNutritionDto.class))),
            @ApiResponse(responseCode = "404", description = "Данные не найдены"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<DailyNutritionDto> getNutrition(
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Optional<Map<String, Double>> result = dailyIntakeRepository.calculateDailyNutrition(email, date);
            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            DailyNutritionDto dto = new DailyNutritionDto(
                    result.get().get("calories"),
                    result.get().get("protein"),
                    result.get().get("fats"),
                    result.get().get("carbs")
            );
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error calculating nutrition for email: {}, date: {}", email, date, e);
            return ResponseEntity.internalServerError()
                    .body(new DailyNutritionDto(0, 0, 0, 0)); // Or handle differently
        }
    }

    @PostMapping("create_intake")
    @Operation(summary = "Создать новую запись о дневном рационе",
            description = "Создает новую запись о потреблении пищи")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Запись успешно создана"),
            @ApiResponse(responseCode = "500", description = "Ошибка при создании записи")
    })
    public ResponseEntity<String> createDailyIntake(@RequestBody DailyIntakeRequest request) {
        try {
            dailyIntakeService.createDailyIntake(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Daily intake created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating daily intake: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DailyIntake> getDailyIntake(@PathVariable Long id) {
        DailyIntake dailyIntake = dailyIntakeService.getDailyIntakeById(id);
        if (dailyIntake == null) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(dailyIntake, HttpStatus.OK);
        }
    }

    @PostMapping("/{dailyIntakeId}/add-food")
    public ResponseEntity<DailyIntake> addFoodToDailyIntake(
            @PathVariable Long dailyIntakeId,
            @RequestBody AddFoodToDailyIntakeRequest request) {
        DailyIntake dailyIntake = dailyIntakeService.addFoodToDailyIntake(
                dailyIntakeId,
                request.getFoodId(),
                request.getWeight()
        );
        return ResponseEntity.ok(dailyIntake);
    }

    @PatchMapping("/update_intake/{id}")
    public ResponseEntity<String> updateDailyIntake(
            @PathVariable Long id,
            @RequestBody UpdateDailyIntakeRequest request) {
        try {
            dailyIntakeService.updateDailyIntake(id, request);
            return ResponseEntity.status(HttpStatus.OK).body("Daily intake updated successfully.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Daily intake not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("delete_intake/{id}")
    public ResponseEntity<String> deleteDailyIntake(@PathVariable Long id) {
        boolean isDeleted = dailyIntakeService.deleteDailyIntake(id);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("DailyIntake not found.");
        }
    }

    @PostMapping("/batch-add-foods")
    @Operation(summary = "Массовое добавление продуктов в дневной рацион")
    public ResponseEntity<BatchFoodAddResponse> addFoodsToDailyIntake(
            @RequestBody @Valid List<AddFoodToDi> requests) {

        log.info("Получен запрос на добавление {} продуктов", requests.size());

        List<FoodAddResult> results = requests.stream()
                .map(request -> {
                    try {
                        DailyIntake intake = dailyIntakeService.addFoodToDailyIntake(
                                request.getDailyIntakeId(),
                                request.getFoodId(),
                                request.getWeight()
                        );
                        return new FoodAddResult(
                                request.getFoodId(),
                                true,
                                "Продукт успешно добавлен в рацион ID: " + intake.getId()
                        );
                    } catch (Exception e) {
                        return new FoodAddResult(
                                request.getFoodId(),
                                false,
                                "Ошибка: " + e.getMessage()
                        );
                    }
                })
                .collect(Collectors.toList());
        
        long successCount = results.stream()
                .filter(FoodAddResult::isSuccess)
                .count();

        return ResponseEntity.ok(
                new BatchFoodAddResponse(
                        results,
                        (int) successCount,
                        requests.size() - (int) successCount
                )
        );
    }
}
