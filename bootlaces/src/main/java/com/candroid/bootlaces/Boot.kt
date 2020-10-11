package com.candroid.bootlaces

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * Boot holds all data related to a persistent background or foreground service.
 * */

/*IBoot and BootConfig were required because crossinline causes us to expose Boot outside the library and I don't want to expose internal sensitive data*/
abstract class IBoot(open var service: String?, open var activity: String?, open var title: String?, open var content: String?, open var icon: Int?){
    abstract fun edit(service: String? = null, activity: String? = null, title: String? = null, content: String? = null, icon: Int?  = null)
    abstract fun <T: IBoot>edit(boot: T?)
}

class BootConfig(service: String? = null, activity: String? = null, title: String? = null, content: String? = null, icon: Int?  = null): IBoot(service, activity, title, content, icon) {
    override fun edit(service: String?, activity: String?, title: String?, content: String?, icon: Int?) {
        service?.let { this.service = it }
        activity?.let { this.activity = it }
        title?.let { this.title = it }
        content?.let { this.content = it }
        icon?.let { this.icon = it }
    }

    override fun <T : IBoot> edit(boot: T?) {
        boot?.service?.let { this.service = it }
        boot?.activity?.let { this.activity = it }
        boot?.title?.let { this.title = it }
        boot?.content?.let { this.content = it }
        boot?.icon?.let { this.icon = it }
    }
}



@PublishedApi
internal class Boot(service: String? = null, activity: String? = null, title: String? = null, content: String? = null, icon: Int?  = null): IBoot(service, activity, title, content, icon){

    @PublishedApi
    internal  companion object Singleton{
        private var INSTANCE: Boot? = null

        @PublishedApi
        @Synchronized
        internal fun getInstance(): Boot{
            if(INSTANCE == null)
                INSTANCE = Boot()
            return INSTANCE!!
        }
    }

    override fun edit(service: String?, activity: String?, title: String?, content: String?, icon: Int?){
        service?.let { this.service = it }
        activity?.let { this.activity = it }
        title?.let { this.title = it }
        content?.let { this.content = it }
        icon?.let { this.icon = it }
    }

    override fun <T: IBoot> edit(boot: T?) {
        boot?.service?.let { this.service = it }
        boot?.activity?.let { this.activity = it }
        boot?.title?.let { this.title = it }
        boot?.content?.let { this.content = it }
        boot?.icon?.let { this.icon = it }    }
}