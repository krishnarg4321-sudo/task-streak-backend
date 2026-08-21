package com.taskstreak.service;

import com.taskstreak.dto.TaskDtos.CreateTaskRequest;
import com.taskstreak.dto.TaskDtos.HistoryResponse;
import com.taskstreak.dto.TaskDtos.UpdateTaskStatusRequest;
import com.taskstreak.dto.TaskDtos.UpdateTaskTimerRequest;
import com.taskstreak.model.ChecklistItem;
import com.taskstreak.model.Friendship;
import com.taskstreak.model.NotificationEvent.NotificationType;
import com.taskstreak.model.Task;
import com.taskstreak.model.Task.TaskStatus;
import com.taskstreak.model.User;
import com.taskstreak.repository.FriendshipRepository;
import com.taskstreak.repository.TaskRepository;
import com.taskstreak.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       FriendshipRepository friendshipRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.notificationService = notificationService;
    }

    public Task createTask(String userId, CreateTaskRequest request) {
        Task task = new Task();
        task.setUserId(userId);
        task.setName(request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : "Untitled Task");
        task.setDescription(request.getDescription());
        
        String dateStr = request.getDate() != null && !request.getDate().isBlank() 
                ? request.getDate() 
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        task.setDate(dateStr);
        
        task.setColor(request.getColor() != null ? request.getColor() : "yellow");
        task.setFace(request.getFace() != null ? request.getFace() : "wink");
        task.setStatus(TaskStatus.NOT_COMPLETED);
        
        if (request.getChecklist() != null) {
            task.setChecklist(request.getChecklist());
        }

        Task saved = taskRepository.save(task);

        // Check if attendance milestone triggered
        long daysTasksCount = taskRepository.countByUserIdAndDate(userId, dateStr);
        if (daysTasksCount == 1) {
            // First task of the day!
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                // Streak check
                Map<String, Object> payload = new HashMap<>();
                payload.put("taskName", saved.getName());
                payload.put("name", user.getName());
            }
        }

        return saved;
    }

    public List<Task> getTodayTasks(String userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return taskRepository.findByUserIdAndDate(userId, today);
    }

    public List<Task> getTasksByDate(String userId, String date) {
        return taskRepository.findByUserIdAndDate(userId, date);
    }

    public Task getTaskById(String taskId, String userId) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    public Task updateTaskStatus(String taskId, String userId, UpdateTaskStatusRequest request) {
        Task task = getTaskById(taskId, userId);
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(request.getStatus());

        if (request.getStatus() == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            task.setCompletedAt(Instant.now());
            
            // Send celebration notification to the user
            Map<String, Object> payload = new HashMap<>();
            payload.put("taskName", task.getName());
            long minutes = task.getTimeSpentSeconds() / 60;
            long secs = task.getTimeSpentSeconds() % 60;
            payload.put("duration", minutes > 0 ? minutes + "m " + secs + "s" : secs + "s");
            
            notificationService.createAndSendNotification(userId, NotificationType.TASK_COMPLETED, payload);

            // Notify friends of completion
            notifyFriendsOfCompletion(userId, task.getName());
        }

        return taskRepository.save(task);
    }

    public Task updateTaskTimer(String taskId, String userId, UpdateTaskTimerRequest request) {
        Task task = getTaskById(taskId, userId);
        
        if (request.getTotalTimeSpentSeconds() != null) {
            task.setTimeSpentSeconds(request.getTotalTimeSpentSeconds());
        } else if (request.getAdditionalSeconds() > 0) {
            task.setTimeSpentSeconds(task.getTimeSpentSeconds() + request.getAdditionalSeconds());
        }

        if ("finish".equalsIgnoreCase(request.getAction())) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(Instant.now());
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("taskName", task.getName());
            long minutes = task.getTimeSpentSeconds() / 60;
            long secs = task.getTimeSpentSeconds() % 60;
            payload.put("duration", minutes > 0 ? minutes + "m " + secs + "s" : secs + "s");
            notificationService.createAndSendNotification(userId, NotificationType.TASK_COMPLETED, payload);
            notifyFriendsOfCompletion(userId, task.getName());
        } else if ("start".equalsIgnoreCase(request.getAction())) {
            if (task.getStatus() == TaskStatus.NOT_COMPLETED) {
                task.setStatus(TaskStatus.IN_PROGRESS);
            }
        }

        return taskRepository.save(task);
    }

    public Task updateChecklist(String taskId, String userId, List<ChecklistItem> checklist) {
        Task task = getTaskById(taskId, userId);
        task.setChecklist(checklist);
        
        // Auto calculate status if all checklist items done
        if (checklist != null && !checklist.isEmpty()) {
            boolean allDone = checklist.stream().allMatch(ChecklistItem::isDone);
            boolean anyDone = checklist.stream().anyMatch(ChecklistItem::isDone);
            if (allDone) {
                task.setStatus(TaskStatus.COMPLETED);
                task.setCompletedAt(Instant.now());
            } else if (anyDone) {
                task.setStatus(TaskStatus.PARTIALLY_COMPLETED);
            }
        }

        return taskRepository.save(task);
    }

    public void deleteTask(String taskId, String userId) {
        Task task = getTaskById(taskId, userId);
        taskRepository.delete(task);
    }

    public HistoryResponse getHistory(String userId, String range) {
        int days = "month".equalsIgnoreCase(range) ? 30 : 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        List<Task> tasks = taskRepository.findByUserIdAndDateBetween(userId, startDate.format(fmt), endDate.format(fmt));

        // Group by date for trend line
        Map<String, List<Task>> byDate = tasks.stream().collect(Collectors.groupingBy(Task::getDate));

        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate cur = startDate;
        while (!cur.isAfter(endDate)) {
            String d = cur.format(fmt);
            List<Task> dayTasks = byDate.getOrDefault(d, Collections.emptyList());
            long completed = dayTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            long partial = dayTasks.stream().filter(t -> t.getStatus() == TaskStatus.PARTIALLY_COMPLETED).count();
            long notCompleted = dayTasks.stream().filter(t -> t.getStatus() == TaskStatus.NOT_COMPLETED || t.getStatus() == TaskStatus.IN_PROGRESS).count();
            long total = dayTasks.size();
            double rate = total > 0 ? ((completed + 0.5 * partial) / total) * 100.0 : 0.0;

            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", d);
            dayMap.put("dayOfWeek", cur.getDayOfWeek().name().substring(0, 3));
            dayMap.put("total", total);
            dayMap.put("completed", completed);
            dayMap.put("partial", partial);
            dayMap.put("notCompleted", notCompleted);
            dayMap.put("completionRate", Math.round(rate * 10.0) / 10.0);
            trend.add(dayMap);

            cur = cur.plusDays(1);
        }

        // Today summary
        String todayStr = endDate.format(fmt);
        List<Task> todayTasks = byDate.getOrDefault(todayStr, Collections.emptyList());
        int todayCompleted = (int) todayTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        int todayPartial = (int) todayTasks.stream().filter(t -> t.getStatus() == TaskStatus.PARTIALLY_COMPLETED).count();
        int todayNotCompleted = (int) todayTasks.stream().filter(t -> t.getStatus() == TaskStatus.NOT_COMPLETED || t.getStatus() == TaskStatus.IN_PROGRESS).count();
        int todayTotal = todayTasks.size();
        long todayTimeSpent = todayTasks.stream().mapToLong(Task::getTimeSpentSeconds).sum();

        Map<String, Object> todaySummary = new HashMap<>();
        todaySummary.put("total", todayTotal);
        todaySummary.put("completed", todayCompleted);
        todaySummary.put("partial", todayPartial);
        todaySummary.put("notCompleted", todayNotCompleted);
        todaySummary.put("timeSpentSeconds", todayTimeSpent);
        todaySummary.put("completionRate", todayTotal > 0 ? Math.round(((todayCompleted + 0.5 * todayPartial) / todayTotal) * 1000.0) / 10.0 : 0.0);

        long totalTimeSpent = tasks.stream().mapToLong(Task::getTimeSpentSeconds).sum();
        double avgTimeSpent = tasks.isEmpty() ? 0 : (double) totalTimeSpent / tasks.size();

        HistoryResponse resp = new HistoryResponse();
        resp.setTodaySummary(todaySummary);
        resp.setTrend(trend);
        resp.setTasks(tasks);
        resp.setTotalTasks(tasks.size());
        resp.setCompletedTasks((int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());
        resp.setPartiallyCompletedTasks((int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.PARTIALLY_COMPLETED).count());
        resp.setNotCompletedTasks((int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.NOT_COMPLETED || t.getStatus() == TaskStatus.IN_PROGRESS).count());
        resp.setTotalTimeSpentSeconds(totalTimeSpent);
        resp.setAverageTimeSpentSeconds(Math.round(avgTimeSpent * 10.0) / 10.0);

        return resp;
    }

    private void notifyFriendsOfCompletion(String userId, String taskName) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        List<Friendship> friendships = friendshipRepository.findByUserIdOrFriendId(userId, userId);
        for (Friendship f : friendships) {
            if (f.getStatus() == Friendship.Status.ACCEPTED) {
                String friendId = f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId();
                Map<String, Object> payload = new HashMap<>();
                payload.put("friendName", user.getName());
                payload.put("taskName", taskName);
                notificationService.createAndSendNotification(friendId, NotificationType.FRIEND_TASK_COMPLETED, payload);
            }
        }
    }
}
