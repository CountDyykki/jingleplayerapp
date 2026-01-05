package com.example.jingleplayerapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.style.TextAlign

@Composable
fun NumberPickerHorizontal(
    state: MutableState<Int>,
    modifier: Modifier = Modifier,
    range: IntRange? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    onStateChanged: (Int) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val numbersRowWidth = 50.dp
    val halvedNumbersRowWidth = numbersRowWidth / 2
    val halvedNumbersRowWidthPx = with(LocalDensity.current) { halvedNumbersRowWidth.toPx() }
    fun animatedStateValue(offset: Float): Int = state.value - (offset / halvedNumbersRowWidthPx).toInt()
    val animatedOffset = remember { Animatable(0f) }.apply {
        if (range != null) {
            val offsetRange = remember(state.value, range) {
                val value = state.value
                val first = -(range.last - value) * halvedNumbersRowWidthPx
                val last = -(range.first - value) * halvedNumbersRowWidthPx
                first..last
            }
            updateBounds(offsetRange.start, offsetRange.endInclusive)
        }
    }
    val coercedAnimatedOffset = animatedOffset.value % halvedNumbersRowWidthPx
    val animatedStateValue = animatedStateValue(animatedOffset.value)
    Row(
        modifier = modifier
            .wrapContentSize()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { deltaX ->
                    coroutineScope.launch {
                        animatedOffset.snapTo(animatedOffset.value + deltaX)
                    }
                },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        val endValue = animatedOffset.fling(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = 20f),
                            adjustTarget = { target ->
                                val coercedTarget = target % halvedNumbersRowWidthPx
                                val coercedAnchors = listOf(-halvedNumbersRowWidthPx, 0f, halvedNumbersRowWidthPx)
                                val coercedPoint = coercedAnchors.minByOrNull { abs(it - coercedTarget) }!!
                                val base = halvedNumbersRowWidthPx * (target / halvedNumbersRowWidthPx).toInt()
                                coercedPoint + base
                            }
                        ).endState.value
                        state.value = animatedStateValue(endValue)
                        onStateChanged(state.value)
                        animatedOffset.snapTo(0f)
                    }
                }
            )
    ) {
        val spacing = 20.dp
        Icon(
            painter = painterResource(R.drawable.chevron_left_24px),
                contentDescription = "Down",
        )
        Spacer(modifier = Modifier.width(spacing))
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .offset { IntOffset(x = coercedAnimatedOffset.roundToInt(), y = 0) }
        ) {
            val baseLabelModifier = Modifier.align(Alignment.Center)
            ProvideTextStyle(textStyle) {
                Label(
                    text = (animatedStateValue - 1).toString(),
                    modifier = baseLabelModifier
                        .offset(x = -halvedNumbersRowWidth)
                        .alpha(coercedAnimatedOffset / halvedNumbersRowWidthPx)
                )

                Text(
                    text = animatedStateValue.toString(),
                    modifier = baseLabelModifier.width(20.dp)
                        .alpha(1 - abs(coercedAnimatedOffset) / halvedNumbersRowWidthPx),
                    textAlign= TextAlign.Center
                )
                Label(
                    text = (animatedStateValue + 1).toString(),
                    modifier = baseLabelModifier
                        .offset(x = halvedNumbersRowWidth)
                        .alpha(-coercedAnimatedOffset / halvedNumbersRowWidthPx)
                )
            }
        }
        Spacer(modifier = Modifier.width(spacing))
        Icon(
            painter = painterResource(R.drawable.chevron_right_24px),
            contentDescription = "Up",
        )
    }
}

@Composable
private fun Label(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onLongPress = {
                // FIXME: Empty to disable text selection
            })
        }
    )
}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
    initialVelocity: Float,
    animationSpec: DecayAnimationSpec<Float>,
    adjustTarget: ((Float) -> Float)?,
    block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
    val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
    val adjustedTarget = adjustTarget?.invoke(targetValue)
    return if (adjustedTarget != null) {
        animateTo(
            targetValue = adjustedTarget,
            initialVelocity = initialVelocity,
            block = block
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}