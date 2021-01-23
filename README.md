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
        implementation 'com.github.evilthreads669966:bootlaces:9.0'
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
  - WorkerNine below demonstrates how to create a WorkReceiver
    - a WorkReceiver is created by passing in an action for it to subscribe to.
    - you can broadcast to this BroadcastReceiver from within your doWork function or anywhere else in your app
    - for now the WorkReceiver is only registered and subscribing to broadcast while you are performing work. Everytime doWork executes it registers the receiver & unregisters it after doWork completes
```kotlin
class WorkerEight: Worker(8, "Worker Eight", withNotification = true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 2 hours")
        delay(AlarmManager.INTERVAL_HOUR * 2)
    }
}

class WorkerOne: Worker(1, "Worker One", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 2 minutes")
        delay(120000)
    }
}

class WorkerTwo: Worker(2, "Worker Two", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 15 minutes")
        delay(AlarmManager.INTERVAL_FIFTEEN_MINUTES)
    }
}

class WorkerThree: Worker(3, "Worker Three", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 1 minute")
        delay(60000)
    }
}

class WorkerFour: Worker(4, "Worker Four", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 5 minutes")
        delay(60000 * 5)
    }
}

class WorkerFive: Worker(5, "Worker Five", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 45 seconds")
        delay(45000)
    }
}

class WorkerSix: Worker(6, "Worker Six", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 1 minute")
        delay(60000)
    }
}

class WorkerSeven: Worker(7, "Worker Seven", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for a minute and a half")
        delay(90000L)
    }
}

class WorkerThirteen: Worker(13, "Worker Thirteen", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 20 seconds")
        delay(20000)
    }
}

class WorkerTwelve: Worker(12, "Worker Twelve", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 30 seconds")
        delay(30000)
    }
}



class WorkerEleven: Worker(11, "Worker Eleven", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 5 seconds")
        delay(5000)
    }
}
class WorkerTen: Worker(10,"Worker Ten", true) {
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 10 seconds")
        for(i in 1..10)
            delay(1000)
    }
}

class WorkerNine: Worker(9,"Worker Nine", true){
    val tag = this::class.java.name

    override val receiver: WorkReceiver?
        get() = object : WorkReceiver(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                //handle broadcast and if you want you can broadcast your action from doWork to onReceive
            }
        }

    override suspend fun doWork(ctx: Context) {
        while(true){
            Log.d(tag, "Working for 25 seconds")
            delay(25000)
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
```kotlin
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
