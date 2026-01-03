package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("admin/common")
@Tag(name = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public Result<String> upload(MultipartFile file) {
//        log.info("文件上传：name={},sie={}", file.getOriginalFilename(), file.getSize());

        try {
            // 原始文件名称
            String originalFilename = file.getOriginalFilename();
            // 获取文件后缀名
            String extenstion = originalFilename.substring(originalFilename.lastIndexOf("."));

            String newFilename = UUID.randomUUID().toString() + extenstion;

            // 上传文件到OSS
            String fileUrl = aliOssUtil.upload(file.getBytes(), newFilename);
            return Result.success(fileUrl);


        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

}
