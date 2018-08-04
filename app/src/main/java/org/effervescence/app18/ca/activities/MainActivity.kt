package org.effervescence.app18.ca.activities

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.get
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.jetbrains.anko.startActivity
import com.androidnetworking.error.ANError
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.fragment_user_details_input.*
import org.effervescence.app18.ca.fragments.UserDetailsInputFragment
import org.effervescence.app18.ca.utilities.UserDetails
import org.jetbrains.anko.toast
import org.json.JSONObject


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
        if (userToken == Constants.TOKEN_DEFAULT) {
            startLogin()
        } else {
            if (intent.getBooleanExtra("loginSuccessFetchUserDetails", false))
                fetchUserDetailsThanDisplay()
            else {
                if (prefs[Constants.NAME_KEY, Constants.NAME_DEFAULT] == Constants.NAME_DEFAULT)
                    askForUserDetails()
                else
                    launchHomeActivity()
            }
        }
    }

    fun launchHomeActivity(){

        UserDetails.Name = prefs[Constants.NAME_KEY, Constants.NAME_DEFAULT]
        UserDetails.Token = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]
        startActivity<HomeActivity>()
        finish()
    }


    fun displayUserDetails() {

        val userDetailsString = "Name - ${prefs[Constants.NAME_KEY, Constants.NAME_DEFAULT]}\n" +
                "College Name - ${prefs[Constants.COLLEGE_NAME_KEY, Constants.COLLEGE_NAME_DEFAULT]}\n" +
                "Gender - ${prefs[Constants.GENDER_KEY, Constants.GENDER_DEFAULT]}\n" +
                "Date of Birth - ${prefs[Constants.DATE_OF_BIRTH_KEY, Constants.DATE_OF_BIRTH_DEFAULT]}\n" +
                "Mobile - ${prefs[Constants.MOBILE_NO_KEY, Constants.MOBILE_NO_DEFAULT]}"

        userID.text = userDetailsString
    }

    private fun fetchUserDetailsThanDisplay() {

        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = false
        progressDialog.setMessage("Getting User Details..")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val userToken = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]

        AndroidNetworking.get(Constants.REGULAR_USER_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + userToken)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        saveUserDetails(response)
                        launchHomeActivity()
                        progressDialog.dismiss()
                    }
                    override fun onError(error: ANError) {
                        if(error.errorBody.contains("query does not exist")) {
                            askForUserDetails()
                            toast("Details not found")
                        }
                        progressDialog.dismiss()
                    }
                })

    }

    fun saveUserDetails(response: JSONObject) {
        //Saving user details in shared preferences
        prefs[Constants.NAME_KEY] = response.optString(Constants.NAME_KEY)
        prefs[Constants.COLLEGE_NAME_KEY] = response.optString(Constants.COLLEGE_NAME_KEY)
        prefs[Constants.DATE_OF_BIRTH_KEY] = response.optString(Constants.DATE_OF_BIRTH_KEY)
        prefs[Constants.GENDER_KEY] = response.optString(Constants.GENDER_KEY)
        prefs[Constants.MOBILE_NO_KEY] = response.optString(Constants.MOBILE_NO_KEY)
    }

    fun askForUserDetails() {
        val userDetailsIntent = Intent(this, LoginSignupActivity::class.java)
        userDetailsIntent.putExtra("userDetailsRequired", true)
        startActivity(userDetailsIntent)
        finish()
    }

    private fun resetSharedPreference() {
        userID.text = ""
        prefs[Constants.KEY_TOKEN] = "0"
        prefs[Constants.NAME_KEY] = Constants.NAME_DEFAULT
        prefs[Constants.COLLEGE_NAME_KEY] = Constants.COLLEGE_NAME_DEFAULT
        prefs[Constants.GENDER_KEY] = Constants.GENDER_DEFAULT
        prefs[Constants.DATE_OF_BIRTH_KEY] = Constants.DATE_OF_BIRTH_DEFAULT
        prefs[Constants.MOBILE_NO_KEY] = Constants.MOBILE_NO_DEFAULT
    }

    private fun startLogin() {
        startActivity<LoginSignupActivity>()
        finish()
    }

    private fun changePassword() {
        startActivity<ChangePasswordActivity>()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.logout -> {
                resetSharedPreference()
                finish()
                return true
            }
            R.id.change_password -> changePassword()
        }
        return super.onOptionsItemSelected(item)
    }
}
