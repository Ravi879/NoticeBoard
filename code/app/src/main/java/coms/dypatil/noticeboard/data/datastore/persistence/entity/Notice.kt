package coms.dypatil.noticeboard.data.datastore.persistence.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import coms.dypatil.noticeboard.data.datastore.persistence.contract.NoticeContract
import kotlinx.android.parcel.Parcelize

/**
 * Created by Lenovo on 12/15/2017.
 */
@Parcelize
@Entity(tableName = NoticeContract.TABLE_NAME)
data class Notice(

        @ColumnInfo(name = NoticeContract.FB_NOTICE_ID)
        @PrimaryKey
        var fbNoticeId: String,

        @ColumnInfo(name = NoticeContract.TITLE)
        var title: String? = null,

        @ColumnInfo(name = NoticeContract.DESCRIPTION)
        var description: String? = null,

        @ColumnInfo(name = NoticeContract.ISSUE_DATE)
        var issueDate: Long? = null,

        @ColumnInfo(name = NoticeContract.LAST_DATE)
        var lastDate: Long? = null,

        @ColumnInfo(name = NoticeContract.ISSUER_Id)
        var issuerId: String? = null,

        @ColumnInfo(name = NoticeContract.ISSUER_NAME)
        var issuerName: String? = null,

        @ColumnInfo(name = NoticeContract.DEPARTMENT)
        var department: String? = null,

        @ColumnInfo(name = NoticeContract.DESIGNATION)
        var designation: String? = null,

        @ColumnInfo(name = NoticeContract.IS_OPEN)
        var isOpen: String? = "close") : Parcelable {


}