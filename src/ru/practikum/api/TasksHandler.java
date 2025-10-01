package ru.practikum.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practikum.exception.TaskTimeConflictException;
import ru.practikum.manager.TaskManager;
import ru.practikum.model.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
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
        if (path.equals("/tasks")) {
            String response = gson.toJson(taskManager.getAllTasks());
            sendSuccess(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            int id = extractIdFromPath(path);
            Task task = taskManager.getTaskById(id);
            if (task == null) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(task);
                sendSuccess(exchange, response);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Task task = parseJson(exchange.getRequestBody(), Task.class);

        if (task == null) {
            sendBadRequest(exchange, "Данные задачи обязательны");
            return;
        }

        if (task.getId() == 0) {
            Task createdTask = taskManager.createTask(task);
            String response = gson.toJson(createdTask);
            sendCreated(exchange, response);
        } else {
            taskManager.updateTask(task);
            sendCreated(exchange, "Задача успешно обновлена");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/tasks/\\d+")) {
            int id = extractIdFromPath(path);
            Task task = taskManager.getTaskById(id);
            if (task == null) {
                sendNotFound(exchange);
            } else {
                taskManager.deleteTaskById(id);
                sendSuccess(exchange, "Задача успешно удалена");
            }
        } else if (path.equals("/tasks")) {
            taskManager.deleteAllTasks();
            sendSuccess(exchange, "Все задачи успешно удалены");
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