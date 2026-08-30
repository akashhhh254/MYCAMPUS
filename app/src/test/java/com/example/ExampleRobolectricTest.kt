package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.CampusRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MyCampus", appName)
  }

  @Test
  fun `verify role mapping matches security rules`() {
    assertEquals(CampusRole.PRINCIPAL, CampusRole.fromValue("principal"))
    assertEquals(CampusRole.PRINCIPAL, CampusRole.fromValue("admin"))
    assertEquals(CampusRole.TEACHER, CampusRole.fromValue("teacher"))
    assertEquals(CampusRole.TEACHER, CampusRole.fromValue("faculty"))
    assertEquals(CampusRole.STUDENT, CampusRole.fromValue("student"))
  }
}

