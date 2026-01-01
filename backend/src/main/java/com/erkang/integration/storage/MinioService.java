package com.erkang.integration.storage;

import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 对象存储服务
 * _Requirements: 12.1, 12.2, 12.3, 12.4_
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket:erkang-files}")
    private String defaultBucket;

    /**
     * 上传文件
     * @param file 文件
     * @param category 分类（medical/prescription/attachment）
     * @return 文件存储路径
     */
    public String uploadFile(MultipartFile file, String category) {
        try {
            String objectName = generateObjectName(file.getOriginalFilename(), category);
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            log.info("文件上传成功: bucket={}, object={}", defaultBucket, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传字节数组
     * @param data 文件数据
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param category 分类
     * @return 文件存储路径
     */
    public String uploadBytes(byte[] data, String fileName, String contentType, String category) {
        try {
            String objectName = generateObjectName(fileName, category);
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType(contentType)
                    .build());
            
            log.info("文件上传成功: bucket={}, object={}, size={}", defaultBucket, objectName, data.length);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectName, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "文件不存在或无法访问");
        }
    }

    /**
     * 下载文件为字节数组
     * @param objectName 对象名称
     * @return 文件字节数组
     */
    public byte[] downloadBytes(String objectName) {
        try (InputStream is = downloadFile(objectName)) {
            return is.readAllBytes();
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectName, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "文件不存在或无法访问");
        }
    }

    /**
     * 生成预签名下载URL
     * @param objectName 对象名称
     * @param expireMinutes 过期时间（分钟）
     * @return 预签名URL
     */
    public String getPresignedUrl(String objectName, int expireMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(expireMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", objectName, e);
            throw new BusinessException(ErrorCode.FILE_ACCESS_DENIED, "无法生成文件访问链接");
        }
    }

    /**
     * 生成预签名上传URL
     * @param objectName 对象名称
     * @param expireMinutes 过期时间（分钟）
     * @return 预签名URL
     */
    public String getPresignedUploadUrl(String objectName, int expireMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectName)
                    .method(Method.PUT)
                    .expiry(expireMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("生成预签名上传URL失败: {}", objectName, e);
            throw new BusinessException(ErrorCode.FILE_ACCESS_DENIED, "无法生成文件上传链接");
        }
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectName)
                    .build());
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "文件删除失败");
        }
    }

    /**
     * 检查文件是否存在
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean exists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 确保 bucket 存在
     */
    public void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(defaultBucket)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(defaultBucket)
                        .build());
                log.info("创建 bucket: {}", defaultBucket);
            }
        } catch (Exception e) {
            log.error("检查/创建 bucket 失败", e);
        }
    }

    /**
     * 生成对象名称
     * 格式: category/yyyy/MM/dd/uuid_filename
     */
    private String generateObjectName(String originalFilename, String category) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String safeName = sanitizeFilename(originalFilename);
        return String.format("%s/%s/%s_%s", category, date, uuid, safeName);
    }

    /**
     * 清理文件名，移除不安全字符
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed";
        }
        // 移除路径遍历攻击字符
        String safe = filename.replace("..", "")
                              .replace("/", "_")
                              .replace("\\", "_");
        // 只保留字母、数字、点、下划线、中划线、中文
        safe = safe.replaceAll("[^a-zA-Z0-9.\\-_\\u4e00-\\u9fa5]", "_");
        // 移除连续的下划线
        safe = safe.replaceAll("_+", "_");
        // 移除首尾下划线
        safe = safe.replaceAll("^_|_$", "");
        return safe.isEmpty() ? "unnamed" : safe;
    }
}
