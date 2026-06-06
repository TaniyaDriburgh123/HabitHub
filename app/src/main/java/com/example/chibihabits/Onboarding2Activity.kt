package com.example.chibihabits

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class Onboarding2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding2)

        // buttons
        findViewById<Button>(R.id.btnSkip2).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        findViewById<Button>(R.id.btnNext2).setOnClickListener {
            startActivity(Intent(this, Onboarding3Activity::class.java))
            finish()
        }

        // dots (icon selector; prevents stretch)
        val tabs = findViewById<TabLayout>(R.id.dotsIndicator2)
        repeat(3) { tabs.addTab(tabs.newTab().setIcon(R.drawable.tab_selector)) }
        tabs.getTabAt(1)?.select()
        tabs.isEnabled = false
        tabs.touchables.forEach { it.isEnabled = false }
    }
}
