package coms.dypatil.noticeboard.domain.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import coms.dypatil.noticeboard.data.datastore.sharedpreference.model.Preference
import coms.dypatil.noticeboard.data.repository.NoticeRepository
import coms.dypatil.noticeboard.ui.activity.NoticeDetailActivity
import coms.dypatil.noticeboard.util.rx.SchedulersFacade

class FCMServiceKT : FirebaseMessagingService() {

    private lateinit var notificationManager: NotificationManagerCompat

    private var notificationId = 1

    private var preference: Preference? = null

    override fun onCreate() {
        super.onCreate()
        preference = App.preference
        notificationId = preference!!.spNotification
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        if (remoteMessage?.data==null) {
            return
        }

        notificationManager = NotificationManagerCompat.from(this)

        val payload: MutableMap<String, String> = remoteMessage.data
        val notice: Notice = getNotice(payload)

        val groupName = payload["groupName"]
        val groupId = payload["groupId"]?.toInt()

        insertNoticeDetails(groupId!!, groupName!!, notice)

    }

    private fun insertNoticeDetails(groupId: Int, groupName: String, notice: Notice) =
            NoticeRepository.insertNoticeData(notice)
                    .subscribeOn(SchedulersFacade.io())
                    .observeOn(SchedulersFacade.ui())
                    .subscribe({ rowInserted: Long? ->
                        rowInserted?.let { row ->
                            if (notice.issuerId!=App.preference.spUserId)
                                createNotification(groupId, groupName, notice, notice.title!!)
                        }
                    }, { th ->
                        th.printStackTrace()
                    })

    private fun getNotice(payload: MutableMap<String, String>): Notice =
            Notice("").apply {
                fbNoticeId = payload["fbNoticeId"]!!
                title = payload["title"]!!
                description = payload["description"]!!
                issueDate = payload["issueDate"]?.toLong()
                lastDate = payload["lastDate"]?.toLong()
                issuerId = payload["issuerId"]!!
                issuerName = payload["issuerName"]!!
                department = payload["department"]!!
                designation = payload["designation"]!!
            }


    private fun createNotification(groupId: Int, groupName: String, notice: Notice, title1: String) {

        notificationId += 1
        preference!!.spNotification = notificationId

        val channelId = "channel_$groupName"
        createNotificationChannels(channelId, groupName)

        val notificationIntent = Intent(this, NoticeDetailActivity::class.java)
        notificationIntent.putExtra(NoticeDetailActivity.NOTICE_OBJECT, notice)
        notificationIntent.putExtra("notification_id", notificationId)
        notificationIntent.putExtra(NoticeDetailActivity.IS_FROM_NOTIFICATION_INTENT, true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val notificationPendingIntent = PendingIntent.getActivity(this, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification1 = NotificationCompat.Builder(this, channelId)
                .setContentTitle(title1)
                .setContentText(notice.description)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setGroup(groupName)
                .setSubText(notice.issuerName)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notice.description))
                .setContentIntent(notificationPendingIntent)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification1.setSmallIcon(R.mipmap.ic_launcher)
            notification1.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
        } else
            notification1.setSmallIcon(R.drawable.ic_notification)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val summaryIntent = Intent(this, NoticeDetailActivity::class.java)
            summaryIntent.putExtra(NoticeDetailActivity.NOTICE_OBJECT, notice)
            summaryIntent.putExtra("notification_id", groupId)
            notificationIntent.putExtra(NoticeDetailActivity.IS_FROM_NOTIFICATION_INTENT, true)
            summaryIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            val summaryPendingIntent = PendingIntent.getActivity(this, groupId, summaryIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val summaryNotificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setStyle(NotificationCompat.InboxStyle()
                            .setBigContentTitle(title1)
                            .addLine(notice.description)
                            .setSummaryText(groupName))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setGroup(groupName)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                    .setGroupSummary(true)
                    .setSubText(groupName)
                    .setAutoCancel(true)
                    .setContentTitle(title1)
                    .setContentText(notice.description)
                    .setContentIntent(summaryPendingIntent)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                summaryNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                notification1.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            } else
                summaryNotificationBuilder.setSmallIcon(R.drawable.ic_notification)

            notificationManager.notify(groupId, summaryNotificationBuilder.build())
        }

        notificationManager.notify(notificationId, notification1.build())

    }

    private fun createNotificationChannels(channelId: String, groupName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    groupName,
                    NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.description = "$groupName Department"
            channel.setShowBadge(true)

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(channel)
        }
    }

}



