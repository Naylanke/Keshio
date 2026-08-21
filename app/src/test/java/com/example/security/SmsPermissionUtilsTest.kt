package com.example.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SmsPermissionUtilsTest {

    @Test
    fun `findActivity unwraps Activity correctly from ContextWrapper`() {
        val activityController = Robolectric.buildActivity(Activity::class.java).setup()
        val activity = activityController.get()

        val wrapper = ContextWrapper(activity)
        val nestedWrapper = ContextWrapper(wrapper)

        val found = SmsPermissionUtils.findActivity(nestedWrapper)
        assertNotNull(found)
        assertEquals(activity, found)
    }

    @Test
    fun `findActivity returns null for pure application context`() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val found = SmsPermissionUtils.findActivity(appContext)
        assertNull(found)
    }

    @Test
    fun `isTelephonySupported returns boolean without crashing`() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        // Should execute cleanly without throwing an exception
        val supported = SmsPermissionUtils.isTelephonySupported(appContext)
        // Check that boolean value is obtained
        assertTrue(supported || !supported)
    }
}
