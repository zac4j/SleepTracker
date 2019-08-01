package com.zac4j.sleeptracker.tracker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.zac4j.sleeptracker.R
import com.zac4j.sleeptracker.TextItemViewHolder
import com.zac4j.sleeptracker.database.SleepNight

/**
 * Desc:
 *
 * @author: zac
 * @date: 2019-08-02
 */
class SleepNightAdapter : Adapter<TextItemViewHolder>() {

  private var data = listOf<SleepNight>()
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): TextItemViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val item = inflater.inflate(R.layout.text_item_view, parent, false) as TextView
    return TextItemViewHolder(item)
  }

  override fun getItemCount() = data.size

  override fun onBindViewHolder(
    holder: TextItemViewHolder,
    position: Int
  ) {
    val item = data[position]
    holder.textView.text = item.toString()
  }
}