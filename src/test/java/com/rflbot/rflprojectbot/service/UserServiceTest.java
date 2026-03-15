package com.rflbot.rflprojectbot.service;

import com.rflbot.entity.User;
import com.rflbot.repository.UserRepository;
import com.rflbot.service.MenuService;
import com.rflbot.service.MessageService;
import com.rflbot.service.SchedulerService;
import com.rflbot.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final long TELEGRAM_ID = 123L;
    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    @Mock
    private UserRepository userRepository;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private MessageService messageService;
    @Mock
    private MenuService menuService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_shouldCreateNewUserWithDefaults() {
        when(userRepository.findById(TELEGRAM_ID)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.registerUser(TELEGRAM_ID);

        assertThat(result.getTelegramId()).isEqualTo(TELEGRAM_ID);
        assertThat(result.getTimezone()).isEqualTo("Europe/Moscow");
        assertThat(result.isNotificationsEnabled()).isTrue();
        assertThat(result.getStreak()).isZero();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void claimReward_shouldIncreaseStreak_whenYesterdayClaimed() {
        LocalDate yesterday = LocalDate.now(MOSCOW_ZONE).minusDays(1);
        User user = User.builder()
                .telegramId(TELEGRAM_ID)
                .lastClaimDate(yesterday)
                .streak(7)
                .timezone("Europe/Moscow")
                .build();

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.claimReward(user, TELEGRAM_ID);

        assertThat(user.getStreak()).isEqualTo(8);
        assertThat(user.getLastClaimDate()).isEqualTo(LocalDate.now(MOSCOW_ZONE));

        verifyMessageSentContaining("Твоя серия теперь: 8");
        verifyMainMenuSent();
        verify(userRepository).save(user);
    }

    @Test
    void claimReward_shouldNotIncreaseStreak_ifAlreadyClaimedToday() {
        LocalDate today = LocalDate.now(MOSCOW_ZONE);
        User user = User.builder()
                .telegramId(TELEGRAM_ID)
                .lastClaimDate(today)
                .streak(5)
                .timezone("Europe/Moscow")
                .build();

        userService.claimReward(user, TELEGRAM_ID);

        assertThat(user.getStreak()).isEqualTo(5);
        assertThat(user.getLastClaimDate()).isEqualTo(today);

        verifyMessageSentContaining("уже забрал награду сегодня");
        verifyMainMenuSent();
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleTimeSelection_shouldUpdateTimeAndSchedule() {
        User user = User.builder().telegramId(TELEGRAM_ID).build();
        String data = "TIME_15";

        userService.handleTimeSelection(user, data, TELEGRAM_ID);

        assertThat(user.getNotificationTime()).isEqualTo(LocalTime.of(15, 0));
        assertThat(user.isNotificationsEnabled()).isTrue();

        verify(userRepository).save(user);
        verify(schedulerService).scheduleUser(user);
        verifyMessageSentContaining("Время уведомлений установлено на 15:00");
        verifyMainMenuSent();
    }

    @Test
    void disableNotifications_shouldDisableAndCancelSchedule() {
        User user = User.builder().telegramId(TELEGRAM_ID).notificationsEnabled(true).build();

        userService.disableNotifications(user, TELEGRAM_ID);

        assertThat(user.isNotificationsEnabled()).isFalse();
        verify(userRepository).save(user);
        verify(schedulerService).cancelUserSchedule(TELEGRAM_ID);
        verifyMessageSentContaining("Уведомления выключены");
        verifyMainMenuSent();
    }

    @Test
    void showStreak_shouldShowCurrentStreak() {
        User user = User.builder()
                .streak(12)
                .lastClaimDate(LocalDate.now().minusDays(1))
                .build();

        userService.showStreak(user, TELEGRAM_ID);

        verifyMessageSentContaining("Текущая серия: 12 дней");
        verifyMainMenuSent();
    }

    @Test
    void fixTimezoneIfNeeded_shouldSetDefaultWhenNull() {
        User user = User.builder().telegramId(TELEGRAM_ID).timezone(null).build();

        userService.fixTimezoneIfNeeded(user);

        assertThat(user.getTimezone()).isEqualTo("Europe/Moscow");
        verify(userRepository).save(user);
    }

    @Test
    void fixTimezoneIfNeeded_shouldDoNothingWhenTimezoneExists() {
        User user = User.builder().telegramId(TELEGRAM_ID).timezone("Asia/Tokyo").build();

        userService.fixTimezoneIfNeeded(user);

        assertThat(user.getTimezone()).isEqualTo("Asia/Tokyo");
        verify(userRepository, never()).save(any());
    }

    private void verifyMainMenuSent() {
        verify(menuService).sendMainMenu(TELEGRAM_ID);
    }

    private void verifyMessageSentContaining(String text) {
        verify(messageService).sendMessage(eq(TELEGRAM_ID), contains(text));
    }
}