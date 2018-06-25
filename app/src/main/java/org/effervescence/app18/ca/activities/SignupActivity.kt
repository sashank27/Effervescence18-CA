package org.effervescence.app18.ca.activities

import android.app.ProgressDialog
import android.util.Log
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_signup.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.set
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import org.effervescence.app18.ca.EffervescenceCA
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        btnSignup.setOnClickListener {
            signup()
        }

        tvLinkLogin.setOnClickListener {
            startActivity<LoginActivity>()
            finish()
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
        }
    }

    private fun signup() {
        val username = inputUsernameSignup.text.toString()
        val email =  inputEmailSignup.text.toString()

        val password = inputPasswordSignup.text.toString()
        val reEnterPassword = inputReEnterPasswordSignup.text.toString()

        if (!validate(username, email, password, reEnterPassword)) {
            onSignupFailed()
            return
        }

        btnSignup.isEnabled = false

        val progressDialog = ProgressDialog(this@SignupActivity,
                R.style.AppTheme_Dark_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()


        val prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)

        AndroidNetworking.post(EffervescenceCA.BASE_URL + "/api/registration/")
                .addBodyParameter("email", email)
                .addBodyParameter("username", username)
                .addBodyParameter("password1", password)
                .addBodyParameter("password2", reEnterPassword)
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

                        onSignupSuccess()
                        progressDialog.dismiss()
                    }

                    override fun onError(error: ANError) {
                        if( error.errorCode != 0) {
                            val errorResponse = JSONObject(error.errorBody)

                            if (errorResponse.has("username")) {
                                inputUsernameSignup.error = errorResponse.getJSONArray("username").getString(0)
                            }

                            if (errorResponse.has("email")) {
                                inputEmailSignup.error = errorResponse.getJSONArray("email").getString(0)
                            }
                        }
                        // handle error
                        onSignupFailed()
                        progressDialog.dismiss()
                    }
                })

    }


    override fun onBackPressed() {
        // Disable going back to the MainActivity
        //moveTaskToBack(true)
        finishAffinity()
    }


    fun onSignupSuccess() {
        btnSignup.isEnabled = true
        setResult(RESULT_OK, null)
        finish()
    }

    fun onSignupFailed() {
        toast("Sign up failed")

        btnSignup.isEnabled = true
    }

    private fun validate(username: String, email: String, password: String, reEnterPassword: String): Boolean {
        var valid = true

        if (username.isEmpty() || username.length < 3) {
            inputUsernameSignup.error = "at least 3 characters"
            valid = false
        } else {
            inputUsernameSignup.error = null
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmailSignup.error = "enter a valid email address"
            valid = false
        } else {
            inputEmailSignup.error = null
        }

        if (password.isEmpty() || password.length < 8) {
            inputPasswordSignup.error = "Password must be at least 8 characters"
            valid = false
        } else {
            inputPasswordSignup.error = null
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length < 8) {
            inputReEnterPasswordSignup.error = "Password must be at least 8 characters long"
            valid = false
        } else if( reEnterPassword != password){
            inputReEnterPasswordSignup.error = "Passwords do not match"
            valid = false
        } else {
            inputReEnterPasswordSignup.error = null
        }

        return valid
    }
}
