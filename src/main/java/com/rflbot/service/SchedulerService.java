package com.rflbot.service;

import com.rflbot.entity.User;
import com.rflbot.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchedulerService {

    private final TaskScheduler taskScheduler;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private final Map<Long, ScheduledFuture<?>> dailyTasks = new ConcurrentHashMap<>();

    public void scheduleUser(User user) {
        cancelUserSchedule(user.getTelegramId());

        if (!user.isNotificationsEnabled() || user.getNotificationTime() == null) {
            return;
        }

        String cron = String.format("0 %d %d * * ?", //нужно починить крон.
                user.getNotificationTime().getMinute(),
                user.getNotificationTime().getHour());

        Runnable dailyTask = () -> executeDailyReminder(user.getTelegramId());

        ScheduledFuture<?> future = taskScheduler.schedule(dailyTask, new CronTrigger(cron));
        dailyTasks.put(user.getTelegramId(), future);

        log.info("Уведомление запланировано для пользователя {} на {}:00",
                user.getTelegramId(), user.getNotificationTime().getHour());
    }

    public void cancelUserSchedule(Long telegramId) {
        ScheduledFuture<?> task = dailyTasks.remove(telegramId);
        if (task != null && !task.isDone()) {
            task.cancel(false);
            log.info("Расписание отменено для пользователя {}", telegramId);
        }
    }

    private void executeDailyReminder(Long telegramId) {
        User user = userRepository.findById(telegramId).orElse(null);
        if (user == null || !user.isNotificationsEnabled()) return;

        userRepository.save(user);

        notificationService.sendDailyReminder(user);
        scheduleSecondReminder(user);
    }

    private void scheduleSecondReminder(User user) {
        Instant secondTime = Instant.now().plus(2, ChronoUnit.HOURS);
        taskScheduler.schedule(() -> {
            User freshUser = userRepository.findById(user.getTelegramId()).orElse(null);
            if (freshUser == null) return;

            ZoneId zone = ZoneId.of(freshUser.getTimezone());
            LocalDate today = LocalDate.now(zone);

            if (!today.equals(freshUser.getLastClaimDate()) && freshUser.isNotificationsEnabled()) {
                notificationService.sendSecondReminder(freshUser);
            }
        }, secondTime);
    }

    @PostConstruct
    public void restoreSchedules() {
        log.info("Восстанавливаем расписания всех пользователей после перезапуска...");
        userRepository.findAll().forEach(this::scheduleUser);
    }
}