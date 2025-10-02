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

class EpicsHandlerTest extends BaseHttpTest {

    @Test
    void testGetAllEpicsWhenEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epics = manager.getAllEpics();
        assertEquals(1, epics.size());
        assertEquals("Эпик", epics.get(0).getTitle());
        assertEquals("Описание", epics.get(0).getDescription());
    }

    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание");
        Epic createdEpic = manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/epics/" + createdEpic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic responseEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(responseEpic);
        assertEquals(createdEpic.getId(), responseEpic.getId());
        assertEquals("Эпик", responseEpic.getTitle());
    }

    @Test
    void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик для удаления", "Описание");
        Epic createdEpic = manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/epics/" + createdEpic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> epics = manager.getAllEpics();
        assertTrue(epics.isEmpty());
    }

    @Test
    void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.createSubtask(new Subtask("Подзадача 1", "Описание 1",
                Status.NEW, epic.getId(), null, null));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = gson.fromJson(response.body(), List.class);
        assertEquals(1, subtasks.size());
    }

    @Test
    void testGetEpicSubtasksEpicNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testCreateEpicWithEmptyBody() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        URI url = URI.create(getBaseUrl() + "/epics");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Тело запроса не может быть пустым"));
    }
}