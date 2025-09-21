package ru.practikum;

import ru.practikum.manager.Managers;
import ru.practikum.manager.TaskManager;
import ru.practikum.model.Epic;
import ru.practikum.model.Status;
import ru.practikum.model.Subtask;
import ru.practikum.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = manager.createTask(new Task("Задача 1", "Описание 1", Status.NEW,
                Duration.ofHours(2), LocalDateTime.of(2025,4,5,6,7)));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание 2", Status.NEW,
                Duration.ofHours(2), LocalDateTime.of(2025,4,5,6,7)));
        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание 1",
                Status.NEW, epic1.getId(), Duration.ofHours(2),
                LocalDateTime.of(2025,4,5,6,7)));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание 2",
                Status.NEW, epic1.getId(), Duration.ofHours(2),
                LocalDateTime.of(2025,4,5,6,7)));
        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Описание 3",
                Status.NEW, epic2.getId(), Duration.ofHours(2),
                LocalDateTime.of(2025,4,5,6,7)));

        System.out.println("=== Создание задач ===");
        printAllTasks(manager);

        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);
        manager.getTaskById(task1.getId());

        task2.setStatus(Status.DONE);
        manager.updateTask(task2);
        manager.getTaskById(task2.getId());

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.getSubtaskById(subtask1.getId());
        manager.getEpicById(epic1.getId());

        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);
        manager.getSubtaskById(subtask2.getId());

        subtask3.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask3);
        manager.getSubtaskById(subtask3.getId());
        manager.getEpicById(epic2.getId());

        System.out.println("\n=== После изменения статусов ===");
        printAllTasks(manager);

        System.out.println("\n=== Проверка статусов ===");
        System.out.println("Задача 1: " + task1.getStatus() + " (Ожидается IN_PROGRESS)");
        System.out.println("Задача 1: " + task2.getStatus() + " (Ожидается DONE)");
        System.out.println("Подзадача 1: " + subtask1.getStatus() + " (Ожидается DONE)");
        System.out.println("Подзадача 2: " + subtask2.getStatus() + " (Ожидается DONE)");
        System.out.println("Подзадача 2: " + subtask3.getStatus() + " (Ожидается IN_PROGRESS)");
        System.out.println("Эпик 1: " + epic1.getStatus() + " (Ожидается DONE)");
        System.out.println("Эпик 2: " + epic2.getStatus() + " (Ожидается IN_PROGRESS)");

        List<Task> history = manager.getHistory();
        System.out.println("\n=== История запросов ===");
        history.forEach(task -> System.out.println(task.getTitle()));

        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic1.getId());
        System.out.println("\n=== После удаления ===");
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        List<Task> tasks = manager.getAllTasks();
        for (Task task : tasks) {
            System.out.println(task.toString());
        }

        System.out.println("\nЭпики:");
        List<Epic> allEpics = manager.getAllEpics();
        for (Epic epic : allEpics) {
            System.out.println(epic);
            System.out.println("  Подзадачи:");
            List<Subtask> subtasks = manager.getSubtasksByEpicId(epic.getId());
            for (Subtask subtask : subtasks) {
                System.out.println("  - " + subtask);
            }
        }

        System.out.println("\nОтдельно все подзадачи:");
        List<Subtask> allSubtasks = manager.getAllSubtasks();
        for (Subtask subtask : allSubtasks) {
            System.out.println(subtask);
        }
    }
}