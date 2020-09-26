package com.neeplayer.compose

import androidx.compose.animation.asDisposableClock
import androidx.compose.animation.core.AnimationClockObservable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.Saver
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Possible values of [BottomDrawerState].
 */
enum class BottomDrawerValue {
    /**
     * The state of the bottom drawer when it is closed.
     */
    Closed,

    /**
     * The state of the bottom drawer when it is expanded (i.e. at 100% height).
     */
    Expanded
}

/**
 * State of the [BottomDrawerLayout] composable.
 *
 * @param initialValue The initial value of the state.
 * @param clock The animation clock that will be used to drive the animations.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@Suppress("NotCloseable")
class BottomDrawerState(
    initialValue: BottomDrawerValue,
    clock: AnimationClockObservable,
    confirmStateChange: (BottomDrawerValue) -> Boolean = { true }
) : SwipeableState<BottomDrawerValue>(
    initialValue = initialValue,
    clock = clock,
    animationSpec = AnimationSpec,
    confirmStateChange = confirmStateChange
) {
    /**
     * Whether the drawer is closed.
     */
    val isClosed: Boolean
        get() = value == BottomDrawerValue.Closed

    /**
     * Whether the drawer is expanded.
     */
    val isExpanded: Boolean
        get() = value == BottomDrawerValue.Expanded

    /**
     * Close the drawer with an animation.
     *
     * @param onClosed Optional callback invoked when the drawer has finished closing.
     */
    fun close(onClosed: (() -> Unit)? = null) {
        animateTo(BottomDrawerValue.Closed, onEnd = { endReason, endValue ->
            if (endReason != AnimationEndReason.Interrupted &&
                endValue == BottomDrawerValue.Closed
            ) {
                onClosed?.invoke()
            }
        })
    }

    /**
     * Expand the drawer with an animation.
     *
     * @param onExpanded Optional callback invoked when the drawer has finished expanding.
     */
    fun expand(onExpanded: (() -> Unit)? = null) {
        animateTo(BottomDrawerValue.Expanded, onEnd = { endReason, endValue ->
            if (endReason != AnimationEndReason.Interrupted &&
                endValue == BottomDrawerValue.Expanded
            ) {
                onExpanded?.invoke()
            }
        })
    }

    companion object {
        /**
         * The default [Saver] implementation for [BottomDrawerState].
         */
        fun Saver(
            clock: AnimationClockObservable,
            confirmStateChange: (BottomDrawerValue) -> Boolean
        ) = Saver<BottomDrawerState, BottomDrawerValue>(
            save = { it.value },
            restore = { BottomDrawerState(it, clock, confirmStateChange) }
        )
    }
}

/**
 * Create and [remember] a [BottomDrawerState] with the default animation clock.
 *
 * @param initialValue The initial value of the state.
 * @param confirmStateChange Optional callback invoked to confirm or veto a pending state change.
 */
@Composable
fun rememberBottomDrawerState(
    initialValue: BottomDrawerValue,
    confirmStateChange: (BottomDrawerValue) -> Boolean = { true }
): BottomDrawerState {
    val clock = AnimationClockAmbient.current.asDisposableClock()
    return rememberSavedInstanceState(
        clock,
        saver = BottomDrawerState.Saver(clock, confirmStateChange)
    ) {
        BottomDrawerState(initialValue, clock, confirmStateChange)
    }
}

/**
 * Navigation drawers provide access to destinations in your app.
 *
 * Bottom navigation drawers are modal drawers that are anchored
 * to the bottom of the screen instead of the left or right edge.
 * They are only used with bottom app bars.
 *
 * These drawers open upon tapping the navigation menu icon in the bottom app bar.
 * They are only for use on mobile.
 *
 * See [ModalDrawerLayout] for a layout that introduces a classic from-the-side drawer.
 *
 * @sample androidx.compose.material.samples.BottomDrawerSample
 *
 * @param drawerState state of the drawer
 * @param modifier optional modifier for the drawer
 * @param gesturesEnabled whether or not drawer can be interacted by gestures
 * @param drawerShape shape of the drawer sheet
 * @param drawerElevation drawer sheet elevation. This controls the size of the shadow below the
 * drawer sheet
 * @param drawerContent composable that represents content inside the drawer
 * @param drawerBackgroundColor background color to be used for the drawer sheet
 * @param drawerContentColor color of the content to use inside the drawer sheet. Defaults to
 * either the matching `onFoo` color for [drawerBackgroundColor], or, if it is not a color from
 * the theme, this will keep the same value set above this Surface.
 * @param scrimColor color of the scrim that obscures content when the drawer is open
 * @param bodyContent content of the rest of the UI
 *
 * @throws IllegalStateException when parent has [Float.POSITIVE_INFINITY] height
 */
