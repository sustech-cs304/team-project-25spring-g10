package com.g10.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageEditUtils {

    // 调整对比度
    public static BufferedImage adjustContrast(BufferedImage image, double contrast) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double factor = (259 * (contrast + 255)) / (255 * (259 - contrast));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int red = (int) (factor * (color.getRed() - 128) + 128);
                int green = (int) (factor * (color.getGreen() - 128) + 128);
                int blue = (int) (factor * (color.getBlue() - 128) + 128);

                red = Math.max(0, Math.min(255, red));
                green = Math.max(0, Math.min(255, green));
                blue = Math.max(0, Math.min(255, blue));

                Color newColor = new Color(red, green, blue);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    // 调整亮度
    public static BufferedImage adjustBrightness(BufferedImage image, int brightness) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int red = color.getRed() + brightness;
                int green = color.getGreen() + brightness;
                int blue = color.getBlue() + brightness;

                red = Math.max(0, Math.min(255, red));
                green = Math.max(0, Math.min(255, green));
                blue = Math.max(0, Math.min(255, blue));

                Color newColor = new Color(red, green, blue);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    // 裁剪图片
    public static BufferedImage cropImage(BufferedImage image, int x, int y, int width, int height) {
        return image.getSubimage(x, y, width, height);
    }

    // 旋转图片
    public static BufferedImage rotateImage(BufferedImage image, int degrees) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        double rads = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));
        
        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);
        
        BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = result.createGraphics();
        
        g2d.translate((newWidth - width) / 2, (newHeight - height) / 2);
        g2d.rotate(rads, width / 2, height / 2);
        g2d.drawRenderedImage(image, null);
        g2d.dispose();
        
        return result;
    }

    // 调整饱和度
    public static BufferedImage adjustSaturation(BufferedImage image, float saturation) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                hsb[1] = Math.max(0, Math.min(1, hsb[1] * saturation)); // 调整饱和度
                Color newColor = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    // 添加黑白滤镜
    public static BufferedImage applyGrayscaleFilter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
                Color newColor = new Color(gray, gray, gray);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    // 添加复古滤镜
    public static BufferedImage applyVintageFilter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                
                int newRed = (int) (r * 0.9 + g * 0.1);
                int newGreen = (int) (g * 0.9 + b * 0.1);
                int newBlue = (int) (b * 0.9 + r * 0.1);
                
                newRed = Math.min(255, Math.max(0, newRed));
                newGreen = Math.min(255, Math.max(0, newGreen));
                newBlue = Math.min(255, Math.max(0, newBlue));
                
                Color newColor = new Color(newRed, newGreen, newBlue);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            // 读取图片
            File input = new File("input.jpg");
            BufferedImage image = ImageIO.read(input);

            // 调整对比度
            BufferedImage contrastAdjusted = adjustContrast(image, 50);
            File contrastOutput = new File("contrast_output.jpg");
            ImageIO.write(contrastAdjusted, "jpg", contrastOutput);

            // 调整亮度
            BufferedImage brightnessAdjusted = adjustBrightness(image, 50);
            File brightnessOutput = new File("brightness_output.jpg");
            ImageIO.write(brightnessAdjusted, "jpg", brightnessOutput);

            // 裁剪图片
            BufferedImage cropped = cropImage(image, 100, 100, 200, 200);
            File cropOutput = new File("crop_output.jpg");
            ImageIO.write(cropped, "jpg", cropOutput);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}    