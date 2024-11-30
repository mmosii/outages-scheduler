package com.moonchase.outages_scheduler.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
public class ImageSaver {
    private final Logger logger = LogManager.getLogger(ImageSaver.class);

    public String saveImage(BufferedImage image, String fileURL) throws IOException {
        File resourcesDir = new File("src/main/resources/images");
        if (!resourcesDir.exists()) {
            resourcesDir.mkdirs();
        }
        File outputFile = new File(resourcesDir, extractFilePath(fileURL));
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