@Composable
fun BottomDrawerLayout(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: BottomDrawerState = rememberBottomDrawerState(BottomDrawerValue.Closed),
    gesturesEnabled: Boolean = true,
    drawerShape: Shape = MaterialTheme.shapes.large,
    drawerElevation: Dp = DrawerConstants.DefaultElevation,
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    scrimColor: Color = MaterialTheme.colors.onSurface.copy(alpha = DrawerConstants.ScrimDefaultOpacity),
    closedAnchorOffset: Dp = 0.dp,
    bodyContent: @Composable () -> Unit
) {
    WithConstraints(modifier.fillMaxSize()) {
        // TODO : think about Infinite max bounds case
        if (!constraints.hasBoundedHeight) {
            throw IllegalStateException("Drawer shouldn't have infinite height")
        }

        val minValue = 0f
        val maxValue = constraints.maxHeight.toFloat() - DensityAmbient.current.run {closedAnchorOffset.toPx() }

        val anchors = mapOf(
            maxValue to BottomDrawerValue.Closed,
            minValue to BottomDrawerValue.Expanded
        )
        Stack {
            Stack {
                bodyContent()
            }
            Scrim(
                open = drawerState.isExpanded,
                onClose = { drawerState.close() },
                fraction = {
                    // as we scroll "from height to 0" , need to reverse fraction
                    1 - calculateFraction(minValue, maxValue, drawerState.offset.value)
                },
                color = scrimColor
            )
            Surface(
                modifier = with(DensityAmbient.current) {
                    Modifier.preferredSizeIn(
                        minWidth = constraints.minWidth.toDp(),
                        minHeight = constraints.minHeight.toDp(),
                        maxWidth = constraints.maxWidth.toDp(),
                        maxHeight = constraints.maxHeight.toDp()
                    )
                }
                    .offsetPx(y = drawerState.offset)
                    .swipeable(
                        state = drawerState,
                        anchors = anchors,
                        thresholds = { _, _ -> FixedThreshold(BottomDrawerThreshold) },
                        orientation = Orientation.Vertical,
                        enabled = gesturesEnabled,
                        resistance = null
                    ),
                shape = drawerShape,
                color = drawerBackgroundColor,
                contentColor = drawerContentColor,
                elevation = drawerElevation
            ) {
                Column(Modifier.fillMaxSize(), children = drawerContent)
            }
        }
    }
}

/**
 * Object to hold default values for [ModalDrawerLayout] and [BottomDrawerLayout]
 */
object DrawerConstants {

    /**
     * Default Elevation for drawer sheet as specified in material specs
     */
    val DefaultElevation = 16.dp

    /**
     * Default alpha for scrim color
     */
    const val ScrimDefaultOpacity = 0.32f
}

private fun calculateFraction(a: Float, b: Float, pos: Float) =
    ((pos - a) / (b - a)).coerceIn(0f, 1f)

@Composable
private fun Scrim(
    open: Boolean,
    onClose: () -> Unit,
    fraction: () -> Float,
    color: Color
) {
    val dismissDrawer = if (open) {
        Modifier.tapGestureFilter { onClose() }
    } else {
        Modifier
    }

    Canvas(
        Modifier
            .fillMaxSize()
            .then(dismissDrawer)
    ) {
        drawRect(color, alpha = fraction())
    }
}

private const val DrawerStiffness = 1000f

private val AnimationSpec = SpringSpec<Float>(stiffness = DrawerStiffness)

internal val BottomDrawerThreshold = 56.dp
