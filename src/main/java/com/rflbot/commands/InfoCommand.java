package com.rflbot.commands;

import com.rflbot.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class InfoCommand implements Command {

    private final MenuService menuService;


    @Override
    public boolean canHandle(Update update) {
        return update.hasCallbackQuery() &&
                "INFO".equals(update.getCallbackQuery().getData());
    }

    @Override
    public void handle(Update update) {

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        menuService.sendInfoMessage(chatId);
    }
}
