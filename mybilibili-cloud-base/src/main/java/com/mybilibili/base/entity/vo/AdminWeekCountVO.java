package com.mybilibili.base.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class AdminWeekCountVO {
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date statisticsDate;
    private Integer statisticsCount;

    public Date getStatisticsDate() { return statisticsDate; }
    public void setStatisticsDate(Date statisticsDate) { this.statisticsDate = statisticsDate; }
    public Integer getStatisticsCount() { return statisticsCount; }
    public void setStatisticsCount(Integer statisticsCount) { this.statisticsCount = statisticsCount; }
}
