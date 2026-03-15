package com.rflbot.service;

import com.rflbot.entity.User;
import com.rflbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SchedulerService schedulerService;
    private final MessageService messageService;
    private final MenuService menuService;

    public User registerUser(Long telegramId) {
        return userRepository.findById(telegramId)
                .orElseGet(() -> {
                    User user = User.builder()
                            .telegramId(telegramId)
                            .notificationTime(LocalTime.of(12, 0))
                            .timezone("Europe/Moscow")
                            .notificationsEnabled(true)
                            .streak(0)
                            .build();

                    log.info("Создаётся новый пользователь {} с timezone='{}'", telegramId, user.getTimezone());
                    return userRepository.save(user);
                });
    }

    public User getOrRegisterUser(Long telegramId) {
        User user = userRepository.findById(telegramId)
                .orElseGet(() -> registerUser(telegramId));

        fixTimezoneIfNeeded(user);

        return user;
    }

    public void claimReward(User user, Long chatId) {
        ZoneId zoneId = user.getTimezone() != null
                ? ZoneId.of(user.getTimezone())
                : ZoneId.of("Europe/Moscow");

        LocalDate today = LocalDate.now(zoneId);

        if (user.getLastClaimDate() != null && user.getLastClaimDate().equals(today)) {
            messageService.sendMessage(chatId, "Ты уже забрал награду сегодня! ✅");
            menuService.sendMainMenu(chatId);
            return;
        }

        if (user.getLastClaimDate() != null && user.getLastClaimDate().equals(today.minusDays(1))) {
            user.setStreak(user.getStreak() + 1);
        } else {
            user.setStreak(1);
        }

        user.setLastClaimDate(today);

        userRepository.save(user);

        messageService.sendMessage(chatId, String.format("Отлично! 🔥\nТвоя серия теперь: %d дней", user.getStreak()));
        menuService.sendMainMenu(chatId);
    }

    public void handleTimeSelection(User user, String data, Long chatId) {
        int hour = Integer.parseInt(data.substring(5));
        user.setNotificationTime(LocalTime.of(hour, 0));
        user.setNotificationsEnabled(true);
        userRepository.save(user);

        schedulerService.scheduleUser(user);
        messageService.sendMessage(chatId, "✅ Время уведомлений установлено на " + user.getNotificationTime());
        menuService.sendMainMenu(chatId);
    }

    public void disableNotifications(User user, Long chatId) {
        user.setNotificationsEnabled(false);
        userRepository.save(user);
        schedulerService.cancelUserSchedule(user.getTelegramId());
        messageService.sendMessage(chatId, "Уведомления выключены ❌");
        menuService.sendMainMenu(chatId);
    }

    public void showStreak(User user, Long chatId) {
        messageService.sendMessage(chatId, String.format("🔥 Текущая серия: %d дней\nПоследний клейм: %s",
                user.getStreak(),
                user.getLastClaimDate() != null ? user.getLastClaimDate() : "ещё не было"));
        menuService.sendMainMenu(chatId);
    }

    public void fixTimezoneIfNeeded(User user) {
        if (user.getTimezone() == null || user.getTimezone().isBlank()) {
            log.warn("Timezone null/пустой для пользователя {}, устанавливаем дефолт", user.getTelegramId());
            user.setTimezone("Europe/Moscow");
            userRepository.save(user);
        }
    }
}