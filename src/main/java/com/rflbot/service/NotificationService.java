package com.rflbot.service;

import com.rflbot.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Slf4j
public class NotificationService {

    private final TelegramClient telegramClient;
    private final String channelUrl;

    public NotificationService(TelegramClient telegramClient,
                               @Value("${telegram.channel-url}") String channelUrl
    ) {
        this.telegramClient = telegramClient;
        this.channelUrl = channelUrl;
    }

    public void sendDailyReminder(User user) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("✅ Я забрал награду")
                                .callbackData("CLAIM")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("Перейти в канал\uD83C\uDFC3\u200D➡\uFE0F")
                                .url(channelUrl)
                                .build()
                ))
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(user.getTelegramId().toString())
                .text("🔔 <b>Время забирать ежедневную награду!</b>\n\n" +
                        "Зайди в канал и забери прямо сейчас 👇")
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        executeSafely(message);
    }

    public void sendSecondReminder(User user) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("✅ Перейти в канал и забрать награду 👇")
                                .url(channelUrl)
                                .build()
                ))
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(user.getTelegramId().toString())
                .text("⏰ <b>Повторное напоминание</b>\n\n" +
                        "Ты ещё не забрал сегодняшнюю награду? Сделай это сейчас 👇")
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        executeSafely(message);
    }

    private void executeSafely(SendMessage message) {
        log.info("Отправка сообщения chatId={}, текст='{}'", message.getChatId(), message.getText());

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение пользователю {}", message.getChatId(), e);
        }
    }
}