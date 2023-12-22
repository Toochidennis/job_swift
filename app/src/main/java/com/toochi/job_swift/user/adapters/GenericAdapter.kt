package com.toochi.job_swift.user.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val bindItem: (itemView: View, model: Model) -> Unit,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<GenericAdapter<Model>.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                itemResLayout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = itemList[position]

        bindItem(holder.itemView, model)

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = itemList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}