package com.rflbot.telegramBot;

import com.rflbot.handler.CallbackHandler;
import com.rflbot.service.MenuService;
import com.rflbot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
@RequiredArgsConstructor
public class RflBot implements LongPollingSingleThreadUpdateConsumer {

    private final UserService userService;
    private final MenuService menuService;
    private final CallbackHandler callbackHandler;

    @Override
    public void consume(Update update) {
        log.info(">>> ПОЛУЧЕНО ОБНОВЛЕНИЕ ОТ TELEGRAM! Update ID: {}, From: {}",
                update.getUpdateId(),
                update.hasMessage() ? update.getMessage().getFrom().getUserName() : "unknown");

        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();

            String text = update.getMessage().hasText() ? update.getMessage().getText().trim() : null;

            if ("/start".equals(text)) {
                log.info("Первый контакт от chatId: {}", chatId);
                userService.registerUser(chatId);
                menuService.sendStartButton(chatId);
                return;
            }
            log.info("Сообщение: '{}', chatId: {}", text, chatId);
        }

        if (update.hasCallbackQuery()) {
            callbackHandler.handle(update);
        }
    }
}
