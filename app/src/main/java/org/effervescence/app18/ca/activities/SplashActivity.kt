package org.effervescence.app18.ca.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.MyPreferences
import org.effervescence.app18.ca.utilities.MyPreferences.get
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.jetbrains.anko.startActivity
import com.androidnetworking.error.ANError
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.cloudinary.android.MediaManager
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_main.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.utilities.UserDetails
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import org.json.JSONObject

class SplashActivity : AppCompatActivity() {

    lateinit var prefs: SharedPreferences
    val REQUEST_WRITE_PERMISSION = 555
    lateinit var userToken: String
    var writeRequestResponse = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Getting the value of the user token
        prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)
        userToken = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]

        effe_logo.setOnClickListener { startAppSettings() }
        Paper.init(applicationContext)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checksAfterGettingPermission()
        } else {
            if (Build.VERSION.SDK_INT >= 23 && shouldShowRequestPermissionRationale(permissions[0])) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Permission Denied")
                builder.setMessage("Without this permission the app will not be able to upload " +
                        "images to the server. Hence you will not get any points.Are you sure " +
                        "you want to deny this permission?")
                builder.setPositiveButton("RE-TRY", DialogInterface.OnClickListener { _, _ ->
                    requestPermissions(Array(1) { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                            REQUEST_WRITE_PERMISSION)
                })
                builder.setNegativeButton("I'M SURE", DialogInterface.OnClickListener { _, _ ->
                    writeRequestResponse = 0
                    splashMessageTV.text = "Please grant storage permission manually, to access the app.\n" +
                            "Tap effe logo to open app settings."
                })

                builder.create().show()
            } else {
                writeRequestResponse = 0
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(Array(1) { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        REQUEST_WRITE_PERMISSION)
            } else {
                checksAfterGettingPermission()
            }
        } else {
            checksAfterGettingPermission()
        }
    }

    override fun onResume() {
        super.onResume()

        if(writeRequestResponse == 0)
            splashMessageTV.text = "Please grant storage permission manually, to access the app.\n" +
                    "Tap effe logo to open app settings."
    }

    private fun checksAfterGettingPermission() {
        if (userToken == Constants.TOKEN_DEFAULT) {
            startLogin()
        } else {
            if (intent.getBooleanExtra("loginSuccessFetchUserDetails", false))
                fetchUserDetailsThanDisplay()
            else {
                if (prefs[Constants.NAME_KEY, Constants.NAME_DEFAULT] == Constants.NAME_DEFAULT)
                    askForUserDetails()
                else
                    launchHomeActivity()
            }
        }
    }

    fun launchHomeActivity() {
        UserDetails.Name = prefs[Constants.NAME_KEY, Constants.NAME_DEFAULT]
        UserDetails.Token = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]
        UserDetails.userName = prefs[Constants.KEY_USERNAME, Constants.USERNAME_DEFAULT]
        UserDetails.collegeName = prefs[Constants.COLLEGE_NAME_KEY, Constants.COLLEGE_NAME_DEFAULT]
        UserDetails.mobileNo = prefs[Constants.MOBILE_NO_KEY, Constants.MOBILE_NO_DEFAULT]
        UserDetails.facebookId = prefs[Constants.FB_ID_KEY, Constants.FB_ID_DEFAULT]
        startActivity<HomeActivity>()
        finish()
    }

    private fun fetchUserDetailsThanDisplay() {

        val progressDialog = ProgressDialog(this)
        progressDialog.isIndeterminate = false
        progressDialog.setMessage("Getting User Details..")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val userToken = prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]

        AndroidNetworking.get(Constants.REGULAR_USER_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + userToken)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        saveUserDetails(response)
                        launchHomeActivity()
                        progressDialog.dismiss()
                    }

                    override fun onError(error: ANError) {
                        if (error.errorBody.contains("query does not exist")) {
                            askForUserDetails()
                            toast("Details not found")
                        }
                        progressDialog.dismiss()
                    }
                })

    }

    fun saveUserDetails(response: JSONObject) {
        //Saving user details in shared preferences
        prefs[Constants.NAME_KEY] = response.optString(Constants.NAME_KEY)
        prefs[Constants.COLLEGE_NAME_KEY] = response.optString(Constants.COLLEGE_NAME_KEY)
        prefs[Constants.DATE_OF_BIRTH_KEY] = response.optString(Constants.DATE_OF_BIRTH_KEY)
        prefs[Constants.GENDER_KEY] = response.optString(Constants.GENDER_KEY)
        prefs[Constants.MOBILE_NO_KEY] = response.optString(Constants.MOBILE_NO_KEY)
        prefs[Constants.REFERRAL_KEY] = response.optString(Constants.REFERRAL_KEY)
        prefs[Constants.FB_ID_KEY] = response.optString(Constants.FB_ID_KEY)
    }

    fun askForUserDetails() {
        val userDetailsIntent = Intent(this, LoginSignupActivity::class.java)
        userDetailsIntent.putExtra("userDetailsRequired", true)
        startActivity(userDetailsIntent)
        finish()
    }

    private fun startLogin() {
        startActivity<LoginSignupActivity>()
        finish()
    }

    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri =  Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQUEST_WRITE_PERMISSION)
    }

}
