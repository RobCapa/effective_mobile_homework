package com.example.work_2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.work_2.databinding.ItemLayoutBinding
import io.reactivex.subjects.PublishSubject

class ItemRecyclerAdapter(
    private val publishSubject: PublishSubject<Int>
) : RecyclerView.Adapter<ItemHolder>() {

    private val differ = AsyncListDiffer(this, ItemDiffUtil())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )

        return ItemHolder(
            binding = binding,
            publishSubject = publishSubject,
        )
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        differ.currentList[position].let(holder::bind)
    }

    fun updateItems(items: List<Item>) {
        differ.submitList(items)
    }
}

class ItemDiffUtil : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.value == newItem.value
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }

}

class ItemHolder(
    private val binding: ItemLayoutBinding,
    private val publishSubject: PublishSubject<Int>,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Item) {
        with(binding) {
            textView.text = "${item.value}"

            root.setOnClickListener {
                publishSubject.onNext(adapterPosition)
            }
        }
    }
}

data class Item(
    val value: Int,
)