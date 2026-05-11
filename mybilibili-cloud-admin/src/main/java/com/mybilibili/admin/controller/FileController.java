package com.mybilibili.admin.controller;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.common.utils.DateUtils;
import com.mybilibili.common.utils.FFmpegUtils;
import com.mybilibili.common.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@RestController
@Validated
@RequestMapping("/file")
public class FileController extends ABaseController {

    @Resource
    private AdminConfig adminConfig;

    @Resource
    private FFmpegUtils ffmpegUtils;

    @RequestMapping("/uploadImage")
    public ResponseVO uploadImage(@NotNull MultipartFile file, boolean createThumbnail) throws IOException {
        String day = DateUtils.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        String folderPath = adminConfig.getProjectFolder()
                + Constants.FILE_PATH_FOLDER
                + Constants.FILE_PATH_FOLDER_COVER
                + day;

        String originalFilename = file.getOriginalFilename();
        String fileSuffix = StringTools.getFileSuffix(originalFilename);
        String fileNameNoSuffix = StringTools.generateRandomStr(Constants.LENGTH_30);
        String fileRealName = fileNameNoSuffix + fileSuffix;

        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String filePath = folderPath + File.separator + fileRealName;
        file.transferTo(new File(filePath));
        if (createThumbnail) {
            // 上传分类图标、封面时复用前台同一套缩略图规则。
            ffmpegUtils.createImageThumbnail(filePath, fileSuffix);
            fileRealName = fileNameNoSuffix + Constants.IMAGE_THUMBNAIL_SUFFIX + fileSuffix;
        }
        return getSuccessResponseVO(Constants.FILE_PATH_FOLDER_COVER + day + "/" + fileRealName);
    }
}
