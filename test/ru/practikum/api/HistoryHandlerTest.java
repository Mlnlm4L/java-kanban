package ru.practikum.api;

import org.junit.jupiter.api.Test;
import ru.practikum.model.Epic;
import ru.practikum.model.Status;
import ru.practikum.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryHandlerTest extends BaseHttpTest {

    @Test
    void testGetHistoryWhenEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), List.class);
        assertTrue(history.isEmpty());
    }

    @Test
    void testGetHistoryWithTasks() throws IOException, InterruptedException {
        Task task1 = manager.createTask(new Task("Задача 1", "Описание 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание 2", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Эпик 1", "Описание эпика"));

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getEpicById(epic.getId());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), List.class);
        assertEquals(3, history.size());
    }

    @Test
    void testGetHistoryAfterDeletion() throws IOException, InterruptedException {
        Task task1 = manager.createTask(new Task("Задача 1", "Описание 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание 2", Status.NEW));

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());

        manager.deleteTaskById(task1.getId());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), List.class);
        assertEquals(1, history.size());
    }
}