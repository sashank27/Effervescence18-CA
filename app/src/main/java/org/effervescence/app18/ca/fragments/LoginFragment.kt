package org.effervescence.app18.ca.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.fragment_login.*
import org.effervescence.app18.ca.EffervescenceCA
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.activities.MainActivity
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.json.JSONObject

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLogin.setOnClickListener { login() }
        tvLinkSignup.setOnClickListener { showSignupFragment() }
    }

    fun login() {
        val username = inputUsernameLogin.text.toString()
        val password = inputPasswordLogin.text.toString()

        if(!validate(username, password)){
            onLoginFailed()
            return
        }

        btnLogin.isEnabled = false

        val progressDialog = ProgressDialog(context)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Authenticating..")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val prefs = MyPreferences.customPrefs(context!!, Constants.MY_SHARED_PREFERENCE)

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

    fun showSignupFragment() {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        val signupFragment = SignupFragment()
        transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out)
        transaction.replace(R.id.login_signup_fragment_holder, signupFragment)
        transaction.commit()
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

    fun onLoginSuccess() {
        btnLogin.isEnabled = true
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        mainActivityIntent.putExtra("loginSuccessFetchUserDetails", true)
        startActivity(mainActivityIntent)
        activity?.finish()
    }

    fun onLoginFailed() {
        Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
        btnLogin.isEnabled = true
    }
}
