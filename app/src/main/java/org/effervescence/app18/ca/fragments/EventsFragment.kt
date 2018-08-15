package org.effervescence.app18.ca.fragments

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import kotlinx.android.synthetic.main.fragment_events.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.adapters.MyEventsRecyclerViewAdapter
import org.effervescence.app18.ca.utilities.Constants
import org.effervescence.app18.ca.utilities.compressImage
import org.json.JSONArray
import org.json.JSONObject
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.effervescence.app18.ca.models.EventDetails
import org.effervescence.app18.ca.utilities.UserDetails
import java.io.File
import java.util.*


class EventsFragment : Fragment() {

    private val IMAGE_PICKER_REQUEST_CODE = 1
    private var pickedEventId = -1

    private var mEventDetailsList = ArrayList<EventDetails>()
    var listAdapter: MyEventsRecyclerViewAdapter = MyEventsRecyclerViewAdapter(mEventDetailsList)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buildRecyclerView()

        listAdapter.setOnClickListener(object : MyEventsRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClicked(position: Int) {
                pickedEventId = position+1
                openImagePicker()
            }
        })
    }

    private fun buildRecyclerView() {
        mEventDetailsList = getEventsList()
        listAdapter.notifyDataSetChanged()

        events_list.setHasFixedSize(true)
        events_list.layoutManager = LinearLayoutManager(context)
        events_list.adapter = listAdapter
    }

    fun getEventsList(): ArrayList<EventDetails> {

        var i = 0

        AndroidNetworking.get(Constants.EVENTS_LIST_URL)
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray) {
                        if(response.length() > 0) {
                            while (response.length() > i) {
                                mEventDetailsList.add(createEventDetailsObject(response.getJSONObject(i++)))
                                listAdapter.notifyItemChanged(i)
                            }
                        }
                        listAdapter.notifyDataSetChanged()
                        events_list_progress_bar.visibility = View.GONE
                    }

                    override fun onError(error: ANError) {
                        Log.e("EventsFragment", error.errorBody)
                    }
                })
        return mEventDetailsList
    }

    fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val title = getTitleFromUri(data.data)
            uploadImageWithURI(compressImage(activity?.applicationContext, data.data, title))
        }
    }

    private fun uploadImageWithURI(imageUri: Uri?) {

        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Uploading...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.isIndeterminate = true
        progressDialog.show()

        if (imageUri != null) {
            val file = File(imageUri.path)

            AndroidNetworking.upload(Constants.FILE_UPLOAD_URL)
                    .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + UserDetails.Token)
                    .addMultipartParameter(Constants.EVENT_ID_KEY, pickedEventId.toString())
                    .addMultipartFile("file", file)
                    .build()
                    .setUploadProgressListener { bytesUploaded, totalBytes ->
                        // do anything with progress
                    }
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            // do anything with response
                            Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show()
                            Log.e("EventFragment", response.toString())
                            progressDialog.dismiss()
                        }

                        override fun onError(error: ANError) {
                            // handle error
                            Toast.makeText(context, error.errorBody.toString(), Toast.LENGTH_LONG).show()
                            progressDialog.dismiss()
                        }
                    })
        } else {
            progressDialog.dismiss()
            Toast.makeText(context, "Image not get selected properly", Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(context, "Request Completed", Toast.LENGTH_LONG).show()
    }

    private fun getTitleFromUri(uri: Uri): String {
        var result = ""

        if(uri.scheme == "content") {
            val cursor = activity?.contentResolver?.query(uri, null, null,
                    null, null)

            cursor.use { cursor ->
                if(cursor != null && cursor.moveToFirst()) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if(id != -1) {
                        result = cursor.getString(id)
                    }
                }
            }
        }
        if (result == "") {
            result = uri.path
            val cut = result.lastIndexOf('/')

            if (cut != -1)
                result = result.substring(cut+1)
        }
        return result
    }

    fun createEventDetailsObject(eventJSONObject: JSONObject): EventDetails {

        return EventDetails(eventJSONObject.optInt(Constants.EVENT_ID_KEY),
                eventJSONObject.optString(Constants.EVENT_NAME_KEY),
                eventJSONObject.optString(Constants.EVENT_DESCRIPTION_KEY),
                eventJSONObject.optInt(Constants.EVENT_PRIZE_KEY),
                eventJSONObject.optInt(Constants.EVENT_POINTS_KEY),
                eventJSONObject.optInt(Constants.EVENT_FEE_KEY))
    }
}