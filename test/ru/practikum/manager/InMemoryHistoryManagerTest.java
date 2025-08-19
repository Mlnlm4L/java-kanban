package ru.practikum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practikum.model.Status;
import ru.practikum.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void set() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Задача 1", "Описание 1", Status.NEW);
        task1.setId(1);
        task2 = new Task("Задача 2", "Описание 2", Status.IN_PROGRESS);
        task2.setId(2);
        task3 = new Task("Задача 3", "Описание 3", Status.DONE);
        task3.setId(3);
    }

    @Test
    void addShouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "История должна содержать 1 задачу");
        assertEquals(task1, history.get(0), "Задачи в истории должны совпадать");
    }

    @Test
    void addShouldNotAddDuplicateTasks() {
        historyManager.add(task1);
        historyManager.add(task1);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(),
                "История должна содержать только одну запись для дублирующихся задач");
    }

    @Test
    void addShouldMoveTaskToEndIfAlreadyExists() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "История должна содержать 3 задачи");
    }

    @Test
    void removeShouldDeleteTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи после удаления");
        assertFalse(history.contains(task2), "История не должна содержать удаленную задачу");
    }

    @Test
    void removeShouldHandleNonExistingTask() {
        historyManager.add(task1);
        historyManager.remove(999);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(),
                "История не должна измениться при удалении несуществующей задачи");
    }

    @Test
    void getHistoryShouldReturnEmptyListWhenEmpty() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void addShouldNotAddNullTask() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна остаться пустой после добавления null");
    }

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
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }
}
