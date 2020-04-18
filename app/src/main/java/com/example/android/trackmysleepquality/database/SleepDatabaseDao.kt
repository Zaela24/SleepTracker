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

package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SleepDatabaseDao{

    @Insert // Inserts new SleepNight into table
    fun insert(night: SleepNight)

    @Update // Updates entry with new information (such as end time)
    fun update(night: SleepNight)

    @Query("SELECT * from daily_sleep_quality_table WHERE nightId = :key")
    fun get(key: Long): SleepNight? // Returns SleepNight object if key matches

    @Query("DELETE FROM daily_sleep_quality_table")
    fun clear() // Clears all entries in table

    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNights(): LiveData<List<SleepNight>> // Returns all entries by most to least recent
    //returns LiveData, so updates when table is updated

    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
    fun getTonight(): SleepNight? // returns most recently entered SleepNight entry

}


