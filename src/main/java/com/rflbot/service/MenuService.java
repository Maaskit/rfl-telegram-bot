package com.rflbot.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MessageService messageService;

    public void sendMainMenu(Long chatId) {
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Инфо ℹ️").callbackData("INFO").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Время оповещения ⏰").callbackData("SET_TIME").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Забрал награду ✅").callbackData("CLAIM").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Моя серия 🔥").callbackData("SHOW_STREAK").build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("Посетить бота \uD83C\uDF1A")
                                .url("https://t.me/rfl_pro_bot")
                                .build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Выключить уведомления ❌").callbackData("DISABLE").build()
                ))
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Главное меню:")
                .replyMarkup(keyboard)
                .build();

        messageService.executeSafely(message);
    }

    public void sendTimeSelection(Long chatId) {

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> keyboardBuilder = InlineKeyboardMarkup.builder();

        for (int h = 8; h <= 23; h++) {
            keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(String.format("%02d:00", h))
                            .callbackData("TIME_" + h)
                            .build()
            ));
        }

        keyboardBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("00:00")
                        .callbackData("TIME_0")
                        .build()
        ));

        InlineKeyboardMarkup keyboard = keyboardBuilder.build();

        SendMessage msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Выбери удобное время ежедневных напоминаний:")
                .replyMarkup(keyboard)
                .build();

        messageService.executeSafely(msg);

    }

    public void sendStartButton(Long chatId) {

        InlineKeyboardButton startButton = InlineKeyboardButton.builder()
                .text("🚀 Старт")
                .callbackData("START_MENU")
                .build();

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(startButton))
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Привет! 👋\n\n" +
                        "Я RFL-бот — буду напоминать тебе забирать ежедневную награду в канале.\n" +
                        "Нажми кнопку ниже, чтобы начать!")
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();

        messageService.executeSafely(message);
    }

    public void sendInfoMessage(Long chatId) {
        String infoText = """
                <b>  Немного математики</b>

                В CS2 дроп падает раз в неделю за ~0,03$ и для этого нужно играть 😴
                В Telegram-боте ты получаешь <b>100 кристаллов ежедневно</b> без лишних часов в игре.

                Копишь 5 дней → открываешь кейс за 500 кристаллов (минимум 3 цента).

                Ольга Владимировна посчитала 🧮:
                • За месяц в CS2 (в худшем случае) → 4 скина по 0,03$ = <b>0,12$</b>
                • В боте за месяц → 6 кейсов по 0,03$ = <b>0,18$</b>

                Разница копеечная, но шанс выбить MP5-SD | Юный некромант (0,12$) или AK-47 | Ледяной уголь (8,00$)
                есть всегда 🍀

                Мой бот будет <b>напоминать тебе каждый день</b>, чтобы ты не упустил свою награду 🔔
                """;

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("← \uD83C\uDFC3 Назад в меню")
                .callbackData("START_MENU")
                .build();

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(backButton))
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(infoText)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();

        messageService.executeSafely(message);
    }
}