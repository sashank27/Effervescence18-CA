package org.effervescence.app18.ca.fragments

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_user_details_input.*

import org.effervescence.app18.ca.R
import java.util.*
import com.androidnetworking.error.ANError
import org.json.JSONObject
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.AndroidNetworking
import org.effervescence.app18.ca.activities.MainActivity
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

        userDetailsSubmitButton.isEnabled = false
        
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

        AndroidNetworking.post(Constants.REGULAR_USER_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + userToken)
                .addBodyParameter(Constants.NAME_KEY, name)
                .addBodyParameter(Constants.COLLEGE_NAME_KEY, collegeName)
                .addBodyParameter(Constants.DATE_OF_BIRTH_KEY, dobString)
                .addBodyParameter(Constants.GENDER_KEY, getSelectedGender())
                .addBodyParameter(Constants.MOBILE_NO_KEY, mobileNo)
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
                        startActivity(Intent(context, MainActivity::class.java))
                    }
                    override fun onError(error: ANError) {
                        progressDialog.dismiss()
                        Toast.makeText(context, error.errorBody, Toast.LENGTH_SHORT).show()
                        userDetailsSubmitButton.isEnabled = true
                    }

                })
    }

    fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDayOfMonth ->
            dobString = formatDate(mYear, mMonth+1, mDayOfMonth)
            dobTextView.text = dobString
        }, year, month, day)
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
