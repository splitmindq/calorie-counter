# Используем базовый образ с Java (выберите версию, соответствующую вашему проекту)
# Например, для Java 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем собранный JAR-файл вашего приложения в контейнер
# Замените 'target/your-app-name.jar' на актуальный путь и имя вашего JAR-файла
COPY target/caloriecounter-0.0.1-SNAPSHOT.jar app.jar

# Указываем порт, который ваше Spring Boot приложение слушает (обычно 8080)
EXPOSE 8080

# Команда для запуска вашего приложения при старте контейнера
ENTRYPOINT ["java", "-jar", "app.jar"]

# Опционально: можно передавать профили Spring или другие аргументы JVM
# ENV SPRING_PROFILES_ACTIVE=docker
# ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]