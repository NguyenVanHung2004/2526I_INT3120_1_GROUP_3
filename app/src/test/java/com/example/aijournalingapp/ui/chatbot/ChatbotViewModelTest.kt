package com.example.aijournalingapp.ui.chatbot

import com.example.aijournalingapp.MainDispatcherRule
import com.example.aijournalingapp.data.FirebaseRepository
import com.example.aijournalingapp.model.ChatMessage
import com.example.aijournalingapp.model.DailyTask
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatbotViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    private val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
    private val mockChatSession = mockk<Chat>(relaxed = true)
    private val mockTasksFlow = MutableStateFlow<List<DailyTask>>(emptyList())

    private lateinit var viewModel: ChatbotViewModel

    @Before
    fun setup() {
        // 1. Mock Static Components
        mockkStatic(FirebaseAuth::class, FirebaseFirestore::class, android.util.Log::class, Calendar::class)
        mockkObject(Firebase)

        every { FirebaseFirestore.getInstance() } returns mockFirestore
        every { FirebaseAuth.getInstance() } returns mockAuth

        // Mock Log để in lỗi ra console nếu có crash (giúp debug)
        every { android.util.Log.e(any(), any(), any()) } answers {
            val tag = firstArg<String>()
            val msg = secondArg<String>()
            val tr = thirdArg<Throwable>()
            println("APP_LOG_ERROR: $tag: $msg")
            tr.printStackTrace() // In stacktrace để debug
            0
        }
        every { android.util.Log.d(any(), any()) } returns 0

        // Mock Calendar cho việc khởi tạo ViewModel (xác định buổi Sáng/Chiều/...)
        val mockCalendar = mockk<Calendar>()
        every { Calendar.getInstance() } returns mockCalendar
        every { mockCalendar.get(Calendar.HOUR_OF_DAY) } returns 16 // 16h = Chiều

        mockkObject(FirebaseRepository)
        coEvery { FirebaseRepository.getDailyTasksFlow() } returns mockTasksFlow
        coEvery { FirebaseRepository.addJournalEntry(any()) } returns Unit

        // 2. Mock Constructor để vượt qua quá trình khởi tạo ViewModel
        // Dùng reflection injection trong test case sẽ ổn định hơn,
        // nhưng ở đây ta dùng mockkConstructor cho tiện setup ban đầu.
        // Ta sẽ dùng phương pháp Injection trong test case C-03 để chắc chắn.
        mockkConstructor(GenerativeModel::class)
        every { anyConstructed<GenerativeModel>().startChat(any()) } returns mockChatSession

        // Mock history ban đầu
        every { mockChatSession.history } returns mutableListOf(mockk(relaxed = true), mockk(relaxed = true))

        // 3. Khởi tạo ViewModel
        viewModel = ChatbotViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // Hàm tiện ích để inject Mock object vào field private (Reflection)
    // Giúp thay thế đối tượng GenerativeModel thật bằng Mock kiểm soát được
    private fun injectMockGenerativeModel(vm: ChatbotViewModel, mockModel: GenerativeModel) {
        val field = vm.javaClass.getDeclaredField("generativeModel")
        field.isAccessible = true
        field.set(vm, mockModel)
    }

    @Test
    fun `C-01_initializeSession_sets_correct_greeting_and_suggests_afternoon_tasks`() = runTest {
        val afternoonTask = DailyTask("t5", "Đi bộ / Chạy bộ", "Chiều", 15, false)
        mockTasksFlow.value = listOf(afternoonTask)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Chào buổi chiều! Cố gắng thêm chút nữa nhé 💪", viewModel.currentGreeting.value)
        assertEquals(2, viewModel.messages.size)
    }

    @Test
    fun `C-02_sendMessage_updates_messages_and_calls_gemini`() = runTest {
        viewModel.messages.clear()

        val userText = "Test User"
        val aiReply = "Test AI"
        val mockResponse = mockk<GenerateContentResponse>()
        every { mockResponse.text } returns aiReply
        coEvery { mockChatSession.sendMessage(any<String>()) } returns mockResponse

        viewModel.sendMessage(userText)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.messages.size)
        coVerify { mockChatSession.sendMessage(userText) }
    }

    @Test
    fun `C-03_endSessionAndSaveJournal_success_saves_journal`() = runTest {
        // [QUAN TRỌNG]: Unmock Calendar để SimpleDateFormat bên trong JournalEntry hoạt động bình thường
        // Nếu không, việc tạo JournalEntry sẽ gây crash do xung đột với MockK Static
        unmockkStatic(Calendar::class)

        var onCompleteCalled = false

        // Tạo một Mock GenerativeModel MỚI hoàn toàn để kiểm soát 100%
        val localMockModel = mockk<GenerativeModel>()
        val journalContent = "Nội dung test"
        val adviceContent = "Lời khuyên test"
        val mockResponseText = "$journalContent ||| $adviceContent"
        val mockResponse = mockk<GenerateContentResponse>()
        every { mockResponse.text } returns mockResponseText

        // Setup hành vi cho Mock mới này
        coEvery { localMockModel.generateContent(any<String>()) } returns mockResponse

        // INJECT Mock mới vào ViewModel (thay thế cái cũ)
        injectMockGenerativeModel(viewModel, localMockModel)

        // Setup messages
        viewModel.messages.clear()
        viewModel.messages.add(ChatMessage("User Msg","user", true))

        // Act
        viewModel.endSessionAndSaveJournal { onCompleteCalled = true }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(onCompleteCalled, "Callback onComplete phải được gọi")
        assertEquals(false, viewModel.isSummarizing.value)

        coVerify(exactly = 1) {
            FirebaseRepository.addJournalEntry(match {
                it.content == journalContent &&
                        it.fakeAiAdvice == adviceContent
            })
        }
    }
}