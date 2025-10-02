package ru.practikum.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practikum.exception.TaskTimeConflictException;
import ru.practikum.manager.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager manager;
    protected final Gson gson = JsonAdapter.createGson();

    protected BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    public abstract void handle(HttpExchange exchange) throws IOException;

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendSuccess(HttpExchange exchange, String text) throws IOException {
        if (text == null) {
            sendNotFound(exchange);
            return;
        }
        System.out.println("Отправка успешного ответа: " + text);
        sendText(exchange, text, 200);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        if (text == null) {
            sendBadRequest(exchange, "Не удалось создать");
            return;
        }
        System.out.println("Создано: " + text);
        sendText(exchange, text, 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        System.out.println("Не найдено");
        String response = "{\"error\": \"Ресурс не найден\"}";
        sendText(exchange, response, 404);
    }

    protected void sendHasOverlaps(HttpExchange exchange) throws IOException {
        System.out.println("Конфликт времени");
        String response = "{\"error\": \"Задача пересекается по времени с существующей\"}";
        sendText(exchange, response, 406);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        System.out.println("Внутренняя ошибка сервера");
        String response = "{\"error\": \"Внутренняя ошибка сервера\"}";
        sendText(exchange, response, 500);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        System.out.println("Некорректный запрос: " + message);
        String response = "{\"error\": \"" + message + "\"}";
        sendText(exchange, response, 400);
    }

    protected <T> T parseJson(InputStream inputStream, Class<T> clazz) throws IOException {
        try {
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Получен JSON: " + body);
            if (body == null || body.trim().isEmpty()) {
                throw new IllegalArgumentException("Тело запроса не может быть пустым");
            }
            return gson.fromJson(body, clazz);
        } catch (JsonSyntaxException e) {
            System.out.println("Ошибка парсинга JSON: " + e.getMessage());
            throw new IllegalArgumentException("Некорректный формат JSON: " + e.getMessage(), e);
        }
    }

    protected int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }

    protected void handleException(HttpExchange exchange, Exception e) throws IOException {
        if (e instanceof IllegalArgumentException) {
            sendBadRequest(exchange, e.getMessage());
        } else if (e instanceof TaskTimeConflictException) {
            sendHasOverlaps(exchange);
        } else {
            sendInternalError(exchange);
        }
    }
}