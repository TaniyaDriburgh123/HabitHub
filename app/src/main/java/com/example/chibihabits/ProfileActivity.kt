package com.example.chibihabits

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {
    private val PREFS = "chibi_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveProfile)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val btnReset = findViewById<MaterialButton>(R.id.btnResetApp)

        fun populate() {
            val name = sp.getString("name", "Chibi") ?: "Chibi"
            val email = sp.getString("email", "") ?: ""

            tvName.text = "Name: $name"
            tvEmail.text = if (email.isBlank()) "Email: —" else "Email: $email"

            // Prefill edit fields
            if (etName.text.isNullOrBlank()) etName.setText(name)
            if (etEmail.text.isNullOrBlank()) etEmail.setText(email)
        }

        btnSave.setOnClickListener {
            val newName = etName.text?.toString()?.trim().orEmpty()
            val newEmail = etEmail.text?.toString()?.trim().orEmpty()

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newEmail.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sp.edit()
                .putString("name", newName)
                .putString("email", newEmail)
                .apply()

            populate()
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()

            // Let the Dashboard re-read name on resume
            setResult(RESULT_OK)
        }

        btnLogout.setOnClickListener {
            sp.edit().putBoolean("logged_in", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnReset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset app data?")
                .setMessage("This will clear saved preferences like name, email, habits for today, hydration, etc.")
                .setPositiveButton("Reset") { _, _ ->
                    sp.edit().clear().apply()
                    Toast.makeText(this, "App data cleared", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        populate()
    }
}
