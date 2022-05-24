package com.example.stego

import android.net.Uri
import android.content.ContentUris
import android.os.Build
import android.os.Environment
import android.content.Context
import android.database.Cursor
import android.provider.DocumentsContract
import android.provider.MediaStore

class UriToPath {
    fun getActualPath(con: Context, u: Uri): String? {
        val sdkINT = Build.VERSION.SDK_INT
        val sdkKIT = Build.VERSION_CODES.KITKAT

        if ((sdkINT >= sdkKIT) && DocumentsContract.isDocumentUri(con, u)) {
            if ("com.android.externalstorage.documents" == u.authority) {
                val doc = DocumentsContract.getDocumentId(u).split(":".toRegex()).toTypedArray()
                val t = doc[0]
                if ("primary".equals(t, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + doc[1]
                }
            } else if ("com.android.providers.downloads.documents" == u.authority) {
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(DocumentsContract.getDocumentId(u)))
                return getData(con, contentUri, null, null)
            } else if ("com.android.providers.media.documents" == u.authority) {
                val doc = DocumentsContract.getDocumentId(u).split(":".toRegex()).toTypedArray()
                val t = doc[0]
                var contentUri: Uri? = null
                when (t) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                return getData(con, contentUri, "_id=?", arrayOf(doc[1]))
            }
        } else if ("content".equals(u.scheme, ignoreCase = true)) {
            return getData(con, u, null, null)
        } else if ("file".equals(u.scheme, ignoreCase = true)) {
            return u.path
        }
        return null
    }

    private fun getData(con: Context, u: Uri?, select: String?, selectArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        try {
            cursor = con.contentResolver.query(u!!, arrayOf("_data"), select, selectArgs,null)
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("_data"))
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}