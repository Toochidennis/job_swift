package com.toochi.job_swift.common.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.postJob
import com.toochi.job_swift.common.dialogs.DatePickerDialog
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.dialogs.PaymentRateDialogFragment
import com.toochi.job_swift.databinding.FragmentJobDescriptionDialogBinding
import com.toochi.job_swift.model.Job
import com.toochi.job_swift.user.fragment.DescriptionDialogFragment
import com.toochi.job_swift.util.Utils.currencyFormatter
import com.toochi.job_swift.util.Utils.dateFormatter


class JobDescriptionDialogFragment(private val job: Job) : DialogFragment() {

    private var _binding: FragmentJobDescriptionDialogBinding? = null

    private val binding get() = _binding!!

    private var descriptionText: String? = null
    private var salaryRateText: String? = null
    private var deadlineText: String? = null
    private var salaryText: String? = null
    private var email: String? = null
    private var isProvideCv = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog2)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentJobDescriptionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(requireActivity().getSharedPreferences("loginDetail", Context.MODE_PRIVATE)) {
            email = getString("email", "")
        }

        handleViewClicks()

        processFormData()
    }


    private fun handleViewClicks() {
        salaryTextWatcher()

        binding.descriptionButton.setOnClickListener {
            DescriptionDialogFragment({
                descriptionText = it
                binding.descriptionTxt.text = Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
            }, descriptionText.toString()).show(parentFragmentManager, "")
        }


        binding.deadlineEditText.setOnClickListener {
            DatePickerDialog(requireContext()) {
                deadlineText = it
                binding.deadlineEditText.setText(dateFormatter(it))
            }
        }

        binding.rateEditText.setOnClickListener {
            PaymentRateDialogFragment {
                salaryRateText = it
                binding.rateEditText.setText(it)
            }.show(parentFragmentManager, "rate")
        }

    }

    private fun isValidForm(): Boolean {
        return when {
            descriptionText.isNullOrEmpty() -> {
                showToast("Please provide job description")
                false
            }

            salaryText.isNullOrEmpty() -> {
                showToast("Please provide pay")
                false
            }

            salaryRateText.isNullOrEmpty() -> {
                showToast("Please provide pay rate")
                false
            }

            else -> true
        }
    }

    private fun processFormData() {
        binding.postJobButton.setOnClickListener {
            isProvideCv = binding.cvCheckBox.isChecked
            val loadingDialog = LoadingDialog(requireContext())

            if (isValidForm()) {
                job.apply {
                    description = descriptionText ?: ""
                    jobEmail = email ?: ""
                    isProvideCV = isProvideCv
                    deadline = deadlineText ?: ""
                    salary = salaryText ?: ""
                    salaryRate = salaryRateText ?: ""
                }
                loadingDialog.show()

                postJob(job) { success, errorMessage ->
                    if (success) {
                        showToast("success")
                        dismiss()
                    } else {
                        showToast(errorMessage.toString())
                    }

                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun salaryTextWatcher() {
        binding.salaryEditText.apply {

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val cleanString = s.toString().replace(Regex("[^\\d]"), "")
                    salaryText = cleanString

                    // Parse the cleaned string to a double
                    val parsed: Double = try {
                        cleanString.toDouble()
                    } catch (e: NumberFormatException) {
                        0.0
                    }

                    // Format the double with commas and periods as the user types
                    val formatted = currencyFormatter(parsed)

                    // Set the formatted text back to the EditText
                    removeTextChangedListener(this)
                    setText(formatted)
                    setSelection(formatted.length)
                    addTextChangedListener(this)
                }
            })
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}