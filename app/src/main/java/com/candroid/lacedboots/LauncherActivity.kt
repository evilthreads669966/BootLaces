/*Copyright 2019 Chris Basinger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.candroid.lacedboots

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.candroid.bootlaces.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

/*
            (   (                ) (             (     (
            )\ ))\ )    *   ) ( /( )\ )     (    )\ )  )\ )
 (   (   ( (()/(()/(  ` )  /( )\()|()/((    )\  (()/( (()/(
 )\  )\  )\ /(_))(_))  ( )(_)|(_)\ /(_))\((((_)( /(_)) /(_))
((_)((_)((_|_))(_))   (_(_()) _((_|_))((_))\ _ )(_))_ (_))
| __\ \ / /|_ _| |    |_   _|| || | _ \ __(_)_\(_)   \/ __|
| _| \ V /  | || |__    | |  | __ |   / _| / _ \ | |) \__ \
|___| \_/  |___|____|   |_|  |_||_|_|_\___/_/ \_\|___/|___/
....................../´¯/)
....................,/¯../
.................../..../
............./´¯/'...'/´¯¯`·¸
........../'/.../..../......./¨¯\
........('(...´...´.... ¯~/'...')
.........\.................'...../
..........''...\.......... _.·´
............\..............(
..............\.............\...
*/
@ExperimentalCoroutinesApi
@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class LauncherActivity: AppCompatActivity(){
    @Inject lateinit var scheduler: WorkScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduler.use {
            WorkerNine().schedulePersistent()
            WorkerSix().scheduleQuarterHour(true, true)
            WorkerFive().scheduleHalfHour()
        }
        scheduler.use {
            WorkerFour().scheduleHour(true)
            WorkerTwelve().scheduleHalfDay()
            WorkerEleven().scheduleMonth(true, true)
            WorkerThirteen().scheduleYearly()
            WorkerTwo().scheduleDay(true)
            val fourtyFiveSeconds = 45000L
            WorkerOne().scheduleFuture(fourtyFiveSeconds, true)
            WorkerThree().scheduleQuarterDay(true)
        }
        scheduler.use {
            WorkerSeven().scheduleNow()
            WorkerEight().scheduleHoursTwo(true)
            WorkerTen().scheduleHalfWeek(false)
            WorkerFourteen().schedulePersistent()
        }
    }
}