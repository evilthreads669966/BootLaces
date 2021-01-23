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
@FlowPreview
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@AndroidEntryPoint
class LauncherActivity: AppCompatActivity(){
    @Inject lateinit var scheduler: WorkScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduler.scheduleWork()
    }
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@FlowPreview
@InternalCoroutinesApi
fun WorkScheduler.scheduleWork(){
    schedulePersistent(PersistentWorker())
    scheduleHour(HourlyWorker(), repeating = false, wakeupIfIdle = true)
    scheduleDay(DailyWorker(), wakeupIfIdle = true)
    scheduleWeek(WeeklyWorker(), repeating = true, wakeupIfIdle = false)
    scheduleMonth(MonthlyWorker(), repeating = true, wakeupIfIdle = true)
    scheduleNow(OneTimeWorker())
    scheduleHalfHour(HalfHourWorker(), repeating = true, wakeupIfIdle = true)
    scheduleQuarterHour(QuarterHourWorker(), repeating = true, wakeupIfIdle = true)
    val tenSeconds = 10000L
    scheduleFuture(tenSeconds, FirstFutureWorker(), repeating = true)
    val thirtySeconds = 30000L
    scheduleFuture(thirtySeconds, SecondFutureWorker(), wakeupIfIdle = true)
    val fourtyFiveSeconds = 45000L
    scheduleFuture(fourtyFiveSeconds, ThirdFutureWorker(), repeating = true, wakeupIfIdle = true)
}