package com.rflbot.rflprojectbot.service;

import com.rflbot.entity.User;
import com.rflbot.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String URL = "https://t.me/rfl_pro_bot";
    private static final long TELEGRAM_ID = 123456789L;

    @Mock
    private TelegramClient telegramClient;

    private NotificationService notificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(telegramClient, URL);

        testUser = User.builder()
                .telegramId(TELEGRAM_ID)
                .build();
    }

    @Test
    void sendDailyReminder_shouldSendMessageWithTwoButtons() throws TelegramApiException {

        notificationService.sendDailyReminder(testUser);

        SendMessage sentMessage = captureSentMessage();

        assertThat(sentMessage.getChatId()).isEqualTo(String.valueOf(TELEGRAM_ID));
        assertThat(sentMessage.getText()).contains("Время забирать ежедневную награду");
        assertThat(sentMessage.getParseMode()).isEqualTo("HTML");

        List<InlineKeyboardButton> buttons = extractButtons(sentMessage);

        assertThat(buttons).hasSize(2);
        assertThat(buttons.get(0).getText()).isEqualTo("✅ Я забрал награду");
        assertThat(buttons.get(0).getCallbackData()).isEqualTo("CLAIM");
        assertThat(buttons.get(1).getText()).contains("Перейти в канал");
        assertThat(buttons.get(1).getUrl()).isEqualTo(URL);
    }


    @Test
    void sendSecondReminder_shouldSendMessageWithOneUrlButton() throws TelegramApiException {
        notificationService.sendSecondReminder(testUser);

        SendMessage sentMessage = captureSentMessage();

        assertThat(sentMessage.getChatId()).isEqualTo(String.valueOf(TELEGRAM_ID));
        assertThat(sentMessage.getText()).contains("Повторное напоминание");
        assertThat(sentMessage.getParseMode()).isEqualTo("HTML");

        List<InlineKeyboardButton> buttons = extractButtons(sentMessage);

        assertThat(buttons).hasSize(1);
        assertThat(buttons.get(0).getText()).contains("Перейти в канал");
        assertThat(buttons.get(0).getUrl()).isEqualTo(URL);
    }

    @Test
    void shouldNotThrow_whenTelegramFails() throws TelegramApiException {

        doThrow(new TelegramApiException("Test exception"))
                .when(telegramClient).execute(any(SendMessage.class));

        notificationService.sendDailyReminder(testUser);

        verify(telegramClient).execute(any(SendMessage.class));
    }

    private SendMessage captureSentMessage() throws TelegramApiException {
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        verify(telegramClient).execute(captor.capture());
        return captor.getValue();
    }

    private List<InlineKeyboardButton> extractButtons(SendMessage message) {
        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) message.getReplyMarkup();

        return markup.getKeyboard().get(0);
    }
}