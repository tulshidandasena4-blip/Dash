package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BusinessInfo
import com.example.data.model.VehicleEntryEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dash Pollution", appName)
  }

  @Test
  fun `verify automatic calculations for vehicle entries and truck fixed rate`() {
    val truckCount = 5
    val truckRate = BusinessInfo.DEFAULT_TRUCK_RATE // 180.0
    val truckAmount = truckCount * truckRate

    val entry = VehicleEntryEntity(
      date = "2026-10-26",
      petrolCount = 10,
      petrolAmount = 1000.0,
      dieselCount = 4,
      dieselAmount = 480.0,
      truckCount = truckCount,
      truckRate = truckRate,
      truckAmount = truckAmount
    )

    // Petrol (10) + Diesel (4) + Truck (5) = 19
    assertEquals(19, entry.totalVehicles)
    // 5 * 180 = 900.0
    assertEquals(900.0, entry.truckAmount, 0.001)
    // 1000 + 480 + 900 = 2380.0
    assertEquals(2380.0, entry.grandTotalCollection, 0.001)

    // Net collection with expenditure
    val expenditure = 350.0
    val netCollection = entry.grandTotalCollection - expenditure
    assertEquals(2030.0, netCollection, 0.001)
  }
}
