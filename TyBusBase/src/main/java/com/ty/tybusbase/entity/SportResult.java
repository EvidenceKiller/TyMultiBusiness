package com.ty.tybusbase.entity;

import com.adl.service.entity.PhysicalTrainingRankEntity;
import com.ty.tybusbase.constants.SportConstant;

import java.io.File;
import java.util.List;

/**
 * @author :WEI_CP
 * Time:2022/3/3 16:55
 * Description: this is SportResult
 */
public class SportResult {

    /**
     * 运动时长
     */
    private int sportDuration;

    /**
     * 运动成绩
     */
    private int sportAchievement;


    /**
     * 技巧完美
     */
    private int perfectCount;

    /**
     * 真棒
     */
    private int goodCount;

    /**
     * 加油
     */
    private int comeOnCount;

    /**
     * 最大连击数
     *
     */
    private int maxComboCount;
    /**
     * 中断次数
     */
    private int interruptCount;
    /**
     * 真棒得分
     */
    public int goodScore;
    /**
     * 加油得分
     */
    public int comeOnScore;

    /**
     * 完美得分
     */
    public int perfectScore;

    /**
     * 总分
     */
    public int totalScore;

    public String userName;

    public String videoLocalPath ;
    /**
     * 头像
     */
    public String headUrl;

    /**
     * 用户id
     */
    public String userId;

    //成语填空正确个数
    public int correctCount;
    //错误个数
    public int errorCount;

    public String location = SportConstant.SPORT_LOCATION_DEFAULT;
    private long checkTime;
    /**
     * 排名
     */
    private int rank;

    /**
     * 组号
     */
    private int multiPersonIndex;

    //运动子项类型，如五育中的德智体美劳
    private String sportSkuSubType;

    //运动开始和结束时间
    private long startTime = 0;
    private long endTime = 0;

    private String multiPersonId;

    private String sportResult = "0";

    //  是否违规 0：正常 1：违规
    private int sportViolationStatus = 0;

    //  运动违规次数
    private String sportViolationNumber;

    private String reportVersion;

    private String reportCode;

    private File videoFile= null;
    private String videoCover= null;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getMultiPersonId() {
        return multiPersonId;
    }

    public void setMultiPersonId(String multiPersonId) {
        this.multiPersonId = multiPersonId;
    }

    public String getSportResult() {
        return sportResult;
    }

    public void setSportResult(String sportResult) {
        this.sportResult = sportResult;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public int getTotalScore() {
        return goodScore+comeOnScore+perfectScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    private List<PhysicalTrainingRankEntity> rankEntityList;

    public List<PhysicalTrainingRankEntity> getRankEntityList() {
        return rankEntityList;
    }

    public void setRankEntityList(List<PhysicalTrainingRankEntity> rankEntityList) {
        this.rankEntityList = rankEntityList;
    }


    public int refreshSportAchievement(int refresh){
        sportAchievement = sportAchievement+refresh;
        return sportAchievement;
    }

    public int getSportDuration() {
        return sportDuration;
    }

    public void setSportDuration(int sportDuration) {
        this.sportDuration = sportDuration;
    }

    public int getSportAchievement() {
        return sportAchievement;
    }

    public void setSportAchievement(int sportAchievement) {
        this.sportAchievement = sportAchievement;
    }

    public int getPerfectCount() {
        return perfectCount;
    }

    public void setPerfectCount(int perfectCount) {
        this.perfectCount = perfectCount;
    }

    public int getGoodCount() {
        return goodCount;
    }

    public void setGoodCount(int goodCount) {
        this.goodCount = goodCount;
    }

    public int getComeOnCount() {
        return comeOnCount;
    }

    public void setComeOnCount(int comeOnCount) {
        this.comeOnCount = comeOnCount;
    }

    public int getMaxComboCount() {
        return maxComboCount;
    }

    public void setMaxComboCount(int maxComboCount) {
        this.maxComboCount = maxComboCount;
    }

    public int getInterruptCount() {
        return interruptCount;
    }

    public void setInterruptCount(int interruptCount) {

        this.interruptCount = interruptCount;
    }

    public int getGoodScore() {
        return goodScore;
    }

    public void setGoodScore(int goodScore) {
        this.goodScore = goodScore;
    }

    public void setRank(int i) {
        rank = i;
    }

    public long getCheckTime() {
        return checkTime;
    }

    public int getRank() {
        return rank;
    }

    public int getMultiPersonIndex() {
        return multiPersonIndex;
    }

    public void setMultiPersonIndex(int multiPersonIndex) {
        this.multiPersonIndex = multiPersonIndex;
    }

    public String getSportSkuSubType() {
        return sportSkuSubType;
    }

    public void setSportSkuSubType(String sportSkuSubType) {
        this.sportSkuSubType = sportSkuSubType;
    }

    public int getSportViolationStatus() {
        return sportViolationStatus;
    }

    public void setSportViolationStatus(int sportViolationStatus) {
        this.sportViolationStatus = sportViolationStatus;
    }

    public String getSportViolationNumber() {
        return sportViolationNumber;
    }

    public void setSportViolationNumber(String sportViolationNumber) {
        this.sportViolationNumber = sportViolationNumber;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public String getReportCode() {
        return reportCode;
    }

    public void setReportCode(String reportCode) {
        this.reportCode = reportCode;
    }

    public String getReportVersion() {
        return reportVersion;
    }

    public void setReportVersion(String reportVersion) {
        this.reportVersion = reportVersion;
    }

    @Override
    public String toString() {
        return "SportResult{" +
                "sportDuration=" + sportDuration +
                ", sportAchievement=" + sportAchievement +
                ", userName='" + userName + '\'' +
                ", headUrl='" + headUrl + '\'' +
                '}';
    }
}
