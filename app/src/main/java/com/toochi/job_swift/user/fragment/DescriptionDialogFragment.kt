package com.toochi.job_swift.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.FragmentDescriptionDialogBinding


class DescriptionDialogFragment(
    private val onFinished: (String) -> Unit,
    private var existingDescription: String = ""
) : DialogFragment() {

    private var _binding: FragmentDescriptionDialogBinding? = null

    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDescriptionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpEditor()

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        saveDescription()
    }

    private fun setUpEditor() {
        binding.richEditor.apply {
            setEditorFontSize(16)
            setPadding(10, 10, 10, 10)
            setPlaceholder("Type here...")
            html = existingDescription
        }

        applyBullets()
        applyBold()
        applyItalic()
        applyNumber()
        applyHeading()
    }


    private fun applyBullets() {
        binding.bulletButton.setOnClickListener {
            binding.richEditor.setBullets()
        }
    }

    private fun applyItalic() {
        binding.italicButton.setOnClickListener {
            binding.richEditor.setItalic()
        }
    }

    private fun applyBold() {
        binding.boldButton.setOnClickListener {
            binding.richEditor.setBold()
        }
    }

    private fun applyNumber() {
        binding.numberButton.setOnClickListener {
            binding.richEditor.setNumbers()
        }
    }

    private fun applyHeading() {
        binding.headingSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedHeading = parent?.getItemAtPosition(position).toString()

                    binding.richEditor.apply {
                        when (selectedHeading) {
                            "Heading 1" -> setHeading(1)
                            "Heading 2" -> setHeading(2)
                            "Heading 3" -> setHeading(3)
                            "Heading 4" -> setHeading(4)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
    }

    private fun saveDescription() {
        binding.saveButton.setOnClickListener {
            existingDescription = binding.richEditor.html

            if (existingDescription.isNotBlank()) {
                onFinished.invoke(existingDescription)
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(), "Please provide a description",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}