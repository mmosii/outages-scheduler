package com.moonchase.outages_scheduler.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RescaleOp;

@Component
public class ImageEnhancer {
    @Autowired
    private ImageSaver imageSaver;

    private final Logger logger = LogManager.getLogger(ImageUrlCollector.class);

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public BufferedImage enhanceImage(BufferedImage image, String fileURL) throws Exception {
        this.logger.info("Початок покращення зображення " + fileURL);
        Mat src = Imgcodecs.imread(preprocessImage(image, fileURL), Imgcodecs.IMREAD_GRAYSCALE);
        Imgproc.resize(src, src, new Size(src.width() * 2, src.height() * 2), 0, 0, Imgproc.INTER_CUBIC);
        Mat contrastEnhanced = new Mat();
        Core.normalize(src, contrastEnhanced, 0, 255, Core.NORM_MINMAX);
        Imgproc.GaussianBlur(contrastEnhanced, contrastEnhanced, new Size(3, 3), 0);
        Imgproc.threshold(contrastEnhanced, contrastEnhanced, 128, 255, Imgproc.THRESH_BINARY);
        this.logger.info("Завершення покращення зображення " + fileURL);
        return matToBufferedImage(src);
    }

    private String preprocessImage(BufferedImage image, String fileURL) throws Exception {
        BufferedImage grayscale = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayscale.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        RescaleOp rescaleOp = new RescaleOp(1.0f, 0, null);
        rescaleOp.filter(grayscale, grayscale);

        BufferedImage binary = new BufferedImage(grayscale.getWidth(), grayscale.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2dBinary = binary.createGraphics();
        g2dBinary.drawImage(grayscale, 0, 0, null);
        g2dBinary.dispose();
        return this.imageSaver.saveImage(binary, fileURL);
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = (mat.channels() > 1) ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);
        return image;
    }
}
