[![Release](https://jitpack.io/v/evilthreads669966/bootlaces.svg)](https://jitpack.io/#evilthreads669966/bootlaces)&nbsp;&nbsp;[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=plastic)](https://android-arsenal.com/api?level=24)&nbsp;&nbsp;[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://kotlin.link)
# Boot Laces
### Boot Laces is an Android library that turns your service persistent
#### It take your background service and puts it into the foreground while making it persistent.
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
        implementation 'com.github.evilthreads669966:bootlaces:6.0'
        implementation 'androidx.lifecycle:lifecycle-service:2.2.0'
        implementation "com.google.dagger:hilt-android:2.29.1-alpha"
        kapt "com.google.dagger:hilt-android-compiler:2.29.1-alpha"
}
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
6. Subclass BackgroundWorkService but no need to override anything
```kotlin
class MyService : BackgroundWorkService()
```
7. Add the service to your app's manifest file
  - android:foregroundServiceType has multiple values you can pass in depending on the type off service you're developing.
    - https://developer.android.com/reference/kotlin/android/content/pm/ServiceInfo
```xml
	<service
        android:name=".MyService"
        android:directBootAware="true"
        android:foregroundServiceType="dataSync"
    />
```
8. Create your worker(s). The description parameter will be used for your notification. You can create a Broadcast receiver for your worker by overriding onReceive and passing true for hasReceiver along with an action.
```kotlin
class MyWorker: Worker(666,"Locking the screen", hasReceiver = true, action = Intent.ACTION_CLOSE_SYSTEM_DIALOGS){
    override suspend fun doWork(ctx: Context) {
        //do some work
    }
    //you only need to override this if your set hasReceiver to true
    override fun onReceive(ctx: Context, intent: Intent) {
        //handle broadcast for action your passed in as parameter to worker
    }
}
```
9. Inject your WorkScheduler into your activity
```kotlin
    @Inject lateinit var scheduler: WorkScheduler
```
10. Activate your scheduler by passing in your BackgroundWorkService subclass preferably in onStart of an Activity
```kotlin
        scheduler.activate(LockService::class.java.name)
```
11. Choose a persistent worker or a one time worker. A persistent worker will cause your service to start at boot and run the worker.
```kotlin
    //persistent worker
    scheduler.schedulePersistent(ScreenLockerJob())
    //one time worker
    scheduler.scheduleOneTime(OneTimeWorker())
```
## Important To Know
- You can schedule as many workers as you want both persistent and one time workers.
- Whenever one or more workers are running a foreground notification will be pinned until all workers complete.
- Each worker recieves its' own non-foreground notification to display progress for the task with the description provided by the worker.
## Ask a Question?
- Use [Github issues](https://github.com/evilthreads669966/bootlaces/issues)
- Send an email to evilthreads669966@gmail.com

## Reporting issues
Found a bug on a specific feature? Open an issue on [Github issues](https://github.com/evilthreads669966/bootlaces/issues)

## Contributing

SMS Backdoor is released under the [Apache 2.0 license](https://github.com/evilthreads669966/bootlaces/blob/master/LICENSE). If you would like to contribute
something, or simply want to hack then this document should help you get started.

### Code of Conduct
- Please refrain from using any profanity
- Please be respectful on [GitHub Issues](https://github.com/evilthreads669966/bootlaces/issues)
- Have fun

### [Pull Requests](https://github.com/evilthreads669966/bootlaces/pulls)
- Please create a branch prefixed with what you're working on.
    - FEATURE_ADDING_SOMETHING
    - BUG_FIXING_SOMETHING
    - REFACTOR_CHANGING_SOMETHING
- Once you're done with your commits to this branch hit a [pull request](https://github.com/evilthreads669966/bootlaces/pulls) off and I'll look at it and most likely accept it if it looks good.

### Using [GitHub Issues](https://github.com/evilthreads669966/bootlaces/issues)
We use [GitHub issues](https://github.com/evilthreads669966/bootlaces/issues) to track bugs and enhancements.
- If you find a bug please fill out an issue report. Provide as much information as possible.
- If you think of a great idea please fill out an issue as a proposal for your idea.

### Code Conventions
None of these is essential for a pull request, but they will all help.  They can also be
added after the original pull request but before a merge.

- We use idiomatic kotlin conventions
- Add yourself as an `@author` to the `.kt` files that you modify or create.
- Add some comments
- A few unit tests would help a lot as well -- someone has to do it.
- If you are able to provide a unit test then do.
    - Because of the types of libraries I develop often times it is hard to test.


### Working with the code
If you don't have an IDE preference we would recommend that you use
[Android Studio](https://developer.android.com/studio/)
## Contributors
This project exists thanks to all the people who contribute.
<a href="https://github.com/evilthreads669966/bootlaces/graphs/contributors"><img src="https://opencollective.com/bootlaces/contributors.svg?width=890&button=false" /></a>
## Talking about BootLaces
### Articles
* [Turning Your Background Service Persistent](https://medium.com/swlh/boot-laces-android-library-9d64f54b30fa)
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
