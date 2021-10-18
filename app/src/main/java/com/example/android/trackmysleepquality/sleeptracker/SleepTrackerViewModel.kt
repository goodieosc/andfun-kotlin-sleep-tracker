package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*


class SleepTrackerViewModel(
    val database: SleepDatabaseDao,
    application: Application) : AndroidViewModel(application) {

    //Define a variable, tonight, to hold the current night, and make it MutableLiveData:
    private var tonight = MutableLiveData<SleepNight?>()

    //Define a variable, nights. Then getAllNights() from the database and assign to the nights variable:
    val nights = database.getAllNights()

    //Add code to transform nights into a nightsString using the formatNights() function from Util.kt:
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }


    /**
     * Variable that tells the Fragment to navigate to a specific [SleepQualityFragment]
     *
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    //Exposed value
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality


    //To initialize the tonight variable, create an init block and call initializeTonight()
    init {
        initializeTonight()

     }



    //Implement initializeTonight(). Use the viewModelScope.launch{} to start a coroutine in the ViewModelScope.
    private fun initializeTonight() {
        run  {
              //Inside, get the value for tonight from the database by calling getTonightFromDatabase(), which you will define in the next step, and assign it to tonight.value:

              viewModelScope.launch {
                  tonight.value = getTonightFromDatabase()
              }
        }
    }

    /**
     *  Handling the case of the stopped app or forgotten recording,
     *  the start and end times will be the same.j
     *
     *  If the start time and end time are not the same, then we do not have an unfinished
     *  recording.
     */
    //Implement getTonightFromDatabase(). Define is as a private suspend function that returns a nullable SleepNight, if there is no current started sleepNight.
    private suspend fun getTonightFromDatabase(): SleepNight? {

        //Let the coroutine get tonight from the database. If the start and end times are the not the same,
        // meaning, the night has already been completed, return null. Otherwise, return night:
        var night = database.getTonight()
        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
    }



    private suspend fun clear() {
        database.clear()
    }

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }

    //Define insert() as a private suspend function that takes a SleepNight as its argument:
    private suspend fun insert(night: SleepNight) {
        database.insert(night)
    }

    //Implement onStartTracking(), the click handler for the Start button:
    fun onStartTracking() {

        //Inside onStartTracking(), launch a coroutine in viewModelScope:
        viewModelScope.launch {
            // Create a new night, which captures the current time,
            // and insert it into the database.

            //Inside the coroutine, create a new SleepNight, which captures the current time as the start time:
            val newNight = SleepNight()

            //Call insert() to insert it into the database. You will define insert() shortly:
            insert(newNight)

            //Set tonight to the new night:
            tonight.value = getTonightFromDatabase()
        }
    }

    /**
     * Executes when the STOP button is clicked.
     */
    fun onStopTracking() {
        viewModelScope.launch {
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch(),
            // not the lambda.
            val oldNight = tonight.value ?: return@launch

            // Update the night in the database to add the end time.
            oldNight.endTimeMilli = System.currentTimeMillis()

            //If it hasn't been set yet, set the endTimeMilli to the current system time and call update() with the night.
            update(oldNight)

            _navigateToSleepQuality.value = oldNight

        }
    }

    //function that resets the event.
    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    private val _navigateToSleepDataQuality = MutableLiveData<Long>()
    val navigateToSleepDataQuality
        get() = _navigateToSleepDataQuality

    fun onSleepNightClicked(id: Long) {
        _navigateToSleepDataQuality.value = id
    }

    fun onSleepDataQualityNavigated() {
        _navigateToSleepDataQuality.value = null
    }



    //The START button should be visible when tonight is null, the STOP button when tonight is not null, and the CLEAR button if nights contains any nights:
    val startButtonVisible = Transformations.map(tonight) {
        null == it
    }
    val stopButtonVisible = Transformations.map(tonight) {
        null != it
    }
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    /**
     * Request a toast by setting this value to true.
     *
     * This is private because we don't want to expose setting this value to the Fragment.
     */
    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    /**
     * If this is true, immediately `show()` a toast and call `doneShowingSnackbar()`.
     */
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent


    /**
     * Call this immediately after calling `show()` on a toast.
     *
     * It will clear the toast request, so if the user rotates their phone it won't show a duplicate
     * toast.
     */
    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    /**
     * Executes when the CLEAR button is clicked.
     */
    fun onClear() {
        viewModelScope.launch {
            // Clear the database table.
            clear()

            // And clear tonight since it's no longer in the database
            tonight.value = null

            //Snackbar
            _showSnackbarEvent.value = true

        }
    }



}

