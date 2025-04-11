package com.g10.utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageEditUtils {

    // 调整对比度
    public static BufferedImage adjustContrast(BufferedImage image, double contrast) {
        // 将前端的 -100 到 100 范围映射到实际的对比度值
        contrast = (contrast + 100) * 1.27; // 映射到 0-254 范围
        
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

        // 将前端的 -100 到 100 范围映射到实际的亮度调整值
        brightness = (int)(brightness * 1.27); // 映射到 -127 到 127 范围

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

    // 调整饱和度
    public static BufferedImage adjustSaturation(BufferedImage image, float saturation) {
        // 将前端的 -100 到 100 范围映射到实际的饱和度值
        saturation = (saturation + 100) / 100f; // 映射到 0-2 范围
        
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                hsb[1] = Math.max(0, Math.min(1, hsb[1] * saturation));
                Color newColor = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
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
        double rads = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);
        
        BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();
        
        g2d.translate((newWidth - width) / 2, (newHeight - height) / 2);
        g2d.rotate(rads, width / 2, height / 2);
        g2d.drawRenderedImage(image, null);
        g2d.dispose();
        
        return result;
    }

    // 水平翻转
    public static BufferedImage flipHorizontal(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();
        
        g2d.drawImage(image, width, 0, -width, height, null);
        g2d.dispose();
        
        return result;
    }

    // 垂直翻转
    public static BufferedImage flipVertical(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();
        
        g2d.drawImage(image, 0, height, width, -height, null);
        g2d.dispose();
        
        return result;
    }

    // 应用黑白滤镜
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

    // 应用怀旧滤镜
    public static BufferedImage applySepiaFilter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                
                int newRed = (int) (0.393 * red + 0.769 * green + 0.189 * blue);
                int newGreen = (int) (0.349 * red + 0.686 * green + 0.168 * blue);
                int newBlue = (int) (0.272 * red + 0.534 * green + 0.131 * blue);
                
                newRed = Math.min(255, newRed);
                newGreen = Math.min(255, newGreen);
                newBlue = Math.min(255, newBlue);
                
                Color newColor = new Color(newRed, newGreen, newBlue);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    // 应用暖色滤镜
    public static BufferedImage applyWarmFilter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int red = Math.min(255, (int)(color.getRed() * 1.1));
                int green = color.getGreen();
                int blue = Math.max(0, (int)(color.getBlue() * 0.9));
                
                Color newColor = new Color(red, green, blue);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    // 应用冷色滤镜
    public static BufferedImage applyCoolFilter(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int red = Math.max(0, (int)(color.getRed() * 0.9));
                int green = color.getGreen();
                int blue = Math.min(255, (int)(color.getBlue() * 1.1));
                
                Color newColor = new Color(red, green, blue);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    // 应用戏剧滤镜
    public static BufferedImage applyDramaticFilter(BufferedImage image) {
        BufferedImage result = adjustContrast(image, 50);
        return adjustSaturation(result, 20);
    }

    // 应用褪色滤镜
    public static BufferedImage applyFadeFilter(BufferedImage image) {
        BufferedImage result = adjustBrightness(image, 10);
        result = adjustContrast(result, -10);
        return adjustSaturation(result, -40);
    }

    // 应用柔和滤镜
    public static BufferedImage applyMutedFilter(BufferedImage image) {
        BufferedImage result = adjustBrightness(image, 5);
        return adjustSaturation(result, -30);
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