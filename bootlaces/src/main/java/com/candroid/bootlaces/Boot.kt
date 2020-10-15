package com.candroid.bootlaces

import javax.inject.Singleton

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

    fun <T: IBoot> clone(other: T)
}
@Singleton
class Boot (override var service: String?, override var activity: String?, override var title: String?, override var content: String?, override var icon: Int?): IBoot{
    override fun <T: IBoot>clone(other: T){
        other.service?.let { this.service = it }
        other.activity?.let { this.activity = it }
        other.title?.let { this.title = it }
        other.content?.let { this.content = it }
        other.icon?.let { this.icon = it }
    }
}