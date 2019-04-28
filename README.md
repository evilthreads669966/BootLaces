# Boot Laces
Boot Laces is an Android library that turns your service persistent.
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
4. Add the service to your app's manifest file
```xml
    <application>
        <service
                android:name=".MyService"
                android:enabled="true"
                android:exported="false">
        </service>
    </application>
```
5. Boot Laces provides you with one public method named tie. Pass it two values: 
- an instance of Context  
- the name of your service  
```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //default background notification 
        Laces.tie(this, MyService::class.java.name)
       
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