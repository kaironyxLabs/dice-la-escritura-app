package com.kaironyx.diceescritura.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kaironyx.diceescritura.MainActivity
import com.kaironyx.diceescritura.R

object Notificaciones {
    const val CANAL_RECORDATORIO = "recordatorio_diario"
    private const val ID_RECORDATORIO = 1001

    /** Crea el canal propio del recordatorio (idempotente). Se llama al iniciar la app. */
    fun crearCanales(context: Context) {
        val canal = NotificationChannel(
            CANAL_RECORDATORIO,
            context.getString(R.string.canal_recordatorio_nombre),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.canal_recordatorio_descripcion) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun mostrarRecordatorio(context: Context) {
        if (!tienePermiso(context)) return
        val abrir = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notificacion = NotificationCompat.Builder(context, CANAL_RECORDATORIO)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(context.getString(R.string.recordatorio_titulo))
            .setContentText(context.getString(R.string.recordatorio_texto))
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        @Suppress("MissingPermission") // comprobado arriba con tienePermiso()
        NotificationManagerCompat.from(context).notify(ID_RECORDATORIO, notificacion)
    }
}
