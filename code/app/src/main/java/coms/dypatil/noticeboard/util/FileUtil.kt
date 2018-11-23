package coms.dypatil.noticeboard.util

import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import coms.dypatil.noticeboard.App
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

object FileUtil {

    fun copyFile(sourceFile: File, destFile: File) {
        if (!sourceFile.exists()) {
            return
        }

        val source: FileChannel? = FileInputStream(sourceFile).channel
        val destination: FileChannel? = FileOutputStream(destFile).channel
        if (destination!=null && source!=null) {
            destination.transferFrom(source, 0, source.size())
        }
        source?.close()
        destination?.close()

    }

    fun getProfilePicFile(userType: String, profilePicName: String): File {
        return File(Environment.getExternalStoragePublicDirectory("NoticeBoard/$userType"), profilePicName)
    }


    fun getFilePathFromMediaUri(contentUri: Uri): String {
        val projection: Array<String> = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = App.getAppContext.contentResolver.query(contentUri, projection, null, null, null)
        val columnIndex: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path: String = cursor.getString(columnIndex)
        cursor.close()
        return path
    }


}