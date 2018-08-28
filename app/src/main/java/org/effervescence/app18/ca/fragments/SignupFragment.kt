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
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.jetbrains.anko.toast
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

        AndroidNetworking.post(Constants.SIGNUP_URL)
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
                            prefs[Constants.KEY_USERNAME] = username
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
                                inputUsernameSignupLayout.error =
                                        errorResponse.getJSONArray("username").getString(0)
                            }

                            if (errorResponse.has("email")) {
                                inputEmailSignupLayout.error = errorResponse.getJSONArray("email").getString(0)
                            }
                        }
                        // handle error
                        onSignupFailed()
                        progressDialog.dismiss()
                    }
                })
    }

    private fun showLoginFragment() {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        val loginFragment = LoginFragment()
        transaction.setCustomAnimations(R.anim.push_right_out_fast, R.anim.push_right_in_fast)
        transaction.replace(R.id.login_signup_fragment_holder, loginFragment)
        transaction.commit()
    }

    fun showUserDetailsInputFragment() {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        val userDetailsInputFragment = UserDetailsInputFragment()
        transaction.setCustomAnimations(R.anim.push_left_in_fast, R.anim.push_left_out_fast)
        transaction.replace(R.id.login_signup_fragment_holder, userDetailsInputFragment)
        transaction.commit()
    }

    fun onSignupSuccess() {
        activity?.toast("Registration Successful")
        //"Registration Successful", Toast.LENGTH_SHORT).show()
        btnSignup.isEnabled = true
    }

    fun onSignupFailed() {
        activity?.toast("Sign up failed")
        btnSignup.isEnabled = true
    }

    private fun validate(username: String, email: String, password: String, reEnterPassword: String): Boolean {
        var valid = true

        if (username.isEmpty() || username.length < 3) {
            inputUsernameSignupLayout.error = "at least 3 characters"
            valid = false
        } else {
            inputUsernameSignupLayout.error = null
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmailSignupLayout.error = "enter a valid email address"
            valid = false
        } else {
            inputEmailSignupLayout.error = null
        }

        if (password.isEmpty() || password.length < 8) {
            inputPasswordSignupLayout.error = "Password must be at least 8 characters long"
            valid = false
        } else {
            inputPasswordSignupLayout.error = null
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length < 8) {
            inputReEnterPasswordSignupLayout.error = "Password must be at least 8 characters long"
            valid = false
        } else if (reEnterPassword != password) {
            inputReEnterPasswordSignupLayout.error = "Passwords do not match"
            valid = false
        } else {
            inputReEnterPasswordSignupLayout.error = null
        }

        return valid
    }

}
