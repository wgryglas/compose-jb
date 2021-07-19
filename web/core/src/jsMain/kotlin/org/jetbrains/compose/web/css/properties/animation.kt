/*
 * Copyright 2020-2021 JetBrains s.r.o. and respective authors and developers.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package org.jetbrains.compose.web.css

@Suppress("EqualsOrHashCode")
data class CSSAnimation(
    val keyframesName: String,
    var duration: List<CSSSizeValue<out CSSUnitTime>>? = null,
    var timingFunction: List<Any>? = null,
    var delay: List<CSSSizeValue<out CSSUnitTime>>? = null,
    var iterationCount: List<Int?>? = null,
    var direction: List<Any>? = null,
    var fillMode: List<AnimationFillMode>? = null,
    var playState: List<Any>? = null
) : CSSStyleValue {
    override fun toString(): String {
        val values = listOfNotNull(
            keyframesName,
            duration?.joinToString(", "),
            timingFunction?.joinToString(", "),
            delay?.joinToString(", "),
            iterationCount?.joinToString(", ") { it?.toString() ?: "infinite" },
            direction?.joinToString(", "),
            fillMode?.joinToString(", "),
            playState?.joinToString(", ")
        )
        return values.joinToString(" ")
    }
}

inline fun CSSAnimation.duration(vararg values: CSSSizeValue<out CSSUnitTime>) {
    this.duration = values.toList()
}


inline fun CSSAnimation.timingFunction(vararg values: BasicStringProperty) {
    this.timingFunction = values.toList()
}

inline fun CSSAnimation.timingFunction(vararg values: AnimationTimingFunction) {
    this.timingFunction = values.toList()
}

inline fun CSSAnimation.delay(vararg values: CSSSizeValue<out CSSUnitTime>) {
    this.delay = values.toList()
}

inline fun CSSAnimation.iterationCount(vararg values: Int?) {
    this.iterationCount = values.toList()
}


inline fun CSSAnimation.direction(vararg values: BasicStringProperty) {
    this.direction = values.toList()
}

inline fun CSSAnimation.direction(vararg values: AnimationDirection) {
    this.direction = values.toList()
}

inline fun CSSAnimation.fillMode(vararg values: AnimationFillMode) {
    this.fillMode = values.toList()
}

inline fun CSSAnimation.playState(vararg values: BasicStringProperty) {
    this.playState = values.toList()
}

inline fun CSSAnimation.playState(vararg values: AnimationPlayState) {
    this.playState = values.toList()
}

fun StyleBuilder.animation(
    keyframesName: String,
    builder: CSSAnimation.() -> Unit
) {
    val animation = CSSAnimation(keyframesName).apply(builder)
    property("animation", animation)
}

inline fun StyleBuilder.animation(
    keyframes: CSSNamedKeyframes,
    noinline builder: CSSAnimation.() -> Unit
) = animation(keyframes.name, builder)


