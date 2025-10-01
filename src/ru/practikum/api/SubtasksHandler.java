package ru.practikum.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practikum.exception.TaskTimeConflictException;
import ru.practikum.manager.TaskManager;
import ru.practikum.model.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
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
            String response = gson.toJson(taskManager.getAllSubtasks());
            sendSuccess(exchange, response);
        } else if (path.matches("/subtasks/\\d+")) {
            int id = extractIdFromPath(path);
            Subtask subtask = taskManager.getSubtaskById(id);
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

        if (subtask == null) {
            sendBadRequest(exchange, "Данные подзадачи обязательны");
            return;
        }

        if (subtask.getId() == 0) {
            Subtask createdSubtask = taskManager.createSubtask(subtask);
            if (createdSubtask == null) {
                sendBadRequest(exchange, "Не удалось создать подзадачу - эпик не найден");
            } else {
                String response = gson.toJson(createdSubtask);
                sendCreated(exchange, response);
            }
        } else {
            taskManager.updateSubtask(subtask);
            sendCreated(exchange, "Подзадача успешно обновлена");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/subtasks/\\d+")) {
            int id = extractIdFromPath(path);
            Subtask subtask = taskManager.getSubtaskById(id);
            if (subtask == null) {
                sendNotFound(exchange);
            } else {
                taskManager.deleteSubtaskById(id);
                sendSuccess(exchange, "Подзадача успешно удалена");
            }
        } else if (path.equals("/subtasks")) {
            taskManager.deleteAllSubtasks();
            sendSuccess(exchange, "Все подзадачи успешно удалены");
        } else {
            sendNotFound(exchange);
        }
    }

    private int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }

    private void handleException(HttpExchange exchange, Exception e) throws IOException {
        if (e instanceof IllegalArgumentException) {
            sendBadRequest(exchange, e.getMessage());
        } else if (e instanceof TaskTimeConflictException) {
            sendHasOverlaps(exchange);
        } else {
            sendInternalError(exchange);
        }
    }
}