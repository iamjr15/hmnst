package com.example.avocode.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.balysv.materialripple.MaterialRippleLayout
import com.bumptech.glide.Glide
import com.example.avocode.R
import com.example.avocode.config.Constants.FRAGMENT_HOME
import com.example.avocode.fragment.HomeFragment
import com.example.avocode.utils.Util.Companion.checkEmptyStrings
import com.google.firebase.auth.FirebaseAuth
import com.orm.SchemaGenerator
import com.orm.SugarContext
import com.orm.SugarDb
import com.orm.SugarRecord.findById
import dbmodel.User
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_main.*

//HomeActivity to show map on screen and navigation to another screens
class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var currentFragment = 0
    private val bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSelectedFragment(FRAGMENT_HOME, bundle)
        imageViewUser.setImageResource(R.drawable.ic_user)
        initDrawer()
        imageViewDrawer.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }
        textViewCity.setOnClickListener {
            showCapitals()
        }
    }

    // TO SET FRAGMENTS ON HomeActivity DYNAMICALLY
    private fun setSelectedFragment(itemId: Int, bundle: Bundle) {
        if (currentFragment != itemId) {
            val fragment: Fragment
            when (itemId) {
                FRAGMENT_HOME -> {
                    fragment = HomeFragment()
                    fragment.setArguments(bundle)
                }
                else -> {
                    fragment = HomeFragment()
                    fragment.setArguments(bundle)
                }
            }
            invalidateOptionsMenu()
            try {
                val ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.content, fragment)
                ft.addToBackStack(fragment.javaClass.getName())
                ft.commitAllowingStateLoss()
            } catch (ignore: Exception) {
            }

        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }
        if (currentFragment == FRAGMENT_HOME) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    // Initialize Drawer and load data
    private fun initDrawer() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this@HomeActivity, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout!!.addDrawerListener(toggle)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val view = navigationView.getHeaderView(0)

        //Get  user's profile
        val user = findById(User::class.java, 1)
        if (user != null) {
            if (!checkEmptyStrings(user.uriPath)) {
                val avatar = Glide.with(this).load(user.uriPath)
                avatar.into(view.findViewById(R.id.imgAvatar) as ImageView)
                avatar.into(imageViewUser)
            }
            val textViewFullName = view.findViewById<TextView>(R.id.textViewFullName)
            val textViewNumber = view.findViewById<TextView>(R.id.textViewNumber)
            textViewFullName.text = String.format("%s %s", user.firstName, user.lastName)
            textViewNumber.text = user.phone
            val btnFamily = view.findViewById<MaterialRippleLayout>(R.id.btnFamily)
            btnFamily.setOnClickListener {
                closeDrawer()

                val intent = Intent(this@HomeActivity, FamilyActivity::class.java)
                intent.putExtra(getString(R.string.familyCode), user.familyCode)
                startActivity(intent)
            }
            val btnSignOut = view.findViewById<MaterialRippleLayout>(R.id.btnSignOut)
            btnSignOut.setOnClickListener {
                closeDrawer()

                val alertDialog = AlertDialog.Builder(this@HomeActivity)
                        .setTitle(getString(R.string.attention))
                        .setMessage(getString(R.string.message_sign_out))
                        .setPositiveButton(android.R.string.yes) { dialog, which ->
                            // Continue with sign out operation
                            try {
                                FirebaseAuth.getInstance().signOut()
                                SugarContext.terminate()
                                val schemaGenerator = SchemaGenerator(applicationContext)
                                schemaGenerator.deleteTables(SugarDb(applicationContext).db)
                                SugarContext.init(applicationContext)
                                schemaGenerator.createDatabase(SugarDb(applicationContext).db)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            startActivity(Intent(this@HomeActivity, MainActivity::class.java))
                            finish()
                        }

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .show()

                if (alertDialog.window != null) {
                    val alertTitle = alertDialog.window!!.findViewById<TextView>(R.id.alertTitle)
                    val message = alertDialog.window!!.findViewById<TextView>(android.R.id.message)
                    val button1 = alertDialog.window!!.findViewById<Button>(android.R.id.button1)
                    val button2 = alertDialog.window!!.findViewById<Button>(android.R.id.button2)
                    val face = ResourcesCompat.getFont(this@HomeActivity, R.font.mr)
                    alertTitle.typeface = face
                    button2.typeface = face
                    button1.typeface = face
                    message.typeface = face
                }
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        startActivity(Intent(this, FamilyActivity::class.java))
        return false
    }

    private fun closeDrawer() {
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    fun setCity(city: String) {
        textViewCity.text = city
    }

    private fun showCapitals() {
        val builderSingle = AlertDialog.Builder(this@HomeActivity)
        builderSingle.setTitle("Select One City")
        val arrayAdapter = ArrayAdapter.createFromResource(this@HomeActivity, R.array.capitals, R.layout.item_capital)

        builderSingle.setNegativeButton("cancel") { dialog, which -> dialog.dismiss() }

        builderSingle.setAdapter(arrayAdapter) { dialog, which -> textViewCity!!.text = arrayAdapter.getItem(which).toString() }
        val alertDialog = builderSingle.show()
        if (alertDialog.window != null) {
            val alertTitle = alertDialog.window!!.findViewById<TextView>(R.id.alertTitle)
            val button2 = alertDialog.window!!.findViewById<Button>(android.R.id.button2)
            val face = ResourcesCompat.getFont(this, R.font.mr)
            alertTitle.typeface = face
            button2.typeface = face
        }
    }

}
