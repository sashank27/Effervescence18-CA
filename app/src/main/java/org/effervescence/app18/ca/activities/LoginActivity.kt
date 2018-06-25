package org.effervescence.app18.ca.activities

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_login.*
import org.effervescence.app18.ca.EffervescenceCA
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.utilities.Constants
import org.json.JSONObject
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.set
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {

    //Random number assigned to the request code
    private val REQUEST_SIGNUP = 44

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener{
            login()
        }

        tvLinkSignup.setOnClickListener {
            startActivityForResult<SignupActivity>(REQUEST_SIGNUP)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }
    private fun login() {
        val username = inputUsernameLogin.text.toString()
        val password = inputPasswordLogin.text.toString()

        if(!validate(username, password)){
            onLoginFailed()
            return
        }

        btnLogin.isEnabled = false

        val progressDialog = ProgressDialog(this, R.style.AppTheme_Dark_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Authenticating..")
        progressDialog.show()

        val prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)

        AndroidNetworking.post(EffervescenceCA.BASE_URL + "/api/login/")
                .addBodyParameter("username", username)
                .addBodyParameter("password", password)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {

                    override fun onResponse(response: JSONObject) {

                        try{
                            val token = response.getString("key")
                            prefs[Constants.KEY_TOKEN] = token
                        } catch (e: Exception){
                            Log.d("Response", response.toString())
                        }
                        onLoginSuccess()
                        progressDialog.dismiss()
                    }

                    override fun onError(error: ANError) {

                        if (error.errorCode != 0) {
                            val errorResponse = JSONObject(error.errorBody)

                            if (errorResponse.has("username")) {
                                inputUsernameLogin.error = errorResponse.getJSONArray("username").getString(0)
                            }
                            if (errorResponse.has("non_field_errors")) {
                                inputUsernameLogin.error = "Either the username or the password is incorrect"
                            }
                        }
                        // handle error
                        onLoginFailed()
                        progressDialog.dismiss()
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                //TODO If we need to implement any other activity or a transition on sign up this is where we
                //would do that
                this.finish()
            }
        }
    }


    private fun validate(username: String, password: String): Boolean {
        var valid = true

        if (username.isEmpty() || username.length < 3) {
            inputUsernameLogin.error = "at least 3 characters"
            valid = false
        } else {
            inputUsernameLogin.error = null
        }

        if (password.isEmpty() || password.length < 8 ) {
            inputPasswordLogin.error = "Password must be at least 8 characters"
            valid = false
        } else {
            inputPasswordLogin.error = null
        }

        return valid
    }

    override fun onBackPressed() {
        // Disable going back to the MainActivity
        //moveTaskToBack(true)
        finishAffinity()
    }

    fun onLoginSuccess() {
        btnLogin.isEnabled = true
        finish()
    }

    fun onLoginFailed() {
        toast("Login Failed")

        btnLogin.isEnabled = true
    }
}
