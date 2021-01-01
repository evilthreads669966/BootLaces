[![Release](https://jitpack.io/v/evilthreads669966/bootlaces.svg)](https://jitpack.io/#evilthreads669966/bootlaces)&nbsp;&nbsp;[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=plastic)](https://android-arsenal.com/api?level=24)&nbsp;&nbsp;[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://kotlin.link)
# Boot Laces
### A kotlin work manager library for Android that includes notifications.
## Warning: Version 8.+ is not stable
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
2. Add the dependency to your app's build.gradle
```gradle
dependencies {
        implementation 'com.github.evilthreads669966:bootlaces:8.4'
        implementation "com.google.dagger:hilt-android:2.29.1-alpha"
        kapt "com.google.dagger:hilt-android-compiler:2.29.1-alpha"
}
```
3. Add the Hilt plugin to your project's build.gradle dependencies
```gradle
dependencies {
    ...
    classpath "com.google.dagger:hilt-android-gradle-plugin:$hilt_version"
}
```
4. Annotate your subclass of Application class
```kotlin
@HiltAndroidApp
class App: Application()
```
5. Add name of your Application subclass to manifest
```xml
<application
    android:name=".App"
    ...
>
```
6. Create your worker(s). The description parameter will be used for your notification. You can create a Broadcast receiver for your worker by overriding onReceive an action to Worker.
```kotlin
class MyWorker: Worker(66,"Something evil") {
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        for(i in 1..10)
            delay(1000)
    }
}

//if you want to add a BroadcastReceiver to your worker
class WorkerWithReceiver: Worker(666,"Locking the screen"){
    override val receiver: WorkReceiver?
        get() = object : WorkReceiver(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                //do something
            }
        }

    override suspend fun doWork(ctx: Context) {
        //do work
    }
}
//worker with a progress notification
class MyProgressWorker: Worker(66,"Working while displaying a notification for progress", withNotification = true) {
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        for(i in 1..10)
            delay(1000)
    }
}
```
7. Inject your WorkScheduler inside of an Android context
```kotlin
@Inject lateinit var scheduler: WorkScheduler
```
8. Schedule your worker
```kotlin
//persistent worker
scheduler.schedulePersistent(WorkerWithReceiver())

//one time worker
scheduler.scheduleOneTime(MyWorker())

//one time worker with notificaton for displaying progress. This works for all scheduler methods
scheduler.scheduleOneTime(MyProgressWorker())

//periodic worker
scheduler.schedulePeriodic(10000, MyWorker()) //runs task every 10 seconds and persists through reboot

//future worker
scheduler.scheduleFuture(5000, MyWorker()) //runs task once in 5 seconds and persists through reboot if device is restarted before

//hourly worker
scheduler.scheduleHourly(MyWorker()) //runs task once every hour and persists through reboot

//daily worker
scheduler.scheduleDaily(MyWorker()) //runs task once every day and persists through reboot

//weekly worker
scheduler.scheduleWeekly(MyWorker()) //runs task once every week (7 days) and persists through reboot

//monthly worker
scheduler.scheduleMonthly(MyWorker()) //runs task once every month and persists through reboot

//yearly worker
scheduler.scheduleYearly(MyWorker()) //runs task once every year and persists through reboot
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
