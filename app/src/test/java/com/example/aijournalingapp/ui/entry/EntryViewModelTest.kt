package com.example.aijournalingapp.ui.entry

import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.aijournalingapp.MainDispatcherRule
import com.example.aijournalingapp.MyNotificationListenerService
import com.example.aijournalingapp.data.FirebaseRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mock Context & System Service
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockUsageStatsManager = mockk<UsageStatsManager>(relaxed = true)

    // Mock Firebase Dependencies
    private val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    private val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)

    // Mock GenerativeModel
    private val mockGenerativeModel = mockk<GenerativeModel>(relaxed = true)

    private lateinit var viewModel: EntryViewModel

    @Before
    fun setup() {
        // --- MOCK FIREBASE STATIC ---
        mockkStatic(FirebaseAuth::class, FirebaseFirestore::class, android.util.Log::class)
        mockkObject(Firebase)

        every { FirebaseFirestore.getInstance() } returns mockFirestore
        every { FirebaseAuth.getInstance() } returns mockAuth
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        // --- [FIX QUAN TRỌNG] MOCK ĐẦY ĐỦ CÁC HÀM LOG ---
        // Mock phiên bản 2 tham số: Log.e(tag, msg) -> Cái code bạn đang dùng
        every { android.util.Log.e(any(), any()) } returns 0
        // Mock phiên bản 3 tham số: Log.e(tag, msg, throwable) -> Phòng hờ
        every { android.util.Log.e(any(), any(), any()) } returns 0
        every { android.util.Log.d(any(), any()) } returns 0
        // -----------------------------------------------------------------

        // 1. Mock Repository & Services
        mockkObject(FirebaseRepository)
        coEvery { FirebaseRepository.addJournalEntry(any()) } returns Unit

        mockkObject(MyNotificationListenerService)
        every { MyNotificationListenerService.getNotificationHistory(any()) } returns "Mock Notification Summary"

        // Mock System Service call cho Context
        every { mockContext.getSystemService(Context.USAGE_STATS_SERVICE) } returns mockUsageStatsManager

        // 2. Khởi tạo ViewModel
        viewModel = EntryViewModel()

        // 3. Inject Mock GenerativeModel (Reflection)
        injectMockGenerativeModel(viewModel, mockGenerativeModel)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // Hàm tiện ích để inject Mock object vào field private
    private fun injectMockGenerativeModel(vm: EntryViewModel, mockModel: GenerativeModel) {
        val field = vm.javaClass.getDeclaredField("generativeModel")
        field.isAccessible = true
        field.set(vm, mockModel)
    }

    @Test
    fun `E-01_analyzeJournal_positive_sentiment_parses_correctly`() = runTest {
        // Setup input
        viewModel.content = "Hôm nay mình hoàn thành tốt mọi việc, cảm thấy rất vui."

        // Mock AI response
        val mockResponseText = "Vui vẻ|😀|Tiếp tục phát huy nhé!"
        val mockResponse = mockk<GenerateContentResponse>()
        every { mockResponse.text } returns mockResponseText

        // Setup Mock behavior
        coEvery { mockGenerativeModel.generateContent(any<String>()) } returns mockResponse

        // Act
        viewModel.analyzeJournal()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals("Vui vẻ", viewModel.selectedMood)
        assertEquals("😀", viewModel.selectedEmoji)
        assertEquals("Tiếp tục phát huy nhé!", viewModel.generatedAdvice)
        assertFalse(viewModel.isAnalyzing)
    }

    @Test
    fun `E-02_analyzeJournal_negative_sentiment_parses_correctly`() = runTest {
        viewModel.content = "Mệt mỏi quá."

        val mockResponseText = "Căng thẳng|😩|Hãy nghỉ ngơi đi."
        val mockResponse = mockk<GenerateContentResponse>()
        every { mockResponse.text } returns mockResponseText
        coEvery { mockGenerativeModel.generateContent(any<String>()) } returns mockResponse

        viewModel.analyzeJournal()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Căng thẳng", viewModel.selectedMood)
        assertEquals("😩", viewModel.selectedEmoji)
        assertEquals("Hãy nghỉ ngơi đi.", viewModel.generatedAdvice)
    }

    @Test
    fun `E-03_generateSmartDiary_calls_analyzeJournal_and_logs_work_app_usage`() = runTest {
        // Setup mock response cho Smart Diary generation
        val mockDiaryContent = "Hôm nay là một ngày bận rộn với nhiều thông báo."
        val mockResponse1 = mockk<GenerateContentResponse>()
        every { mockResponse1.text } returns mockDiaryContent

        // Setup mock response cho Analyze Journal (được gọi ngay sau khi generate xong)
        val mockAnalyzeText = "Bận rộn|💼|Cố lên nhé"
        val mockResponse2 = mockk<GenerateContentResponse>()
        every { mockResponse2.text } returns mockAnalyzeText

        // Mock chuỗi gọi: Lần 1 (Tạo nhật ký) -> Lần 2 (Phân tích)
        coEvery { mockGenerativeModel.generateContent(any<String>()) } returnsMany listOf(mockResponse1, mockResponse2)

        // Act
        viewModel.generateSmartDiary(mockContext)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        // 1. Content phải được update từ AI
        assertEquals(mockDiaryContent, viewModel.content)

        // 2. Mood/Emoji phải được update từ lần gọi analyzeJournal sau đó
        assertEquals("Bận rộn", viewModel.selectedMood)
        assertEquals("💼", viewModel.selectedEmoji)
    }

    @Test
    fun `saveEntry_calls_FirebaseRepository_addJournalEntry`() = runTest {
        // Setup
        viewModel.content = "Test entry content"
        viewModel.selectedMood = "Vui vẻ"
        viewModel.selectedEmoji = "😊"
        viewModel.generatedAdvice = "Advice"

        var onSuccessCalled = false

        // Act
        viewModel.saveEntry(mockContext) { onSuccessCalled = true }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(onSuccessCalled)
        coVerify(exactly = 1) {
            FirebaseRepository.addJournalEntry(match {
                it.content == "Test entry content" &&
                        it.mood == "😊 Vui vẻ"
            })
        }
    }
}