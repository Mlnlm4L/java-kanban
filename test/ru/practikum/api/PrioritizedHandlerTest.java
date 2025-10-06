package ru.practikum.api;

import org.junit.jupiter.api.Test;
import ru.practikum.model.Status;
import ru.practikum.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHandlerTest extends BaseHttpTest {

    @Test
    void testGetPrioritizedTasksWhenEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(), List.class);
        assertTrue(prioritized.isEmpty());
    }

    @Test
    void testGetPrioritizedTasksWithTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        Task task2 = new Task("Задача 2", "Описание 2", Status.IN_PROGRESS,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(2));

        manager.createTask(task1);
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(), List.class);
        assertEquals(2, prioritized.size());
    }

    @Test
    void testGetPrioritizedTasksWithoutTime() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", Status.IN_PROGRESS);

        manager.createTask(task1);
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> prioritized = gson.fromJson(response.body(), List.class);
        assertTrue(prioritized.isEmpty());
    }
}