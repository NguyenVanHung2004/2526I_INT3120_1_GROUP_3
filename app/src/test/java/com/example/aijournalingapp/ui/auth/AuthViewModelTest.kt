package com.example.aijournalingapp.ui.auth

import com.example.aijournalingapp.MainDispatcherRule
import com.example.aijournalingapp.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        // Mocking static Firebase.auth
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockAuth

        // FIX 1: Mock auth.currentUser để trả về NULL trước khi ViewModel được khởi tạo.
        // Điều này ngăn ViewModel gọi .uid, .email, ... trên một đối tượng chưa được cấu hình đầy đủ.
        every { mockAuth.currentUser } returns null

        // Cấu hình Task thành công mặc định
        val mockAuthResult = mockk<AuthResult>()
        val mockTask = mockk<Task<AuthResult>>()

        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockAuthResult
        every { mockTask.exception } returns null
        every { mockTask.isComplete } returns true

        // FIX 2: BẮT BUỘC MOCK isCanceled() cho await() trong Coroutine
        every { mockTask.isCanceled } returns false

        val taskSlot = slot<OnCompleteListener<AuthResult>>()
        every { mockTask.addOnCompleteListener(capture(taskSlot)) } answers {
            taskSlot.captured.onComplete(mockTask)
            mockTask
        }

        // Mock Firebase Auth methods
        every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns mockTask
        every { mockAuth.signInWithEmailAndPassword(any(), any()) } returns mockTask
        every { mockAuth.signInWithCredential(any()) } returns mockTask

        // Khởi tạo ViewModel sau khi các mock cơ bản đã được thiết lập
        viewModel = AuthViewModel()
    }

    @Test
    fun `A-01_signup_success_updates_loading_state`() = runTest {
        viewModel.signup("test@mail.com", "pass123", "Test User")

        // Sau khi thành công, error phải là null (đã được reset trong hàm login/signup)
        assertEquals(false, viewModel.loading.value)
        assertEquals(null, viewModel.error.value)
        verify(exactly = 1) { mockAuth.createUserWithEmailAndPassword("test@mail.com", "pass123") }
    }

    @Test
    fun `A-02_login_success_updates_loading_state`() = runTest {
        // Test này thất bại vì error = "Sai Email hoặc Mật khẩu."
        // Vấn đề là error không được reset về null khi thành công.
        // FIX: Đảm bảo mock Task đã xử lý hết các trường hợp exception/cancellation.
        viewModel.login("test@mail.com", "pass123")

        assertEquals(false, viewModel.loading.value)
        assertEquals(null, viewModel.error.value) // Bây giờ sẽ PASS
        verify(exactly = 1) { mockAuth.signInWithEmailAndPassword("test@mail.com", "pass123") }
    }

    @Test
    fun `A-03_login_failure_sets_error_message`() = runTest {
        // Mock Task to return failure
        val mockTask = mockk<Task<AuthResult>>()
        val mockException = Exception("INVALID_CREDENTIAL")

        every { mockTask.isSuccessful } returns false
        every { mockTask.exception } returns mockException
        every { mockTask.isComplete } returns true
        every { mockTask.isCanceled } returns false // FIX 2: Thêm mock này

        val taskSlot = slot<OnCompleteListener<AuthResult>>()
        every { mockTask.addOnCompleteListener(capture(taskSlot)) } answers {
            taskSlot.captured.onComplete(mockTask)
            mockTask
        }

        // Cấu hình mockAuth chỉ trả về Task thất bại cho test case này
        every { mockAuth.signInWithEmailAndPassword("wrong@mail.com", "wrongpass") } returns mockTask

        viewModel.login("wrong@mail.com", "wrongpass")

        assertFalse(viewModel.loading.value)
        assertEquals("Sai Email hoặc Mật khẩu.", viewModel.error.value)
    }

    @Test
    fun `A-05_logout_calls_firebase_signOut`() {
        viewModel.logout()

        verify(exactly = 1) { mockAuth.signOut() }
    }

    @Test
    fun `addAuthStateListener_updates_user_on_login`() {
        // Không cần setup initial user state ở đây vì nó đã được đặt là null trong @Before

        // Slot to capture the listener được cài đặt trong init{} của VM
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        verify { mockAuth.addAuthStateListener(capture(listenerSlot)) }

        // Mock a successful login event
        val mockLoggedInUser = mockk<FirebaseUser>()
        every { mockLoggedInUser.uid } returns "user123"
        every { mockLoggedInUser.email } returns "user@test.com"
        every { mockLoggedInUser.displayName } returns "Test User"

        // Simulate AuthStateListener firing with a new user
        listenerSlot.captured.onAuthStateChanged(mockk {
            every { currentUser } returns mockLoggedInUser
        })

        // Verify the user state is updated
        val expectedUser = User(id = "user123", email = "user@test.com", name = "Test User")
        assertEquals(expectedUser, viewModel.user.value)
    }
}