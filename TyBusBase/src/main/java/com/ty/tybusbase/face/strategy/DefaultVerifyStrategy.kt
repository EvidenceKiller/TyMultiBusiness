package com.ty.tybusbase.face.strategy

class DefaultVerifyStrategy: IVerifyStrategy {

    private val tempIdentityMap by lazy { HashMap<String, Pair<Int, Float>>() }

    private var minScoreThreshold = 0.5f //最小之心分数阈值

    private var minMatchTimes = 2 //最小匹配次数

    private var maxCountTimes = 4 //连续帧数

    override fun verifyUser(faceId: String, score: Float): Boolean {
        if (score >= 0.9f) {
            tempIdentityMap.clear()
            return true
        }
        // 低分时，累计识别次数
        val pair = tempIdentityMap.getOrDefault(faceId, Pair(0, 0f))
        val count = pair.first + 1
        val times = if (score > minScoreThreshold) pair.second + 1 else pair.second
        tempIdentityMap.clear()
        return if (count >= maxCountTimes && times >= minMatchTimes) {
            true
        } else {
            tempIdentityMap[faceId] = Pair(count, times)
            false
        }
    }
}