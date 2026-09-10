package com.rflbot.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final TelegramClient telegramClient;

    public void sendMessage(Long chatId, String text) {
        executeSafely(SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build());
    }

    public void executeSafely(SendMessage message) {
        log.info("Отправка сообщения chatId={}, текст='{}'", message.getChatId(), message.getText());
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения пользователю {}", message.getChatId(), e);
        }
    }

    public void deleteCallbackMessage(CallbackQuery callback) {
        try {
            telegramClient.execute(DeleteMessage.builder()
                    .chatId(callback.getMessage().getChatId().toString())
                    .messageId(callback.getMessage().getMessageId()).build());
            log.info("Удалено сообщение с кнопкой: chatId={}, messageId={}",
                    callback.getMessage().getChatId(),
                    callback.getMessage().getMessageId());
        } catch (TelegramApiException e) {
            log.warn("Не удалось удалить сообщение с callback: {}", e.getMessage());
        }
    }
}
