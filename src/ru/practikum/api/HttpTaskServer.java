package ru.practikum.api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import ru.practikum.manager.Managers;
import ru.practikum.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        this.gson = JsonAdapter.createGson();
        configureHandlers();
    }

    private void configureHandlers() {
        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        System.out.println("HTTP сервер задач запущен на порту " + PORT);
        server.start();
    }

    public void stop() {
        System.out.println("HTTP сервер задач остановлен");
        server.stop(0);
    }

    public Gson getGson() {
        return gson;
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();
        } catch (IOException e) {
            System.out.println("Не удалось запустить HTTP сервер задач: " + e.getMessage());
        }
    }
}