package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.config.FileUploadConfig;
import com.example.backend.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Autowired
    private FileUploadConfig fileUploadConfig;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件为空");
        }
        try {
            String url = FileUploadUtil.uploadFile(file, fileUploadConfig.getUploadDir());
            return Result.success(url);
        } catch (IOException e) {
            return Result.error(500, "上传失败：" + e.getMessage());
        }
    }
}
