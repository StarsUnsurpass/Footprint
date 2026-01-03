package com.footprint.utils

import com.footprint.data.model.Mood
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object AIStoryGenerator {

    private val timeDescriptors = listOf(
        "dawn" to "晨光熹微",
        "morning" to "阳光正好",
        "noon" to "烈日当空",
        "afternoon" to "午后慵懒",
        "dusk" to "暮色四合",
        "night" to "霓虹闪烁",
        "midnight" to "万籁俱寂"
    )

    private val cyberPrefixes = listOf(
        "数据流穿过", "全息投影映照着", "赛博空间的低语中，", "在合成光芒的照耀下，", "伴随着远处飞行车的轰鸣，"
    )

    fun generateStory(location: String, mood: Mood, date: LocalDate): String {
        val weather = listOf("微雨", "晴朗", "多云", "赛博雾霾", "电磁风暴").random()
        val timeDesc = timeDescriptors.random().second
        
        val moodDesc = when(mood) {
            Mood.HAPPY -> "心中充满愉悦的频率。"
            Mood.SAD -> "思绪像断连的数据包一样零乱。"
            Mood.EXCITED -> "肾上腺素随着城市的脉搏一起跳动。"
            Mood.TIRED -> "义体似乎发出了过载的警告，急需充能。"
            Mood.NEUTRAL -> "保持着观察者的冷静与疏离。"
            else -> "情绪模块运转正常。"
        }

        val templates = listOf(
            "今天是 $date。来到 $location，这里$weather。$timeDesc，我感到$moodDesc",
            "坐标锁定：$location。时间戳：$date。环境扫描显示$weather。$moodDesc 这是一次难忘的记录。",
            "${cyberPrefixes.random()} 我抵达了 $location。在这个$weather 的日子里，只为了寻找那一抹独特的色彩。$moodDesc",
            "$location 的空气中弥漫着$weather 的味道。$timeDesc，我在这里留下了足迹。$moodDesc",
            "记忆转存中... 地点：$location，日期：$date。状态：$weather。备注：$moodDesc"
        )

        return templates.random()
    }
}
