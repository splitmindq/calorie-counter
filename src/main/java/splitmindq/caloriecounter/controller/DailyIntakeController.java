package splitmindq.caloriecounter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
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
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/daily_intakes")
@AllArgsConstructor
@Validated
@Slf4j
@Tag(name = "Дневной рацион", description = "Операции для учета ежедневного потребления пищи")
public class DailyIntakeController {
    private final DailyIntakeRepository dailyIntakeRepository;
    private final DailyIntakeService dailyIntakeService;

    @GetMapping
    @Operation(summary = "Получить все записи о дневном рационе",
            description = "Возвращает список всех записей о потреблении пищи")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение списка",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DailyIntake.class)))),
            @ApiResponse(responseCode = "204", description = "Список пуст")
    })
    public ResponseEntity<List<DailyIntake>> getAll() {
        List<DailyIntake> dailyIntakeList = dailyIntakeService.getAllDailyIntakes();
        if (dailyIntakeList.isEmpty()) {
            log.info("No daily intakes found");
            return ResponseEntity.noContent().build();
        }
        log.info("Retrieved {} daily intakes", dailyIntakeList.size());
        return ResponseEntity.ok(dailyIntakeList);
    }

    @GetMapping("/filter")
    @Operation(summary = "Получить дневные рационы пользователя по дате",
            description = "Возвращает список дневных рационов пользователя по email и дате")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение данных",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DailyIntake.class)))),
            @ApiResponse(responseCode = "404", description = "Рационы не найдены"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<List<DailyIntake>> getIntakesByUserAndDate(
            @RequestParam @NotBlank @Email String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<DailyIntake> intakes = dailyIntakeService.getUserIntakes(email, date);
            if (intakes.isEmpty()) {
                log.warn("No intakes found for email={} and date={}", email, date);
                return ResponseEntity.notFound().build();
            }
            log.info("Retrieved {} intakes for email={} and date={}", intakes.size(), email, date);
            return ResponseEntity.ok(intakes);
        } catch (Exception e) {
            log.error("Error retrieving intakes for email={} and date={}: {}", email, date, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nutrition")
    @Operation(summary = "Получить суммарную дневную nutritional информацию",
            description = "Возвращает суммарную nutritional информацию за указанный день для пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение данных",
                    content = @Content(schema = @Schema(implementation = DailyNutritionDto.class))),
            @ApiResponse(responseCode = "404", description = "Данные не найдены"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<DailyNutritionDto> getNutrition(
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            DailyNutritionDto dto = dailyIntakeService.getDailyNutrition(email, date);
            if (dto == null) {
                log.warn("Nutrition data not found for email={} and date={}", email, date);
                return ResponseEntity.notFound().build();
            }
            log.info("Retrieved nutrition data for email={} and date={}: {}", email, date, dto);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error calculating nutrition for email={} and date={}: {}", email, date, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DailyNutritionDto(0.0, 0.0, 0.0, 0.0));
        }
    }

    @GetMapping("/{intakeId}/nutrition")
    @Operation(summary = "Получить nutritional информацию для конкретного рациона",
            description = "Возвращает nutritional информацию для указанного рациона по его ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение данных",
                    content = @Content(schema = @Schema(implementation = DailyNutritionDto.class))),
            @ApiResponse(responseCode = "404", description = "Данные не найдены"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<DailyNutritionDto> getNutritionForIntake(
            @PathVariable Long intakeId) {
        try {
            DailyNutritionDto dto = dailyIntakeService.getNutritionForIntake(intakeId);
            if (dto.getCalories() == 0.0 && dto.getProtein() == 0.0 && dto.getFats() == 0.0 && dto.getCarbs() == 0.0) {
                log.warn("Nutrition data not found for intakeId={}", intakeId);
                return ResponseEntity.notFound().build();
            }
            log.info("Retrieved nutrition data for intakeId={}: {}", intakeId, dto);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error calculating nutrition for intakeId={}: {}", intakeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DailyNutritionDto(0.0, 0.0, 0.0, 0.0));
        }
    }

    @PostMapping("/create_intake")
    @Operation(summary = "Создать новую запись о дневном рационе",
            description = "Создает новую запись о потреблении пищи")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Запись успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<String> createDailyIntake(@RequestBody @Valid DailyIntakeRequest request) {
        try {
            DailyIntake intake = dailyIntakeService.createDailyIntake(request);
            log.info("Created daily intake with id={} for userId={}", intake.getId(), request.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Daily intake created successfully with id: " + intake.getId());
        } catch (ResourceNotFoundException e) {
            log.error("Error creating daily intake: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating daily intake: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating daily intake: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error creating daily intake: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить запись о дневном рационе по ID",
            description = "Возвращает запись о дневном рационе по указанному ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение данных",
                    content = @Content(schema = @Schema(implementation = DailyIntake.class))),
            @ApiResponse(responseCode = "404", description = "Запись не найдена")
    })

    public ResponseEntity<DailyIntake> getDailyIntake(@PathVariable Long id) {
        try {
            DailyIntake dailyIntake = dailyIntakeService.getDailyIntakeById(id);
            log.info("Retrieved daily intake with id={}", id);
            return ResponseEntity.ok(dailyIntake);
        } catch (ResourceNotFoundException e) {
            log.warn("Daily intake with id={} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{dailyIntakeId}/add-food")
    @Operation(summary = "Добавить продукт в дневной рацион",
            description = "Добавляет продукт в указанный дневной рацион")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Продукт успешно добавлен",
                    content = @Content(schema = @Schema(implementation = DailyIntake.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Рацион или продукт не найдены")
    })
    public ResponseEntity<DailyIntake> addFoodToDailyIntake(
            @PathVariable Long dailyIntakeId,
            @RequestBody @Valid AddFoodToDailyIntakeRequest request) {
        try {
            DailyIntake dailyIntake = dailyIntakeService.addFoodToDailyIntake(
                    dailyIntakeId,
                    request.getFoodId(),
                    request.getWeight()
            );
            log.info("Added foodId={} to dailyIntakeId={}", request.getFoodId(), dailyIntakeId);
            return ResponseEntity.ok(dailyIntake);
        } catch (ResourceNotFoundException e) {
            log.error("Error adding food to daily intake: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/update_intake/{id}")
    @Operation(summary = "Обновить дневной рацион",
            description = "Обновляет существующий дневной рацион по указанному ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Запись успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Запись не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<String> updateDailyIntake(
            @PathVariable Long id,
            @RequestBody @Valid UpdateDailyIntakeRequest request) {
        try {
            dailyIntakeService.updateDailyIntake(id, request);
            log.info("Updated daily intake with id={}", id);
            return ResponseEntity.ok("Daily intake updated successfully.");
        } catch (ResourceNotFoundException e) {
            log.warn("Daily intake with id={} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Daily intake not found.");
        } catch (Exception e) {
            log.error("Error updating daily intake with id={}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete_intake/{id}")
    @Operation(summary = "Удалить дневной рацион",
            description = "Удаляет дневной рацион по указанному ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Запись успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Запись не найдена")
    })
    public ResponseEntity<String> deleteDailyIntake(@PathVariable Long id) {
        boolean isDeleted = dailyIntakeService.deleteDailyIntake(id);
        if (isDeleted) {
            log.info("Deleted daily intake with id={}", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Daily intake with id={} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("DailyIntake not found.");
        }
    }

    @PostMapping("/batch-add-foods")
    @Operation(summary = "Массовое добавление продуктов в дневной рацион",
            description = "Добавляет несколько продуктов в дневной рацион")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Продукты добавлены",
                    content = @Content(schema = @Schema(implementation = BatchFoodAddResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    })
    public ResponseEntity<BatchFoodAddResponse> addFoodsToDailyIntake(
            @RequestBody @Valid List<AddFoodToDi> requests) {
        log.info("Received batch request to add {} foods", requests.size());

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
                    } catch (ResourceNotFoundException e) {
                        return new FoodAddResult(
                                request.getFoodId(),
                                false,
                                "Ошибка: " + e.getMessage()
                        );
                    } catch (Exception e) {
                        return new FoodAddResult(
                                request.getFoodId(),
                                false,
                                "Неожиданная ошибка: " + e.getMessage()
                        );
                    }
                })
                .collect(Collectors.toList());

        long successCount = results.stream()
                .filter(FoodAddResult::isSuccess)
                .count();

        log.info("Batch add completed: {} successes, {} failures", successCount, requests.size() - successCount);
        return ResponseEntity.ok(
                new BatchFoodAddResponse(
                        results,
                        (int) successCount,
                        (int) (requests.size() - successCount)
                )
        );
    }
}