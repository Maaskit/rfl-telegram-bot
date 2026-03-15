package com.rflbot.rflprojectbot.service;

import com.rflbot.entity.User;
import com.rflbot.repository.UserRepository;
import com.rflbot.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    private static final long TELEGRAM_ID = 123L;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScheduledFuture scheduledFuture;

    @InjectMocks
    private SchedulerService schedulerService;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .telegramId(TELEGRAM_ID)
                .notificationsEnabled(true)
                .notificationTime(LocalTime.of(10,30))
                .timezone("UTC")
                .build();
    }

    @Test
    void scheduleUser_shouldScheduleTask() {
        when(taskScheduler.schedule(any(Runnable.class), any(CronTrigger.class)))
                .thenReturn(scheduledFuture);

        schedulerService.scheduleUser(user);

        verify(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));
    }

    @Test
    void scheduleUser_shouldNotSchedule_whenNotificationsDisabled() {
        user.setNotificationsEnabled(false);

        schedulerService.scheduleUser(user);

        verify(taskScheduler, never())
                .schedule(any(Runnable.class), any(CronTrigger.class));
    }

    @Test
    void cancelUserSchedule_shouldCancelTask() {
        when(taskScheduler.schedule(any(Runnable.class), any(CronTrigger.class)))
                .thenReturn(scheduledFuture);

        schedulerService.scheduleUser(user);

        when(scheduledFuture.isDone()).thenReturn(false);

        schedulerService.cancelUserSchedule(user.getTelegramId());

        verify(scheduledFuture).cancel(false);
    }

    @Test
    void restoreSchedules_shouldScheduleForAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(taskScheduler.schedule(any(Runnable.class), any(CronTrigger.class)))
                .thenReturn(scheduledFuture);

        schedulerService.restoreSchedules();

        verify(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));
    }
}