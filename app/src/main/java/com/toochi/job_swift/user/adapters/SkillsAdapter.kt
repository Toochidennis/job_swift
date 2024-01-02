package com.toochi.job_swift.user.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.toochi.job_swift.BR
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.ItemAboutSkillsBinding
import com.toochi.job_swift.model.Skills
import com.toochi.job_swift.util.Constants.Companion.DESELECTED
import com.toochi.job_swift.util.Constants.Companion.SELECTED

class SkillsAdapter(
    private val itemList: MutableList<Skills>,
    private val selectedItems: HashMap<String, String>
) : RecyclerView.Adapter<SkillsAdapter.ViewHolder>() {

    private companion object {
        private const val MAX_SELECTED_SKILLS = 5
        private const val TOAST_MAX_SKILLS_MESSAGE =
            "Only $MAX_SELECTED_SKILLS skills can be selected"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemAboutSkillsBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkillsAdapter.ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount() = itemList.size

    inner class ViewHolder(private val binding: ItemAboutSkillsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NotifyDataSetChanged")
        fun bind(skills: Skills) {
            binding.setVariable(BR.skill, skills)
            binding.executePendingBindings()

            val itemView = binding.root

            itemView.isSelected = skills.isSelected

            if (itemView.isSelected) {
                setBackgroundDrawable(binding.skillsLayout, SELECTED)
            } else {
                setBackgroundDrawable(binding.skillsLayout, DESELECTED)
            }

            itemView.setOnClickListener {
                skills.isSelected = !skills.isSelected
                itemView.isSelected = skills.isSelected

                if (itemView.isSelected) {
                    if (selectedItems.size == MAX_SELECTED_SKILLS) {
                        skills.isSelected = !skills.isSelected
                        itemView.isSelected = skills.isSelected

                        Toast.makeText(
                            itemView.context,
                            TOAST_MAX_SKILLS_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        selectedItems[skills.skillsId] = skills.skillName
                    }
                } else {
                    if (selectedItems.containsKey(skills.skillsId)) {
                        selectedItems.remove(skills.skillsId)
                    }
                }

                notifyDataSetChanged()
            }
        }

        private fun setBackgroundDrawable(view: View, state: String) {
            if (state == SELECTED)
                view.background =
                    ContextCompat.getDrawable(view.context, R.drawable.ic_left_curved_drawable2)
            else
                view.background =
                    ContextCompat.getDrawable(view.context, R.drawable.ic_left_curved_drawable)
        }

    }
}