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
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.fragment_events.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.adapters.MyEventsRecyclerViewAdapter
import org.json.JSONArray
import org.json.JSONObject
import io.paperdb.Paper
import okhttp3.*
import org.effervescence.app18.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app18.ca.models.EventDetails
import org.effervescence.app18.ca.utilities.*
import org.effervescence.app18.ca.utilities.MyPreferences.get
import org.effervescence.app18.ca.utilities.MyPreferences.set
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import kotlin.collections.ArrayList

class EventsFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private val IMAGE_PICKER_REQUEST_CODE = 1
    private var pickedEventId = -1
    private lateinit var prefs: SharedPreferences

    private var mEventDetailsList = ArrayList<EventDetails>()
    var listAdapter: MyEventsRecyclerViewAdapter = MyEventsRecyclerViewAdapter(mEventDetailsList)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        prefs = MyPreferences.customPrefs(activity!!.applicationContext, Constants.MY_SHARED_PREFERENCE)

        if (listener != null) {
            listener!!.setTitleTo("Events")
        }
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buildRecyclerView()

        listAdapter.setOnClickListener(object : MyEventsRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClicked(position: Int) {
                pickedEventId = position + 1
                openImagePicker()
            }
        })

        events_swipe_refresh.setOnRefreshListener { updateEventsListCache() }
    }

    private fun updateEventsListCache() {
        doAsync {
            Paper.book().delete(Constants.EVENTS_CACHED_KEY)
        }
        mEventDetailsList.clear()
        prefs[Constants.EVENTS_CACHED_KEY] = Constants.EVENTS_CACHED_DEFAULT
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
        if (prefs[Constants.EVENTS_CACHED_KEY, Constants.EVENTS_CACHED_DEFAULT] == "true") {
            doAsync {
                mEventDetailsList = ArrayList(Paper.book().read<ArrayList<EventDetails>>(Constants.EVENTS_CACHED_KEY))
                uiThread {
                    listAdapter.swapList(mEventDetailsList)
                    listAdapter.notifyDataSetChanged()
                    events_list_progress_bar.visibility = View.GONE
                }
            }
        } else {
            prefs[Constants.EVENTS_CACHED_KEY] = "true"
            AndroidNetworking.get(Constants.EVENTS_LIST_URL)
                    .setPriority(Priority.IMMEDIATE)
                    .build()
                    .getAsJSONArray(object : JSONArrayRequestListener {
                        override fun onResponse(response: JSONArray) {
                            if (response.length() > 0) {
                                while (response.length() > i) {
                                    mEventDetailsList.add(createEventDetailsObject(response.getJSONObject(i++)))
                                }
                            }
                            listAdapter.notifyDataSetChanged()
                            events_list_progress_bar.visibility = View.GONE
                            doAsync {
                                Paper.book().write(Constants.EVENTS_CACHED_KEY, mEventDetailsList)
                                uiThread {
                                    events_swipe_refresh.isRefreshing = false
                                }
                            }
                        }

                        override fun onError(error: ANError) {
                            Log.e("EventsFragment", error.errorBody)
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
            uploadImageWithURI(compressImage(context, data.data, title))
//            uploadImageWithURI(data.data)
        }
    }

    private fun getRealPathFromURI(contentURI: Uri?): String {
        val result: String
        val cursor = activity!!.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            result = contentURI!!.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    private fun uploadImageWithURI(imageUri: Uri?) {

        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Uploading...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.isIndeterminate = true
        progressDialog.show()

        if (imageUri != null) {
            val file = File(getRealPathFromURI(imageUri))
            val userToken = Constants.TOKEN_STRING + prefs[Constants.KEY_TOKEN, Constants.TOKEN_DEFAULT]

            val eventIdPart = RequestBody.create(MultipartBody.FORM, pickedEventId.toString())
            val imagePart = RequestBody.create(
                    MediaType.parse(activity!!.contentResolver.getType(imageUri)), file)

            val retrofitBuilder = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())

            val retrofit = retrofitBuilder.build()
            val client = retrofit.create(ImageUploadClient::class.java)
            val call: Call<ResponseBody> = client.uploadImage(userToken, eventIdPart, imagePart)
            val callback: Callback<ResponseBody> = object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                    Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show()
                    progressDialog.dismiss()
                }

                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    Toast.makeText(context, "Error :(", Toast.LENGTH_LONG).show()
                    Log.e("ImageUpload", t.toString())
                }
            }
            call.enqueue(callback)
        } else {
            progressDialog.dismiss()
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