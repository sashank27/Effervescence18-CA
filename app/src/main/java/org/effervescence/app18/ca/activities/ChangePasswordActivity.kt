package org.effervescence.app18.ca.activities

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import kotlinx.android.synthetic.main.activity_change_password.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.get
import org.effervescence.app18.ca.utilities.MyPreferences.set
import com.androidnetworking.error.ANError
import org.json.JSONObject
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.jetbrains.anko.toast


class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var userToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        changePasswordButton.setOnClickListener { changePassword() }
        changePasswordCancelButton.setOnClickListener { finish() }

        val prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)
        userToken = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]

        AndroidNetworking.initialize(applicationContext)
    }

    private fun changePassword() {
        val currentPassword = currentPasswordEditTextView.text.toString()
        val newPassword = newPasswordEditTextView.text.toString()
        val confirmNewPassword = confirmNewPasswordEditTextView.text.toString()

        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate
        progressDialog.setMessage("Changing password..")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        AndroidNetworking.post(Constants.CHANGE_PASSWORD_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_BASE_VALUE + userToken)
                .addBodyParameter(Constants.NEW_PASSWORD_KEY, newPassword)
                .addBodyParameter(Constants.CONFIRM_NEW_PASSWORD_KEY, confirmNewPassword)
                .addBodyParameter(Constants.CURRENT_PASSWORD_KEY, currentPassword)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        progressDialog.dismiss()
                        finish()
                        toast(response.getString("detail"))
                    }

                    override fun onError(error: ANError) {
                        progressDialog.dismiss()
                        toast("Passwords didn't match")
                    }
                })
    }

}
