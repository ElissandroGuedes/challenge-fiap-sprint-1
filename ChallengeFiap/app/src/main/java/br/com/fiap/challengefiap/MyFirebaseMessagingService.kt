package br.com.fiap.challengefiap

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data["payload"]?.let { json ->
            handleIncomingMessage(json)
        }
    }


    private fun handleIncomingMessage(json: String) {
        try {
            val data = JSONObject(json)
            val title = data.optString("title", "Nova campanha")
            val body = data.optString("body", "Confira os detalhes")
            val url = data.optString("url", "")
            val buttonLabel = data.optString("buttonLabel","Saiba mais")
            val actions = data.optString("actions", "[]")
            val actionUrls = data.optString("actionUrls", "{}")

            val intent = Intent(this, CampaignDetailActivity::class.java).apply {
                putExtra("title", title)
                putExtra("body", body)
                putExtra("url", url)
                putExtra("buttonLabel", buttonLabel)
                putExtra("actions", data.optString("actions"))
                putExtra("actionUrls", data.optString("actionUrls"))
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(0, buttonLabel,pendingIntent)


            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, builder.build())

            Log.d("PushReceiver", "Notificação recebida: $title - $body")

        } catch (e: Exception) {
            Log.e("PushReceiver", "Erro ao processar mensagem: ${e.message}")
        }
    }
}