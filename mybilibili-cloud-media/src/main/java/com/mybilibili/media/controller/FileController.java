package com.mybilibili.media.controller;


import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.dto.UploadingFileDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.common.utils.DateUtils;
import com.mybilibili.common.utils.FFmpegUtils;
import com.mybilibili.common.utils.StringTools;
import com.mybilibili.media.component.MediaRedisComponent;
import com.mybilibili.media.component.VideoPlayerComponent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.MalformedURLException;
import java.util.Date;

@RestController
@Validated
@RequestMapping("/file")
public class FileController extends ABaseController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    @Resource
    private AdminConfig adminConfig;

    @Resource
    private FFmpegUtils fFmpegUtils;

    @Resource
    private MediaRedisComponent redisComponent;

    @Resource
    private VideoPlayerComponent videoPlayerComponent;

    @RequestMapping("/uploadImage")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO uploadFile(@NotNull MultipartFile file, @NotNull boolean createThumbnail) throws IOException {

        //创建上传文件保存路径
        String day = DateUtils.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        String folderPath = adminConfig.getProjectFolder()
                + Constants.FILE_PATH_FOLDER + Constants.FILE_PATH_FOLDER_COVER
                + day;

        //获取文件后缀".xxx"
        String originalFilename = file.getOriginalFilename();
        String fileSuffix = StringTools.getFileSuffix(originalFilename);
        //设置文件名
        String name = StringTools.generateRandomStr(Constants.LENGTH_30);
        String fileRealName = name + fileSuffix;
        File folder = new File(folderPath);

        if (!folder.exists())
        {
            folder.mkdirs();
        }

        //将上传文件转移到指定目录
        String filePath = folderPath + "/" +  fileRealName;
        file.transferTo(new File(filePath));
        if (createThumbnail)
        {
            //生成缩略图
            fFmpegUtils.createImageThumbnail(filePath, fileSuffix);
            fileRealName = name + Constants.IMAGE_THUMBNAIL_SUFFIX + fileSuffix;
        }
        //返回图片路径
        return getSuccessResponseVO(Constants.FILE_PATH_FOLDER_COVER + day + "/" + fileRealName);
    }

    /**
     * @description 前端用uploadFile返回的地址向getResource发起get请求
     * @param response 返回图片
     * @param sourcePath 文件相对路径地址
     */
    @RequestMapping("/getResource")
    public void getResource(HttpServletResponse response,  @RequestParam("sourceName") @NotNull String sourcePath)
    {
        if (sourcePath == null || !StringTools.pathIsOk(sourcePath))
        {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String suffixName = StringTools.getFileSuffix(sourcePath);

        //设置图片类型
        response.setContentType("image/" + suffixName.replace(".", ""));
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, sourcePath);
    }

    protected void readFile(HttpServletResponse response, String filePath) {
        File file = new File(adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + filePath);
        if (!file.exists())
            return;

        try (OutputStream out = response.getOutputStream(); FileInputStream in = new FileInputStream(file)) {
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = in.read(bytes)) != -1)
            {
                out.write(bytes, 0, len);
            }
        } catch (Exception e)
        {
            log.error("读取文件异常", e);
        }
    }

    @RequestMapping("preUploadVideo")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO preUploadVideo(@NotEmpty String fileName, @NotNull Integer chunks) {
        TokenUserInfoDTO userInfo = getTokenUserInfo();
        String uploadingId = StringTools.generateRandomStr(Constants.LENGTH_15);

        UploadingFileDTO uploadingFileDto = new UploadingFileDTO();
        uploadingFileDto.setFileName(fileName);
        uploadingFileDto.setChunks(chunks);
        uploadingFileDto.setChunkIndex(0);
        uploadingFileDto.setUploadId(uploadingId);

        String day = DateUtils.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        String filePath = day + "/" + userInfo.getUserId() + uploadingId;

        String folderPath =  adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + Constants.FILE_PATH_FOLDER_TEMP + filePath;
        File folder = new File(folderPath);
        if (!folder.exists())
        {
            folder.mkdirs();
        }
        uploadingFileDto.setFilePath(filePath);
        redisComponent.saveFileInfo(userInfo.getUserId(), uploadingFileDto);
        return getSuccessResponseVO(uploadingId);
    }

    @RequestMapping("/uploadVideo")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO uploadVideo(@NotNull MultipartFile chunkFile, @NotNull Integer chunkIndex, @NotEmpty String uploadId) throws IOException {

        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();
        UploadingFileDTO uploadFileInfo = redisComponent.getUploadFileInfo( Constants.REDIS_WEB_UPLOADING_FILE_INFO_KEY + tokenUserInfo.getUserId() + uploadId);


        if (uploadFileInfo == null)
            throw new BindException("文件不存在请重新上传");

        //判断是否符合文件大小限制
        SysSettingDTO sysSettingDto = redisComponent.getSysSetting();
        if (uploadFileInfo.getFileSize() > sysSettingDto.getVideoSize() * Constants.MB_SIZE)
            throw new BusinessException("超过文件上传大小限制");

        //判断分片参数
        if ((chunkIndex - 1) > uploadFileInfo.getChunkIndex() || chunkIndex > uploadFileInfo.getChunks() - 1)
            throw new BusinessException(ResponseCodeEnum.CODE_600);

        String folderPath =  adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + Constants.FILE_PATH_FOLDER_TEMP + uploadFileInfo.getFilePath();
        String targetFilePath = folderPath + "/" + chunkIndex;
        chunkFile.transferTo(new File(targetFilePath));

        //更新上柴文件信息
        uploadFileInfo.setFileSize(chunkFile.getSize() + uploadFileInfo.getFileSize());
        uploadFileInfo.setChunkIndex(chunkIndex);
        redisComponent.saveFileInfo(tokenUserInfo.getUserId(), uploadFileInfo);

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delUploadVideo")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO delUploadVideo(@NotEmpty String uploadId) throws IOException {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();
        String userId = tokenUserInfo.getUserId();
        UploadingFileDTO uploadFileInfo = redisComponent.getUploadFileInfo(Constants.REDIS_WEB_UPLOADING_FILE_INFO_KEY + userId + uploadId);

        if (uploadFileInfo == null)
            throw new BusinessException("文件失效请重新上传");

        //删除文件
        String folderPath = uploadFileInfo.getFilePath();
        String folderAbsolutePath = adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + Constants.FILE_PATH_FOLDER_TEMP+ folderPath;

        FileUtils.deleteDirectory(new File(folderAbsolutePath));
        // 删除缓存
        redisComponent.delUploadVideoInfo(userId, uploadId);


        return getSuccessResponseVO(null);
    }

    @RequestMapping("/videoResource/{fileId}/")
    public ResponseEntity<UrlResource> videoResource(@PathVariable String fileId) throws MalformedURLException {
        return videoPlayerComponent.videoResource(fileId, adminConfig.getProjectFolder());
    }

    @RequestMapping("/videoResource/{fileId}/{name}")
    public ResponseEntity<UrlResource> videoResource(@PathVariable String fileId, @PathVariable String name) throws MalformedURLException {
        return videoPlayerComponent.videoResource(fileId, name, adminConfig.getProjectFolder());
    }


}
