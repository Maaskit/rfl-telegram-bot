package com.rflbot.commands;

import com.rflbot.entity.User;
import com.rflbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class ClaimCommand implements Command {

    private final UserService userService;

    @Override
    public boolean canHandle(Update update) {
        return update.hasCallbackQuery() && "CLAIM".equals(update.getCallbackQuery().getData());

    }

    @Override
    public void handle(Update update) {
        CallbackQuery callback = update.getCallbackQuery();
        Long chatId = callback.getMessage().getChatId();

        User user = userService.getOrRegisterUser(chatId);
        userService.claimReward(user, chatId);
    }
}
