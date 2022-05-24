package com.example.stego

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File


class SecondActivity : AppCompatActivity() {

    private lateinit var fileName: TextView
    private lateinit var chooseFile: Button
    private lateinit var buttonOK: Button
    private lateinit var buttonHide: Button
    private lateinit var editTextMes : EditText
    private lateinit var filePath: String
    private lateinit var textMes: String
    private val PICKFILE_RESULT_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        fileName = findViewById(R.id.fileName)
        chooseFile = findViewById(R.id.chooseFile)
        buttonOK = findViewById(R.id.readyMessage)
        buttonHide = findViewById(R.id.toHide)
        editTextMes = findViewById(R.id.message)
        chooseFile.isEnabled = false
        buttonHide.isEnabled = false
        buttonOK.isEnabled = false

        if (intent.getBooleanExtra("flagFile", false)) {
            filePath = intent.getStringExtra("fileName").toString()
            fileName.text = intent.getStringExtra("fileName").toString()
            buttonOK.isEnabled = true

        } else {
            chooseFile.isEnabled = true
            editTextMes.isEnabled = false
        }
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
                filePath = uriPath.getActualPath(this, data.data!!).toString()
                fileName.text = filePath
                buttonOK.isEnabled = true
                editTextMes.isEnabled = true
                chooseFile.isEnabled = false
            } else {
                Toast.makeText(this, "Файл не выбран!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun encryptLeft(txt_symbol_code: Int, b: Byte): Int {
        var txt_symbol_left = txt_symbol_code and 0b11110000
        txt_symbol_left = txt_symbol_left shr 4
        var ex = b.toInt() and 0b11110000
        ex = ex or txt_symbol_left
        return ex
    }

    fun encryptRight(txt_symbol_code: Int, b: Byte): Int {
        val txt_symbol_right = txt_symbol_code and 0b00001111
        var ex = b.toInt() and 0b11110000
        ex = ex or txt_symbol_right
        return ex
    }

    fun messageOK (view: View) {
        if (editTextMes.text.toString().isNotEmpty()) {
            textMes = editTextMes.text.toString()
            buttonOK.isEnabled = false
            buttonHide.isEnabled = true
            editTextMes.clearFocus()
        } else {
            Toast.makeText(this, "Введите сообщение!", Toast.LENGTH_SHORT).show()
        }
    }

    fun toHide (view: View) {
        val message = textMes
        val len_message = message.length
        val len_and_message = len_message.toString() + "_" + message
        val data = File(filePath).readBytes()
        val buff = ByteArray(data.size)

        data.inputStream().buffered().use {
                input ->
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
        println("ALL: " + number_frame)
        val k = number_frame / ((len_message + len_message.toString().length) * 2) - 1

        if (number_frame > 10 && k / 2 >= len_and_message.length) {

            println("K = " + k)

            val out = File(filePath.dropLast(4) + " 1.mp3")

            var pos = 0
            var flag_first = true
            var txt_symbol = 'a'
            var txt_symbol_code = 0
            var flag_data = false
            var counter = 0
            val list = (('a'..'z') + ('A'..'Z') + ('0'..'9'))

            for (index in 0..buff.size - 2) {
                if (buff[index] == (-1).toByte() && buff[index + 1] == (-5).toByte()) {
                    if (!flag_data) {
                        if (flag_first) {
                            txt_symbol = len_and_message[pos]
                            flag_first = false
                            txt_symbol_code = txt_symbol.code
                            buff[index + 3] = encryptLeft(txt_symbol_code, buff[index + 3]).toByte()
                        } else {
                            flag_first = true
                            buff[index + 3] =
                                encryptRight(txt_symbol_code, buff[index + 3]).toByte()
                            pos += 1
                            if (txt_symbol == '_') {
                                flag_data = true
                            }
                        }
                    } else {
                        if (counter % k == 0) {
                            if (flag_first) {
                                txt_symbol = len_and_message[pos]
                                flag_first = false
                                txt_symbol_code = txt_symbol.code
                                buff[index + 3] =
                                    encryptLeft(txt_symbol_code, buff[index + 3]).toByte()
                                println(counter)
                            } else {
                                flag_first = true
                                buff[index + 3] =
                                    encryptRight(txt_symbol_code, buff[index + 3]).toByte()
                                pos += 1
                                println(counter)
                                if (pos == len_and_message.length) {
                                    break
                                }
                            }
                        } else {
                            buff[index + 3] =
                                encryptLeft(list.random().code, buff[index + 3]).toByte()
                        }
                        counter++
                    }

                }
            }
            out.writeBytes(buff)
            Toast.makeText(this, "Успешно спрятано!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Выберите другой файл!", Toast.LENGTH_SHORT).show()
            chooseFile.isEnabled = true
            fileName.text = "ФАЙЛ НЕ ВЫБРАН"
            buttonHide.isEnabled = false
        }

    }
}