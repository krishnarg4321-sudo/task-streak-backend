package com.taskstreak.config;

import com.taskstreak.model.*;
import com.taskstreak.repository.*;
import com.taskstreak.service.StreakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final FriendshipRepository friendshipRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;
    private final StreakService streakService;

    public DataSeeder(UserRepository userRepository, TaskRepository taskRepository,
                      FriendshipRepository friendshipRepository, GroupRepository groupRepository,
                      PasswordEncoder passwordEncoder, StreakService streakService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.friendshipRepository = friendshipRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
        this.streakService = streakService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial demo users and streak data...");

            String defaultPassword = passwordEncoder.encode("password123");

            User adomin = new User("Adomin", "adomin", "adomin@example.com", defaultPassword, "/avatars/avatar-1.svg");
            User sarah = new User("Sarah Jenkins", "sarah_fit", "sarah@example.com", defaultPassword, "/avatars/avatar-2.svg");
            User leo = new User("Leo Chen", "leo_design", "leo@example.com", defaultPassword, "/avatars/avatar-3.svg");
            User maya = new User("Maya Patel", "maya_code", "maya@example.com", defaultPassword, "/avatars/avatar-4.svg");

            adomin = userRepository.save(adomin);
            sarah = userRepository.save(sarah);
            leo = userRepository.save(leo);
            maya = userRepository.save(maya);

            // Friendships for Adomin
            friendshipRepository.save(new Friendship(adomin.getId(), sarah.getId(), Friendship.Status.ACCEPTED));
            friendshipRepository.save(new Friendship(adomin.getId(), leo.getId(), Friendship.Status.ACCEPTED));
            friendshipRepository.save(new Friendship(adomin.getId(), maya.getId(), Friendship.Status.ACCEPTED));

            // Seed tasks for past 7 days to give rich charts
            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
            LocalDate today = LocalDate.now();

            String[] colors = {"yellow", "blue", "pink", "green", "purple"};
            String[] faces = {"wink", "happy", "focused", "poker", "frown"};

            // Seed tasks for Adomin
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dStr = date.format(fmt);

                Task t1 = new Task();
                t1.setUserId(adomin.getId());
                t1.setName(i == 0 ? "Design Neo-Brutalist UI" : "Sprint Task #" + (7 - i));
                t1.setDescription("Implement clean outlines and pastel card surfaces");
                t1.setDate(dStr);
                t1.setColor(colors[i % colors.length]);
                t1.setFace(faces[i % faces.length]);
                t1.setStatus(i == 0 ? Task.TaskStatus.IN_PROGRESS : Task.TaskStatus.COMPLETED);
                t1.setTimeSpentSeconds(1500 + i * 300);
                
                List<ChecklistItem> cl = new ArrayList<>();
                cl.add(new ChecklistItem("c1", "Wireframe components", true));
                cl.add(new ChecklistItem("c2", "Test responsiveness", i != 0));
                t1.setChecklist(cl);
                taskRepository.save(t1);

                if (i <= 2) {
                    Task t2 = new Task();
                    t2.setUserId(adomin.getId());
                    t2.setName("Focus 3D Pomodoro Session");
                    t2.setDescription("Deep work block with hourglass timer");
                    t2.setDate(dStr);
                    t2.setColor(colors[(i + 1) % colors.length]);
                    t2.setFace("focused");
                    t2.setStatus(Task.TaskStatus.COMPLETED);
                    t2.setTimeSpentSeconds(1800);
                    taskRepository.save(t2);
                }
            }

            // Seed tasks for Friends (Sarah, Leo, Maya)
            List<User> friends = List.of(sarah, leo, maya);
            for (User friend : friends) {
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    String dStr = date.format(fmt);
                    Task ft = new Task();
                    ft.setUserId(friend.getId());
                    ft.setName("Daily " + friend.getName().split(" ")[0] + " Goal");
                    ft.setDate(dStr);
                    ft.setColor(colors[(friend.getName().length() + i) % colors.length]);
                    ft.setFace(faces[i % faces.length]);
                    ft.setStatus(Task.TaskStatus.COMPLETED);
                    ft.setTimeSpentSeconds(1200 + (long)(Math.random() * 1200));
                    taskRepository.save(ft);
                }
            }

            // Create Group: "Alpha Productivity Crew"
            Group group = new Group(
                    "Alpha Productivity Squad",
                    "Daily streak grinders and focus masters.",
                    adomin.getId(),
                    List.of(adomin.getId(), sarah.getId(), leo.getId(), maya.getId())
            );
            group = groupRepository.save(group);

            // Compute weekly ranking
            streakService.computeAndSaveWeeklyRanking(group.getId());
            log.info("Demo data seed completed successfully!");
        }
    }
}
