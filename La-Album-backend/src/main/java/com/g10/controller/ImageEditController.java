package com.g10.controller;

import com.g10.utils.ImageEditUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
@CrossOrigin
public class ImageEditController {

    @PostMapping("/edit")
    public ResponseEntity<?> editImage(@RequestBody Map<String, Object> request) {
        try {
            String imageUrl = (String) request.get("imageUrl");
            Map<String, Object> edits = (Map<String, Object>) request.get("edits");
            
            // 从 Base64 或 URL 获取图片
            BufferedImage image = getImageFromSource(imageUrl);
            
            // 应用编辑
            if (edits.containsKey("brightness")) {
                int brightness = ((Number) edits.get("brightness")).intValue();
                image = ImageEditUtils.adjustBrightness(image, brightness);
            }
            
            if (edits.containsKey("contrast")) {
                double contrast = ((Number) edits.get("contrast")).doubleValue();
                image = ImageEditUtils.adjustContrast(image, contrast);
            }
            
            if (edits.containsKey("saturation")) {
                float saturation = ((Number) edits.get("saturation")).floatValue();
                image = ImageEditUtils.adjustSaturation(image, saturation);
            }
            
            if (edits.containsKey("rotation")) {
                int rotation = ((Number) edits.get("rotation")).intValue();
                image = ImageEditUtils.rotateImage(image, rotation);
            }
            
            if (edits.containsKey("filter")) {
                String filter = (String) edits.get("filter");
                if ("grayscale".equals(filter)) {
                    image = ImageEditUtils.applyGrayscaleFilter(image);
                } else if ("vintage".equals(filter)) {
                    image = ImageEditUtils.applyVintageFilter(image);
                }
            }
            
            // 转换为 Base64
            String editedImageUrl = convertToBase64(image);
            
            return ResponseEntity.ok().body(Map.of("editedImageUrl", editedImageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/crop")
    public ResponseEntity<?> cropImage(@RequestBody Map<String, Object> request) {
        try {
            String imageUrl = (String) request.get("imageUrl");
            Map<String, Object> cropData = (Map<String, Object>) request.get("cropData");
            
            BufferedImage image = getImageFromSource(imageUrl);
            
            int x = ((Number) cropData.get("x")).intValue();
            int y = ((Number) cropData.get("y")).intValue();
            int width = ((Number) cropData.get("width")).intValue();
            int height = ((Number) cropData.get("height")).intValue();
            
            BufferedImage croppedImage = ImageEditUtils.cropImage(image, x, y, width, height);
            String editedImageUrl = convertToBase64(croppedImage);
            
            return ResponseEntity.ok().body(Map.of("editedImageUrl", editedImageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveImage(@RequestBody Map<String, Object> request) {
        try {
            String editedImageUrl = (String) request.get("editedImageUrl");
            
            // 这里可以添加保存图片到文件系统或数据库的逻辑
            // 暂时直接返回编辑后的图片URL
            return ResponseEntity.ok().body(Map.of("savedImageUrl", editedImageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private BufferedImage getImageFromSource(String source) throws IOException {
        if (source.startsWith("data:image")) {
            // 处理 Base64 图片
            String base64Image = source.split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } else {
            // 处理 URL 图片
            return ImageIO.read(new java.net.URL(source));
        }
    }

    private String convertToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        byte[] imageBytes = output.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
} 