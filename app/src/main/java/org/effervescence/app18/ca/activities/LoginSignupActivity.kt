package org.effervescence.app18.ca.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.app_bar_home.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.fragments.*

class LoginSignupActivity : AppCompatActivity() {

    val manager = supportFragmentManager
    var isLoginFragmentLoaded = false

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

    fun showLoginFragment() {
        val transaction = manager.beginTransaction()
        val loginFragment = LoginFragment()
        transaction.replace(R.id.login_signup_fragment_holder, loginFragment)
        transaction.commit()
        isLoginFragmentLoaded = true
    }

    fun showSignupFragment() {
        val transaction = manager.beginTransaction()
        val signupFragment = SignupFragment()
        transaction.replace(R.id.login_signup_fragment_holder, signupFragment)
        transaction.commit()
        isLoginFragmentLoaded = false
    }

    fun showUserDetailInputFragment() {
        val transaction = manager.beginTransaction()
        val userDetailInputFragment = UserDetailsInputFragment()
        transaction.replace(R.id.login_signup_fragment_holder, userDetailInputFragment)
        transaction.commit()
    }


}
