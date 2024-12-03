package com.moonchase.outages_scheduler.service;

import com.moonchase.outages_scheduler.util.EventHelper;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
    private final String userFilePath = "userChatMap.txt";

    private final ConcurrentHashMap<String, String> userChatMap = new ConcurrentHashMap<>();

    @PreDestroy
    public void onShutdown() {
        saveUserData();
        this.logger.info("Програма закривається. Дані про користувачів збережено.");
    }

    public TelegramBotService() {
        loadUserData();
    }

    public void saveUserData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFilePath))) {
            for (String chatId : userChatMap.keySet()) {
                String group = userChatMap.get(chatId);
                writer.write(chatId + "=" + group + "\n");
            }
            this.logger.info("Дані про користувачів збережено");
        } catch (IOException e) {
            this.logger.error("Помилка при збереженні даних про користувачів " + e.getMessage());
        }
    }

    public void loadUserData() {
        File file = new File(userFilePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String chatId = parts[0];
                        String group = parts[1];
                        userChatMap.put(chatId, group);
                    }
                }
                this.logger.info("Попередні користувачі завантажені");
            } catch (IOException e) {
                this.logger.error("Помилка при завантаженні користувачів " + e.getMessage());
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String chatId = String.valueOf(message.getChatId());
            String receivedText = message.getText();

            switch (receivedText) {
                case "/start":
                case "Назад в меню":
                    sendMenu(chatId);
                    break;

                case "Активувати сповіщення":
                    sendNotificationMenu(chatId);
                    break;

                case "Вимкнути сповіщення":
                    userChatMap.remove(chatId);
                    sendMessage(chatId, "Сповіщення вимкнено.");
                    break;

                case "Отримати графік":
                    sendOutagesMenu(chatId);
                    break;

                case "Підписатись на календар":
                    sendCalendarMenu(chatId);
                    break;
                case "Яка я група?":
                    sendGroupLink(chatId);
                    break;

                case "Групи 1.1":
                    try {
                        sendMessage(chatId, eventHelper.formatEventsToMessage(eventHelper.getOneOneEvents()));
                    } catch (IOException e) {
                        this.logger.error("Помилка при отриманні подій для 1.1: " + e.getMessage());
                    }
                    break;

                case "Групи 1.2":
                    try {
                        sendMessage(chatId, eventHelper.formatEventsToMessage(eventHelper.getOneTwoEvents()));
                    } catch (IOException e) {
                        this.logger.error("Помилка при отриманні подій для 1.2: " + e.getMessage());
                    }
                    break;

                case "Групи 2.1":
                    try {
                        sendMessage(chatId, eventHelper.formatEventsToMessage(eventHelper.getTwoOneEvents()));
                    } catch (IOException e) {
                        this.logger.error("Помилка при отриманні подій для 2.1: " + e.getMessage());
                    }
                    break;

                case "Групи 2.2":
                    try {
                        sendMessage(chatId, eventHelper.formatEventsToMessage(eventHelper.getTwoTwoEvents()));
                    } catch (IOException e) {
                        this.logger.error("Помилка при отриманні подій для 2.2: " + e.getMessage());
                    }
                    break;

                case "Групи 3.1":
                    try {
                        sendMessage(chatId, eventHelper.formatEventsToMessage(eventHelper.getThreeOneEvents()));
                    } catch (IOException e) {
                        this.logger.error("Помилка при отриманні подій для 3.1: " + e.getMessage());
                    }
                    break;

                case "Групи 3.2":
                    try {
                        sendMessage(chatId, eventHelper.formatEventsToMessage(eventHelper.getThreeTwoEvents()));
                    } catch (IOException e) {
                        this.logger.error("Помилка при отриманні подій для 3.2: " + e.getMessage());
                    }
                    break;

                case "Всіх груп":
                    try {
                        sendMessage(chatId, eventHelper.formatEventsToMessage(eventHelper.getOneOneEvents()) +
                                "\n\n" + eventHelper.formatEventsToMessage(eventHelper.getOneTwoEvents()) +
                                "\n\n" + eventHelper.formatEventsToMessage(eventHelper.getTwoOneEvents()) +
                                "\n\n" + eventHelper.formatEventsToMessage(eventHelper.getTwoTwoEvents()) +
                                "\n\n" + eventHelper.formatEventsToMessage(eventHelper.getThreeOneEvents()) +
                                "\n\n" + eventHelper.formatEventsToMessage(eventHelper.getThreeTwoEvents()));
                    } catch (IOException e) {
                        this.logger.error("Помилка при отриманні подій для всіх груп: " + e.getMessage());
                    }
                    break;

                case "1.1":
                case "1.2":
                case "2.1":
                case "2.2":
                case "3.1":
                case "3.2":
                    userChatMap.put(chatId, receivedText);
                    sendMessage(chatId, "Сповіщення для групи " + receivedText + " активовано.");
                    break;

                case "Всі групи":
                    userChatMap.put(chatId, "all");
                    sendMessage(chatId, "Сповіщення для всіх груп активовано.");
                    break;

                default:
                    sendMessage(chatId, "Команду не розпізнано. Використовуйте меню.");
                    break;
            }
        }
    }

    private void sendCalendarMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Підписатись на Google календар з актуальними відключеннями: ");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(createInlineButtonRow("Календар 1.1", "https://calendar.google.com/calendar/u/0?cid=Y2U0NzMzZjAwZTBiMjkzY2Y4YzU5Yjg3YzZmMDQ5YzMwYzJlMzljN2YzYjY4Mjg2MjE2NWQ5YTMxYWFhODYyMkBncm91cC5jYWxlbmRhci5nb29nbGUuY29t"));
        keyboard.add(createInlineButtonRow("Календар 1.2", "https://calendar.google.com/calendar/u/0?cid=ZWNlMDVmNWNmOTgxZjAwZmI2YzYyYzEzNWNlMTJjMGM0NGE4NTY4ODNlYTkyNzM5MmNlMmM0ZDFhYzE0MjQxMUBncm91cC5jYWxlbmRhci5nb29nbGUuY29t"));
        keyboard.add(createInlineButtonRow("Календар 2.1", "https://calendar.google.com/calendar/u/0?cid=ODEwMjNmNTFmNzE2NjdiZjdmNDFmYTlkNzIxNTc1YjYyOGMzNjA3Y2I2ZTRiOTg2M2VlNWQyNDc0YzA0ZmM0ZEBncm91cC5jYWxlbmRhci5nb29nbGUuY29t"));
        keyboard.add(createInlineButtonRow("Календар 2.2", "https://calendar.google.com/calendar/u/0?cid=ZDMwMjE4NjhjMTQ0MTZiNWNkNGUzNGJmY2VlMjliNDkwYTVmNzlkYzk4MjhlZTcxMGEzODVkNDg2YWUyMmI1YkBncm91cC5jYWxlbmRhci5nb29nbGUuY29t"));
        keyboard.add(createInlineButtonRow("Календар 3.1", "https://calendar.google.com/calendar/u/0?cid=MGY5NGVkYTAyMzc0MTFiNjgxN2U0NmRiMjhiMGZlMWZhNTY0NThlZjE1Y2JhNTVmMjFkMTA4NmM2NzFlZWJjOUBncm91cC5jYWxlbmRhci5nb29nbGUuY29t"));
        keyboard.add(createInlineButtonRow("Календар 3.2", "https://calendar.google.com/calendar/u/0?cid=YzA0MWVjOWRlNDdhMmFmMWM5NDQ4MzUzNTcyNGY5MDcyNDFmMWY0MWQ4YWU2YjI5MzUwMzQ2MmYzZGUwY2YyZkBncm91cC5jYWxlbmRhci5nb29nbGUuY29t"));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            this.logger.error("Помилка при створенні меню календарів: " + e.getMessage());
        }
    }

    private List<InlineKeyboardButton> createInlineButtonRow(String text, String url) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setUrl(url);
        row.add(button);
        return row;
    }

    private void sendOutagesMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Отримати актуальні відключення для: ");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Групи 1.1"));
        row1.add(new KeyboardButton("Групи 1.2"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Групи 2.1"));
        row2.add(new KeyboardButton("Групи 2.2"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Групи 3.1"));
        row3.add(new KeyboardButton("Групи 3.2"));

        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton("Всіх груп"));
        row4.add(new KeyboardButton("Назад в меню"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            this.logger.error("Помилка при створенні меню відключень: " + e.getMessage());
        }
    }

    private void sendMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вітаю! Оберіть одну з опцій:");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Активувати сповіщення"));
        row1.add(new KeyboardButton("Вимкнути сповіщення"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Отримати графік"));
        row2.add(new KeyboardButton("Яка я група?"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Підписатись на календар"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            this.logger.error("Помилка при створенні меню: " + e.getMessage());
        }
    }

    private void sendGroupLink(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Перейдіть за посиланням, щоб дізнатись, до якої групи ви належите:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();

        List<InlineKeyboardButton> urlRow = new ArrayList<>();
        InlineKeyboardButton urlButton = new InlineKeyboardButton();
        urlButton.setText("Яка я група?");
        urlButton.setUrl("https://poweron.loe.lviv.ua/shedule-off");
        urlRow.add(urlButton);
        inlineKeyboard.add(urlRow);

        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            this.logger.error("Помилка при відправленні посилання: " + e.getMessage());
        }
    }

    private void sendNotificationMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Оберіть групу для сповіщень:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("1.1"));
        row1.add(new KeyboardButton("1.2"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("2.1"));
        row2.add(new KeyboardButton("2.2"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("3.1"));
        row3.add(new KeyboardButton("3.2"));

        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton("Всі групи"));
        row4.add(new KeyboardButton("Назад в меню"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            this.logger.error("Помилка при створенні меню сповіщень: " + e.getMessage());
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
        Thread.sleep(40000);

        String eventsOneOne = eventHelper.formatEventsToMessage(eventHelper.getOneOneEvents());
        String eventsOneTwo = eventHelper.formatEventsToMessage(eventHelper.getOneTwoEvents());
        String eventsTwoOne = eventHelper.formatEventsToMessage(eventHelper.getTwoOneEvents());
        String eventsTwoTwo = eventHelper.formatEventsToMessage(eventHelper.getTwoTwoEvents());
        String eventsThreeOne = eventHelper.formatEventsToMessage(eventHelper.getThreeOneEvents());
        String eventsThreeTwo = eventHelper.formatEventsToMessage(eventHelper.getThreeTwoEvents());

        userChatMap.forEach((chatId, group) -> {
            String text = switch (group) {
                case "1.1" -> eventsOneOne;
                case "1.2" -> eventsOneTwo;
                case "2.1" -> eventsTwoOne;
                case "2.2" -> eventsTwoTwo;
                case "3.1" -> eventsThreeOne;
                case "3.2" -> eventsThreeTwo;
                case "all" -> String.join("\n\n",
                        eventsOneOne, eventsOneTwo,
                        eventsTwoOne, eventsTwoTwo,
                        eventsThreeOne, eventsThreeTwo);
                default -> "Немає доступних подій для вашої групи.";
            };
            sendMessage(chatId, text);
        });
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
