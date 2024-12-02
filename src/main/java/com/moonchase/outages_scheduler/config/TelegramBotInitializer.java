package com.moonchase.outages_scheduler.config;

import com.moonchase.outages_scheduler.service.TelegramBotService;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class TelegramBotInitializer {
    private final Logger logger = LogManager.getLogger(TelegramBotInitializer.class);
    private final TelegramBotService telegramBotService;

    public TelegramBotInitializer(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
            this.logger.info("Бот зареєстрований і готовий до дій");
        } catch (TelegramApiException e) {
            this.logger.error("Помилка при реєстрації телеграм бота: " + e.getMessage());
        }
    }
}
