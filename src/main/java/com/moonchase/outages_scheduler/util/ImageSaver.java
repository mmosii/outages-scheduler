package com.moonchase.outages_scheduler.util;

import com.moonchase.outages_scheduler.service.TelegramBotService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Component
public class ImageSaver {
    @Autowired
    private TelegramBotService telegramBotService;

    private final Logger logger = LogManager.getLogger(ImageSaver.class);

    public String saveImage(BufferedImage image, String fileURL) throws Exception {
        File resourcesDir = new File("src/main/resources/images");
        if (!resourcesDir.exists()) {
            resourcesDir.mkdirs();
        }
        File outputFile = new File(resourcesDir, extractFilePath(fileURL));

        if (!outputFile.exists()) {
            String message = "Зображення відсутнє: " + outputFile.getAbsolutePath();
            telegramBotService.sendMessageToUsers();
            logger.warn(message);
        }

        final String absolutePath = outputFile.getAbsolutePath();
        ImageIO.write(image, "png", outputFile);
        this.logger.info("Зображення збережено: " + absolutePath);
        return absolutePath;
    }

    private String extractFilePath(String url) {
        String baseUrl = "https://api.loe.lviv.ua/media/";
        return url.substring(url.indexOf(baseUrl) + baseUrl.length());
    }
}
