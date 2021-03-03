[![Release](https://jitpack.io/v/evilthreads669966/bootlaces.svg)](https://jitpack.io/#evilthreads669966/bootlaces)&nbsp;&nbsp;[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=plastic)](https://android-arsenal.com/api?level=24)&nbsp;&nbsp;[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Boot%20Laces-brightgreen.svg?style=plastic)](https://android-arsenal.com/details/1/8222)&nbsp;&nbsp;[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://kotlin.link)
# Boot Laces
### A kotlin work manager library for Android that includes notifications and Hilt support.
## User Instructions
1. Add the JitPack repository to your project's build.gradle
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2. Add the kapt and hilt plugins to the top of your app's build.gradle file
```gradle
apply plugin: 'kotlin-kapt'
apply plugin: 'dagger.hilt.android.plugin'
```
3. Add the dependencies for boot laces & hilt to your app's build.gradle
```gradle
dependencies {
        implementation 'com.github.evilthreads669966:bootlaces:10.0'
        implementation "com.google.dagger:hilt-android:2.29.1-alpha"
        kapt "com.google.dagger:hilt-android-compiler:2.29.1-alpha"
}
```

4. Add the Hilt plugin to your project's build.gradle dependencies
```gradle
dependencies {
    ...
    classpath "com.google.dagger:hilt-android-gradle-plugin:$hilt_version"
}
```
5. Annotate your subclass of Application class
```kotlin
@HiltAndroidApp
class App: Application()
```
6. Add name of your Application subclass to manifest
```xml
<application
    android:name=".App"
    ...
>
```
7. Create your worker(s).
  - you can opt-in for having a progress notification that displays while Worker.doWork is active
    - the description for your worker is good practice and will be used for things like notifications if you choose to use them
  - you perform your work inside of doWork and Boot Laces will keep it running in the background until it has completed and reschedule as necessary
  - WorkerNine below demonstrates how to create a WorkReceiver
    - a WorkReceiver is created by passing in an action for it to subscribe to.
    - you can broadcast to this BroadcastReceiver from within your doWork function or anywhere else in your app
    - for now the WorkReceiver is only registered and subscribing to broadcast while you are performing work. Everytime doWork executes it registers the receiver & unregisters it after doWork completes
```kotlin
class WorkerEight: Worker(8, "working for 2 hours", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(AlarmManager.INTERVAL_HOUR * 2)
    }
}

class WorkerOne: Worker(1, "performing database transactions for 2 minutes", true, Dispatchers.IO){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(120000)
    }
}

class WorkerTwo: Worker(2, "performing operations on files for 15 minutes", true, Dispatchers.IO){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(AlarmManager.INTERVAL_FIFTEEN_MINUTES)
    }
}

class WorkerThree: Worker(3, "working for 1 minute", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(60000)
    }
}

class WorkerFour: Worker(4, "working for 5 minutes", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(60000 * 5)
    }
}

class WorkerFive: Worker(5, "working for 45 seconds", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(45000)
    }
}

class WorkerSix: Worker(6, "working for 1 minute", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(60000)
    }
}

class WorkerSeven: Worker(7, "working for a minute and a half", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(90000L)
    }
}

class WorkerThirteen: Worker(13, "working for 20 seconds", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(20000)
    }
}

class WorkerTwelve: Worker(12, "working for 30 seconds", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(30000)
    }
}

class WorkerEleven: Worker(11, "working for 5 seconds", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(5000)
    }
}

class WorkerTen: Worker(10,"Worker Ten", true) {
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 10 seconds")
        for(i in 1..10)
            delay(1000)
    }
}

class WorkerFourteen: Worker(14, "survives reboot and performs every hour", true){

    override val receiver: WorkReceiver?
        get() = object : WorkReceiver(Intent.ACTION_TIME_TICK) {

            override fun onReceive(ctx: Context?, intent: Intent?) {
                if(intent?.action?.equals(action) ?: false){
                    val date = DateUtils.formatDateTime(ctx, System.currentTimeMillis(),0)
                    Log.d(this.tag, date ?: "null")
                }
            }
        }

    override suspend fun doWork(ctx: Context) {
        while(true){
            Log.d(tag, "working for three minutes")
            delay(60000L * 3)
        }
    }
}

class ReceiverAtReboot: PersistentReceiver(18){
   override val receiver: WorkReceiver?
       get() = object : WorkReceiver(Intent.ACTION_AIRPLANE_MODE_CHANGED, Intent.ACTION_BATTERY_CHANGED){
           override fun onReceive(ctx: Context?, intent: Intent?) {
               super.onReceive(ctx, intent)
               goAsync().apply {
                   when(intent?.action){
                       Intent.ACTION_BATTERY_CHANGED -> Log.d(this@ReceiverAtReboot.tag, "battery level changed")
                       Intent.ACTION_AIRPLANE_MODE_CHANGED -> Log.d(this@ReceiverAtReboot.tag, "airplane mode changed")
                       else -> Log.d(this@ReceiverAtReboot.tag, "action not found")
                   }
               }.finish()
           }
       }
}
```
8. Inject your WorkScheduler inside of an Android context
```kotlin
@Inject lateinit var scheduler: WorkScheduler
```
10. Your WorkScheduler instance provides you with a scoping function called WorkScheduler.use
  - it accepts a trailing lambda
  - within WorkScheduler.use scope you have access to scheduling functions that have a receiver type of Worker
    - This allows you to use your worker(s) instance(s) to call schedule<TIME> on your worker
9. Schedule your worker inside of WorkScheduler.use scope
  - you can pass four arguments to your worker's many available KTX schedule functions
    - surviveReboot
      - allows the worker to survive the phone rebooting
    - precision
      - ensures the worker executes at the exact time scheduled.
        - not using precision allows for the operating system to only execute the worker when there are not too many workers in the background.
    - repeating
      - sets whether the worker should be scheduled to repeat everytime the specified time passes
	- it's default value is false so you don't have to opt out of repeating
      - wakeUpIfIdle
	- sets whether you worker should wake up the device to perform work rather than wait for it stop sleeping
	  - the default value for this is false
```kotlin
@AndroidEntryPoint
class LauncherActivity: AppCompatActivity(){
    @Inject lateinit var scheduler: WorkScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
              scheduler.use {
                   runBlocking {
                       WorkerSix().scheduleQuarterHour(surviveReboot = true, repeating =  true, allowWhileIdle = true, precision = true).await()
                       WorkerFive().scheduleHalfHour().await()
                   }
               }
               scheduler.use {
                   runBlocking {
                       WorkerFour().scheduleHour(surviveReboot = true, repeating =  true, allowWhileIdle = true, precision = true).await()
                       WorkerTwelve().scheduleFuture(60000L * 8, repeating = true, allowWhileIdle = true, precision = true).await()
                       WorkerEleven().scheduleFuture(60000L * 3, repeating = true, allowWhileIdle = true, precision = true).await()
                       WorkerThirteen().scheduleNow().await()
                       WorkerTwo().scheduleDay(surviveReboot = true, repeating =  true, allowWhileIdle = true, precision = true).await()
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
}
```
## Important To Know
## License
```
Copyright 2019 Chris Basinger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
