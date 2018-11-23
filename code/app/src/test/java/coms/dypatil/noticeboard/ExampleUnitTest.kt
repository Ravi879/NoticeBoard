package coms.dypatil.noticeboard

import com.google.firebase.storage.FirebaseStorage
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleUnitTest {


    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun getFileNameFromProfilePicUrlTest() {
        val url =
            "https://firebasestorage.googleapis.com/v0/b/noticeboard-app-28d79.appspot.com/o/faculty%2Fmech%2FAdmingAdming?alt=media&token=4eb4fdfc-3038-4366-842b-29b710c5e5a5"
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        val fileName = storageReference.name

        print(fileName)

        assertEquals("AdmingAdming", fileName)

    }


}
