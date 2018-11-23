package coms.dypatil.noticeboard

import android.app.Application
import com.google.firebase.storage.FirebaseStorage
import coms.dypatil.noticeboard.util.validation.FormFieldValidation
import coms.dypatil.noticeboard.viewmodel.LoginVM
import coms.dypatil.noticeboard.viewmodel.contracts.LoginContract
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class LoginVMUnitTest {
    private lateinit var loginVM: LoginVM

    @Mock
    lateinit var loginContract: LoginContract

    @Mock
    lateinit var ctx: Application

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        loginVM = LoginVM(ctx)
        loginVM.loginContract = loginContract
    }

    @Test
    fun validLoginEmail() {
        val email = "abc@gmail.com"
        val password = "aaA1##"
        assertEquals(true, loginVM.isValidCredentials(email, password))
    }

    @Test
    fun invalidLoginEmail() {
        val email = "abc@gmail."
        val password = ""
        assertEquals(false, loginVM.isValidCredentials(email, password))
    }

    @Test
    fun invalidPassword() {
        val password = "aA1#"
        assertEquals(false, FormFieldValidation.isPasswordValid(password))
    }

    @Test
    fun validPassword() {
        val password = "aaA$1aa"
        assertEquals(true, FormFieldValidation.isPasswordValid(password))
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
