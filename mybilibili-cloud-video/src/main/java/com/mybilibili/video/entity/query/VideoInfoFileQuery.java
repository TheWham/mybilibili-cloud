package com.mybilibili.video.entity.query;

import com.mybilibili.base.entity.query.BaseQuery;

/**
 * @author amani
 * @date 2026/02/09
 * @description 视频文件信息
 */
public class VideoInfoFileQuery extends BaseQuery {
	/**
	 * @description 文件id
	 */
	private String fileId;
	private String fileIdFuzzy;
	/**
	 * @description 用户id
	 */
	private String userId;
	private String userIdFuzzy;
	/**
	 * @description 视频id
	 */
	private String videoId;
	private String videoIdFuzzy;
	/**
	 * @description 文件名
	 */
	private String fileName;
	private String fileNameFuzzy;
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
	private String filePathFuzzy;
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
	public void setFileIdFuzzy(String fileIdFuzzy) {
		this.fileIdFuzzy = fileIdFuzzy;
	}
	public String getFileIdFuzzy() {
		return this.fileIdFuzzy;
	}
	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}
	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}
	public void setVideoIdFuzzy(String videoIdFuzzy) {
		this.videoIdFuzzy = videoIdFuzzy;
	}
	public String getVideoIdFuzzy() {
		return this.videoIdFuzzy;
	}
	public void setFileNameFuzzy(String fileNameFuzzy) {
		this.fileNameFuzzy = fileNameFuzzy;
	}
	public String getFileNameFuzzy() {
		return this.fileNameFuzzy;
	}
	public void setFilePathFuzzy(String filePathFuzzy) {
		this.filePathFuzzy = filePathFuzzy;
	}
	public String getFilePathFuzzy() {
		return this.filePathFuzzy;
	}

}