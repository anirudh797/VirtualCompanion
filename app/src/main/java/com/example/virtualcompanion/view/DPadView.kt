package com.example.virtualcompanion.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import com.example.virtualcompanion.R
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min

class DPadView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs), GestureDetector.OnGestureListener {

    /**
     * TextStyle Flags for center text
     */
    enum class TextStyle(val style: Int) {
        NORMAL(0),
        BOLD(1),
        ITALIC(2),
        UNDERLINE(4)
    }

    /**
     * Size mode for the center icon
     */
    enum class CenterIconSizeMode {
        WRAP,
        FIXED
    }

    enum class Direction {
        UP, DOWN, LEFT, RIGHT, CENTER
    }

    /**
     * Color of the direction section when it is pressed
     */
    @ColorInt
    var normalColor: Int

    /**
     * Color of the direction section when it is not pressed
     */
    @ColorInt
    var pressedColor: Int

    /**
     * Angle for each direction section (should be between 1 and 90)
     */
    @FloatRange(fromInclusive = true, from = 1.0, to = 90.0, toInclusive = true)
    var directionSectionAngle: Float

    /**
     * Padding for the image to be displayed
     */
    var padding = 20f


    /**
     * Whether to enable the center circle
     */
    var isCenterCircleEnabled = true

    /**
     * If center circle is enabled, whether it should detect touch on them
     */
    var isCenterCirclePressEnabled = true

    /**
     * Color of the center circle when it is not pressed
     */
    @ColorInt
    var centerCircleNormalColor: Int

    /**
     * Color of the center circle when it is pressed
     */
    @ColorInt
    var centerCirclePressedColor: Int

    /**
     * The size of the center circle will be the size of view divided by this value
     */
    var centerCircleRatio = 3.5f


    /**
     *  Text to be displayed in the center (e.g. OK)
     */
    var centerText: String? = null

    /**
     * Text size for the center text
     */
    var centerTextSize: Float

    /**
     * Text color for the center text
     */
    @ColorInt
    var centerTextColor: Int

    /**
     * Text style for the center text, can be combination like `BOLD or ITALIC or UNDERLINE`
     */
    var centerTextStyle: Int


    /**
     * The drawable to be displayed in the center
     */
    @DrawableRes
    var centerIcon: Int? = null

    private var size = 0

    private var circleCenter = size.toFloat() / 2
    private var centerCircleRadius = size / centerCircleRatio

    /**
     * Size mode for the icon, whether the size should be fixed or it should wrap the image src
     */
    var centerIconSizeMode: CenterIconSizeMode

    /**
     * Icon size to be used if centerIconSizeMode is set to fixed
     */
    var centerIconSize: Float

    /**
     * The color to tint the center icon with
     */
    @ColorInt
    var centerIconTint: Int

    /**
     * Direction Presses Listener
     */
    var onDirectionPressListener: (direction: Direction?, action: Int) -> Unit = { _, _ -> }

    /**
     * Direction Clicks Listener
     */
    var onDirectionClickListener: (direction: Direction?) -> Unit = {}

    /**
     * When DPad Center button is Long Clicked, this method is called
     */
    var onCenterLongClick = {}

    private var centerIconDrawable: Drawable? = null
    private var textPaint = Paint()
    private val centerCirclePaint = Paint()
    private val upPaint = Paint()
    private val downPaint = Paint()
    private val leftPaint = Paint()
    private val rightPaint = Paint()
    private var upTouched = false
    private var downTouched = false
    private var leftTouched = false
    private var rightTouched = false
    private var centerCircleTouched = false
    private val halfPadding = padding / 2
    private var sectionAngleHalf = 88f / 2
    private var clipBoundsRect = Rect()
    private var textBoundsRect = Rect()
    private var centerIconRect = Rect()
    private var arcsRect = RectF()
    private var detector: GestureDetector

    private var downLowerLimit = 90 - sectionAngleHalf
    private var downUpperLimit = 90 + sectionAngleHalf

    private var upLowerLimit = 270 - sectionAngleHalf
    private var upUpperLimit = 270 + sectionAngleHalf

    private var leftLowerLimit = 180 - sectionAngleHalf
    private var leftUpperLimit = 180 + sectionAngleHalf

