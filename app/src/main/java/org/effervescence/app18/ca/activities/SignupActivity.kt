package org.effervescence.app18.ca.activities

import android.app.ProgressDialog
import android.content.Intent
import android.util.Log
import android.widget.Toast
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

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        btn_signup.setOnClickListener {
            signup()
        }

        link_login.setOnClickListener {
            val intentLoginActivity = Intent(this, LoginActivity::class.java)
            startActivity(intentLoginActivity)
            finish()
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
        }
    }

    private fun signup() {

        if (!validate()) {
            onSignupFailed()
            return
        }

        btn_signup.isEnabled = false

        val progressDialog = ProgressDialog(this@SignupActivity,
                R.style.AppTheme_Dark_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        val username = input_username.text.toString()
        val email =  input_email.text.toString()

        val password = input_password.text.toString()
        val reEnterPassword = input_reEnterPassword.text.toString()


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
                            val responseStr = error.errorBody


                            val errorResponse = JSONObject(responseStr)


                            if (errorResponse.has("username")) {
                                val errorStrArray = errorResponse.getJSONArray("username")
                                val errorInUserName = errorStrArray.getString(0)

                                input_username.error = errorInUserName
                            }

                            if (errorResponse.has("email")) {
                                val errorStrArray = errorResponse.getJSONArray("email")
                                val errorInEmail = errorStrArray.getString(0)

                                input_email.error = errorInEmail
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
        moveTaskToBack(true)
    }


    fun onSignupSuccess() {
        btn_signup.isEnabled = true
        setResult(RESULT_OK, null)
        finish()
    }

    fun onSignupFailed() {
        Toast.makeText(baseContext, "Login failed", Toast.LENGTH_LONG).show()

        btn_signup.isEnabled = true
    }

    private fun validate(): Boolean {
        var valid = true

        val username = input_username.text
        val email =  input_email.text

        val password = input_password.text.toString()
        val reEnterPassword = input_reEnterPassword.text.toString()


        if (username.isEmpty() || username.length < 3) {
            input_username.error = "at least 3 characters"
            valid = false
        } else {
            input_username.error = null
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email.error = "enter a valid email address"
            valid = false
        } else {
            input_email.error = null
        }



        if (password.isEmpty() || password.length < 8) {
            input_password.error = "Password must be atleast 8 characters"
            valid = false
        } else {
            input_password.error = null
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length < 8) {
            input_reEnterPassword.error = "Password must be atleast 8 characters long"
            valid = false
        } else if( reEnterPassword != password){
            input_reEnterPassword.error = "Passwords do not match"
            valid = false
        } else {
            input_reEnterPassword.error = null
        }

        return valid
    }
}
