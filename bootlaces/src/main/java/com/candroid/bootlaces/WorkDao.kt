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
package com.candroid.bootlaces

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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
/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/31/20
 *
 **/
@Dao
interface WorkDao{

    @Query("SELECT * FROM work WHERE interval IS NULL AND delay IS NULL AND hourly != 1 AND daily != 1 AND weekly != 1 AND monthly != 1 AND yearly != 1")
    fun getPersistentWork(): Flow<Work>

    @Query("SELECT * FROM work WHERE delay IS NOT NULL AND interval IS NULL")
    fun getFutureWork(): Flow<Work>

    @Query("SELECT * FROM work WHERE hourly = 1 OR daily = 1 OR weekly = 1 OR monthly = 1 OR yearly = 1 OR interval IS NOT NULL")
    fun getPeriodicWork(): Flow<Work>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(worker: Work)

    @Delete
    fun delete(worker: Work)
}