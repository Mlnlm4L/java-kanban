package ru.practikum.model;

import org.junit.jupiter.api.Test;
import ru.practikum.manager.Managers;
import ru.practikum.manager.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание 2", Status.IN_PROGRESS);
        task2.setId(1);
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    @Test
    void taskShouldNotChangeWhenAddedToManager() {
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Задача", "Описание", Status.NEW);
        task.setId(1);
        Task newTask = manager.createTask(task);
        assertEquals(task.getTitle(), newTask.getTitle(), "Название задачи не должно изменяться");
        assertEquals(task.getDescription(), newTask.getDescription(),
                "Описание задачи не должно изменяться");
        assertEquals(task.getStatus(), newTask.getStatus(), "Статус задачи не должен изменяться");
        assertEquals(task.getId(), newTask.getId(), "Id задачи не должен изменяться");
    }
}