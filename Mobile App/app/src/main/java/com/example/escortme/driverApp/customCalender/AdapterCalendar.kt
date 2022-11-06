package com.example.escortme.driverApp.customCalender

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.escortme.R
import com.example.escortme.databinding.CalenderRowLayoutBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AdapterCalendar(
    val list: ArrayList<DataCalendar>,
    val iCalendar: ICalendar
) :
    RecyclerView.Adapter<AdapterCalendar.CalendarVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarVH {
        return CalendarVH(
            CalenderRowLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CalendarVH, position: Int) {

        // Check for current date against the date on the calendar
/*
        val dateFormat: DateFormat = SimpleDateFormat("d")
        val d = Date()

        println("DATE FROM LIST ${list[position].date}")
        println("DATE FROM LIST 2 ${dateFormat.format(d)}")

        if (list[position].date == dateFormat.format(d)) {

            holder.day.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
            holder.date.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.primary
                )
            )
        }
*/


        holder.bind(list[position])

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(listC: ArrayList<DataCalendar>) {
        list.clear()
        list.addAll(listC)
        notifyDataSetChanged()
    }

    inner class CalendarVH(val binding: CalenderRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val day = binding.tvCalendarDay
        val date = binding.tvCalendarDate
        val cardView = binding.root

        fun bind(model: DataCalendar) {

            if (model.selected) {
                day.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )
                )
                date.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )
                )
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.primary
                    )
                )
            } else {
                day.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.black
                    )
                )
                date.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.black
                    )
                )
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )
                )
            }
            day.text = model.day
            date.text = model.date
            cardView.setOnClickListener {
                iCalendar.onSelect(model, adapterPosition)
            }
        }

    }


    interface ICalendar {
        fun onSelect(data: DataCalendar, position: Int)
    }

}

