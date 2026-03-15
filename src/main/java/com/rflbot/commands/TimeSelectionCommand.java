package com.rflbot.commands;

import com.rflbot.entity.User;
import com.rflbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TimeSelectionCommand implements Command {

    private final UserService userService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasCallbackQuery() &&
                update.getCallbackQuery().getData() != null &&
                update.getCallbackQuery().getData().startsWith("TIME_");
    }

    @Override
    public void handle(Update update) {

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = userService.getOrRegisterUser(chatId);
        String data = update.getCallbackQuery().getData();

        userService.handleTimeSelection(user, data, chatId);

    }
}