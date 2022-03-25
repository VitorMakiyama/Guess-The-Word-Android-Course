package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)


class GameViewModel : ViewModel() {
    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    companion object {
        // These represent different important times
        // This is when the phone will start panic buzzing
        const val PANIC_TIME = 0L

        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L

        // This is the total time of the game
        const val COUNTDOWN_TIME = 60000L
    }

    private var timer: CountDownTimer
    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long>
        get() = _timeRemaining

    val timeRemainingString = Transformations.map(timeRemaining, {time ->
        DateUtils.formatElapsedTime(time)})

    private val _buzzEvent = MutableLiveData<BuzzType>()
    val buzzEvent : LiveData<BuzzType>
        get() = _buzzEvent

    // The current word
    private val _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word

    // The current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score
    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean>
        get() = _eventGameFinish

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    init {
        _word.value = ""
        _score.value = 0
        _eventGameFinish.value = false
        resetList()
        nextWord()

        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(milliSecondsUntilFinished: Long) {
                _timeRemaining.value = milliSecondsUntilFinished / 1000
                if (timeRemaining.value!! < PANIC_TIME) {
                    onBuzzPanic()
                }
            }

            override fun onFinish() {
                onGameFinish()
            }
        }
        timer.start()
        Log.i("GameViewModel", "GameViewModel criado!")
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }

    /** Methods for buttons presses **/
    fun onSkip() {
        _score.value = score.value?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value = score.value?.plus(1)
        _buzzEvent.value = BuzzType.CORRECT
        nextWord()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        if (wordList.isNotEmpty()) {
            //Select and remove a word from the list
            _word.value = wordList.removeAt(0)
        } else resetList()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        Log.i("GameViewModel", "GameViewModel destruido!")
    }

    fun onGameFinish() {
        _eventGameFinish.value = true
        _buzzEvent.value = BuzzType.GAME_OVER
    }

    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }
    fun onBuzzCompleted() {
        _buzzEvent.value = BuzzType.NO_BUZZ
    }
    fun onBuzzPanic() {
        _buzzEvent.value = BuzzType.COUNTDOWN_PANIC
    }
}