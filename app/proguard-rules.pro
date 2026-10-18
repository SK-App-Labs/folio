# File: app/proguard-rules.pro
# --- Folio Production Keep Rules (Room SQLite & Kotlinx Serialization) ---
-keep class com.folioledger.app.data.local.entity.** { *; }
-keep class com.folioledger.app.domain.model.** { *; }

# Keep Kotlinx Serialization generated serializers for ZIP backups
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.folioledger.app.**$$serializer { *; }
-keepclassmembers class com.folioledger.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.folioledger.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}