package com.rflbot.handler;

import com.rflbot.commands.Command;
import com.rflbot.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallbackHandler {

    private final List<Command> commands;
    private final MessageService messageService;

    public void handle(Update update) {
        log.info("CallbackHandler: Update ID {} получен", update.getUpdateId());

        if (update.hasCallbackQuery()) {
            messageService.deleteCallbackMessage(update.getCallbackQuery());
        }

        for (Command command : commands) {
            if (command.canHandle(update)) {
                command.handle(update);
                return;
            }
        }

        log.warn("Неизвестный callback: {}", update.getCallbackQuery() != null ?
                update.getCallbackQuery().getData() : "null");
    }
}