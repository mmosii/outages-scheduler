package com.moonchase.outages_scheduler.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImageUrlCollector {
    private final Logger logger = LogManager.getLogger(ImageUrlCollector.class);

    private static final WebDriver driver;

    static {
        WebDriverManager.edgedriver().setup();  // Use Edge WebDriver
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--headless");  // Add headless option for Edge
        driver = new EdgeDriver(options);  // Initialize EdgeDriver
    }

    public List<String> getImageUrls(String pageUrl) {
        List<String> imageUrls = new ArrayList<>();

        try {
            driver.get(pageUrl);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("img")));

            List<WebElement> imageElements = driver.findElements(By.xpath("//img[@alt='grafic' or @alt='grafic-tom']"));

            for (WebElement image : imageElements) {
                String imageUrl = image.getAttribute("src");
                imageUrls.add(imageUrl);
                this.logger.info("Знайдено фото графіку: " + imageUrl);
            }
        } catch (Exception e) {
            this.logger.warn("Фото графіку не знайдено");
        }
        return imageUrls;
    }
}
