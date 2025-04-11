package com.g10.controller;

import com.g10.model.Photo;
import com.g10.repository.PhotoRepository;
import com.g10.utils.ImageEditUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/image-edit")
@CrossOrigin(origins = "*")
public class ImageEditController {

    @Autowired
    private PhotoRepository photoRepository;

    @Value("${static.files.location:static/images}")
    private String staticFilesLocation;

    @Value("${app.base-url:http://localhost:9090}")
    private String baseUrl;

    // 获取照片信息
    @GetMapping("/{id}")
    public ResponseEntity<Photo> getPhoto(@PathVariable Long id) {
        return photoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 综合编辑接口 - 支持亮度、对比度、饱和度、旋转和滤镜
    @PostMapping("/edit")
    public ResponseEntity<Map<String, String>> editImage(@RequestBody Map<String, Object> request) {
        try {
            String imageUrl = (String) request.get("imageUrl");
            Map<String, Object> edits = (Map<String, Object>) request.get("edits");
            
            // 加载原始图片
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            
            // 应用亮度调整
            if (edits.containsKey("brightness")) {
                int brightness = ((Number) edits.get("brightness")).intValue();
                image = ImageEditUtils.adjustBrightness(image, brightness);
            }
            
            // 应用对比度调整
            if (edits.containsKey("contrast")) {
                double contrast = ((Number) edits.get("contrast")).doubleValue();
                image = ImageEditUtils.adjustContrast(image, contrast);
            }
            
            // 应用饱和度调整
            if (edits.containsKey("saturation")) {
                float saturation = ((Number) edits.get("saturation")).floatValue();
                image = ImageEditUtils.adjustSaturation(image, saturation);
            }
            
            // 应用旋转
            if (edits.containsKey("rotation")) {
                int rotation = ((Number) edits.get("rotation")).intValue();
                image = ImageEditUtils.rotateImage(image, rotation);
            }
            
            // 应用滤镜
            if (edits.containsKey("filter")) {
                String filterType = (String) edits.get("filter");
                switch (filterType) {
                    case "grayscale":
                        image = ImageEditUtils.applyGrayscaleFilter(image);
                        break;
                    case "vintage":
                        image = ImageEditUtils.applySepiaFilter(image);
                        break;
                    case "warm":
                        image = ImageEditUtils.applyWarmFilter(image);
                        break;
                    case "cool":
                        image = ImageEditUtils.applyCoolFilter(image);
                        break;
                    case "dramatic":
                        image = ImageEditUtils.applyDramaticFilter(image);
                        break;
                    case "fade":
                        image = ImageEditUtils.applyFadeFilter(image);
                        break;
                    case "muted":
                        image = ImageEditUtils.applyMutedFilter(image);
                        break;
                }
            }
            
            // 保存编辑后的图片并返回URL
            String editedImageUrl = saveEditedImage(image, "preview_" + UUID.randomUUID().toString() + ".jpg");
            
            Map<String, String> response = new HashMap<>();
            response.put("editedImageUrl", editedImageUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 裁剪图片
    @PostMapping("/crop")
    public ResponseEntity<Map<String, String>> cropImage(@RequestBody Map<String, Object> request) {
        try {
            String imageUrl = (String) request.get("imageUrl");
            Map<String, Integer> cropData = (Map<String, Integer>) request.get("cropData");
            
            // 加载原始图片
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            
            // 应用裁剪
            BufferedImage croppedImage = ImageEditUtils.cropImage(
                image,
                cropData.get("x"),
                cropData.get("y"),
                cropData.get("width"),
                cropData.get("height")
            );
            
            // 保存裁剪后的图片并返回URL
            String editedImageUrl = saveEditedImage(croppedImage, "cropped_" + UUID.randomUUID().toString() + ".jpg");
            
            Map<String, String> response = new HashMap<>();
            response.put("editedImageUrl", editedImageUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 保存编辑后的图片
    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveImage(@RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("imageUrl");
            String editedImageUrl = request.get("editedImageUrl");
            
            // 从URL中提取文件名
            String filename = editedImageUrl.substring(editedImageUrl.lastIndexOf("/") + 1);
            
            // 确保目录存在
            Path directory = Paths.get(staticFilesLocation);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            // 生成新的文件名（移除preview_前缀）
            String newFilename = filename.replace("preview_", "final_");
            Path filePath = directory.resolve(newFilename);
            
            // 复制文件
            BufferedImage image = ImageIO.read(new URL(editedImageUrl));
            File outputFile = filePath.toFile();
            ImageIO.write(image, "jpg", outputFile);
            
            // 返回保存后的URL
            String savedImageUrl = baseUrl + "/static/" + newFilename;
            
            Map<String, String> response = new HashMap<>();
            response.put("savedImageUrl", savedImageUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 应用基本调整（亮度、对比度、饱和度）
    @PutMapping("/{id}/adjust")
    public ResponseEntity<Photo> adjustImage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> adjustments) {
        try {
            Photo photo = photoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("照片未找到: " + id));

            BufferedImage image = ImageIO.read(new URL(photo.getUrl()));
            
            // 应用亮度调整
            if (adjustments.containsKey("brightness")) {
                int brightness = ((Number) adjustments.get("brightness")).intValue();
                image = ImageEditUtils.adjustBrightness(image, brightness);
            }
            
            // 应用对比度调整
            if (adjustments.containsKey("contrast")) {
                double contrast = ((Number) adjustments.get("contrast")).doubleValue();
                image = ImageEditUtils.adjustContrast(image, contrast);
            }
            
            // 应用饱和度调整
            if (adjustments.containsKey("saturation")) {
                float saturation = ((Number) adjustments.get("saturation")).floatValue();
                image = ImageEditUtils.adjustSaturation(image, saturation);
            }

            // 保存编辑后的图片
            String newImageUrl = saveEditedImage(image, "edited_" + id + "_adjusted.jpg");
            photo.setUrl(newImageUrl);
            
            return ResponseEntity.ok(photoRepository.save(photo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 应用滤镜
    @PutMapping("/{id}/filter")
    public ResponseEntity<Photo> applyFilter(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Photo photo = photoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("照片未找到: " + id));

            BufferedImage image = ImageIO.read(new URL(photo.getUrl()));
            String filterType = request.get("filter");
            
            BufferedImage filteredImage = switch (filterType) {
                case "grayscale" -> ImageEditUtils.applyGrayscaleFilter(image);
                case "sepia" -> ImageEditUtils.applySepiaFilter(image);
                case "warm" -> ImageEditUtils.applyWarmFilter(image);
                case "cool" -> ImageEditUtils.applyCoolFilter(image);
                case "dramatic" -> ImageEditUtils.applyDramaticFilter(image);
                case "fade" -> ImageEditUtils.applyFadeFilter(image);
                case "muted" -> ImageEditUtils.applyMutedFilter(image);
                default -> image;
            };

            String newImageUrl = saveEditedImage(filteredImage, "edited_" + id + "_" + filterType + ".jpg");
            photo.setUrl(newImageUrl);
            
            return ResponseEntity.ok(photoRepository.save(photo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 旋转和翻转
    @PutMapping("/{id}/transform")
    public ResponseEntity<Photo> transformImage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> transform) {
        try {
            Photo photo = photoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("照片未找到: " + id));

            BufferedImage image = ImageIO.read(new URL(photo.getUrl()));
            
            // 应用旋转
            if (transform.containsKey("rotation")) {
                int degrees = ((Number) transform.get("rotation")).intValue();
                image = ImageEditUtils.rotateImage(image, degrees);
            }
            
            // 应用水平翻转
            if (transform.containsKey("flipH") && (Boolean) transform.get("flipH")) {
                image = ImageEditUtils.flipHorizontal(image);
            }
            
            // 应用垂直翻转
            if (transform.containsKey("flipV") && (Boolean) transform.get("flipV")) {
                image = ImageEditUtils.flipVertical(image);
            }

            String newImageUrl = saveEditedImage(image, "edited_" + id + "_transformed.jpg");
            photo.setUrl(newImageUrl);
            
            return ResponseEntity.ok(photoRepository.save(photo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 裁剪图片
    @PutMapping("/{id}/crop")
    public ResponseEntity<Photo> cropImage(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> cropData) {
        try {
            Photo photo = photoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("照片未找到: " + id));

            BufferedImage image = ImageIO.read(new URL(photo.getUrl()));
            BufferedImage croppedImage = ImageEditUtils.cropImage(
                image,
                cropData.get("x"),
                cropData.get("y"),
                cropData.get("width"),
                cropData.get("height")
            );

            String newImageUrl = saveEditedImage(croppedImage, "edited_" + id + "_cropped.jpg");
            photo.setUrl(newImageUrl);
            
            return ResponseEntity.ok(photoRepository.save(photo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 保存编辑后的图片
    private String saveEditedImage(BufferedImage image, String filename) throws IOException {
        // 确保目录存在
        Path directory = Paths.get(staticFilesLocation);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // 生成唯一文件名
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        Path filePath = directory.resolve(uniqueFilename);
        
        // 保存图片
        File outputFile = filePath.toFile();
        ImageIO.write(image, "jpg", outputFile);
        
        // 返回可访问的URL
        return baseUrl + "/api/files/" + uniqueFilename;
    }
} 