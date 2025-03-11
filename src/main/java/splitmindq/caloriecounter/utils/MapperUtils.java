//package splitmindq.caloriecounter.utils;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import org.springframework.stereotype.Component;
//import splitmindq.caloriecounter.dto.DailyIntakeDto;
//import splitmindq.caloriecounter.dto.FoodDto;
//import splitmindq.caloriecounter.dto.UserDto;
//import splitmindq.caloriecounter.model.DailyIntake;
//import splitmindq.caloriecounter.model.Food;
//import splitmindq.caloriecounter.model.User;
//
//@Component
//public class MapperUtils {
//    public UserDto convertToUserDto(User user) {
//        UserDto userDto = new UserDto();
//        userDto.setId(user.getId());
//        userDto.setFirstName(user.getFirstName());
//        userDto.setLastName(user.getLastName());
//        userDto.setAge(user.getAge());
//        userDto.setEmail(user.getEmail());
//        userDto.setGender(user.getGender());
//        userDto.setHeight(user.getHeight());
//        userDto.setWeight(user.getWeight());
//
//        List<DailyIntakeDto> dailyIntakeDto = user.getDailyIntakes().stream()
//                .map(this::convertToDailyIntakeDto)
//                .collect(Collectors.toList());
//
//        userDto.setDailyIntakeList(dailyIntakeDto);
//        return userDto;
//    }
//
//    public DailyIntakeDto convertToDailyIntakeDto(DailyIntake dailyIntake) {
//        DailyIntakeDto dto = new DailyIntakeDto();
//        dto.setId(dailyIntake.getId());
//        dto.setCreationDate(dailyIntake.getCreationDate());
//        dto.setUserId(dailyIntake.getUser().getId());
//
//        List<FoodDto> foodDtos = dailyIntake.getFoods().stream()
//                .map(this::convertToFoodDto)
//                .collect(Collectors.toList());
//
//        dto.setFoodList(foodDtos);
//        return dto;
//    }
//
//    public FoodDto convertToFoodDto(Food food) {
//        FoodDto dto = new FoodDto();
//        dto.setId(food.getId());
//        dto.setName(food.getName());
//        dto.setCalories(food.getCalories());
//        dto.setProtein(food.getProtein());
//        dto.setFats(food.getFats());
//        dto.setCarbs(food.getCarbs());
//        return dto;
//    }
//}
