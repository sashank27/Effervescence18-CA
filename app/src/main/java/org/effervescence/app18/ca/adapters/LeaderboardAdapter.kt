package org.effervescence.app18.ca.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.models.LeaderbooardEntry

class LeaderboardAdapter(val context: Context) : RecyclerView.Adapter<LeaderboardAdapter.LeaderBoardViewHolder>() {

    private var leaderboardEntry = ArrayList<LeaderbooardEntry>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderBoardViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_litem_eaderboard, parent, false)
        return LeaderBoardViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return leaderboardEntry.size
    }

    override fun onBindViewHolder(holder: LeaderBoardViewHolder, position: Int) {
        if(position==0||position==1||position==itemCount-1){
            holder.itemView.visibility = View.INVISIBLE
        } else {
            holder.itemView.visibility = View.VISIBLE
        }
        holder.bind(leaderboardEntry[position], position-1)
    }


    inner class LeaderBoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nameTextView = itemView.findViewById<TextView>(R.id.helloText)
        val srNoTextView = itemView.findViewById<TextView>(R.id.srNoTextView)
        val emailTextView = itemView.findViewById<TextView>(R.id.emailTextView)
        val pointsTextView = itemView.findViewById<TextView>(R.id.pointsTextView)
        val separator = itemView.findViewById<View>(R.id.separator)

        fun bind(entry: LeaderbooardEntry, position: Int){
            nameTextView.text = entry.name
            srNoTextView.text = "$position."
            pointsTextView.text = "${entry.points}"
            emailTextView.text = entry.collegeName
            if(entry.isCurrentUser){
                itemView.setBackgroundResource(R.color.gray1)
            } else {
                itemView.setBackgroundResource(R.color.gray0)
            }
            if(position >= itemCount-2){
                separator.visibility = View.INVISIBLE
            }else {
                separator.visibility = View.VISIBLE
            }
        }
    }

    fun swapList(list: ArrayList<LeaderbooardEntry>){
        if(leaderboardEntry.size == 0){
            leaderboardEntry.add(LeaderbooardEntry())
            leaderboardEntry.add(LeaderbooardEntry())
        } else {
            leaderboardEntry.removeAt(leaderboardEntry.size - 1)
        }

        leaderboardEntry.addAll(list)
        leaderboardEntry.add(LeaderbooardEntry())
        notifyDataSetChanged()
    }
}