    private var rightLowerLimit = 360 - sectionAngleHalf
    private var rightUpperLimit = sectionAngleHalf

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.DPadView, 0, 0).apply {
            normalColor = getColor(R.styleable.DPadView_normalColor, context.resolveColorAttr(android.R.attr.textColorSecondary))
            pressedColor = getColor(R.styleable.DPadView_pressedColor, context.resolveColorAttr(android.R.attr.colorPrimary))

            val angle = getFloat(R.styleable.DPadView_directionSectionAngle, 88f)
            directionSectionAngle = if (angle > 90f || angle < 1f) {
                88f
            } else {
                angle
            }

            centerCircleNormalColor = getColor(R.styleable.DPadView_centerCircleNormalColor, context.resolveColorAttr(
                androidx.transition.R.attr.colorPrimary))
            centerCirclePressedColor = getColor(R.styleable.DPadView_centerCirclePressedColor, context.resolveColorAttr(
                androidx.appcompat.R.attr.colorPrimaryDark))
            isCenterCircleEnabled = getBoolean(R.styleable.DPadView_centerCircleEnabled, true)
            isCenterCirclePressEnabled = getBoolean(R.styleable.DPadView_centerCirclePressEnabled, true)
            centerText = getString(R.styleable.DPadView_centerText)
            centerIcon = getResourceId(R.styleable.DPadView_centerIcon, 0)
            centerCircleRatio = getFloat(R.styleable.DPadView_centerCircleRatio, 3.5f)
            padding = getDimension(R.styleable.DPadView_imagePadding, 20f)

            centerIconSizeMode = if (getInt(R.styleable.DPadView_centerIconSizeMode, 0) == 1) CenterIconSizeMode.FIXED else CenterIconSizeMode.WRAP
            centerIconSize = getDimension(R.styleable.DPadView_centerIconSize, 24f)
            centerIconTint = getColor(R.styleable.DPadView_centerIconTint, 0)

            centerTextSize = getDimension(R.styleable.DPadView_centerTextSize, 14f)
            val defaultTextColor = if (ColorUtils.calculateLuminance(centerCircleNormalColor) < 0.5f) {
                Color.WHITE
            } else {
                Color.BLACK
            }
            centerTextColor = getColor(R.styleable.DPadView_centerTextColor, defaultTextColor)
            centerTextStyle = getInteger(R.styleable.DPadView_centerTextStyle, 0)

            recycle()

            detector = GestureDetector(context, this@DPadView).apply {
                setIsLongpressEnabled(true)
            }

            init()
        }
    }

    private fun init() {
        centerIconDrawable = if (centerIcon != null) {
            if (centerIcon != 0) {
                ContextCompat.getDrawable(context, centerIcon!!)
            } else null
        } else null

        if (centerIconDrawable != null) {
            if (centerIconTint != 0) {
                DrawableCompat.setTint(centerIconDrawable!!, centerIconTint)
            }
        }

        textPaint.apply {
            color = centerTextColor
            textSize = centerTextSize
            flags = if (centerTextStyle and 4 == 4) {
                Paint.UNDERLINE_TEXT_FLAG
            } else {
                0
            }
            val typeface = if (centerTextStyle and 1 == 1 && centerTextStyle and 2 == 2) {
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            } else if (centerTextStyle and 1 == 1) {
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            } else if (centerTextStyle and 2 == 2) {
                Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            } else {
                null
            }
            setTypeface(typeface)
        }

        upPaint.color = normalColor
        downPaint.color = normalColor
        rightPaint.color = normalColor
        leftPaint.color = normalColor

        centerCirclePaint.color = centerCircleNormalColor

        sectionAngleHalf = directionSectionAngle / 2

        downLowerLimit = 90 - sectionAngleHalf
        downUpperLimit = 90 + sectionAngleHalf

        upLowerLimit = 270 - sectionAngleHalf
        upUpperLimit = 270 + sectionAngleHalf

        leftLowerLimit = 180 - sectionAngleHalf
        leftUpperLimit = 180 + sectionAngleHalf

        rightLowerLimit = 360 - sectionAngleHalf
        rightUpperLimit = sectionAngleHalf

        setPadding(padding.toInt(), padding.toInt(), padding.toInt(), padding.toInt())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        size = min(measuredWidth, measuredHeight)
        circleCenter = size.toFloat() / 2
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        if (canvas == null) return
        circleCenter = size.toFloat() / 2
        centerCircleRadius = size / centerCircleRatio

        canvas.getClipBounds(clipBoundsRect)

        arcsRect.set(clipBoundsRect.left.toFloat(), clipBoundsRect.top.toFloat(), clipBoundsRect.right.toFloat(), clipBoundsRect.bottom.toFloat())

        canvas.drawArc(arcsRect, downLowerLimit, directionSectionAngle, true, downPaint)
        canvas.drawArc(arcsRect, leftLowerLimit, directionSectionAngle, true, leftPaint)
        canvas.drawArc(arcsRect, upLowerLimit, directionSectionAngle, true, upPaint)
        canvas.drawArc(arcsRect, rightLowerLimit, directionSectionAngle, true, rightPaint)

        if (isCenterCircleEnabled) {
            canvas.drawCircle(circleCenter, circleCenter, centerCircleRadius, centerCirclePaint)
        }

        super.onDraw(canvas)

        centerIconDrawable?.let {
            if (centerIconSizeMode == CenterIconSizeMode.WRAP) {
                centerIconRect.set(
                    circleCenter.toInt() - it.intrinsicWidth / 2,
                    circleCenter.toInt() - it.intrinsicHeight / 2,
                    circleCenter.toInt() + it.intrinsicWidth / 2,
                    circleCenter.toInt() + it.intrinsicHeight / 2
                )
            } else {
                centerIconRect.set(
                    circleCenter.toInt() - centerIconSize.toInt() / 2,
                    circleCenter.toInt() - centerIconSize.toInt() / 2,
                    circleCenter.toInt() + centerIconSize.toInt() / 2,
                    circleCenter.toInt() + centerIconSize.toInt() / 2
                )
            }
            it.bounds = centerIconRect
            it.draw(canvas)
        }

        if (!centerText.isNullOrEmpty()) {
            textPaint.getTextBounds(centerText, 0, centerText!!.length, textBoundsRect)
            val textX = clipBoundsRect.width() / 2f - textBoundsRect.width() / 2f - textBoundsRect.left
            val textY = clipBoundsRect.height() / 2f + textBoundsRect.height() / 2f - textBoundsRect.bottom
            canvas.drawText(centerText!!, textX, textY, textPaint)
        }
    }

    fun reInit() {
        init()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        val isInCircle = isInCircle(event.x, event.y, circleCenter.toInt(), circleCenter - halfPadding)
        val isInCenterCircle = if (isCenterCircleEnabled && isCenterCirclePressEnabled) {
            isInCircle(event.x, event.y, circleCenter.toInt(), centerCircleRadius - halfPadding)
        } else false

        if (isInCircle) {
            centerCircleTouched = false

            rightTouched = false
            leftTouched = false
            upTouched = false
            downTouched = false

            if (event.action == MotionEvent.ACTION_DOWN) performClick()
            val isTouched = event.isTouched

            if (isInCenterCircle) {
                kotlin.run {
                detector.onTouchEvent(event)
                } ?: return false
                centerCircleTouched = true
            } else {
                var deg = Math.toDegrees(atan2(event.y - width / 2, event.x - width / 2).toDouble())
                if (deg < 0) {
                    deg += 360
                }

                if (deg > rightLowerLimit || deg <= rightUpperLimit) {
                    rightTouched = true
                } else if (deg in downLowerLimit..downUpperLimit) {
                    downTouched = true
                } else if (deg in leftLowerLimit..leftUpperLimit) {
                    leftTouched = true
                } else if (deg in upLowerLimit..upUpperLimit) {
                    upTouched = true
                }
            }

            val d = getDirection()

            if (event.action == MotionEvent.ACTION_DOWN && d != Direction.CENTER) onDirectionClickListener(d)

            onDirectionPressListener(d, event.action)

            centerCircleTouched = centerCircleTouched and isTouched
            upTouched = upTouched and isTouched
            downTouched = downTouched and isTouched
            leftTouched = leftTouched and isTouched
            rightTouched = rightTouched and isTouched
        } else {
            getDirection()?.let {
                onDirectionPressListener(it, MotionEvent.ACTION_UP)
                when (it) {
                    Direction.CENTER -> centerCircleTouched = false
                    Direction.LEFT -> leftTouched = false
                    Direction.RIGHT -> rightTouched = false
                    Direction.UP -> upTouched = false
                    Direction.DOWN -> downTouched = false
                }
            }
        }

        centerCirclePaint.color = if (!centerCircleTouched) centerCircleNormalColor else centerCirclePressedColor
        rightPaint.color = if (!rightTouched) normalColor else pressedColor
        downPaint.color = if (!downTouched) normalColor else pressedColor
        leftPaint.color = if (!leftTouched) normalColor else pressedColor
        upPaint.color = if (!upTouched) normalColor else pressedColor

        if (isHapticFeedbackEnabled && event.action == MotionEvent.ACTION_DOWN) {
            if (centerCircleTouched || rightTouched || leftTouched || upTouched || downTouched) {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }

        invalidate()

        return isInCircle
    }

    private val MotionEvent.isTouched
        get() = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE

    private fun isInCircle(x: Float, y: Float, center: Int, radius: Float): Boolean {
        val dx = abs(x - center)
        if (dx > radius) return false

        val dy = abs(y - center)
        if (dy > radius) return false

        if (dx + dy <= radius) return true
        return dx * dx + dy * dy <= radius * radius
    }

    fun modify(func: DPadView.() -> Unit) {
        func()
        reInit()
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        onDirectionClickListener(Direction.CENTER)
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
       return true
    }

    override fun onDown(e: MotionEvent): Boolean = true

    override fun onFling(
        p0: MotionEvent?,
        e1: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean = false


    override fun onLongPress(e: MotionEvent) {
        onCenterLongClick()
        centerCircleTouched = false
    }


    private fun getDirection(): Direction? = when {
        centerCircleTouched -> Direction.CENTER
        upTouched -> Direction.UP
        downTouched -> Direction.DOWN
        leftTouched -> Direction.LEFT
        rightTouched -> Direction.RIGHT
        else -> null
    }
}

@ColorInt
fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
    val resolvedAttr = resolveThemeAttr(colorAttr)
    // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
    val colorRes = if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
    return ContextCompat.getColor(this, colorRes)
}

fun Context.resolveThemeAttr(@AttrRes attrRes: Int): TypedValue {
    val theme = theme
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue
}