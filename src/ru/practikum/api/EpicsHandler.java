package ru.practikum.api;

import com.sun.net.httpserver.HttpExchange;
import ru.practikum.manager.TaskManager;
import ru.practikum.model.Epic;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager manager) {
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
        if (path.equals("/epics")) {
            String response = gson.toJson(manager.getAllEpics());
            sendSuccess(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            int id = extractIdFromPath(path);
            Epic epic = manager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(epic);
                sendSuccess(exchange, response);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            int id = extractIdFromPath(path.replace("/subtasks", ""));
            Epic epic = manager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(manager.getSubtasksByEpicId(id));
                sendSuccess(exchange, response);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Epic epic = parseJson(exchange.getRequestBody(), Epic.class);
        if (epic.getTitle() == null || epic.getTitle().isBlank()) {
            sendBadRequest(exchange, "Название эпика обязательно");
            return;
        }
        Epic createdEpic = manager.createEpic(epic);
        String response = gson.toJson(createdEpic);
        sendCreated(exchange, response);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+")) {
            int id = extractIdFromPath(path);
            Epic epic = manager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                manager.deleteEpicById(id);
                sendSuccess(exchange, "Эпик успешно удален");
            }
        } else {
            sendNotFound(exchange);
        }
    }
}