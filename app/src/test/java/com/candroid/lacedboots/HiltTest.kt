package com.candroid.lacedboots

import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class HiltTest {
    @get:Rule
    val rule = HiltAndroidRule(this)

    @Inject val dataStore: DataStore<Preferences> = TODO()

    @Test
    fun testDataStore(){
        Assert.assertNull(dataStore)
        rule.inject()
        Assert.assertNotNull(dataStore)
    }
}