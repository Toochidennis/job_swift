package com.toochi.job_swift.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.PersonalDetailsManager.updateExistingUser
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentAboutDialogBinding
import com.toochi.job_swift.model.Skills
import com.toochi.job_swift.model.User
import com.toochi.job_swift.user.adapters.SkillsAdapter


class AboutDialogFragment(
    private val user: User? = null,
    private val onSave: () -> Unit
) : DialogFragment() {

    private var _binding: FragmentAboutDialogBinding? = null

    private val binding get() = _binding!!

    private var selectedItems = hashMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAboutDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillFieldsWithData()

        binding.saveButton.setOnClickListener {
            updateDetails()
        }

        binding.navigateUp.setOnClickListener {
            dismiss()
        }
    }


    private fun fillFieldsWithData() {
        if (user != null) {
            binding.aboutTextField.setText(user.about)

            val wordList = user.skills.split(",").map { it.trim() }
            val idList = user.skillsId.split(",").map { it.trim() }

            if (wordList.isNotEmpty() && idList.isNotEmpty()) {
                idList.forEachIndexed { index, value ->
                    if (value.isNotBlank())
                        selectedItems[value] = wordList[index]
                }
            }

            setUpAdapter()
        }
    }


    private fun setUpAdapter() {
        val skills = resources.getStringArray(R.array.skills).toMutableList()
        val newSkillsList = mutableListOf<Skills>()

        skills.forEachIndexed { index, skill ->
            if (skill.isNotEmpty())
                newSkillsList.add(Skills((index + 1).toString(), skill))
        }

        newSkillsList.forEach {
            val skillName = selectedItems[it.skillsId]

            if (skillName != null) {
                it.isSelected = true
            }
        }

        newSkillsList.sortBy { it.skillName }

        val skillsAdapter = SkillsAdapter(newSkillsList, selectedItems)

        binding.skillsRecyclerView.apply {
            hasFixedSize()
            adapter = skillsAdapter
        }
    }

    private fun isValidForm(user: User): Boolean {
        return when {
            user.about.isEmpty() -> {
                showToast(getString(R.string.please_provide_about))
                false
            }

            user.skills.isEmpty() && user.skillsId.isEmpty() -> {
                showToast(getString(R.string.please_add_skills))
                false
            }

            else -> true
        }
    }

    private fun updateDetails() {
        val loadingDialog = LoadingDialog(requireContext())
        val data = getDataFromForm()

        try {
            if (isValidForm(data)) {
                loadingDialog.show()

                updateExistingUser(
                    user?.profileId!!,
                    hashMap = hashMapOf(
                        "about" to data.about,
                        "skillsId" to data.skillsId,
                        "skills" to data.skills
                    )
                ) { success, error ->
                    if (success) {
                        onSave.invoke()
                        dismiss()
                    } else {
                        showToast(error.toString())
                    }

                    loadingDialog.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("An error occurred.")
        }

    }

    private fun getDataFromForm(): User {
        val skillsId = selectedItems.keys.joinToString(", ") { it }
        val skills = selectedItems.values.joinToString(", ") { it }

        return User(
            about = binding.aboutTextField.text.toString().trim(),
            skillsId = skillsId,
            skills = skills
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
