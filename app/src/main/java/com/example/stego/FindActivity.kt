package com.example.stego

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.lang.Exception

class FindActivity : AppCompatActivity() {

    private lateinit var fileName: TextView
    private lateinit var chooseFile: Button
    private lateinit var buttonFind: Button
    private lateinit var filePath: String
    private lateinit var textMes: TextView
    private val PICKFILE_RESULT_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find)
        fileName = findViewById(R.id.fileName)
        chooseFile = findViewById(R.id.chooseFile)
        buttonFind = findViewById(R.id.toFind)
        textMes = findViewById(R.id.message)
        textMes.visibility = View.INVISIBLE
        buttonFind.isEnabled = false
    }

    fun chooseFile (view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/mpeg"
        startActivityForResult(Intent.createChooser(intent, "Music File"), PICKFILE_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK && data != null && data.data != null) {
                val uriPath = UriToPath()
                filePath = uriPath.getPath(this, data.data!!).toString()
                fileName.text = filePath
                chooseFile.isEnabled = false
                buttonFind.isEnabled = true
            } else {
                Toast.makeText(this, "Файл не выбран!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun notMessage() {
        Toast.makeText(this, "Здесь нет сообщения!", Toast.LENGTH_SHORT).show()
        chooseFile.isEnabled = true
        fileName.text = "ФАЙЛ НЕ ВЫБРАН"
        buttonFind.isEnabled = false
    }

    fun toFind(view: View) {
        val data = File(filePath).readBytes()
        val buff = ByteArray(data.size)

        data.inputStream().buffered().use { input ->
            while (true) {
                val sz = input.read(buff)
                if (sz <= 0) break
            }
        }

        var number_frame = 0

        for (index in 0..buff.size - 2) {
            if (buff[index] == (-1).toByte() && buff[index + 1] == (-5).toByte()) {
                number_frame++
            }
        }

        println("ALL: $number_frame")

        if (number_frame > 10) {

            var len_mes = 0
            var flag_first = true
            var frame_left_secret_data = 0
            var frame_right_secret_data = 0
            var secret_data = 0
            var one_data = '0'
            var mes = ""
            var flag_ = false
            var k = 0
            var counter = 0

            for (index in 0..buff.size - 2) {
                if (buff[index] == (-1).toByte() && buff[index + 1] == (-5).toByte()) {
                    if (!flag_) {
                        if (flag_first) {
                            flag_first = false
                            frame_left_secret_data = buff[index + 3].toInt()
                            frame_left_secret_data = frame_left_secret_data shl 4
                            frame_left_secret_data = frame_left_secret_data and 0b11110000
                        } else {
                            flag_first = true
                            frame_right_secret_data = buff[index + 3].toInt()
                            frame_right_secret_data = frame_right_secret_data and 0b00001111
                            secret_data = frame_left_secret_data or frame_right_secret_data

                            try {
                                one_data = secret_data.toChar()
                            } catch (e: Exception) {
                                notMessage()
                                return
                            }

                            if (one_data == '_' && !flag_) {
                                len_mes = mes.toInt()
                                mes = ""
                                flag_ = true
                                k = number_frame / ((len_mes + len_mes.toString().length) * 2) - 1
                            }
                            mes += one_data
                        }
                    } else {
                        if (counter % k == 0) {
                            if (flag_first) {
                                flag_first = false
                                frame_left_secret_data = buff[index + 3].toInt()
                                frame_left_secret_data = frame_left_secret_data shl 4
                                frame_left_secret_data = frame_left_secret_data and 0b11110000
                                println(counter)
                            } else {
                                flag_first = true
                                frame_right_secret_data = buff[index + 3].toInt()
                                frame_right_secret_data = frame_right_secret_data and 0b00001111
                                secret_data = frame_left_secret_data or frame_right_secret_data
                                one_data = secret_data.toChar()
                                mes += one_data
                                if (mes.length == len_mes + 1) {
                                    mes = mes.drop(1)
                                    break
                                }
                                println(counter)
                            }
                        }
                        counter++
                    }
                }
            }


            if (flag_ == true) {
                textMes.visibility = View.VISIBLE
                textMes.text = mes
            } else {
                notMessage()
            }

        } else {
            notMessage()
        }
    }


}