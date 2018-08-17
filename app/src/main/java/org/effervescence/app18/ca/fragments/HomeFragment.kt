package org.effervescence.app18.ca.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*

import org.jetbrains.anko.startActivity
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.activities.EditUserDetailsActivity
import org.effervescence.app18.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app18.ca.utilities.UserDetails


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
        nameTextView.text = "Hello, ${UserDetails.Name}"
        collegeNameTextView.text = UserDetails.collegeName
        mobileNoTextView.text = "Mobile No : ${UserDetails.mobileNo}"

        button.setOnClickListener { startActivity(Intent(activity!!, EditUserDetailsActivity::class.java)) }
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
