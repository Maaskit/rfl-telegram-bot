package com.rflbot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@Slf4j
public class TelegramBotConfig {

    @Bean
    public TelegramClient telegramClient(@Value("${telegram.bot.token}") String botToken) {
        log.info("Создан Client с токеном: {}...", botToken.substring(0, 8));
        return new OkHttpTelegramClient(botToken);
    }
}