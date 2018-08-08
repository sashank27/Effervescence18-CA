package org.effervescence.app18.ca.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.nav_header_home.*
import org.effervescence.app18.ca.R
import org.effervescence.app18.ca.fragments.AboutFragment
import org.effervescence.app18.ca.fragments.HomeFragment
import org.effervescence.app18.ca.fragments.LeaderBoardFragment
import org.effervescence.app18.ca.utilities.UserDetails

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var fragment: Fragment? = null
    private var fragmentClass: Class<*>? = null
    private var currentPage = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.userNameNav).text = UserDetails.Name


        fragmentClass = HomeFragment::class.java
        try {
            fragment = fragmentClass!!.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }

        supportFragmentManager.beginTransaction().replace(R.id.mainContentSpace, fragment).commit()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        var currentFragmentClass = fragmentClass
        fragmentClass = null
        var selectedPage = currentPage

        when (item.itemId) {
            R.id.nav_home -> {
                selectedPage = 1
                fragmentClass = HomeFragment::class.java
            }

            R.id.nav_leader_board -> {
                selectedPage = 2
                fragmentClass = LeaderBoardFragment::class.java
            }

            R.id.nav_about -> {
                selectedPage = 3
                fragmentClass = AboutFragment::class.java
            }

            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        if (currentPage != selectedPage) {

            var startAnimation = R.anim.push_left_in
            var endAnimation = R.anim.push_left_out
            if (currentPage > selectedPage) {
                startAnimation = R.anim.push_right_out
                endAnimation = R.anim.push_right_in
            }
            currentPage = selectedPage


            if(fragmentClass!= null){
                try {
                    fragment = fragmentClass!!.newInstance() as Fragment
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                supportFragmentManager.beginTransaction().setCustomAnimations(startAnimation, endAnimation).replace(R.id.mainContentSpace, fragment).commit()
            }
            else{
                fragmentClass = currentFragmentClass
            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
