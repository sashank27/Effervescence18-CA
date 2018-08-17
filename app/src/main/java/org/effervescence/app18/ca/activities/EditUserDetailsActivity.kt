package org.effervescence.app18.ca.activities

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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

        if(!validate(name, collegeName, mobileNo)){
            progressDialog.dismiss()
            return
        }

        AndroidNetworking.put(Constants.REGULAR_USER_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + userToken)
                .addBodyParameter(Constants.NAME_KEY, name)
                .addBodyParameter(Constants.COLLEGE_NAME_KEY, collegeName)
                .addBodyParameter(Constants.MOBILE_NO_KEY, mobileNo)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        progressDialog.dismiss()

                        //Saving user details in shared preferences
                        prefs[Constants.NAME_KEY] = name
                        prefs[Constants.COLLEGE_NAME_KEY] = collegeName
                        prefs[Constants.MOBILE_NO_KEY] = mobileNo

                        finishAffinity()
                        Toast.makeText(this@EditUserDetailsActivity, "Details updated successfully", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@EditUserDetailsActivity, SplashActivity::class.java))
                    }
                    override fun onError(error: ANError) {
                        progressDialog.dismiss()
                        Toast.makeText(this@EditUserDetailsActivity, error.errorBody, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun validate(name: String, collegeName: String, mobileNo: String): Boolean {
        var valid = true
        if (name.isEmpty()) {
            nameEditTextView.error = "This field should not be empty"
            valid = false
        } else {
            nameEditTextView.error = null
        }

        if(collegeName.isEmpty()) {
            collegeEditTextView.error = "This field should not be empty"
            valid = false
        } else {
            collegeEditTextView.error = null
        }

        if(mobileNo.isEmpty()){
            mobileNoEditTextView.error = "This field should not be empty"
            valid = false
        } else if(mobileNo.length != 10) {
            mobileNoEditTextView.error = "Not a valid mobile no."
            valid = false
        } else {
            mobileNoEditTextView.error = null
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
