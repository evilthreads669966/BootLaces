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
import com.candroid.bootlaces.IntentFactory
import com.candroid.bootlaces.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
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
@AndroidEntryPoint
class LauncherActivity: AppCompatActivity(){
    @Inject lateinit var scheduler: WorkScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduler.use {
            runBlocking {
                WorkerSix().scheduleQuarterHour(repeating =  true, allowWhileIdle = true, precision = true).await()
                WorkerFive().scheduleHalfHour().await()
            }
        }
        scheduler.use {
            runBlocking {
                WorkerFour().scheduleHour(repeating =  true, allowWhileIdle = true, precision = true).await()
                WorkerTwelve().scheduleFuture(60000L * 8, repeating = true, allowWhileIdle = true, precision = true).await()
                WorkerEleven().scheduleFuture(60000L * 3, repeating = true, allowWhileIdle = true, precision = true).await()
                WorkerThirteen().scheduleNow().await()
                WorkerTwo().scheduleDay(repeating =  true, allowWhileIdle = true, precision = true).await()
                val fourtyFiveSeconds = 45000L
                WorkerOne().scheduleFuture(fourtyFiveSeconds, repeating = true, allowWhileIdle = true).await()
                WorkerThree().scheduleQuarterDay(repeating =  true, allowWhileIdle = true, precision = true).await()
            }
        }
        scheduler.use {
            runBlocking {
                WorkerSeven().scheduleNow().await()
                WorkerEight().scheduleHoursTwo(repeating =  true, allowWhileIdle = true, precision = true).await()
                WorkerTen().scheduleHalfWeek(repeating =  true, allowWhileIdle = true, precision = true).await()
                WorkerFourteen().scheduleHour(surviveReboot = true, repeating = true, allowWhileIdle = true, precision = true).await()
                ReceiverAtReboot().scheduleReceiver().await()
            }
        }
    }
}