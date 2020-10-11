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

    open fun copy(service: String? = null, activity: String? = null, title: String? = null, content: String? = null, icon: Int?  = null){
        service?.let { this.service = it }
        activity?.let { this.activity = it }
        title?.let { this.title = it }
        content?.let { this.content = it }
        icon?.let { this.icon = it }
    }

    open fun <T: IBoot>clone(other: T){
        other.service?.let { this.service = it }
        other.activity?.let { this.activity = it }
        other.title?.let { this.title = it }
        other.content?.let { this.content = it }
        other.icon?.let { this.icon = it }
    }
}

class BootConfig(service: String? = null, activity: String? = null, title: String? = null, content: String? = null, icon: Int?  = null): IBoot(service, activity, title, content, icon)

@PublishedApi
internal class Boot(service: String? = null, activity: String? = null, title: String? = null, content: String? = null, icon: Int?  = null): IBoot(service, activity, title, content, icon){
    @PublishedApi
    internal companion object{
        private var INSTANCE: Boot? = null

        @PublishedApi
        @Synchronized
        internal fun getInstance(): Boot{
            if(INSTANCE == null)
                INSTANCE = Boot()
            return INSTANCE!!
        }
    }
}