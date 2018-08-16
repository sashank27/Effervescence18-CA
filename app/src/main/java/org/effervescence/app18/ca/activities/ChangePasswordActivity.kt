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
    }

    private fun changePassword() {

        if(currentPasswordEditTextView.text.isEmpty() || newPasswordEditTextView.text.length < 8){
            currentPasswordEditTextView.error = "Doesn't match the current password"
        }
        if(newPasswordEditTextView.text.isEmpty() || newPasswordEditTextView.text.length < 8){
            newPasswordEditTextView.error = "Password must be at least 8 characters long"
            return
        }
        else if(newPasswordEditTextView.text.toString() != confirmNewPasswordEditTextView.text.toString()) {
            confirmNewPasswordEditTextView.error = "Password didn't match"
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
                        //Handle any error other than password mismatch
                    }
                })
    }
}
