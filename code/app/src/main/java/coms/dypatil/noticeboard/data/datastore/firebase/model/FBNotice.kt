package coms.dypatil.noticeboard.data.datastore.firebase.model

import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice

/**
 * Created by Lenovo on 12/15/2017.
 */
class FBNotice {

    var title: String? = null
    var description: String? = null
    var issueDate: Long? = null
    var lastDate: Long? = null
    var issuerId: String? = null


    companion object {
        fun getNotice(fbNotice: FBNotice) = Notice("").also { notice ->
            with(fbNotice) {
                notice.title = title
                notice.description = description
                notice.issueDate = issueDate
                notice.lastDate = lastDate
                notice.issuerId = issuerId
            }
        }
    }

}