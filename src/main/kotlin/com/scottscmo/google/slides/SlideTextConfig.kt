package com.scottscmo.google.slides

data class SlideTextConfig(
    val alignment: String = "",
    val margin: Double = -1.0,
    val fontFamily: String = "",
    val fontSize: Double,
    val fontColor: String = "",
    val fontStyles: String = "",
    val x: Int,
    val y: Int,
)
