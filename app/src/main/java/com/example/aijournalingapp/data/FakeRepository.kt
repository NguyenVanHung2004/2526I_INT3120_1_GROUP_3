package com.example.aijournalingapp.data

import android.content.Context
import com.example.aijournalingapp.model.JournalEntry
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object FakeRepository {
    private var _journals = mutableListOf<JournalEntry>()
    private const val FILE_NAME = "my_journals.dat"

    // Hàm tải dữ liệu từ file lên (gọi khi mở app)
    fun loadData(context: Context) {
        try {
            val fileInput = context.openFileInput(FILE_NAME)
            val objectInput = ObjectInputStream(fileInput)
            @Suppress("UNCHECKED_CAST")
            _journals = objectInput.readObject() as MutableList<JournalEntry>
            objectInput.close()
            fileInput.close()
        } catch (e: Exception) {
            // Nếu chưa có file (lần đầu mở), thêm 1 cái mẫu
            if (_journals.isEmpty()) {
                _journals.add(JournalEntry(content = "Chào ngày mới!", mood = "😄 Vui", fakeAiAdvice = "App đã sẵn sàng lưu ký ức của bạn!"))
            }
        }
    }

    // Hàm lưu danh sách xuống file (gọi khi có thay đổi)
    private fun saveData(context: Context) {
        try {
            val fileOutput = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
            val objectOutput = ObjectOutputStream(fileOutput)
            objectOutput.writeObject(_journals)
            objectOutput.close()
            fileOutput.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAll() = _journals.toList()

    // Thêm mới và Lưu ngay
    fun add(entry: JournalEntry, context: Context) {
        _journals.add(0, entry)
        saveData(context)
    }

    fun getById(id: String) = _journals.find { it.id == id }
}