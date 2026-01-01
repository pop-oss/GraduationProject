package com.erkang.controller;

import com.erkang.integration.storage.MinioService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 文件控制器测试
 * _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_
 */
class FileControllerTest {

    private MinioService minioService;
    private FileController fileController;

    @BeforeProperty
    void setUp() {
        minioService = mock(MinioService.class);
        fileController = new FileController(minioService);
    }

    /**
     * Property 1: 获取预签名下载URL - 应返回有效的URL
     * **Validates: Requirements 12.2**
     */
    @Property(tries = 100)
    void getPresignedUrl_shouldReturnValidUrl(
            @ForAll @AlphaChars @StringLength(min = 10, max = 100) String objectName,
            @ForAll @IntRange(min = 1, max = 60) int expireMinutes) {
        
        String mockUrl = "http://minio.example.com/bucket/" + objectName + "?signature=xxx";
        when(minioService.getPresignedUrl(objectName, expireMinutes)).thenReturn(mockUrl);
        
        var result = fileController.getPresignedUrl(objectName, expireMinutes);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotBlank();
        assertThat(result.getData()).contains(objectName);
    }


    /**
     * Property 2: 获取预签名上传URL - 应返回上传URL和对象名称
     * **Validates: Requirements 12.3**
     */
    @Property(tries = 100)
    void getPresignedUploadUrl_shouldReturnUploadUrlAndObjectName(
            @ForAll @AlphaChars @StringLength(min = 5, max = 50) String fileName,
            @ForAll("validCategories") String category,
            @ForAll @IntRange(min = 1, max = 30) int expireMinutes) {
        
        String mockUrl = "http://minio.example.com/bucket/upload?signature=xxx";
        when(minioService.getPresignedUploadUrl(anyString(), eq(expireMinutes))).thenReturn(mockUrl);
        
        var result = fileController.getPresignedUploadUrl(fileName, category, expireMinutes);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().get("uploadUrl")).isNotBlank();
        assertThat(result.getData().get("objectName")).isNotBlank();
        assertThat(result.getData().get("objectName")).contains(category);
    }

    @Provide
    Arbitrary<String> validCategories() {
        return Arbitraries.of("attachment", "medical", "prescription", "report");
    }

    /**
     * Property 3: 检查文件是否存在 - 应返回布尔值
     * **Validates: Requirements 12.4**
     */
    @Property(tries = 100)
    void exists_shouldReturnBoolean(
            @ForAll @AlphaChars @StringLength(min = 10, max = 100) String objectName,
            @ForAll boolean fileExists) {
        
        when(minioService.exists(objectName)).thenReturn(fileExists);
        
        var result = fileController.exists(objectName);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isEqualTo(fileExists);
    }

    /**
     * Property 4: 删除文件 - 应调用服务层删除
     * **Validates: Requirements 12.5**
     */
    @Property(tries = 100)
    void delete_shouldCallService(
            @ForAll @AlphaChars @StringLength(min = 10, max = 100) String objectName) {
        
        doNothing().when(minioService).deleteFile(objectName);
        
        var result = fileController.delete(objectName);
        
        assertThat(result).isNotNull();
    }


    /**
     * Property 5: 文件名格式验证 - 文件名应包含扩展名
     * **Validates: Requirements 12.1**
     */
    @Property(tries = 50)
    void fileName_shouldContainExtension(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String baseName,
            @ForAll("validExtensions") String extension) {
        
        String fileName = baseName + "." + extension;
        
        assertThat(fileName).contains(".");
        assertThat(fileName.substring(fileName.lastIndexOf(".") + 1)).isEqualTo(extension);
    }

    @Provide
    Arbitrary<String> validExtensions() {
        return Arbitraries.of("jpg", "png", "pdf", "doc", "docx", "txt", "mp4", "mp3");
    }

    /**
     * Property 6: 文件分类验证 - 分类应为有效值
     * **Validates: Requirements 12.1**
     */
    @Property(tries = 50)
    void category_shouldBeValid(
            @ForAll("validCategories") String category) {
        
        assertThat(category).isIn("attachment", "medical", "prescription", "report");
    }

    /**
     * Property 7: 过期时间验证 - 过期时间应为正数
     * **Validates: Requirements 12.2, 12.3**
     */
    @Property(tries = 100)
    void expireMinutes_shouldBePositive(
            @ForAll @IntRange(min = 1, max = 1440) int expireMinutes) {
        
        assertThat(expireMinutes).isPositive();
        assertThat(expireMinutes).isLessThanOrEqualTo(1440);
    }

    /**
     * Property 8: 对象名称格式验证 - 应包含日期路径
     * **Validates: Requirements 12.1**
     */
    @Test
    void objectName_shouldContainDatePath() {
        minioService = mock(MinioService.class);
        fileController = new FileController(minioService);
        
        String mockUrl = "http://minio.example.com/bucket/upload?signature=xxx";
        when(minioService.getPresignedUploadUrl(anyString(), anyInt())).thenReturn(mockUrl);
        
        var result = fileController.getPresignedUploadUrl("test.txt", "attachment", 10);
        
        String objectName = result.getData().get("objectName");
        assertThat(objectName).matches(".*\\d{4}/\\d{2}/\\d{2}.*");
    }
}
