package ru.practikum.manager;

import ru.practikum.exception.TaskTimeConflictException;
import ru.practikum.model.Status;
import ru.practikum.model.Task;
import ru.practikum.model.Epic;
import ru.practikum.model.Subtask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final TreeSet<Task> sortedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getType).thenComparing(Task::getId));

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            sortedTasks.add(task);
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(sortedTasks);
    }

    private boolean hasTimeConflictWith(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null ||
                task1.getDuration() == null || task2.getDuration() == null) {
            return false;
        }
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private boolean isOverlappingWithAny(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return false;
        }
        for (Task existingTask : sortedTasks) {
            if (!existingTask.equals(task) && hasTimeConflictWith(task, existingTask)) {
                return true;
            }
        }
        return false;
    }

    protected int generateId() {
        return nextId++;
    }

    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Status> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Subtask::getStatus)
                .toList();

        if (statuses.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = statuses.stream().allMatch(Status.NEW::equals);
        boolean allDone = statuses.stream().allMatch(Status.DONE::equals);

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStartTime = null;
        LocalDateTime latestEndTime = null;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }
            if (subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
            if (subtask.getStartTime() != null) {
                if (earliestStartTime == null || subtask.getStartTime().isBefore(earliestStartTime)) {
                    earliestStartTime = subtask.getStartTime();
                }
            }
            LocalDateTime subtaskEndTime = subtask.getEndTime();
            if (subtaskEndTime != null) {
                if (latestEndTime == null || subtaskEndTime.isAfter(latestEndTime)) {
                    latestEndTime = subtaskEndTime;
                }
            }
        }

        epic.setDuration(totalDuration);
        epic.setStartTime(earliestStartTime);
        epic.setEndTime(latestEndTime);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(sortedTasks::remove);
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Task createTask(Task task) {
        if (isOverlappingWithAny(task)) {
            throw new TaskTimeConflictException("Задача '" + task.getTitle() +
                    "' пересекается по времени с существующей задачей");
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task existingTask = tasks.get(task.getId());
            if (isOverlappingWithAny(task)) {
                addToPrioritized(existingTask);
                throw new TaskTimeConflictException("Задача '" + task.getTitle() +
                        "' пересекается по времени с существующей задачей");
            }
            sortedTasks.remove(existingTask);
            tasks.put(task.getId(), task);
            addToPrioritized(task);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            sortedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        subtasks.values().forEach(sortedTasks::remove);
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            epic.getSubtaskIds().forEach(historyManager::remove);
        });
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic savedEpic = epics.get(epic.getId());
        if (savedEpic != null) {
            savedEpic.setTitle(epic.getTitle());
            savedEpic.setDescription(epic.getDescription());
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            sortedTasks.remove(epic);
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    sortedTasks.remove(subtask);
                }
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(sortedTasks::remove);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
        });
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            return null;
        }
        if (isOverlappingWithAny(subtask)) {
            throw new TaskTimeConflictException("Подзадача '" + subtask.getTitle() +
                    "' пересекается по времени с существующей задачей");
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epics.get(epicId).addSubtaskId(subtask.getId());
        addToPrioritized(subtask);
        updateEpicStatus(epicId);
        updateEpicTime(epicId);
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        Subtask savedSubtask = subtasks.get(subtaskId);
        if (savedSubtask == null) {
            return;
        }
        int epicId = savedSubtask.getEpicId();
        if (isOverlappingWithAny(subtask)) {
            throw new TaskTimeConflictException("Подзадача '" + subtask.getTitle() +
                    "' пересекается по времени с существующей задачей");
        }
        sortedTasks.remove(savedSubtask);
        subtasks.put(subtaskId, subtask);
        addToPrioritized(subtask);
        updateEpicStatus(epicId);
        updateEpicTime(epicId);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            sortedTasks.remove(subtask);
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epicId);
                updateEpicTime(epicId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        return Optional.ofNullable(epics.get(epicId))
                .map(Epic::getSubtaskIds)
                .orElse(Collections.emptyList())
                .stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}