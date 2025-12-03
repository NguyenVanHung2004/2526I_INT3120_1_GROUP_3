// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}
// [FIX STABLE] Định nghĩa hàm dưới dạng Function Type Property
// Hàm này đọc key từ file local.properties
val getLocalProperty: (String, String) -> String = { key, defaultValue ->
    val properties = java.util.Properties()
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { input ->
            properties.load(input)
        }
    }

    val rawValue = properties.getProperty(key) ?: defaultValue
    // Gradle cần giá trị được bọc trong dấu ngoặc kép khi đưa vào buildConfigField
    if (rawValue.isNotBlank() && rawValue.startsWith('"') && rawValue.endsWith('"')) {
        rawValue
    } else {
        "\"$rawValue\""
    }
}

// Công khai hàm này vào ext (Extension Properties) để module app có thể truy cập
rootProject.extensions.extraProperties.set("getLocalProperty", getLocalProperty)