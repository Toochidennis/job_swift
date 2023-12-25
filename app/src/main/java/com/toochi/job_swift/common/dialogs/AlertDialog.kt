package com.toochi.job_swift.common.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.toochi.job_swift.databinding.DialogAlertBinding

class AlertDialog(context: Context) {

    private val dialog = Dialog(context)

    private var _binding: DialogAlertBinding? = null
    private var _message: String? = null
    private var _negativeMessage: String? = null
    private var _positiveMessage: String? = null

    private val binding get() = _binding!!

    private val message get() = _message!!

    private val negativeMessage get() = _negativeMessage!!

    private val positiveMessage get() = _positiveMessage!!

    init {
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            _binding = DialogAlertBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.messageTextView.text = message
            binding.negativeButton.text = negativeMessage
            binding.positiveButton.text = positiveMessage

            setOnDismissListener {
                _binding = null
            }
        }
    }

    fun message(message: String) {
        _message = message
    }

    fun negativeMessage(message: String) {
        _negativeMessage = message
    }

    fun positiveMessage(message: String) {
        _positiveMessage = message
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}