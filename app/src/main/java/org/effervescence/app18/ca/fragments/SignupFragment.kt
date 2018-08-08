package org.effervescence.app18.ca.fragments

import android.app.ProgressDialog
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
import kotlinx.android.synthetic.main.fragment_signup.*
import org.effervescence.app18.ca.EffervescenceCA
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.json.JSONObject

class SignupFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSignup.setOnClickListener { signup() }
        tvLinkLogin.setOnClickListener { showLoginFragment() }
    }

    private fun signup() {
        val username = inputUsernameSignup.text.toString()
        val email = inputEmailSignup.text.toString()

        val password = inputPasswordSignup.text.toString()
        val reEnterPassword = inputReEnterPasswordSignup.text.toString()

        if (!validate(username, email, password, reEnterPassword)) {
            onSignupFailed()
            return
        }

        btnSignup.isEnabled = false

        val progressDialog = ProgressDialog(context)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Creating Account...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()


        val prefs = MyPreferences.customPrefs(context!!, Constants.MY_SHARED_PREFERENCE)

        AndroidNetworking.post(EffervescenceCA.BASE_URL + "/api/registration/")
                .addBodyParameter("email", email)
                .addBodyParameter("username", username)
                .addBodyParameter("password1", password)
                .addBodyParameter("password2", reEnterPassword)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {

                    override fun onResponse(response: JSONObject) {

                        try {
                            val token = response.getString("key")
                            prefs[Constants.KEY_TOKEN] = token
                        } catch (e: Exception) {
                            Log.d("Response", response.toString())
                        }

                        onSignupSuccess()
                        progressDialog.dismiss()
                        showUserDetailsInputFragment()
                    }

                    override fun onError(error: ANError) {
                        if (error.errorCode != 0) {
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

    fun showLoginFragment() {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        val loginFragment = LoginFragment()
        transaction.setCustomAnimations(R.anim.push_right_out_fast, R.anim.push_right_in_fast)
        transaction.replace(R.id.login_signup_fragment_holder, loginFragment)
        transaction.commit()
    }

    fun showUserDetailsInputFragment() {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        val userDetailsInputFragment = UserDetailsInputFragment()
        transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out)
        transaction.replace(R.id.login_signup_fragment_holder, userDetailsInputFragment)
        transaction.commit()
    }

    fun onSignupSuccess() {
        Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
        btnSignup.isEnabled = true
    }

    fun onSignupFailed() {
        Toast.makeText(context, "Sign up failed", Toast.LENGTH_SHORT).show()
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
        } else if (reEnterPassword != password) {
            inputReEnterPasswordSignup.error = "Passwords do not match"
            valid = false
        } else {
            inputReEnterPasswordSignup.error = null
        }

        return valid
    }

}
