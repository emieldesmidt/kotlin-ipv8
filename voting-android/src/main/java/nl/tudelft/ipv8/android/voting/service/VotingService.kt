package nl.tudelft.ipv8.android.voting.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import nl.tudelft.ipv8.android.voting.R
import nl.tudelft.ipv8.android.voting.ui.peers.MainActivity
import nl.tudelft.ipv8.android.service.IPv8Service

class VotingService : IPv8Service() {
    override fun createNotification(): NotificationCompat.Builder {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0,
            notificationIntent, 0)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_CONNECTION)
            .setContentTitle("TrustChain Explorer")
            .setContentText("Running")
            .setSmallIcon(R.drawable.ic_insert_link_black_24dp)
            .setContentIntent(pendingIntent)
    }
}
