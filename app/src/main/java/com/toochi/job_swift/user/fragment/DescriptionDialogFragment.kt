package com.toochi.job_swift.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import jp.wasabeef.richeditor.RichEditor


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class DescriptionDialogFragment : DialogFragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_description_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val richEditor:RichEditor = view.findViewById(R.id.richEditor)

        richEditor.setItalic()
        richEditor.setBullets()
        richEditor.setNumbers()
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DescriptionDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}