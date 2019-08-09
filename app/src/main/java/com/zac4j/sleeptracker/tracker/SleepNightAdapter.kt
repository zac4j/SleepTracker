package com.zac4j.sleeptracker.tracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.zac4j.sleeptracker.R
import com.zac4j.sleeptracker.R.drawable
import com.zac4j.sleeptracker.convertDurationToFormatted
import com.zac4j.sleeptracker.convertNumericQualityToString
import com.zac4j.sleeptracker.database.SleepNight

/**
 * Desc:
 *
 * @author: zac
 * @date: 2019-08-02
 */
class SleepNightAdapter : Adapter<SleepNightAdapter.ViewHolder>() {

  var data = listOf<SleepNight>()
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val item = inflater.inflate(R.layout.list_item_sleep_night, parent, false)
    return ViewHolder(item)
  }

  override fun getItemCount() = data.size

  override fun onBindViewHolder(
    holder: ViewHolder,
    position: Int
  ) {
    val item = data[position]
    holder.bind(item)
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val sleepLength: TextView = itemView.findViewById(R.id.sleep_length)
    val quality: TextView = itemView.findViewById(R.id.quality_string)
    val qualityImage: ImageView = itemView.findViewById(R.id.quality_image)

    fun bind(item: SleepNight) {
      val res = itemView.context.resources

      sleepLength.text =
        convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
      quality.text = convertNumericQualityToString(item.sleepQuality, res)

      qualityImage.setImageResource(
          when (item.sleepQuality) {
            0 -> drawable.ic_sleep_0
            1 -> drawable.ic_sleep_1
            2 -> drawable.ic_sleep_2
            3 -> drawable.ic_sleep_3
            4 -> drawable.ic_sleep_4
            5 -> drawable.ic_sleep_5
            else -> drawable.ic_sleep_active
          }
      )
    }
  }
}