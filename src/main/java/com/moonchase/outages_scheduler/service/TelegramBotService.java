package com.moonchase.outages_scheduler.service;

import com.moonchase.outages_scheduler.util.EventHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramBotService extends TelegramLongPollingBot {
    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private EventHelper eventHelper;

    private final Logger logger = LogManager.getLogger(TelegramBotService.class);

    private final String botToken = "replaceitwithactualtokenplease";
    private final String botUsername = "dudewhereismycar_bot";


    private final ConcurrentHashMap<String, String> userChatMap = new ConcurrentHashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String chatId = String.valueOf(message.getChatId());
            String receivedText = message.getText();

            switch (receivedText) {
                case "/start":
                    sendMenu(chatId, "Вітаю! Оберіть одну з опцій:");
                    break;

                case "Активувати сповіщення":
                    sendMessage(chatId, "Сповіщення про зміни активовано.");
                    userChatMap.put(chatId, chatId);
                    break;

                case "Жидачів 3.1":
                    String scheduleOne;
                    try {
                        scheduleOne = eventHelper.formatEventsToMessage(eventHelper.getThreeOneEvents());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    sendMessage(chatId, scheduleOne);
                    break;

                case "Станиля 3.2":
                    String scheduleTwo;
                    try {
                        scheduleTwo = eventHelper.formatEventsToMessage(eventHelper.getThreeTwoEvents());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    sendMessage(chatId, scheduleTwo);
                    break;

                default:
                    sendMessage(chatId, "Команду не розпізнано. Використовуйте меню.");
                    break;
            }
        }
    }

    private void sendMenu(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("Активувати сповіщення"));
        row.add(new KeyboardButton("Жидачів 3.1"));
        row.add(new KeyboardButton("Станиля 3.2"));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            this.logger.error("Помилка при створенні меню: " + e.getMessage());
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            this.logger.error("Помилка при відправленні телеграм повідомлення: " + e.getMessage());
        }
    }

    @Async
    public void sendMessageToUsers() throws Exception {
        Thread.sleep(30000);

        String text = eventHelper.formatEventsToMessage(eventHelper.getThreeOneEvents()) + "\n" + eventHelper.formatEventsToMessage(eventHelper.getThreeTwoEvents());
        userChatMap.values().forEach(id -> sendMessage(id, text));
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
