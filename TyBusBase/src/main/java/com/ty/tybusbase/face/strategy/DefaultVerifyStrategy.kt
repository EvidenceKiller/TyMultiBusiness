package com.ty.tybusbase.face.strategy

import com.ty.tybusbase.utils.LogUtil

class DefaultVerifyStrategy: IVerifyStrategy {

    private val tempIdentityMap by lazy { HashMap<String, Pair<Int, Float>>() }

    private val defaultMinScoreThreshold = 0.5f //默认最小信任分数阈值

    private val defaultMinMatchTimes = 2 //默认最小匹配次数

    private val defaultMaxCountTimes = 4 //默认最大连续帧数

    private var minScoreThreshold = defaultMinScoreThreshold //最小之心分数阈值

    private var minMatchTimes = defaultMinMatchTimes //最小匹配次数

    private var maxCountTimes = defaultMaxCountTimes //连续帧数

    /**
     * 校验人员，如果，分数低于设定值（0.5）的，需要X次累计识别是同一个人，且其中x帧的分要达到一个阈值（如0.5），才认为是识别成功
     */
    override fun verifyUser(faceId: String, score: Float): Boolean {
        if (score >= 0.9f) {
            LogUtil.d("识别的置信分数达到0.9，直接确认身份")
            tempIdentityMap.clear()
            return true
        }
//        val studentEntity = findUerStrategy.findStudent(faceId)
        // 低分时，累计识别次数
        val pair = tempIdentityMap.getOrDefault(faceId, Pair(0, 0f))
        val count = pair.first + 1
        val times = if (score > minScoreThreshold) pair.second + 1 else pair.second
        return if (count >= maxCountTimes && times >= minMatchTimes) {
//            LogUtil.d("识别成功,达到累计策略${studentEntity?.accountName},当前帧置信分数： ${score},累计识别次数：$count,累计超分次数：$times")
            tempIdentityMap.clear()
            true
        } else {
//            LogUtil.d("累计帧数不够人员：${studentEntity?.accountName},当前帧置信分数： ${score},累计识别次数：$count,累计超分次数：$times")
            tempIdentityMap[faceId] = Pair(count, times)
            false
        }
    }

    fun setMinScoreThreshold(thres: Float) {
        minScoreThreshold = thres
    }

    fun setMinTimes(times: Int) {
        minMatchTimes = times
    }

    fun setMaxCountTimes(times: Int) {
        maxCountTimes = times
    }

    override fun reset() {
        minScoreThreshold = defaultMinScoreThreshold
        minMatchTimes = defaultMinMatchTimes
        maxCountTimes = defaultMaxCountTimes
    }
}