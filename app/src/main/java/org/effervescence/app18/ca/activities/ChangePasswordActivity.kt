package org.effervescence.app18.ca.activities

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.androidnetworking.AndroidNetworking
import kotlinx.android.synthetic.main.activity_change_password.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.get
import com.androidnetworking.error.ANError
import org.json.JSONObject
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.jetbrains.anko.toast


class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var userToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        changePasswordButton.setOnClickListener { changePassword() }

        val prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)
        userToken = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]
    }

    private fun changePassword() {
        if (!isFieldsValid()) {
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate
        progressDialog.setMessage("Changing password..")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        AndroidNetworking.post(Constants.CHANGE_PASSWORD_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + userToken)
                .addBodyParameter(Constants.NEW_PASSWORD_KEY, newPasswordEditTextView.text.toString())
                .addBodyParameter(Constants.CONFIRM_NEW_PASSWORD_KEY, confirmNewPasswordEditTextView.text.toString())
                .addBodyParameter(Constants.CURRENT_PASSWORD_KEY, currentPasswordEditTextView.text.toString())
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        progressDialog.dismiss()
                        finish()
                        toast(response.getString("detail"))
                    }

                    override fun onError(error: ANError) {
                        progressDialog.dismiss()
                        toast("Connection Broke :(")
                    }
                })
    }

    private fun isFieldsValid(): Boolean {
        var valid = true
        if (currentPasswordEditTextView.text.isEmpty()) {
            currentPasswordEditTextViewLayout.error = "This field should not be empty"
            valid = false
        }
        else if (currentPasswordEditTextView.text.length < 8) {
            currentPasswordEditTextViewLayout.error = "Wrong Password"
            valid = false
        }
        else {
            currentPasswordEditTextViewLayout.error = null
        }

        when {
            newPasswordEditTextView.text.isEmpty() -> {
                newPasswordEditTextViewLayout.error = "This field should not be empty"
                valid = false
            }
            newPasswordEditTextView.text.length < 8 -> {
                newPasswordEditTextViewLayout.error = "Password must be at least of 8 characters"
                valid = false
            }
            else -> newPasswordEditTextViewLayout.error = null

        }

        when {
            confirmNewPasswordEditTextView.text.isEmpty() -> {
                confirmNewPasswordEditTextViewLayout.error = "This field should not be empty"
                valid = false
            }
            confirmNewPasswordEditTextView.text.length < 8 -> {
                confirmNewPasswordEditTextViewLayout.error = "Password must be at least of 8 characters"
                valid = false
            }
            (confirmNewPasswordEditTextView.text.toString() != newPasswordEditTextView.text.toString()) -> {
                confirmNewPasswordEditTextViewLayout.error = "Those passwords didn't match"
                valid = false
            }
            else -> confirmNewPasswordEditTextViewLayout.error = null
        }
        return valid
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
