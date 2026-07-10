package com.example.AerionSports_BE.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Cấu hình để Spring Boot phục vụ (serve) file ảnh tĩnh đã upload qua URL "/uploads/**".
 *
 * Nếu KHÔNG có file này, ảnh nhân viên (avatar) lưu ở luuFileAnh() sẽ được ghi ra ổ đĩa
 * nhưng trình duyệt sẽ không truy cập được qua đường dẫn "/uploads/xxx.jpg" (lỗi 404),
 * vì Spring Boot mặc định chỉ phục vụ file tĩnh trong classpath "static/" hoặc "public/".
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Phải trùng với giá trị app.upload.dir dùng trong NhanVienController
    @Value("${app.upload.dir:${user.dir}/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir.replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}