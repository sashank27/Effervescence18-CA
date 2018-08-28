package org.effervescence.app18.ca.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.fragment_home.*

import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.effervescence.app18.ca.utilities.UserDetails
import org.jetbrains.anko.doAsync
import org.json.JSONObject


class HomeFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (listener != null) {
            listener!!.setTitleTo("Home")
        }
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userNameTextView.text = UserDetails.userName
        collegeNameTextView.text = "Glad to have our campus ambassador in ${UserDetails.collegeName} :)"

        if (UserDetails.isFirstLaunch) {
            loadUserDetails()
        } else {
            displayReportCard()
        }

        shareAppCardView.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "")
                type = "text/plain"
            }
            startActivity(sendIntent)
        }
    }

    private fun displayReportCard() {
        pointsTextView?.text = "Points: ${UserDetails.points}"
        val rank = UserDetails.rank.toString()
        rank_text_view?.textSize = when (rank.length) {
            4 -> 25f
            3 -> 80f
            2 -> 120f
            else -> 180f
        }
        rank_text_view?.text = UserDetails.rank.toString()
        rank_loading_spin_kit?.visibility = View.GONE
        rank_inferring_text_view?.visibility = View.GONE
        rank_inferring_text_view?.text = "Your Rank"
    }

    private fun loadUserDetails() {

        AndroidNetworking.get(Constants.REGULAR_USER_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + UserDetails.Token)
                .setTag("userRequest")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        UserDetails.points = response.getInt("total_points")
                        UserDetails.rank = response.getInt("rank")
                        doAsync {
                            val prefs = MyPreferences.customPrefs(context!!, Constants.MY_SHARED_PREFERENCE)

                            prefs[Constants.NAME_KEY] = response.optString(Constants.NAME_KEY)
                            prefs[Constants.COLLEGE_NAME_KEY] = response.optString(Constants.COLLEGE_NAME_KEY)
                            prefs[Constants.DATE_OF_BIRTH_KEY] = response.optString(Constants.DATE_OF_BIRTH_KEY)
                            prefs[Constants.GENDER_KEY] = response.optString(Constants.GENDER_KEY)
                            prefs[Constants.MOBILE_NO_KEY] = response.optString(Constants.MOBILE_NO_KEY)
                            prefs[Constants.REFERRAL_KEY] = response.optString(Constants.REFERRAL_KEY)
                            prefs[Constants.FB_ID_KEY] = response.optString(Constants.FB_ID_KEY)
                        }

                        UserDetails.isFirstLaunch = false
                        displayReportCard()
                    }

                    override fun onError(error: ANError) {

                    }
                })
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
        AndroidNetworking.cancel("userRequest")
    }

}
