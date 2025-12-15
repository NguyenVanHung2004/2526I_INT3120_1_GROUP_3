package com.example.aijournalingapp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import java.io.File
import java.io.FileOutputStream

object ImageShareUtils {

    /**
     * Hàm này gắn ComposeView vào màn hình thật (nhưng ẩn đi) để nó có Lifecycle,
     * sau đó chụp ảnh và tự gỡ bỏ chính nó.
     */
    fun shareComposableAsImage(
        context: Context,
        content: @Composable () -> Unit
    ) {
        val activity = context as? Activity ?: return // Yêu cầu phải là Activity
        val rootLayout = activity.findViewById<ViewGroup>(android.R.id.content) // Lấy View gốc của Activity

        // 1. Tạo View
        val composeView = ComposeView(context).apply {
            visibility = View.INVISIBLE // Ẩn đi để người dùng không thấy nháy
            setContent { content() }
        }

        // 2. Thêm View vào Activity (Để nó nhận Lifecycle tự động -> Fix lỗi crash)
        rootLayout.addView(composeView)

        // 3. Đợi View được vẽ xong thì chụp
        composeView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Đảm bảo chỉ chạy 1 lần
                composeView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Đo đạc kích thước thực tế sau khi layout
                val width = composeView.width
                val height = composeView.height

                if (width > 0 && height > 0) {
                    // 4. Tạo Bitmap
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)

                    // Vẽ background trắng nếu cần (vì mặc định có thể trong suốt)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    composeView.draw(canvas)

                    // 5. Share
                    shareBitmapToSocial(context, bitmap)
                }

                // 6. Dọn dẹp: Gỡ View khỏi màn hình
                rootLayout.removeView(composeView)
            }
        })
    }

    private fun shareBitmapToSocial(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()

            val newFile = File(cachePath, "share_achievement.png")
            val stream = FileOutputStream(newFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                newFile
            )

            if (contentUri != null) {
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                    putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Chia sẻ thành tựu"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Lỗi chia sẻ: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}