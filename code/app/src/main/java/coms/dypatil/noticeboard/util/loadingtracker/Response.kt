package coms.dypatil.noticeboard.util.loadingtracker

import coms.dypatil.noticeboard.util.loadingtracker.Status.*

class Response private constructor(val status: Status, val data: String?, val error: Throwable?) {
    companion object {

        fun loading(): Response {
            return Response(LOADING, null, null)
        }

        fun success(data: String?): Response {
            return Response(SUCCESS, data, null)
        }

        fun error(error: Throwable, msg: String): Response {
            return Response(ERROR, msg, error)
        }
    }
}
