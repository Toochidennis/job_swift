package com.toochi.job_swift.common.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.core.view.isVisible
import com.toochi.job_swift.databinding.DialogAlertBinding

class AlertDialog private constructor(builder: Builder) {

    private val dialog = Dialog(builder.context)
    private val binding: DialogAlertBinding = DialogAlertBinding.inflate(dialog.layoutInflater)

    init {
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            setContentView(binding.root)

            binding.titleTextView.text = builder.title
            binding.bodyTextView.text = builder.body
            binding.negativeButton.text = builder.negativeMessage
            binding.positiveButton.text = builder.positiveMessage

            binding.positiveButton.isVisible = builder.isPositiveVisible
            binding.negativeButton.isVisible = builder.isNegativeVisible


            binding.positiveButton.setOnClickListener {
                builder.positiveClickListener?.invoke()
                dismiss()
            }

            binding.negativeButton.setOnClickListener {
                builder.negativeClickListener?.invoke()
                dismiss()
            }
        }
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    class Builder(val context: Context) {
        var title: String = ""
        var body: String = ""
        var negativeMessage: String = ""
        var positiveMessage: String = ""
        var isPositiveVisible: Boolean = false
        var isNegativeVisible: Boolean = false
        var positiveClickListener: (() -> Unit)? = null
        var negativeClickListener: (() -> Unit)? = null

        fun build(): AlertDialog {
            return AlertDialog(this)
        }
    }

}