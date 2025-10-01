package ru.practikum.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practikum.exception.TaskTimeConflictException;
import ru.practikum.model.Epic;
import ru.practikum.manager.TaskManager;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
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
        if (path.equals("/epics")) {
            String response = gson.toJson(taskManager.getAllEpics());
            sendSuccess(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            int id = extractIdFromPath(path);
            Epic epic = taskManager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(epic);
                sendSuccess(exchange, response);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            int id = extractIdFromPath(path.replace("/subtasks", ""));
            Epic epic = taskManager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(taskManager.getSubtasksByEpicId(id));
                sendSuccess(exchange, response);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Epic epic = parseJson(exchange.getRequestBody(), Epic.class);

        if (epic == null) {
            sendBadRequest(exchange, "Данные эпика обязательны");
            return;
        }

        if (epic.getId() == 0) {
            Epic createdEpic = taskManager.createEpic(epic);
            String response = gson.toJson(createdEpic);
            sendCreated(exchange, response);
        } else {
            Epic existingEpic = taskManager.getEpicById(epic.getId());
            if (existingEpic == null) {
                sendNotFound(exchange);
            } else {
                existingEpic.setTitle(epic.getTitle());
                existingEpic.setDescription(epic.getDescription());
                sendCreated(exchange, "Эпик успешно обновлен");
            }
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+")) {
            int id = extractIdFromPath(path);
            Epic epic = taskManager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                taskManager.deleteEpicById(id);
                sendSuccess(exchange, "Эпик успешно удален");
            }
        } else if (path.equals("/epics")) {
            taskManager.deleteAllEpics();
            sendSuccess(exchange, "Все эпики успешно удалены");
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