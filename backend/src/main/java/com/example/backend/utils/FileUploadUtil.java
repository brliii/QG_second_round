package com.example.backend.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUploadUtil {
    public static String uploadFile(MultipartFile file, String uploadDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//结尾文件类型
        }
        String newFileName = UUID.randomUUID().toString() + suffix;//用uuid做唯一标识
        File dest = new File(uploadDir, newFileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();//多层目录
        }
        file.transferTo(dest);
        return "/uploads/" + newFileName;
    }
}