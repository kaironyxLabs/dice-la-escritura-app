# kotlinx.serialization: conservar los serializadores generados de los modelos de contenido.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.example.lampara.**$$serializer { *; }
-keepclassmembers class com.example.lampara.** { *** Companion; }
-keepclasseswithmembers class com.example.lampara.** { kotlinx.serialization.KSerializer serializer(...); }
