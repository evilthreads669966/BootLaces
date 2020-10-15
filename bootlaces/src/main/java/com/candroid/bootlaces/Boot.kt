package com.candroid.bootlaces

import javax.inject.Inject

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * Boot holds the configuration data for creating persistent foreground service
 * */
interface IBoot{
    var service: String?
    var activity: String?
    var title: String?
    var content: String?
    var icon: Int?

    fun <T: IBoot>clone(other: T)
}

data class Boot (override var service: String?, override var activity: String?, override var title: String?, override var content: String?, override var icon: Int?): IBoot{

/*    fun copy( service: String?, activity: String?, title: String?, content: String?, icon: Int?){
        service?.let { this.service = it }
        activity?.let { this.activity = it }
        title?.let { this.title = it }
        content?.let { this.content = it }
        icon?.let { this.icon = it }
    }*/

    override fun <T: IBoot>clone(other: T){
        other.service?.let { this.service = it }
        other.activity?.let { this.activity = it }
        other.title?.let { this.title = it }
        other.content?.let { this.content = it }
        other.icon?.let { this.icon = it }
    }
}