package org.effervescence.app18.ca.fragments

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.adapters.MyEventsRecyclerViewAdapter
import org.json.JSONArray
import org.json.JSONObject
import io.paperdb.Paper
import kotlinx.android.synthetic.main.fragment_events.*
import org.effervescence.app18.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app18.ca.models.EventDetails
import org.effervescence.app18.ca.utilities.*
import org.effervescence.app18.ca.utilities.MyPreferences.get
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import kotlin.collections.ArrayList

class EventsFragment : Fragment() {

    companion object {
        const val IMAGE_PICKER_REQUEST_CODE = 1
    }

    private var mPickedEventId = -1
    private lateinit var mPrefs: SharedPreferences
    private lateinit var mImageUploadRequestId: String
    private var mEventDetailsList = ArrayList<EventDetails>()
    private var listener: OnFragmentInteractionListener? = null
    var listAdapter: MyEventsRecyclerViewAdapter = MyEventsRecyclerViewAdapter(mEventDetailsList)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mPrefs = MyPreferences.customPrefs(activity!!.applicationContext, Constants.MY_SHARED_PREFERENCE)
        if (listener != null) {
            listener!!.setTitleTo("Events")
        }
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        events_swipe_refresh.isRefreshing = true
        buildRecyclerView()

        listAdapter.setOnClickListener(object : MyEventsRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClicked(position: Int) {
                mPickedEventId = position + 1
                openImagePicker()
            }
        })
        events_swipe_refresh.setOnRefreshListener { updateEventsListCache() }
    }

    private fun updateEventsListCache() {
        doAsync {
            Paper.book().delete(Constants.EVENTS_CACHED_KEY)
        }
        mPrefs[Constants.EVENTS_CACHED_KEY] = Constants.EVENTS_CACHED_DEFAULT
        getEventsList()
    }

    private fun buildRecyclerView() {
        getEventsList()

        events_list.setHasFixedSize(true)
        events_list.layoutManager = LinearLayoutManager(context)
        events_list.adapter = listAdapter
    }

    private fun getEventsList() {
        var i = 0
        if (isEventsCached()) {
            doAsync {
                mEventDetailsList = ArrayList(Paper.book()
                        .read<ArrayList<EventDetails>>(Constants.EVENTS_CACHED_KEY))
                uiThread {
                    listAdapter.swapList(mEventDetailsList)
                    listAdapter.notifyDataSetChanged()
                    events_swipe_refresh.isRefreshing = false
                }
            }
        } else {
            mPrefs[Constants.EVENTS_CACHED_KEY] = "true"
            AndroidNetworking.get(Constants.EVENTS_LIST_URL)
                    .build()
                    .getAsJSONArray(object : JSONArrayRequestListener {
                        override fun onResponse(response: JSONArray) {
                            if (response.length() > 0) {
                                mEventDetailsList.clear()
                                while (response.length() > i) {
                                    mEventDetailsList.add(createEventDetailsObject(response.getJSONObject(i++)))
                                }
                            }
                            listAdapter.notifyDataSetChanged()
                            events_swipe_refresh.isRefreshing = false
                            doAsync {
                                Paper.book().write(Constants.EVENTS_CACHED_KEY, mEventDetailsList)
                            }
                        }

                        override fun onError(error: ANError) {
                            Log.e("EventsFragment", error.errorBody)
                            events_swipe_refresh.isRefreshing = false
                            Toast.makeText(context, "Connection Broke :(", Toast.LENGTH_SHORT).show()
                        }
                    })
        }
    }

    fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val title = getTitleFromUri(data.data)
            uploadImageWithURI(compressImage(context, data.data, title), title)
        }
    }

    private fun uploadImageWithURI(imageUri: Uri?, title: String) {

        val progressDialog = ProgressDialog(context)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Compressing Image...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        if (imageUri != null) {

            val options = HashMap<String, String>()
            options["public_id"] = "${UserDetails.userName}/$title"
            options["tags"] = mPickedEventId.toString()

            mImageUploadRequestId = MediaManager.get()
                    .upload(imageUri).options(options)
                    .unsigned("i5nefzvx")
                    .callback(object : UploadCallback {

                        override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                            Toast.makeText(activity!!.applicationContext, "Upload Successful :)",
                                    Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
//                            val pro: Double = (bytes.toDouble() / totalBytes.toDouble()) * 100
//                            progressDialog.progress = pro.toInt()
//                            progressDialog.setMessage("${pro.toInt()}% Uploaded")
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            //Not Required
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            Toast.makeText(context, "Error happened :(", Toast.LENGTH_SHORT).show()
                            Log.e("Image Upload Error", error.toString())
                        }

                        override fun onStart(requestId: String?) {
                            progressDialog.setMessage("Uploading...")
                        }

                    })
                    .dispatch()
        } else {
            Toast.makeText(context, "Image no more on storage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTitleFromUri(uri: Uri): String {
        var result = ""

        if (uri.scheme == "content") {
            val cursor = activity?.contentResolver?.query(uri, null, null,
                    null, null)

            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (id != -1) {
                        result = cursor.getString(id)
                    }
                }
            }
        }
        if (result == "") {
            result = uri.path
            val cut = result.lastIndexOf('/')

            if (cut != -1)
                result = result.substring(cut + 1)
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

    private fun isEventsCached(): Boolean {
        return when(mPrefs[Constants.EVENTS_CACHED_KEY, Constants.EVENTS_CACHED_DEFAULT]) {
            "true" -> true
            else -> false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}