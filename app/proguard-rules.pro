# kotlinx.serialization: conservar los serializadores generados de los modelos de contenido.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.kaironyx.diceescritura.**$$serializer { *; }
-keepclassmembers class com.kaironyx.diceescritura.** { *** Companion; }
-keepclasseswithmembers class com.kaironyx.diceescritura.** { kotlinx.serialization.KSerializer serializer(...); }
