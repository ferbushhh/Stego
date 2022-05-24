package com.example.stego

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var record: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        record = findViewById(com.example.stego.R.id.record)

        record.setOnClickListener {
            val intent = Intent(this@MainActivity, Dictophone::class.java)
            startActivity(intent)
        }
    }

    fun toHide (view: View) {
        val intent = Intent(this, SecondActivity::class.java)
        intent.putExtra("fileName", " ")
        intent.putExtra("flagFile", false)
        startActivity(intent)
    }

    fun toFind (view: View) {
        val intent = Intent(this, FindActivity::class.java)
        startActivity(intent)
    }


}