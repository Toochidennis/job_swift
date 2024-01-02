package com.toochi.job_swift.common.fragments

import android.os.Build
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
import com.toochi.job_swift.backend.PostJobManager
import com.toochi.job_swift.common.dialogs.AlertDialog
import com.toochi.job_swift.common.dialogs.DatePickerDialog
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.dialogs.PaymentRateDialogFragment
import com.toochi.job_swift.databinding.FragmentJobDescriptionDialogBinding
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.user.fragment.DescriptionDialogFragment
import com.toochi.job_swift.util.Utils.currencyFormatter
import com.toochi.job_swift.util.Utils.dateFormatter


class JobDescriptionDialogFragment(private val postJob: PostJob) : DialogFragment() {

    private var _binding: FragmentJobDescriptionDialogBinding? = null

    private val binding get() = _binding!!

    private var descriptionText: String = ""
    private var deadlineText: String? = null
    private var salaryText: String? = null

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

        handleViewClicks()

    }

    private fun handleViewClicks() {
        salaryTextWatcher()

        binding.descriptionButton.setOnClickListener {
            DescriptionDialogFragment({
                setDescriptionText(it)
            }, descriptionText).show(parentFragmentManager, "")
        }


        binding.deadlineEditText.setOnClickListener {
            DatePickerDialog(requireContext()) {
                deadlineText = it
                binding.deadlineEditText.setText(dateFormatter(it))
            }.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        binding.rateEditText.setOnClickListener {
            PaymentRateDialogFragment {
                binding.rateEditText.setText(it)
            }.show(parentFragmentManager, "rate")
        }

        binding.postJobButton.setOnClickListener {
            processForm()
        }
    }

    private fun isValidForm(job: PostJob): Boolean {
        return when {
            job.description.isEmpty() -> {
                showToast(getString(R.string.please_provide_description))
                false
            }

            job.salary.isEmpty() -> {
                showToast(getString(R.string.please_provide_pay))
                false
            }

            job.salaryRate.isEmpty() -> {
                showToast(getString(R.string.please_provide_pay_rate))
                false
            }

            else -> true
        }
    }

    private fun processForm() {
        val loadingDialog = LoadingDialog(requireContext())

        try {
            val job = getDataFromForm()

            if (isValidForm(job)) {
                loadingDialog.show()

                PostJobManager.postJob(job) { success, errorMessage ->
                    if (success) {
                        showCongratsMessage()
                    } else {
                        showToast(errorMessage.toString())
                    }

                    loadingDialog.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("An error occurred.")
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

    private fun getDataFromForm(): PostJob {
        return postJob.apply {
            description = descriptionText
            isProvideCV = binding.cvCheckBox.isChecked
            deadline = deadlineText ?: ""
            salary = salaryText ?: ""
            salaryRate = binding.salaryEditText.text.toString()
        }
    }

    private fun showCongratsMessage() {
        AlertDialog.Builder(requireContext()).run {
            title = getString(R.string.congratulations_on_posting_your_job)
            body = getString(R.string.job_is_live)
            isNegativeVisible = false
            isPositiveVisible = true
            positiveMessage = getString(R.string.awesome)
            positiveClickListener = {
                dismiss()
            }
            build()
        }.show()
    }

    private fun setDescriptionText(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.descriptionTxt.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            binding.descriptionTxt.text = Html.fromHtml(text)
        }

        descriptionText = text
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}