package com.example.stego

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException



class Dictophone : AppCompatActivity() {

    private var out: String? = null
    private var recorderDic: MediaRecorder? = null
    private var status: Boolean = false
    private lateinit var nameFile: EditText
    private lateinit var start: ImageButton
    private lateinit var stop: ImageButton
    private lateinit var hide: Button
    private lateinit var strFileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictophone)
        nameFile = findViewById(R.id.nameFile)
        start = findViewById(R.id.startRecord)
        stop = findViewById(R.id.stopRecord)
        hide = findViewById(R.id.hide)

        start.isEnabled = false
        stop.isEnabled = false
        hide.isEnabled = false
    }



    private fun checkPermis() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            val recordPermis = arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this,
                recordPermis, 0)
        }
    }

    fun createFile(view: View) {
        checkPermis()
        nameFile.clearFocus()

        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        strFileName = nameFile.text.toString()
        if (strFileName.length > 0) {
            out =
                Environment.getExternalStorageDirectory().absolutePath + "/" + strFileName + ".mp3"
            recorderDic = MediaRecorder()
            recorderDic?.setOutputFile(out)
            recorderDic?.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorderDic?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorderDic?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            start.isEnabled = true
            start.setBackgroundColor(Color.WHITE)
            stop.isEnabled = true
            stop.setBackgroundColor(Color.WHITE)
        } else {
            Toast.makeText(this,"Введите название файла", Toast.LENGTH_SHORT).show()
        }
    }


    fun startRecording(view: View) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val recordPermis = arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, recordPermis, 0)
        } else {
            try {
                recorderDic?.prepare()
                recorderDic?.start()
                status = true
                Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stopRecording(view: View){
        if (status) {
            recorderDic?.stop()
            recorderDic?.release()
            status = false
            Toast.makeText(this, "Recording stoped!", Toast.LENGTH_SHORT).show()
            hide.isEnabled = true
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    fun hide(view: View) {
        val hideIntent = Intent(this, SecondActivity::class.java)
        hideIntent.putExtra("fileName", "$out")
        hideIntent.putExtra("flagFile", true)
        startActivity(hideIntent)
    }
}