package org.effervescence.app18.ca.fragments

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_user_details_input.*

import org.effervescence.app18.ca.R
import com.androidnetworking.error.ANError
import org.json.JSONObject
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.AndroidNetworking
import org.effervescence.app18.ca.activities.SplashActivity
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.get
import org.effervescence.app18.ca.utilities.MyPreferences.set


class UserDetailsInputFragment : Fragment() {

    var dobString: String = Constants.DATE_OF_BIRTH_DEFAULT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_details_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dobTextView.setOnClickListener { showDatePickerDialog() }
        userDetailsSubmitButton.setOnClickListener { submit() }
    }

    fun submit() {
        
        val progressDialog = ProgressDialog(context)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Saving Information..")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val prefs = MyPreferences.customPrefs(context!!, Constants.MY_SHARED_PREFERENCE)
        val userToken = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]
        val name = nameEditTextView.text.toString()
        val collegeName = collegeEditTextView.text.toString()
        val mobileNo = mobileNoEditTextView.text.toString()
        val referralCode = referralCodeEditTextView.text.toString()
        val fbProfileIdLink = fbUsernameEditTextView.text.toString()

        if(!validate(name, collegeName, mobileNo, fbProfileIdLink)){
            progressDialog.dismiss()
            return
        }


//        val fbLink = "https://www.facebook.com/${fbProfileIdLink.takeLast(15)}"

        AndroidNetworking.post(Constants.REGULAR_USER_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + userToken)
                .addBodyParameter(Constants.NAME_KEY, name)
                .addBodyParameter(Constants.COLLEGE_NAME_KEY, collegeName)
                .addBodyParameter(Constants.DATE_OF_BIRTH_KEY, dobString)
                .addBodyParameter(Constants.GENDER_KEY, getSelectedGender())
                .addBodyParameter(Constants.MOBILE_NO_KEY, mobileNo)
                .addBodyParameter(Constants.SUGGESTED_REFERRAL_KEY, referralCode)
                .addBodyParameter(Constants.FB_ID_KEY, fbProfileIdLink)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        progressDialog.dismiss()

                        //Saving user details in shared preferences
                        prefs[Constants.NAME_KEY] = name
                        prefs[Constants.COLLEGE_NAME_KEY] = collegeName
                        prefs[Constants.DATE_OF_BIRTH_KEY] = dobString
                        prefs[Constants.GENDER_KEY] = getSelectedGender()
                        prefs[Constants.MOBILE_NO_KEY] = mobileNo

                        activity?.finish()
                        Toast.makeText(context, "Details saved successfully", Toast.LENGTH_LONG).show()
                        startActivity(Intent(context, SplashActivity::class.java))
                    }
                    override fun onError(error: ANError) {
                        if (error.errorCode != 0) {

                            val errorResponse = JSONObject(error.errorBody)

                            if (errorResponse.has("phone")) {
                                mobileNoEditTextView.error = "Not a valid mobile no"
                            }

                            if (errorResponse.has("fb_id")) {
                                fbUsernameEditTextView.error = "The profile entered is incorrect"
                            }
                        }
                        progressDialog.dismiss()
                    }
                })
    }

    private fun validate(name: String, collegeName: String, mobileNo: String, fbProfileIdLink: String): Boolean{
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

        if(fbProfileIdLink.isEmpty()){
            fbUsernameEditTextView.error = "This field should not be empty"
            valid = false
        } else {
            mobileNoEditTextView.error = null
        }

        return valid
    }

    fun showDatePickerDialog() {
        val year = 1999
        val month = 0
        val day = 1

        val datePickerDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDayOfMonth ->
            dobString = formatDate(mYear, mMonth+1, mDayOfMonth)
            dobTextView.text = dobString
        }, year, month, day)
        datePickerDialog.datePicker.maxDate = 1136053800000

        datePickerDialog.show()
    }

    private fun getSelectedGender(): String {
        val selectedGenderId = genderRadioGroup.checkedRadioButtonId

        return when(selectedGenderId) {
            R.id.femaleRadioButton -> "F"
            else -> "M"
        }
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {

        val yearString = year.toString()
        val monthString =
                if (month < 10)
                    "0$month"
                else
                    month.toString()
        val dayString =
                if (day < 10)
                    "0$day"
                else
                    day.toString()

        return "$yearString-$monthString-$dayString"

    }
}
