package com.example.assignment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.assignment.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(),  NavigationView.OnNavigationItemSelectedListener {
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        navView.itemTextColor = colorStateList
        navView.itemIconTintList = colorStateList
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navigationBar()
    }

    private fun navigationBar() {
       val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView!!.setNavigationItemSelectedListener(this)
        binding.apply {
            toggle = ActionBarDrawerToggle(
                this@MainActivity,
                navDrawerLayout,
                R.string.nav_open,
                R.string.nav_close
            )
            navDrawerLayout.addDrawerListener(toggle)
            toggle.syncState()

            val img: ImageView = findViewById(R.id.nav_icon)
            img.setOnClickListener() {
                if (navDrawerLayout.isOpen) {
                    navDrawerLayout.openDrawer(Gravity.RIGHT)
                } else {
                    navDrawerLayout.openDrawer(Gravity.LEFT)
                }
            }

            supportActionBar?.setDisplayHomeAsUpEnabled(true)

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }
}