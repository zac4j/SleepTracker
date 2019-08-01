package com.zac4j.sleeptracker

import android.app.Application
import com.sensorsdata.analytics.android.sdk.SAConfigOptions
import com.sensorsdata.analytics.android.sdk.SensorsAnalyticsAutoTrackEventType
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils.getAppName
import org.json.JSONObject

/**
 * App
 *
 * @author: zac
 * @date: 2019-07-31
 */
class App : Application() {

  companion object {
    // Data collect url
    const val SA_SERVER_URL = "http://newdsj.gtjadev.com:8106/sa?project=default"
  }

  override fun onCreate() {
    super.onCreate()

    setUpSaConfig()
  }

  private fun setUpSaConfig() {
    val saConfig = SAConfigOptions(SA_SERVER_URL)
    saConfig.setAutoTrackEventType(
        SensorsAnalyticsAutoTrackEventType.APP_CLICK +
            SensorsAnalyticsAutoTrackEventType.APP_START +
            SensorsAnalyticsAutoTrackEventType.APP_END +
            SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN
    )
    SensorsDataAPI.sharedInstance(this, saConfig)
        .enableLog(true)
  }

  /**
   * Register common public property
   */
  private fun registerAppProperties() {
    val properties = JSONObject()
    properties.put("AppName", getAppName(this))
    SensorsDataAPI.sharedInstance()
        .registerSuperProperties(properties)
  }

  override fun onTerminate() {
    super.onTerminate()
    SensorsDataAPI.sharedInstance().clearSuperProperties()
  }
}