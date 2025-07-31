package ru.practikum.manager;

import org.junit.jupiter.api.Test;
import ru.practikum.model.Status;
import ru.practikum.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    @Test
    void historyManagerShouldPreserveTaskState() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Задача", "Описание", Status.NEW);
        task.setId(1);
        historyManager.add(task);
        Task historyTask = historyManager.getHistory().getFirst();
        assertEquals(Status.NEW, historyTask.getStatus(), "История должна сохранять исходный статус");
        assertEquals("Задача", historyTask.getTitle(), "История должна сохранять исходное название");
        assertEquals("Описание", historyTask.getDescription(),
                "История должна сохранять исходное описание");
    }

    @Test
    void add() {
        Task task = new Task("Задача", "Описание", Status.NEW);
        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }
}
