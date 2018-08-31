package org.effervescence.app18.ca.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.fragments.ContactsFragment
import org.effervescence.app18.ca.fragments.InfoFragment
import org.effervescence.app18.ca.listeners.OnFragmentInteractionListener

class ContactInfoActivity : AppCompatActivity(), OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_info)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.getIntExtra("fragmentIndex", 0) == 0)
            loadInfoFragment()
        else
            loadContactsFragment()
    }

    private fun loadInfoFragment() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.contact_info_fragment_container, InfoFragment())
                .commit()
    }

    private fun loadContactsFragment() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.contact_info_fragment_container, ContactsFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setTitleTo(title: String) {
        supportActionBar?.title = title
    }


}
