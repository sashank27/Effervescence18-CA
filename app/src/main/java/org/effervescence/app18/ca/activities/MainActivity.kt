package org.effervescence.app18.ca.activities

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.R.id.userID
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.get
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    override fun onStart() {
        super.onStart()

        //Getting the value of the user token
        prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)
        val userToken: String = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]

        //If its the default token it means we need to launch the login activity
        if(userToken == Constants.TOKEN_DEFAULT){
            startLogin()
        }

        //else we can display the userId
        //TODO this is the place where we will manage the start page for a user who has already logged in
        else {
            userID.text = "Hello user id " + userToken
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            R.id.logout -> {
                resetSharedPreference()
                finish()
                return true
            }
            R.id.change_password -> changePassword()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changePassword() {
        startActivity<ChangePasswordActivity>()
    }

    private fun resetSharedPreference() {
        userID.text = "Logged out"
        prefs[Constants.KEY_TOKEN] = "0"
    }

    fun startLogin(){
        startActivity<LoginActivity>()
    }
}
