package ru.practikum.api;

import com.sun.net.httpserver.HttpExchange;
import ru.practikum.manager.TaskManager;
import ru.practikum.model.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            String response = gson.toJson(manager.getAllSubtasks());
            sendSuccess(exchange, response);
        } else if (path.matches("/subtasks/\\d+")) {
            int id = extractIdFromPath(path);
            Subtask subtask = manager.getSubtaskById(id);
            if (subtask == null) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(subtask);
                sendSuccess(exchange, response);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Subtask subtask = parseJson(exchange.getRequestBody(), Subtask.class);
        if (subtask.getId() == 0) {
            if (subtask.getTitle() == null || subtask.getTitle().isBlank()) {
                sendBadRequest(exchange, "Название подзадачи обязательно");
                return;
            }
            Subtask createdSubtask = manager.createSubtask(subtask);
            if (createdSubtask == null) {
                sendBadRequest(exchange, "Не удалось создать подзадачу - эпик не найден");
            } else {
                String response = gson.toJson(createdSubtask);
                sendCreated(exchange, response);
            }
        } else {
            manager.updateSubtask(subtask);
            sendCreated(exchange, "Подзадача успешно обновлена");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/subtasks/\\d+")) {
            int id = extractIdFromPath(path);
            Subtask subtask = manager.getSubtaskById(id);
            if (subtask == null) {
                sendNotFound(exchange);
            } else {
                manager.deleteSubtaskById(id);
                sendSuccess(exchange, "Подзадача успешно удалена");
            }
        } else {
            sendNotFound(exchange);
        }
    }
}