package coms.dypatil.noticeboard.util.rx

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SchedulersFacade {

    companion object {

        //IO thread pool scheduler
        fun io(): Scheduler {
            return Schedulers.io()
        }

        //Computation thread pool scheduler
        fun computation(): Scheduler {
            return Schedulers.computation()
        }

        //Main Thread scheduler
        fun ui(): Scheduler {
            return AndroidSchedulers.mainThread()
        }
    }
}