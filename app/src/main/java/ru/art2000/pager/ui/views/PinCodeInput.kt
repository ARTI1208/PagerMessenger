package ru.art2000.pager.ui.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginRight
import androidx.core.view.updatePadding
import ru.art2000.pager.R
import ru.art2000.pager.databinding.PinInputLayoutBinding

class PinCodeInput(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        const val MIN_NUMBER_COUNT = 4
        const val MAX_NUMBER_COUNT = 8
    }

    private val input = mutableListOf<Int>()

    public var biometricPromptOpener: (() -> Unit)? = null
        set(value) {
            setBiometricVisible(value != null)
            field = value
        }

//    public var verifyOnInput: Boolean = false
//        set(value) {
//            setOkButtonVisible(!verifyOnInput)
//            field = value
//        }

    public var onInput: (String) -> Boolean = { false }
    public var onOkPressed: (String) -> Unit = { }

    private val dotNoInput = ContextCompat.getDrawable(context, R.drawable.pin_dot)!!
    private val dotWithInput = ContextCompat.getDrawable(context, R.drawable.pin_dot_full)!!

    private val biometricPadButton: Button
    private val okPadButton: Button

    private val dotsLayout = LinearLayout(context)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
            this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context): this(context, null)

    init {

        val pinBinding = PinInputLayoutBinding.inflate(LayoutInflater.from(context))
        addView(pinBinding.root, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        pinBinding.root.layoutParams = (pinBinding.root.layoutParams as LayoutParams).apply {
            addRule(ALIGN_PARENT_BOTTOM)
            addRule(CENTER_HORIZONTAL)
        }

        updatePadding(bottom = context.resources.getDimensionPixelSize(R.dimen.pin_pad_bottom_margin))

        val numButtons = listOf(
            pinBinding.pad0, pinBinding.pad1, pinBinding.pad2, pinBinding.pad3, pinBinding.pad4,
            pinBinding.pad5, pinBinding.pad6, pinBinding.pad7, pinBinding.pad8, pinBinding.pad9,
        )

        numButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                appendNum(index)
            }
        }

        pinBinding.padDelete.setOnClickListener {
            removeLastNum()
        }

        pinBinding.padDelete.setOnLongClickListener {
            clearPinInput()
            true
        }

        biometricPadButton = pinBinding.padBiometrics
        biometricPadButton.setOnClickListener {
            openBiometricPrompt()
        }

        okPadButton = pinBinding.padOk
        okPadButton.setOnClickListener {
            onOkPressed(inputToString())
        }

        setBiometricVisible(false)



        dotsLayout.gravity = Gravity.CENTER
        dotsLayout.orientation = LinearLayout.HORIZONTAL
        dotsLayout.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 60).apply {
            addRule(ABOVE, pinBinding.pinPadRoot.id)
            bottomMargin = context.resources.getDimensionPixelSize(R.dimen.pin_pad_top_margin)
        }

        calculateDots(false)

        addView(dotsLayout)
    }

    private fun inputToString() = input.joinToString("")

    private fun createDotView(drawable: Drawable, withMargin: Boolean = true): ImageView {
        return ImageView(context).apply {
            setImageDrawable(drawable)
            layoutParams = MarginLayoutParams(40, 40).apply {
                if (withMargin) {
                    rightMargin = 40
                }
            }
        }
    }

    private fun calculateDots(addOneMoreDot: Boolean = false) {
        dotsLayout.removeAllViews()

        val totalDotsCount = input.size.coerceAtLeast(MIN_NUMBER_COUNT) +
                if (addOneMoreDot && input.size in MIN_NUMBER_COUNT until MAX_NUMBER_COUNT) 1 else 0

        repeat(input.size) {
            dotsLayout.addView(createDotView(dotWithInput, it < totalDotsCount - 1))
        }

        for (i in input.size until MIN_NUMBER_COUNT) {
            dotsLayout.addView(createDotView(dotNoInput, i < totalDotsCount - 1))
        }

        if (addOneMoreDot && input.size in MIN_NUMBER_COUNT until MAX_NUMBER_COUNT) {
            dotsLayout.addView(createDotView(dotNoInput))
        }
    }

    private fun appendNum(num: Int) {
        if (num < 0 || num > 9 || input.size >= MAX_NUMBER_COUNT) return
        input.add(num)
        if (onInput(inputToString())) {
            calculateDots()
        } else {
            calculateDots(true)
        }
    }

    private fun removeLastNum() {
        input.removeLastOrNull()
        calculateDots(true)
    }

    private fun clearPinInput() {
        input.clear()
        calculateDots()
    }

    private fun openBiometricPrompt() {
        biometricPromptOpener?.invoke()
    }

    private fun setBiometricVisible(isVisible: Boolean) {
        biometricPadButton.visibility = if (isVisible) View.VISIBLE else View.GONE
        okPadButton.visibility = if (isVisible) View.GONE else View.VISIBLE
    }
}