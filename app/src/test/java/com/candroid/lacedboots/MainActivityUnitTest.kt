/*
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
*/
package com.candroid.lacedboots

import android.content.Intent
import android.widget.TextView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class MainActivityUnitTest {
    lateinit var activity : MainActivity
    @Before
    fun setUp(){
        this.activity = Robolectric.setupActivity(MainActivity::class.java)
    }
    @Test
    fun textViewEqualsHelloWorld(){
        val textView = this.activity.findViewById<TextView>(R.id.textView)
        assert(textView.text.equals("Hello World!"))
    }


    @Test
    fun myServiceIsRunning(){
        assert( activity.stopService(Intent(activity.applicationContext, MyService::class.java)))
    }
}