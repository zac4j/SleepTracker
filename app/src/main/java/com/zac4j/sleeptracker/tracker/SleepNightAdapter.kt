package com.zac4j.sleeptracker.tracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zac4j.sleeptracker.R
import com.zac4j.sleeptracker.database.SleepNight
import com.zac4j.sleeptracker.databinding.ListItemSleepNightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adapter for sleep quality list.
 *
 * @author: zac
 * @date: 2019-08-02
 */

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class SleepNightAdapter(private val clickListener: SleepNightListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(
    SleepNightDiffCallback()
) {

  private val adapterScope = CoroutineScope(Dispatchers.Default)
  fun addHeaderAndSubmitList(list: List<SleepNight>?) {
    adapterScope.launch {
      val items = when (list) {
        null -> listOf(DataItem.Header)
        else -> listOf(DataItem.Header) + list.map {
          DataItem.SleepNightItem(it)
        }
      }

      withContext(Dispatchers.Main) {
        submitList(items)
      }
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecyclerView.ViewHolder {
    return when (viewType) {
      ITEM_VIEW_TYPE_HEADER -> TextHolder.from(parent)
      ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
      else -> throw ClassCastException("Unknown viewType $viewType")
    }
  }

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int
  ) {
    when (holder) {
      is ViewHolder -> {
        val nightItem = getItem(position) as DataItem.SleepNightItem
        holder.bind(clickListener, nightItem.sleepNight)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
      is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
    }
  }

  class ViewHolder private constructor(val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(
      binding.root
  ) {

    fun bind(
      clickListener: SleepNightListener,
      item: SleepNight
    ) {
      binding.sleep = item
      binding.clickListener = clickListener
      binding.executePendingBindings()
    }

    companion object {
      fun from(parent: ViewGroup): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemSleepNightBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
      }
    }
  }

  class TextHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
      fun from(parent: ViewGroup): TextHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.header, parent, false)
        return TextHolder(view)
      }
    }
  }
}

class SleepNightDiffCallback : DiffUtil.ItemCallback<DataItem>() {

  override fun areItemsTheSame(
    oldItem: DataItem,
    newItem: DataItem
  ) = oldItem.id == newItem.id

  override fun areContentsTheSame(
    oldItem: DataItem,
    newItem: DataItem
  ) = oldItem == newItem
}

class SleepNightListener(val onClickListener: (sleepId: Long) -> Unit) {
  fun onClick(night: SleepNight) = onClickListener(night.nightId)
}

sealed class DataItem {
  data class SleepNightItem(val sleepNight: SleepNight) : DataItem() {
    override val id = sleepNight.nightId
  }

  object Header : DataItem() {
    override val id = Long.MIN_VALUE
  }

  abstract val id: Long
}