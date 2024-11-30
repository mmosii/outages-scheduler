package com.moonchase.outages_scheduler.util;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class OCRProcessor {
    @Autowired
    private ImageEnhancer imageEnhancer;

    @Autowired
    private ImageUrlCollector imageUrlCollector;

    private final Logger logger = LogManager.getLogger(OCRProcessor.class);

    public List<String> processImageFromURL(String baseUrl) throws Exception {
        List<String> results = new ArrayList<>();
        for (String imageUrl : this.imageUrlCollector.getImageUrls(baseUrl)) {
            try (InputStream input = new URL(imageUrl).openStream()) {
                BufferedImage enhancedImage = this.imageEnhancer.enhanceImage(ImageIO.read(input), imageUrl);

                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
                tesseract.setLanguage("ukr");
                tesseract.setVariable("tessedit_char_whitelist", "0123456789енергіяїЕНЕРГІЯЇ");
                tesseract.setOcrEngineMode(1);
                tesseract.setPageSegMode(2);

                try {
                    this.logger.info("OCR зображення " + imageUrl);
                    String result = tesseract.doOCR(enhancedImage);
                    result = cleanOCRResult(result);
                    results.add(result);
                } catch (TesseractException e) {
                    this.logger.error("OCR зображення " + imageUrl + " не вдалось. " + e.getMessage());
                }
            } catch (IOException e) {
                this.logger.error("Не вдалось отримати зображення з " + imageUrl + e.getMessage());
            }
        }
        return results;
    }

    private String cleanOCRResult(String ocrResult) {
        return ocrResult.replaceAll("[^\\p{L}\\p{Nd}\\s]", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }
}
