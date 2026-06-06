package com.example.chibihabits

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class Onboarding1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        // buttons
        findViewById<Button>(R.id.btnSkip1).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        findViewById<Button>(R.id.btnNext1).setOnClickListener {
            startActivity(Intent(this, Onboarding2Activity::class.java))
            finish()
        }

        // dots (icon selector; prevents stretch)
        val tabs = findViewById<TabLayout>(R.id.dotsIndicator)
        repeat(3) { tabs.addTab(tabs.newTab().setIcon(R.drawable.tab_selector)) }
        tabs.getTabAt(0)?.select()
        tabs.isEnabled = false
        tabs.touchables.forEach { it.isEnabled = false }
    }
}
