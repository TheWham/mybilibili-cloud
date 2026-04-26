package com.mybilibili.common.utils;


import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.base.constants.Constants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;

@Component
public class FFmpegUtils {
    @Resource
    private AdminConfig adminConfig;

    public void createImageThumbnail(String filePath, String fileSuffix)
    {
        String name = filePath.substring(0, filePath.lastIndexOf("."));

        String thumbnailPath = name + Constants.IMAGE_THUMBNAIL_SUFFIX + fileSuffix;

        String CMD = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";
        CMD = String.format(CMD, filePath, thumbnailPath);

        ProcessUtils.executeCommand(CMD, adminConfig.getShowFFmpegLog());
    }

    public Integer getVideoInfoDuration(String completeVideo)
    {
        final String CMD_GET_CODE = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"";
        String cmd = String.format(CMD_GET_CODE, completeVideo);
        String result = ProcessUtils.executeCommand(cmd, adminConfig.getShowFFmpegLog());
        if (StringTools.isEmpty(result))
            return 0;
        result = result.replace("\n", "");
        return new BigDecimal(result).intValue();
    }
    public String getVideoCodec(String videoFilePath)
    {
        final String CMD_GET_CODE = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%s\"";
        String cmd = String.format(CMD_GET_CODE, videoFilePath);
        String result = ProcessUtils.executeCommand(cmd, adminConfig.getShowFFmpegLog());
        result = result.replace("\n", "");
        int start = result.indexOf("codec_name=") + "codec_name=".length();
        int end = result.indexOf("[/STREAM]");
        String codec = result.substring(start, end);
        return codec;
    }

    public void convertHevc2Mp4(String newFileName, String videoFilePath)
    {
        String CMD_HEVC_264 = "ffmpeg -i \"%s\" -c:v libx264 -crf 20 \"%s\" -y";
        String cmd = String.format(CMD_HEVC_264, newFileName, videoFilePath);
        ProcessUtils.executeCommand(cmd, adminConfig.getShowFFmpegLog());
    }

    public void convertVideo2Ts(File tsFolder, String videoFilePath)
    {
      //  final String CMD_TRANSFER_2TS ="ffmpeg -y -i \"%s\" -c:v copy -c:a copy -bsf:v hevc_mp4toannexb \"%s\"";
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i \"%s\" -c:v copy -c:a copy -bsf:v h264_mp4toannexb \"%s\"";
        final String CMD_CUT_TS = "ffmpeg -i \"%s\" -c copy -map 0 -f segment -segment_list \"%s\" -segment_time 10 %s/%%4d.ts";
        String tsPath = tsFolder.getPath() + "\\" + Constants.TS_NAME;
        //生成.ts
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, adminConfig.getShowFFmpegLog());
        //生成索引文件.m3u8 和切片.ts
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "\\" + Constants.M3U8_NAME, tsFolder.getPath());
        ProcessUtils.executeCommand(cmd, adminConfig.getShowFFmpegLog());
        new File(tsPath).delete();
    }
}
