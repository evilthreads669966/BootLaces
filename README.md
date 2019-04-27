# Boot Laces
Boot Laces is an Android library that turns your service persistent.
### User Instructions
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
        implementation 'com.github.candroidtb:bootlaces:v1.0-beta'
}
```
3. Create a Java or Kotlin class file that extends BootService.
```kotlin
import com.candroid.bootlaces.BootService

class MyService : BootService() {
    override fun onCreate() {
        super.onCreate()
        //do something here
    }
}
```
4. Use the provided method inside your app's main launcher activity's onCreate method to make sure your service starts when the device is rebooted as well as make sure it gets started for its' first time when the application is first used.
- You just need to provide two things.
-- Context
-- The name of your service class extending from BootService
--- "MyService" or how I did it below in the example code
- Optional parameters
-- There are several optional parameters not shown below that are used for configuring the display of your persistent service's foreground notification which is shown in the device's system status bar.
```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Use this method to persist and start your service for its first time 
        Laces.tie(this, MyService::class.java.name)
       
    }
```
