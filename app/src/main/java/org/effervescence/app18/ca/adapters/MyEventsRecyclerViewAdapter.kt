package org.effervescence.app18.ca.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_events_list_item.view.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.models.EventDetails


class MyEventsRecyclerViewAdapter(@get:JvmName("getEventsList_") private val mEventsList: ArrayList<EventDetails>)
    : RecyclerView.Adapter<MyEventsRecyclerViewAdapter.MyViewHolder>() {

    private var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClicked(position: Int)
    }

    fun setOnClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_events_list_item, parent, false)
        return MyViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentEvent = mEventsList[position]

        holder.titleTV.text = currentEvent.eventName
        holder.descriptionTV.text = currentEvent.eventDescription
        holder.pointsTV.text = currentEvent.eventPoint.toString()

        holder.pointTextTV.text = if (currentEvent.eventPoint > 1) "pts" else "point"

    }

    override fun getItemCount(): Int = mEventsList.size

    inner class MyViewHolder(mView: View, mListener: OnItemClickListener?) : RecyclerView.ViewHolder(mView) {

        var titleTV = mView.event_title
        var descriptionTV = mView.event_description
        var pointsTV = mView.events_points
        var pointTextTV = mView.point_text_TV
        var uploadButton = mView.upload_button

        init {
            uploadButton.setOnClickListener {
                if (mListener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClicked(position)
                    }
                }
            }
        }
    }
}