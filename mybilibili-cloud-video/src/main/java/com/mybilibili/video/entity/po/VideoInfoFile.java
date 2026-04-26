package com.mybilibili.video.entity.po;

import java.io.Serializable;

/**
 * @author amani
 * @date 2026/02/09
 * @description 视频文件信息
 */
public class VideoInfoFile implements Serializable {
	/**
	 * @description 文件id
	 */
    private String fileId;

	/**
	 * @description 用户id
	 */
    private String userId;

	/**
	 * @description 视频id
	 */
    private String videoId;

	/**
	 * @description 文件名
	 */
    private String fileName;

	/**
	 * @description 文件索引
	 */
    private Integer fileIndex;

	/**
	 * @description 文件大小
	 */
    private Long fileSize;

	/**
	 * @description 文件路径
	 */
    private String filePath;

	/**
	 * @description 持续时间(秒)
	 */
    private Integer duration;

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getFileId() {
		return this.fileId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getVideoId() {
		return this.videoId;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return this.fileName;
	}
	public void setFileIndex(Integer fileIndex) {
		this.fileIndex = fileIndex;
	}
	public Integer getFileIndex() {
		return this.fileIndex;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	public Long getFileSize() {
		return this.fileSize;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFilePath() {
		return this.filePath;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public Integer getDuration() {
		return this.duration;
	}

	@Override
	public String toString() {
		return "VideoInfoFile{" +
				"fileId='" + fileId + 
				", userId='" + userId + '\'' + 
				", videoId='" + videoId + '\'' + 
				", fileName='" + fileName + '\'' + 
				", fileIndex='" + fileIndex + '\'' + 
				", fileSize='" + fileSize + '\'' + 
				", filePath='" + filePath + '\'' + 
				", duration='" + duration + '\'' + 
				'}';
	}

}