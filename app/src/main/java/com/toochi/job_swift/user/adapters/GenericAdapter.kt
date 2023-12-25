package com.toochi.job_swift.user.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * GenericAdapter for RecyclerView.
 *
 * @param itemList List of items to be displayed.
 * @param itemResLayout Layout resource for individual items.
 * @param bindItem Function to bind item data to the view.
 * @param onItemClick Function to handle item click events.
 */

class GenericAdapter<Model>(
    private val itemList: MutableList<Model>,
    private val itemResLayout: Int,
    private val bindItem: (binding: ViewDataBinding, model: Model) -> Unit,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<GenericAdapter<Model>.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            itemResLayout,
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = itemList[position]

        bindItem(holder.binding, model)

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = itemList.size

    inner class ViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
}