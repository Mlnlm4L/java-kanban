package ru.practikum.api;

import org.junit.jupiter.api.Test;
import ru.practikum.model.Epic;
import ru.practikum.model.Status;
import ru.practikum.model.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubtasksHandlerTest extends BaseHttpTest {

    private Epic createTestEpic() {
        return manager.createEpic(new Epic("Эпик", "Описание"));
    }

    @Test
    void testGetAllSubtasksWhenEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = createTestEpic();

        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи",
                Status.NEW, epic.getId(), null, null);
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("Подзадача", subtasks.get(0).getTitle());
        assertEquals(epic.getId(), subtasks.get(0).getEpicId());
    }

    @Test
    void testCreateSubtaskWithInvalidEpic() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Подзадача", "Описание",
                Status.NEW, 999, null, null);
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        Subtask subtask = new Subtask("Подзадача", "Описание",
                Status.NEW, epic.getId(), null, null);
        Subtask createdSubtask = manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/subtasks/" + createdSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask responseSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(responseSubtask);
        assertEquals(createdSubtask.getId(), responseSubtask.getId());
        assertEquals("Подзадача", responseSubtask.getTitle());
        assertEquals(epic.getId(), responseSubtask.getEpicId());
    }

    @Test
    void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        Subtask subtask = new Subtask("Подзадача", "Описание",
                Status.NEW, epic.getId(), null, null);
        Subtask createdSubtask = manager.createSubtask(subtask);

        Subtask updatedSubtask = new Subtask("Обновленная подзадача", "Обновленное описание",
                Status.IN_PROGRESS, epic.getId(), null, null);
        updatedSubtask.setId(createdSubtask.getId());
        String updatedSubtaskJson = gson.toJson(updatedSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtaskFromManager = manager.getSubtaskById(createdSubtask.getId());
        assertEquals("Обновленная подзадача", subtaskFromManager.getTitle());
        assertEquals(Status.IN_PROGRESS, subtaskFromManager.getStatus());
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = createTestEpic();
        Subtask subtask = new Subtask("Подзадача", "Описание",
                Status.NEW, epic.getId(), null, null);
        Subtask createdSubtask = manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/subtasks/" + createdSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = manager.getAllSubtasks();
        assertTrue(subtasks.isEmpty());
    }

    @Test
    void testCreateSubtaskWithTimeConflict() throws IOException, InterruptedException {
        Epic epic = createTestEpic();

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1",
                Status.NEW, epic.getId(),
                java.time.Duration.ofMinutes(60),
                java.time.LocalDateTime.of(2024, 1, 15, 10, 0));
        String subtask1Json = gson.toJson(subtask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/subtasks");

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtask1Json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode());

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2",
                Status.NEW, epic.getId(),
                java.time.Duration.ofMinutes(30),
                java.time.LocalDateTime.of(2024, 1, 15, 10, 30));

        String subtask2Json = gson.toJson(subtask2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtask2Json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response2.statusCode());
    }
}