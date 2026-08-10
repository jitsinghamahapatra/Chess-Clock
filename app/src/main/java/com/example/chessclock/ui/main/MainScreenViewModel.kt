package com.example.chessclock.ui.main

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Player {
    NONE, WHITE, BLACK
}

enum class GameState {
    NOT_STARTED, RUNNING, PAUSED, FINISHED
}

data class ChessClockUiState(
    val remainingTimeWhiteMs: Long = 300000L,
    val remainingTimeBlackMs: Long = 300000L,
    val incrementMs: Long = 0L,
    val initialTimeMs: Long = 300000L,
    val activePlayer: Player = Player.NONE,
    val gameState: GameState = GameState.NOT_STARTED,
    val winner: Player = Player.NONE,
    val moveCountWhite: Int = 0,
    val moveCountBlack: Int = 0,
    val soundEnabled: Boolean = true
)

class MainScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChessClockUiState())
    val uiState: StateFlow<ChessClockUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var lastTickTime: Long = 0L
    private var baseRemainingTimeMs: Long = 0L

    private val toneGenerator = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    } catch (e: Exception) {
        null
    }

    fun playClick() {
        if (_uiState.value.soundEnabled) {
            viewModelScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 65)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    fun playTimeout() {
        if (_uiState.value.soundEnabled) {
            viewModelScope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 700)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    fun selectPreset(timeMs: Long, incrementMs: Long) {
        stopTimer()
        _uiState.update {
            it.copy(
                initialTimeMs = timeMs,
                remainingTimeWhiteMs = timeMs,
                remainingTimeBlackMs = timeMs,
                incrementMs = incrementMs,
                activePlayer = Player.NONE,
                gameState = GameState.NOT_STARTED,
                winner = Player.NONE,
                moveCountWhite = 0,
                moveCountBlack = 0
            )
        }
    }

    fun setCustomTime(minutes: Int, seconds: Int, incrementSeconds: Int) {
        val totalMs = (minutes * 60 + seconds) * 1000L
        val incMs = incrementSeconds * 1000L
        selectPreset(totalMs, incMs)
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun onClockTapped(player: Player) {
        val state = _uiState.value
        if (state.gameState == GameState.FINISHED) return

        // If not started yet, tapping a clock starts the other player's clock
        if (state.gameState == GameState.NOT_STARTED) {
            playClick()
            val nextPlayer = if (player == Player.WHITE) Player.BLACK else Player.WHITE
            startClockForPlayer(nextPlayer, state.remainingTimeWhiteMs, state.remainingTimeBlackMs)
            return
        }

        if (state.gameState == GameState.PAUSED) {
            return
        }

        // Standard turn switch
        if (player == state.activePlayer) {
            playClick()
            // Add increment to the player who just finished their turn
            val updatedWhiteMs = if (player == Player.WHITE) {
                state.remainingTimeWhiteMs + state.incrementMs
            } else {
                state.remainingTimeWhiteMs
            }
            val updatedBlackMs = if (player == Player.BLACK) {
                state.remainingTimeBlackMs + state.incrementMs
            } else {
                state.remainingTimeBlackMs
            }

            val nextPlayer = if (player == Player.WHITE) Player.BLACK else Player.WHITE
            val newMoveCountWhite = if (player == Player.WHITE) state.moveCountWhite + 1 else state.moveCountWhite
            val newMoveCountBlack = if (player == Player.BLACK) state.moveCountBlack + 1 else state.moveCountBlack

            _uiState.update {
                it.copy(
                    remainingTimeWhiteMs = updatedWhiteMs,
                    remainingTimeBlackMs = updatedBlackMs,
                    moveCountWhite = newMoveCountWhite,
                    moveCountBlack = newMoveCountBlack
                )
            }
            startClockForPlayer(nextPlayer, updatedWhiteMs, updatedBlackMs)
        }
    }

    fun togglePlayPause() {
        val state = _uiState.value
        if (state.gameState == GameState.FINISHED) return

        if (state.gameState == GameState.RUNNING) {
            playClick()
            stopTimer()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        } else if (state.gameState == GameState.PAUSED || state.gameState == GameState.NOT_STARTED) {
            playClick()
            val active = if (state.activePlayer == Player.NONE) Player.WHITE else state.activePlayer
            startClockForPlayer(active, state.remainingTimeWhiteMs, state.remainingTimeBlackMs)
        }
    }

    fun resetGame() {
        playClick()
        stopTimer()
        val state = _uiState.value
        _uiState.update {
            it.copy(
                remainingTimeWhiteMs = state.initialTimeMs,
                remainingTimeBlackMs = state.initialTimeMs,
                activePlayer = Player.NONE,
                gameState = GameState.NOT_STARTED,
                winner = Player.NONE,
                moveCountWhite = 0,
                moveCountBlack = 0
            )
        }
    }

    private fun startClockForPlayer(player: Player, whiteMs: Long, blackMs: Long) {
        stopTimer()
        lastTickTime = SystemClock.elapsedRealtime()
        baseRemainingTimeMs = if (player == Player.WHITE) whiteMs else blackMs

        _uiState.update {
            it.copy(
                activePlayer = player,
                gameState = GameState.RUNNING
            )
        }

        timerJob = viewModelScope.launch {
            while (true) {
                delay(16) // ~60fps updates for smooth millisecond countdown
                val now = SystemClock.elapsedRealtime()
                val elapsed = now - lastTickTime
                val newRemaining = baseRemainingTimeMs - elapsed

                if (newRemaining <= 0) {
                    playTimeout()
                    val winner = if (player == Player.WHITE) Player.BLACK else Player.WHITE
                    _uiState.update {
                        if (player == Player.WHITE) {
                            it.copy(
                                remainingTimeWhiteMs = 0L,
                                gameState = GameState.FINISHED,
                                winner = winner
                            )
                        } else {
                            it.copy(
                                remainingTimeBlackMs = 0L,
                                gameState = GameState.FINISHED,
                                winner = winner
                            )
                        }
                    }
                    break
                } else {
                    _uiState.update {
                        if (player == Player.WHITE) {
                            it.copy(remainingTimeWhiteMs = newRemaining)
                        } else {
                            it.copy(remainingTimeBlackMs = newRemaining)
                        }
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            // ignore
        }
    }
}
