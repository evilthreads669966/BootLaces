package com.candroid.bootlaces

/**
* @author Chris Basinger
* @email evilthreads669966@gmail.com
* @date 10/09/20
 *
* Boot holds all data related to a persistent background or foreground service.
* */
data class Boot(var service: String? = null, var activity: String? = null, var title: String? = null, var content: String? = null, var icon: Int?  = null)