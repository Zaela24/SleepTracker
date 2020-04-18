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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    // Parent for coroutines ; allows cancellation of all coroutines through cancelling just this
    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel() // Cancels all coroutines when onCleared() is called
    }

    // sets scope of UI coroutines to the main thread
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Creates observable and mutable var to hold a SleepNight instance
    private var tonight = MutableLiveData<SleepNight?>()

    private val nights = database.getAllNights()

    val nightsString = Transformations.map(nights) {nights ->
        formatNights(nights, application.resources)
    }



    init{
        initializeTonight()
    }

    private fun initializeTonight() {
        // in UI scope since result needs to update UI, long running work done in suspend fun below
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        // coroutine on IO thread to fetch SleepNight from database without blocking main thread:
        return withContext(Dispatchers.IO){
            var night = database.getTonight()
            if(night?.endTimeMilli != night?.startTimeMilli){
                night = null // if previously completed night, return null
            }
            night // else return night
        }
    }

    fun onStartTracking() { // Important component for start button click tracker

        // In UI scope so result can continue and update UI
        uiScope.launch {
            // create new SleepNight (captures current time as start time)
            val newNight = SleepNight()

            insert(newNight) // inserts new SleepNight into database

            tonight.value = getTonightFromDatabase() // sets tonight to newNight via database
        }
    }

    private suspend fun insert(night: SleepNight) {
        // again, does potentially long running work on IO thread to avoid blocking main thread
        withContext(Dispatchers.IO){
            database.insert(night)
        }
    }

    fun onStopTracking() { // important component to stop button click handler
        uiScope.launch {
            // if tonight.value is null, returns from launch, not from the lambda
            val oldNight = tonight.value ?: return@launch

            // sets end time
            oldNight.endTimeMilli = System.currentTimeMillis()

            update(oldNight) // updates database with end time
        }
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    fun onClear() { // important component of click handler for clear button
        uiScope.launch {
            clear()
            tonight.value = null
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }
}

