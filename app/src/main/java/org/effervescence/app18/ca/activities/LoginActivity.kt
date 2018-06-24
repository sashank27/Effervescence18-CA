package org.effervescence.app18.ca.activities

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

class LoginActivity : AppCompatActivity() {

    //Random number assigned to the request code
    private val REQUEST_SIGNUP = 44

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener{
            login()
        }

        tv_linkSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivityForResult(intent, REQUEST_SIGNUP)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }

    fun login() {
        Log.d("LoginActivity", "Login")

        if(!validate()){
            onLoginFailed()
            return
        }

        btn_login.isEnabled = false

        val progressDialog = ProgressDialog(this, R.style.AppTheme_Dark_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Authenticating..")
        progressDialog.show()


        val username = input_usernameLogin.text.toString()


        val password = input_passwordLogin.text.toString()



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
                            Log.d("Token", token)
                            prefs[Constants.KEY_TOKEN] = token
                        } catch (e: Exception){
                            Log.d("Response", response.toString())
                        }



                        onLoginSuccess()

                        progressDialog.dismiss()
                    }

                    override fun onError(error: ANError) {

                        if (error.getErrorCode() != 0) {
                            val responseStr = error.errorBody


                            val errorResponse = JSONObject(responseStr)


                            if (errorResponse.has("username")) {
                                val errorStrArray = errorResponse.getJSONArray("username")
                                val errorInUserName = errorStrArray.getString(0)

                                input_usernameLogin.error = errorInUserName
                            }

                            if (errorResponse.has("non_field_errors")) {
                                input_usernameLogin.error = "Either the username or the password is incorrect"

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

                //TODO If we need to implement any other activity or a transition on signup this is where we
                //would do that


                this.finish()
            }
        }
    }


    fun validate(): Boolean {
        var valid = true

        val username = input_usernameLogin.text
        val password = input_passwordLogin.text


        if (username.isEmpty() || username.length < 3) {
            input_usernameLogin.error = "at least 3 characters"
            valid = false
        } else {
            input_usernameLogin.error = null
        }

        if (password.isEmpty() || password.length < 8 ) {
            input_passwordLogin.error = "Password must be atleast 8 characters"
            valid = false
        } else {
            input_passwordLogin.error = null
        }

        return valid
    }


    override fun onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true)
    }

    fun onLoginSuccess() {
        btn_login.isEnabled = true
        finish()
    }

    fun onLoginFailed() {
        Toast.makeText(baseContext, "Login failed", Toast.LENGTH_LONG).show()

        btn_login.isEnabled = true
    }
}
