package org.effervescence.app18.ca.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import org.effervescence.app18.ca.fragments.LoginFragment
import org.effervescence.app18.ca.fragments.SignupFragment
import org.effervescence.app18.ca.fragments.UserDetailsInputFragment

class LoginSignupFragmentPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> LoginFragment()
            1 -> SignupFragment()
            else -> UserDetailsInputFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

}