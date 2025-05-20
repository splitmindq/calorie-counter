package splitmindq.caloriecounter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import splitmindq.caloriecounter.excpetions.ResourceNotFoundException;
import splitmindq.caloriecounter.model.Food;
import splitmindq.caloriecounter.service.FoodService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/foods")
@AllArgsConstructor
@Tag(name = "Продукты", description = "Операции для управления списком продуктов")
public class FoodController {
    private final FoodService foodService;

    @GetMapping
    @Operation(summary = "Получить все продукты",
            description = "Возвращает список всех доступных продуктов")
    @ApiResponse(responseCode = "200", description = "Успешное получение списка",
            content = @Content(schema = @Schema(implementation = Food.class)))
    @ApiResponse(responseCode = "204", description = "Список продуктов пуст")
    public ResponseEntity<List<Food>> getFoods() {
        List<Food> foods = foodService.getAllFood();
        if (foods.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(foods, HttpStatus.OK);
    }

    @PostMapping("create_food")
    @Operation(summary = "Создать новый продукт",
            description = "Добавляет новый продукт в базу данных")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Продукт успешно создан"),
            @ApiResponse(responseCode = "500", description = "Ошибка при создании продукта")
    })
    public ResponseEntity<String> createFood(
            @Parameter(description = "Данные продукта для создания", required = true,
                    content = @Content(schema = @Schema(implementation = Food.class)))
            @RequestBody Food food) {
        foodService.createFood(food);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить продукт по ID",
            description = "Возвращает продукт по указанному идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Продукт найден",
                    content = @Content(schema = @Schema(implementation = Food.class))),
            @ApiResponse(responseCode = "404", description = "Продукт не найден"),
            @ApiResponse(responseCode = "409", description = "Конфликт данных"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Food> getFoodById(
            @Parameter(description = "ID продукта", required = true)
            @PathVariable Long id) {
        try {
            Food food = foodService.getFoodById(id);
            return new ResponseEntity<>(food, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("update_food/{id}")
    @Operation(summary = "Обновить продукт",
            description = "Обновляет данные продукта по указанному ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Продукт успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден"),
            @ApiResponse(responseCode = "409", description = "Конфликт данных (email уже используется)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<String> updateFood(
            @Parameter(description = "ID продукта для обновления", required = true)
            @PathVariable Long id,
            @Parameter(description = "Обновленные данные продукта", required = true,
                    content = @Content(schema = @Schema(implementation = Food.class)))
            @RequestBody Food food) {
        try {
            foodService.updateFood(id, food);
            return ResponseEntity.status(HttpStatus.OK).body("Продукт успешно обновлен.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка: " + e.getMessage());
        }
    }

    @DeleteMapping("delete_food/{id}")
    @Operation(summary = "Удалить продукт",
            description = "Удаляет продукт по указанному ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Продукт успешно удален"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден"),
            @ApiResponse(responseCode = "409", description = "Нельзя удалить продукт, связанный с дневными рационами"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<String> deleteFood(
            @Parameter(description = "ID продукта для удаления", required = true)
            @PathVariable Long id) {
        try {
            boolean isDeleted = foodService.deleteFood(id);
            if (isDeleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Продукт не найден.");
            }
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Нельзя удалить продукт, который связан с дневными рационами.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Произошла ошибка: " + e.getMessage());
        }
    }
}