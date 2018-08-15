package org.effervescence.app18.ca.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.View
import kotlinx.android.synthetic.main.fragment_login.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.fragments.LoginFragment
import org.effervescence.app18.ca.fragments.SignupFragment
import org.effervescence.app18.ca.fragments.UserDetailsInputFragment

class LoginSignupActivity : AppCompatActivity() {

    val manager = supportFragmentManager
    private var isLoginFragmentLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)

        loadFragments(intent)
    }

    private fun loadFragments(intent: Intent) {

        if (intent.getBooleanExtra("userDetailsRequired", false))
            showUserDetailInputFragment()
        else {
            when (isLoginFragmentLoaded) {
                true -> showSignupFragment()
                else -> showLoginFragment()
            }
        }
    }

    private fun showLoginFragment() {
        val transaction = manager.beginTransaction()
        val loginFragment = LoginFragment()
        transaction.replace(R.id.login_signup_fragment_holder, loginFragment)
        transaction.commit()
        isLoginFragmentLoaded = true
    }

    private fun showSignupFragment() {
        val transaction = manager.beginTransaction()
        val signupFragment = SignupFragment()
        transaction.replace(R.id.login_signup_fragment_holder, signupFragment)
        transaction.commit()
        isLoginFragmentLoaded = false
    }

    private fun showUserDetailInputFragment() {
        val transaction = manager.beginTransaction()
        val showUserDetailInputFragment = UserDetailsInputFragment()
        transaction.replace(R.id.login_signup_fragment_holder, showUserDetailInputFragment)
        transaction.commit()
    }


}
