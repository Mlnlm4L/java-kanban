import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = manager.createTask(new Task("Задача 1", "Описание 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание 2", Status.NEW));

        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание 1",
                Status.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание 2",
                Status.NEW, epic1.getId()));

        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Описание 3",
                Status.NEW, epic2.getId()));

        //Печать начального состояния
        System.out.println("=== Создание задач ===");
        printAllTasks(manager);

        //Изменение статусов
        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);

        task2.setStatus(Status.DONE);
        manager.updateTask(task2);

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);

        subtask3.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask3);

        //Печать после изменений
        System.out.println("\n=== После изменения статусов ===");
        printAllTasks(manager);

        //Проверка статусов
        System.out.println("\n=== Проверка статусов ===");
        System.out.println("Задача 1: " + task1.getStatus() + " (Ожидается IN_PROGRESS)");
        System.out.println("Задача 1: " + task2.getStatus() + " (Ожидается DONE)");
        System.out.println("Подзадача 1: " + subtask1.getStatus() + " (Ожидается DONE)");
        System.out.println("Подзадача 2: " + subtask2.getStatus() + " (Ожидается DONE)");
        System.out.println("Подзадача 2: " + subtask3.getStatus() + " (Ожидается IN_PROGRESS)");
        System.out.println("Эпик 1: " + epic1.getStatus() + " (Ожидается DONE)");
        System.out.println("Эпик 2: " + epic2.getStatus() + " (Ожидается IN_PROGRESS)");

        //Удаление
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic1.getId());
        System.out.println("\n=== После удаления ===");
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        List<Task> tasks = manager.getAllTasks();
        for (Task task : tasks) {
            System.out.println(task);
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