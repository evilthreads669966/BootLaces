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
        implementation 'com.github.evilthreads669966:bootlaces:1.0'
}
```
3. Create a Kotlin class file that extends BootService.
```kotlin
import com.candroid.bootlaces.BootService
//BootService is lifecycle aware so you can register an observer
class MyService : BootService() {
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
5. Boot Laces provides you with two functional methods named bootService and updateNotification. 
Pass in one argument and initialize one property inside your lambda block: 
- Activity Context  
- initialize service property to the name of your derived service class of BootService  
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    bootService(this){
        service = LockService::class
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            noPress = true
            notificationTitle = "I LOVE YOU"
            notificationContent = "Evil Threads love you one time!"
        }
    }
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        bootNotification(ctx){
            notificationContent = "Evil Threads love you ${ScreenVisibility.count()} times!"
        }
}
```

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
