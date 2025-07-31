package ru.practikum.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void managersShouldBeInitializedAndReadyToWork() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован");
        assertNotNull(taskManager, "TaskManager должен быть проинициализирован");
        assertTrue(historyManager instanceof InMemoryHistoryManager,
                "Должен возвращаться InMemoryHistoryManager");
        assertTrue(taskManager instanceof InMemoryTaskManager, "Должен возвращаться InMemoryTaskManager");
    }
}