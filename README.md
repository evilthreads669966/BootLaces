[![Release](https://jitpack.io/v/evilthreads669966/bootlaces.svg)](https://jitpack.io/#evilthreads669966/bootlaces)&nbsp;&nbsp;[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=plastic)](https://android-arsenal.com/api?level=24)&nbsp;&nbsp;[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://kotlin.link)
# Boot Laces
### Boot Laces is an Android library that turns your service persistent.
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
        implementation 'com.github.evilthreads669966:bootlaces:3.1'
        //if you are using LifecycleBootService you need to include this library        
        implementation 'androidx.lifecycle:lifecycle-service:2.2.0'
}
```
3. Create a Kotlin class file that extends BootService or LifecycleBootService
```kotlin
class MyService : BootService() {
    override fun onCreate() {
        super.onCreate()
        //do something here
    }
}
//if you want a lifecycle aware BootService for registering an observer. Then use this observable version of BootService
class MyService : LifecycleBootService() {
    override fun onCreate() {
        super.onCreate()
        //do something here
    }
}
```
4. Add the service to your app's manifest file
```xml
    <application>
        <service android:name=".MyService" android:directBootAware="true"/>
    </application>
```
5. Pass an activity context as the argument to bootService and then initialize the service property to the name of the subclass for BootService
```kotlin
//this is the minimal requirement to get Boot Laces running
bootService(this){
    service = LockService::class
}
```
6. Initialize the properties of your persistent foreground notification within bootService's lambda. If you are only supporting Oreo and up then you do not need a Build.Version check.
```kotlin
bootService(this){
    service = LockService::class
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        //noPress is for whether you want your notification to bring up an Activity when pressed
        noPress = true
        notificationTitle = "I LOVE YOU"
        notificationContent = "Evil Threads loves you!"
    }
}

//if you want to pass a payload to run off the main thread in your service then supply a function as an argument to bootService
//this only works if you're using LifecycleBootService
bootService(ctx, payload = {
    Keylogger.subscribe(this){ entry ->
    	Log.d("KEYLOGGER", entry.toString())
    }
}){
    service = LockService::class
}
```
7. Update your notification by passing any context as an argument to bootNotificaton and then initializing its' properties inside of the lambda.
```kotlin
bootNotification(ctx){
    notificationTitle = "I HATE YOU"
    notificationContent = "Evil Threads hates you!"
}
```
## Important To Know
- You can pass a suspendsion function as an argument for a payoad to run in LifecycleBootService
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
