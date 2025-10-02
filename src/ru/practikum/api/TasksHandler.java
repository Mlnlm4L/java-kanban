package ru.practikum.api;

import com.sun.net.httpserver.HttpExchange;
import ru.practikum.manager.TaskManager;
import ru.practikum.model.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager manager) {
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
        if (path.equals("/tasks")) {
            String response = gson.toJson(manager.getAllTasks());
            sendSuccess(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            int id = extractIdFromPath(path);
            Task task = manager.getTaskById(id);
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
        if (task.getId() == 0) {
            if (task.getTitle() == null || task.getTitle().isBlank()) {
                sendBadRequest(exchange, "Название задачи обязательно");
                return;
            }
            Task createdTask = manager.createTask(task);
            String response = gson.toJson(createdTask);
            sendCreated(exchange, response);
        } else {
            manager.updateTask(task);
            sendCreated(exchange, "Задача успешно обновлена");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/tasks/\\d+")) {
            int id = extractIdFromPath(path);
            Task task = manager.getTaskById(id);
            if (task == null) {
                sendNotFound(exchange);
            } else {
                manager.deleteTaskById(id);
                sendSuccess(exchange, "Задача успешно удалена");
            }
        } else {
            sendNotFound(exchange);
        }
    }
}