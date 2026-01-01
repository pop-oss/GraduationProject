package com.erkang.integration.storage;

import io.minio.*;
import io.minio.http.Method;
import net.jqwik.api.*;
import net.jqwik.api.constraints.ByteRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.StringLength;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 文件上传下载属性测试
 * **Property 12: 文件上传下载一致性（Round-Trip）**
 * **Validates: Requirements 12.1, 12.2, 12.3**
 */
class FileStoragePropertyTest {

    /**
     * Property 12.1: 上传后下载的文件内容应与原始内容一致
     * *For any* byte array, upload then download should return identical content
     */
    @Property(tries = 100)
    void uploadThenDownloadShouldReturnSameContent(
            @ForAll @Size(min = 1, max = 10000) byte[] content,
            @ForAll("validFilenames") String filename,
            @ForAll("categories") String category) throws Exception {
        
        MinioClient minioClient = Mockito.mock(MinioClient.class);
        MinioService service = createServiceWithMock(minioClient);
        
        // 捕获上传的数据
        ArgumentCaptor<PutObjectArgs> putCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        when(minioClient.putObject(putCaptor.capture())).thenReturn(null);
        
        // 上传文件
        String objectName = service.uploadBytes(content, filename, "application/octet-stream", category);
        
        // 验证上传被调用
        verify(minioClient).putObject(any(PutObjectArgs.class));
        
        // 模拟下载返回相同内容
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(new GetObjectResponse(
                        null, 
                        "erkang-files", 
                        null, 
                        objectName, 
                        new ByteArrayInputStream(content)));
        
        // 下载文件
        byte[] downloaded = service.downloadBytes(objectName);
        
        // 验证内容一致
        assertThat(downloaded).isEqualTo(content);
    }

    /**
     * Property 12.2: 生成的对象名称应包含分类和日期路径
     * *For any* filename and category, object name should follow pattern
     */
    @Property(tries = 100)
    void objectNameShouldFollowPattern(
            @ForAll("validFilenames") String filename,
            @ForAll("categories") String category) throws Exception {
        
        MinioClient minioClient = Mockito.mock(MinioClient.class);
        MinioService service = createServiceWithMock(minioClient);
        
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        when(minioClient.putObject(captor.capture())).thenReturn(null);
        
        byte[] content = "test".getBytes();
        String objectName = service.uploadBytes(content, filename, "text/plain", category);
        
        // 验证对象名称格式: category/yyyy/MM/dd/uuid_filename
        assertThat(objectName).startsWith(category + "/");
        assertThat(objectName).matches(category + "/\\d{4}/\\d{2}/\\d{2}/[a-f0-9]{8}_.*");
    }

    /**
     * Property 12.3: 预签名URL应包含正确的对象名称
     * *For any* object name, presigned URL should be generated
     */
    @Property(tries = 100)
    void presignedUrlShouldBeGenerated(
            @ForAll("objectNames") String objectName,
            @ForAll @net.jqwik.api.constraints.IntRange(min = 1, max = 60) int expireMinutes) throws Exception {
        
        MinioClient minioClient = Mockito.mock(MinioClient.class);
        MinioService service = createServiceWithMock(minioClient);
        
        String expectedUrl = "http://localhost:9000/erkang-files/" + objectName + "?signature=xxx";
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);
        
        String url = service.getPresignedUrl(objectName, expireMinutes);
        
        assertThat(url).isNotNull();
        assertThat(url).contains(objectName);
    }

    /**
     * Property 12.4: 文件名应被正确清理，移除不安全字符
     * *For any* filename with special characters, should be sanitized
     */
    @Property(tries = 100)
    void filenameShouldBeSanitized(
            @ForAll("filenamesWithSpecialChars") String filename) throws Exception {
        
        MinioClient minioClient = Mockito.mock(MinioClient.class);
        MinioService service = createServiceWithMock(minioClient);
        
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        when(minioClient.putObject(captor.capture())).thenReturn(null);
        
        byte[] content = "test".getBytes();
        String objectName = service.uploadBytes(content, filename, "text/plain", "test");
        
        // 验证对象名称不包含危险字符
        assertThat(objectName).doesNotContain("..");
        assertThat(objectName).doesNotContain("//");
        assertThat(objectName).doesNotContain("\\");
        assertThat(objectName).doesNotContain("<");
        assertThat(objectName).doesNotContain(">");
    }

    /**
     * Property 12.5: 空文件名应使用默认名称
     * *For any* empty or null filename, should use default name
     */
    @Property(tries = 100)
    void emptyFilenameShouldUseDefault(
            @ForAll("emptyFilenames") String filename) throws Exception {
        
        MinioClient minioClient = Mockito.mock(MinioClient.class);
        MinioService service = createServiceWithMock(minioClient);
        
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        when(minioClient.putObject(captor.capture())).thenReturn(null);
        
        byte[] content = "test".getBytes();
        String objectName = service.uploadBytes(content, filename, "text/plain", "test");
        
        // 验证使用了默认名称
        assertThat(objectName).contains("unnamed");
    }

    @Provide
    Arbitrary<String> validFilenames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(50)
                .map(s -> s + ".txt");
    }

    @Provide
    Arbitrary<String> categories() {
        return Arbitraries.of("medical", "prescription", "attachment", "report");
    }

    @Provide
    Arbitrary<String> objectNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(5)
                .ofMaxLength(20)
                .map(s -> "test/2024/01/01/" + s + ".txt");
    }

    @Provide
    Arbitrary<String> filenamesWithSpecialChars() {
        return Arbitraries.of(
                "test<script>.txt",
                "file/../../../etc/passwd",
                "file\\path\\test.txt",
                "file with spaces.txt",
                "文件名.txt",
                "test@#$%.txt"
        );
    }

    @Provide
    Arbitrary<String> emptyFilenames() {
        return Arbitraries.of("", null);
    }

    private MinioService createServiceWithMock(MinioClient minioClient) {
        MinioService service = new MinioService(minioClient);
        // 使用反射设置默认bucket
        try {
            java.lang.reflect.Field field = MinioService.class.getDeclaredField("defaultBucket");
            field.setAccessible(true);
            field.set(service, "erkang-files");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return service;
    }
}
