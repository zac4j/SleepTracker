/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zac4j.sleeptracker.tracker

import android.app.Application
import android.text.Spanned
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.zac4j.sleeptracker.database.SleepDatabaseDao
import com.zac4j.sleeptracker.database.SleepNight
import com.zac4j.sleeptracker.formatNights
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
  val database: SleepDatabaseDao,
  application: Application
) : AndroidViewModel(application) {

  private var viewModelJob = Job()

  private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

  private var tonight = MutableLiveData<SleepNight?>()

  private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
  val navigateToSleepQuality: LiveData<SleepNight>
    get() = _navigateToSleepQuality

  private val _navigateToSleepDataQuality = MutableLiveData<Long>()
  val navigateToSleepDataQuality
    get() = _navigateToSleepDataQuality

  private val _nights = database.getAllNights()
  val nights: LiveData<List<SleepNight>>
    get() = _nights

  val nightsString: LiveData<Spanned> = Transformations.map(_nights) { nights ->
    formatNights(nights, application.resources)
  }

  val startButtonVisible: LiveData<Boolean> = Transformations.map(tonight) {
    null == it
  }
  val stopButtonVisible: LiveData<Boolean> = Transformations.map(tonight) {
    null != it
  }
  val clearButtonVisible: LiveData<Boolean> = Transformations.map(_nights) {
    it?.isNotEmpty()
  }

  private var _showSnackbarEvent = MutableLiveData<Boolean>()
  val showSnackbarEvent: LiveData<Boolean>
    get() = _showSnackbarEvent

  init {
    initializeTonight()
  }

  private fun initializeTonight() {
    uiScope.launch {
      tonight.value = getTonightFromDatabase()
    }
  }

  private suspend fun getTonightFromDatabase(): SleepNight? {
    return withContext(Dispatchers.IO) {
      var night = database.getTonight()

      if (night?.endTimeMilli != night?.startTimeMilli) {
        night = null
      }

      night
    }
  }

  fun onStartTracking() {
    uiScope.launch {
      var sleepNight = SleepNight()

      insert(sleepNight)

      tonight.value = getTonightFromDatabase()

    }
  }

  fun onStopTracking() {
    uiScope.launch {
      val oldNight = tonight.value ?: return@launch

      oldNight.endTimeMilli = System.currentTimeMillis()

      _navigateToSleepQuality.value = oldNight

      update(oldNight)
    }
  }

  fun onClear() {
    uiScope.launch {
      clear()
      tonight.value = null

      _showSnackbarEvent.value = true
    }
  }

  override fun onCleared() {
    super.onCleared()

    viewModelJob.cancel()
  }

  private suspend fun insert(night: SleepNight) {
    withContext(Dispatchers.IO) {
      database.insert(night)
    }
  }

  private suspend fun update(night: SleepNight) {
    withContext(Dispatchers.IO) {
      database.update(night)
    }
  }

  private suspend fun clear() {
    withContext(Dispatchers.IO) {
      database.clear()
    }
  }

  fun doneNavigating() {
    _navigateToSleepQuality.value = null
  }

  fun doneShowingSnackbar() {
    _showSnackbarEvent.value = false
  }

  fun onSleepNightClicked(nightId: Long) {
    _navigateToSleepDataQuality.value = nightId
  }

  fun onSleepDataQualityNavigated() {
    _navigateToSleepDataQuality.value = null
  }

}

