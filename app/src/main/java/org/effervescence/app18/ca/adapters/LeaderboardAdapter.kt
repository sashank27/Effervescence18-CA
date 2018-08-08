package org.effervescence.app18.ca.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.R.color.baseBackground3
import org.effervescence.app18.ca.models.LeaderbooardEntry
import org.effervescence.app18.ca.utilities.UserDetails
import org.w3c.dom.Text
import org.w3c.dom.UserDataHandler

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
        }
        holder.bind(leaderboardEntry[position], position-1)
    }


    inner class LeaderBoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nameTextView = itemView.findViewById<TextView>(R.id.nameTextView)
        val srNoTextView = itemView.findViewById<TextView>(R.id.srNoTextView)
        val emailTextView = itemView.findViewById<TextView>(R.id.emailTextView)
        val pointsTextView = itemView.findViewById<TextView>(R.id.pointsTextView)
        val separator = itemView.findViewById<View>(R.id.separator)

        fun bind(entry: LeaderbooardEntry, position: Int){
            nameTextView.text = entry.name
            srNoTextView.text = "$position."
            pointsTextView.text = "${entry.points}"

            if(entry.name.trim() == UserDetails.Name.trim()){
                itemView.setBackgroundResource(R.color.gray1)
            }
            if(position >= itemCount-2){
                separator.visibility = View.INVISIBLE
            }
        }
    }

    fun swapList(list: ArrayList<LeaderbooardEntry>){
        leaderboardEntry.add(LeaderbooardEntry())
        leaderboardEntry.add(LeaderbooardEntry())
        leaderboardEntry.addAll(list)
        leaderboardEntry.add(LeaderbooardEntry())
        notifyDataSetChanged()
    }
}