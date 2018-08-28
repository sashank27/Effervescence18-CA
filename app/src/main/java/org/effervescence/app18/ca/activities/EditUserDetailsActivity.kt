package org.effervescence.app18.ca.activities

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_edit_user_details.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.get
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.effervescence.app18.ca.utilities.UserDetails
import org.json.JSONObject

class EditUserDetailsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_details)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        nameEditTextView.setText(UserDetails.Name)
        collegeEditTextView.setText(UserDetails.collegeName)
        mobileNoEditTextView.setText(UserDetails.mobileNo)
        fbIdLinkEditTextView.setText(UserDetails.facebookId)
        userDetailsSubmitButton.setOnClickListener { putNewData() }
    }

    private fun putNewData() {
        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Saving Information..")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)
        val userToken = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]
        val name = nameEditTextView.text.toString()
        val collegeName = collegeEditTextView.text.toString()
        val mobileNo = mobileNoEditTextView.text.toString()
        val facebook = fbIdLinkEditTextView.text.toString()

        if(!validate(name, collegeName, mobileNo, facebook)){
            progressDialog.dismiss()
            return
        }

        if(name == UserDetails.Name && collegeName == UserDetails.collegeName
            && mobileNo == UserDetails.mobileNo && facebook == UserDetails.facebookId){
            finish()
            return
        }

        AndroidNetworking.put(Constants.REGULAR_USER_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + userToken)
                .addBodyParameter(Constants.NAME_KEY, name)
                .addBodyParameter(Constants.COLLEGE_NAME_KEY, collegeName)
                .addBodyParameter(Constants.MOBILE_NO_KEY, mobileNo)
                .addBodyParameter(Constants.FB_ID_KEY, facebook)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        progressDialog.dismiss()

                        //Saving user details in shared preferences
                        prefs[Constants.NAME_KEY] = name
                        prefs[Constants.COLLEGE_NAME_KEY] = collegeName
                        prefs[Constants.MOBILE_NO_KEY] = mobileNo
                        prefs[Constants.FB_ID_KEY] = facebook

                        finishAffinity()
                        Toast.makeText(this@EditUserDetailsActivity, "Details updated successfully", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@EditUserDetailsActivity, SplashActivity::class.java))
                    }
                    override fun onError(error: ANError) {
                        if (error.errorCode != 0) {

                            val errorResponse = JSONObject(error.errorBody)

                            if (errorResponse.has("phone")) {
                                mobileNoEditTextView.error = "Not a valid mobile no"
                            }
                            if (errorResponse.has("fb_id")) {
                                fbIdLinkEditTextViewLayout.error = "The profile entered is incorrect"
                            }
                        }
                        Log.e("UserDetailsInput", error.errorBody)
                        progressDialog.dismiss()
                        Toast.makeText(this@EditUserDetailsActivity, error.errorBody, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun validate(name: String, collegeName: String, mobileNo: String, facebook: String): Boolean {
        var valid = true
        if (name.isEmpty()) {
            nameEditTextViewLayout.error = "This field should not be empty"
            valid = false
        } else {
            nameEditTextViewLayout.error = null
        }

        if(collegeName.isEmpty()) {
            collegeEditTextViewLayout.error = "This field should not be empty"
            valid = false
        } else {
            collegeEditTextViewLayout.error = null
        }

        if(mobileNo.isEmpty()){
            mobileNoEditTextViewLayout.error = "This field should not be empty"
            valid = false
        } else if(mobileNo.length != 10) {
            mobileNoEditTextViewLayout.error = "Not a valid mobile no."
            valid = false
        } else {
            mobileNoEditTextViewLayout.error = null
        }

        if(facebook.isEmpty()){
            fbIdLinkEditTextViewLayout.error = "This field should not be empty"
            valid = false
        } else {
            fbIdLinkEditTextViewLayout.error = null
        }
        return valid
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
