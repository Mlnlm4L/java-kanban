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

class TasksHandlerTest extends BaseHttpTest {

    @Test
    void testGetAllTasksWhenEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Task> tasks = gson.fromJson(response.body(), List.class);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals("Задача", tasksFromManager.get(0).getTitle());
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание задачи", Status.NEW);
        Task createdTask = manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/tasks/" + createdTask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(responseTask);
        assertEquals(createdTask.getId(), responseTask.getId());
        assertEquals("Задача", responseTask.getTitle());
    }

    @Test
    void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание", Status.NEW);
        Task createdTask = manager.createTask(task);

        Task updatedTask = new Task("Обновленная задача", "Обновленное описание", Status.IN_PROGRESS);
        updatedTask.setId(createdTask.getId());
        String updatedTaskJson = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task taskFromManager = manager.getTaskById(createdTask.getId());
        assertEquals("Обновленная задача", taskFromManager.getTitle());
        assertEquals(Status.IN_PROGRESS, taskFromManager.getStatus());
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание", Status.NEW);
        Task createdTask = manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/tasks/" + createdTask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testCreateTaskWithTimeConflict() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2024, 1, 15, 10, 0));
        String task1Json = gson.toJson(task1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/tasks");

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode());

        Task task2 = new Task("Задача 2", "Описание 2",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2024, 1, 15, 10, 30));

        String task2Json = gson.toJson(task2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task2Json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response2.statusCode());
    }

}