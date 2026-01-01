package com.erkang.controller;

import com.erkang.common.Result;
import com.erkang.integration.storage.MinioService;
import com.erkang.security.Auditable;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传下载控制器
 * _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST"})
    @Auditable(action = "FILE_UPLOAD", module = "file")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "attachment") String category) {
        
        Long userId = UserContext.getUserId();
        String objectName = minioService.uploadFile(file, category);
        
        Map<String, String> result = new HashMap<>();
        result.put("objectName", objectName);
        result.put("fileName", file.getOriginalFilename());
        result.put("fileSize", String.valueOf(file.getSize()));
        result.put("contentType", file.getContentType());
        
        log.info("文件上传成功: userId={}, objectName={}", userId, objectName);
        return Result.success(result);
    }

    /**
     * 获取文件预签名下载URL
     */
    @GetMapping("/presigned-url")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST", "ADMIN"})
    public Result<String> getPresignedUrl(
            @RequestParam String objectName,
            @RequestParam(defaultValue = "30") int expireMinutes) {
        
        String url = minioService.getPresignedUrl(objectName, expireMinutes);
        return Result.success(url);
    }

    /**
     * 获取预签名上传URL（用于前端直传）
     */
    @GetMapping("/presigned-upload-url")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST"})
    public Result<Map<String, String>> getPresignedUploadUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "attachment") String category,
            @RequestParam(defaultValue = "10") int expireMinutes) {
        
        // 生成对象名称
        String objectName = category + "/" + java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/" +
                java.util.UUID.randomUUID().toString().substring(0, 8) + "_" + fileName;
        
        String url = minioService.getPresignedUploadUrl(objectName, expireMinutes);
        
        Map<String, String> result = new HashMap<>();
        result.put("uploadUrl", url);
        result.put("objectName", objectName);
        
        return Result.success(result);
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST", "ADMIN"})
    public Result<Boolean> exists(@RequestParam String objectName) {
        boolean exists = minioService.exists(objectName);
        return Result.success(exists);
    }

    /**
     * 删除文件（仅管理员）
     */
    @DeleteMapping
    @RequireRole({"ADMIN"})
    @Auditable(action = "FILE_DELETE", module = "file")
    public Result<Void> delete(@RequestParam String objectName) {
        minioService.deleteFile(objectName);
        log.info("文件删除成功: objectName={}", objectName);
        return Result.success(null);
    }
}
