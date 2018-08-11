package org.effervescence.app18.ca.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.SyncStateContract
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import kotlinx.android.synthetic.main.fragment_leader_board.*

import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.R.id.leaderRecylcerView
import org.effervescence.app18.ca.adapters.LeaderboardAdapter
import org.effervescence.app18.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app18.ca.listeners.ScrollListener
import org.effervescence.app18.ca.models.LeaderbooardEntry
import org.effervescence.app18.ca.utilities.Constants
import org.json.JSONArray
import org.json.JSONObject

class LeaderBoardFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    val list = ArrayList<LeaderbooardEntry>()
    lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (listener != null) {
            listener!!.setTitleTo("Leader Board")
        }
        return inflater.inflate(R.layout.fragment_leader_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getLeaderboardData()
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        leaderRecylcerView.layoutManager = layoutManager
        adapter = LeaderboardAdapter(context!!)
        leaderRecylcerView.adapter = adapter
        leaderRecylcerView.isNestedScrollingEnabled = true
        leaderRecylcerView.addOnScrollListener(ScrollListener(back_view))

    }

    fun getLeaderboardData(){
        AndroidNetworking.get(Constants.LEADERBOARD_URL)
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener{
                    override fun onResponse(response: JSONArray?) {

                            populateListFromJson(response!!)


                    }

                    override fun onError(anError: ANError?) {
                        showErrorMessage()
                    }

                })
    }

    private fun showErrorMessage() {
        back_view.text = "Coulds't fetch the leaderboard. Try checking the internet connection"
        progressLeaderboard.visibility = View.GONE
    }

    private fun populateListFromJson(response: JSONArray) {
        val len = response.length()

        var entry: JSONObject
        var name: String
        var college: String
        var points: Int

        for ( i in 0 until len){
            entry = response.getJSONObject(i)
            name = entry.getString("name")
            points = entry.getInt("points")
            list.add(LeaderbooardEntry(name, points))
        }
        adapter.swapList(list)
        back_view.text = "See where you stand among campus ambassadors of other colleges"
        progressLeaderboard.visibility = View.GONE
        leaderRecylcerView.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
