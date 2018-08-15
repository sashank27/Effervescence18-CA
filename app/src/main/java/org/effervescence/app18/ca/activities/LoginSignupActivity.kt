package org.effervescence.app18.ca.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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

    override fun onResume() {
        super.onResume()

        checkPermission()
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

    fun checkPermission() {
        if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }

}
