package com.example.chessclock.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.chessclock.theme.ChessClockTheme

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Black Player Clock Area (Top half, rotated 180 degrees)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .rotate(180f)
            ) {
                PlayerClockArea(
                    player = Player.BLACK,
                    remainingTimeMs = uiState.remainingTimeBlackMs,
                    isActive = uiState.activePlayer == Player.BLACK && uiState.gameState == GameState.RUNNING,
                    isTimeUp = uiState.remainingTimeBlackMs <= 0 && uiState.gameState == GameState.FINISHED,
                    moveCount = uiState.moveCountBlack,
                    incrementSec = uiState.incrementMs / 1000,
                    onClick = { viewModel.onClockTapped(Player.BLACK) }
                )
            }

            // Central Divider Control Bar
            ControlBar(
                gameState = uiState.gameState,
                soundEnabled = uiState.soundEnabled,
                onPlayPause = { viewModel.togglePlayPause() },
                onReset = {
                    if (uiState.gameState == GameState.RUNNING || uiState.gameState == GameState.PAUSED) {
                        showResetConfirmDialog = true
                    } else {
                        viewModel.resetGame()
                    }
                },
                onSettings = {
                    if (uiState.gameState == GameState.RUNNING) {
                        viewModel.togglePlayPause() // Auto-pause if running
                    }
                    showSettingsDialog = true
                },
                onToggleSound = { viewModel.toggleSound() }
            )

            // White Player Clock Area (Bottom half, normal orientation)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                PlayerClockArea(
                    player = Player.WHITE,
                    remainingTimeMs = uiState.remainingTimeWhiteMs,
                    isActive = uiState.activePlayer == Player.WHITE && uiState.gameState == GameState.RUNNING,
                    isTimeUp = uiState.remainingTimeWhiteMs <= 0 && uiState.gameState == GameState.FINISHED,
                    moveCount = uiState.moveCountWhite,
                    incrementSec = uiState.incrementMs / 1000,
                    onClick = { viewModel.onClockTapped(Player.WHITE) }
                )
            }
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                currentInitialTimeMs = uiState.initialTimeMs,
                currentIncrementMs = uiState.incrementMs,
                onDismiss = { showSettingsDialog = false },
                onSave = { minutes, seconds, incrementSeconds ->
                    viewModel.setCustomTime(minutes, seconds, incrementSeconds)
                    showSettingsDialog = false
                },
                onPresetSelected = { timeMs, incMs ->
                    viewModel.selectPreset(timeMs, incMs)
                    showSettingsDialog = false
                }
            )
        }

        // Reset Confirmation Dialog
        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = { Text(text = "Reset Game?") },
                text = { Text(text = "Are you sure you want to reset the clocks? The current game state will be lost.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetGame()
                            showResetConfirmDialog = false
                        }
                    ) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PlayerClockArea(
    player: Player,
    remainingTimeMs: Long,
    isActive: Boolean,
    isTimeUp: Boolean,
    moveCount: Int,
    incrementSec: Long,
    onClick: () -> Unit
) {
    val isLightSide = player == Player.WHITE

    // Premium Color System based on state and light/dark side
    val backgroundColor = when {
        isTimeUp -> Color(0xFFD32F2F) // Clear, dramatic red indicating lost on time
        isActive -> if (isLightSide) Color(0xFFFFFFFF) else Color(0xFF121212)
        else -> if (isLightSide) Color(0xFFE5E5E5) else Color(0xFF242424)
    }

    val textColor = when {
        isTimeUp -> Color(0xFFFFFFFF)
        isActive -> if (isLightSide) Color(0xFF111111) else Color(0xFFFFFFFF)
        else -> if (isLightSide) Color(0xFF757575) else Color(0xFF8E8E8E)
    }

    val infoColor = when {
        isTimeUp -> Color(0xCCEAEAEA)
        isActive -> if (isLightSide) Color(0xFF666666) else Color(0xCCECECEC)
        else -> if (isLightSide) Color(0xFF888888) else Color(0x88FFFFFF)
    }

    val formattedTime = formatTime(remainingTimeMs)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Disable standard slow ripple for ultra-responsive click feel
                onClick = onClick
            )
            .padding(24.dp)
    ) {
        // Active border glow indicator
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(if (isLightSide) Color(0xFF1E88E5) else Color(0xFFBB86FC))
                    .align(Alignment.BottomCenter)
            )
        }

        // Center Countdown Display
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formattedTime,
                color = textColor,
                fontSize = 84.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace // Monospace keeps numerals perfectly aligned during fast updates
            )

            if (isTimeUp) {
                Text(
                    text = "TIME UP!",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        // Bottom Left/Right details (Move Count & Increment info)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MOVES: $moveCount",
                color = infoColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            if (incrementSec > 0) {
                Text(
                    text = "+${incrementSec}s",
                    color = infoColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ControlBar(
    gameState: GameState,
    soundEnabled: Boolean,
    onPlayPause: () -> Unit,
    onReset: () -> Unit,
    onSettings: () -> Unit,
    onToggleSound: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF1E1E1E)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset Button
        IconButton(
            onClick = onReset,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Clock",
                modifier = Modifier.size(28.dp)
            )
        }

        // Play / Pause Button
        IconButton(
            onClick = onPlayPause,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White,
                containerColor = if (gameState == GameState.RUNNING) Color(0x33FFFFFF) else Color(0xFF3388FF)
            ),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (gameState == GameState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (gameState == GameState.RUNNING) "Pause" else "Play",
                modifier = Modifier.size(32.dp)
            )
        }

        // Sound Toggle Button
        IconButton(
            onClick = onToggleSound,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
        ) {
            Icon(
                imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                contentDescription = "Toggle Sound",
                modifier = Modifier.size(28.dp)
            )
        }

        // Settings Button
        IconButton(
            onClick = onSettings,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun SettingsDialog(
    currentInitialTimeMs: Long,
    currentIncrementMs: Long,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int) -> Unit,
    onPresetSelected: (Long, Long) -> Unit
) {
    // Determine initial fields based on existing duration
    val initialTotalSeconds = currentInitialTimeMs / 1000
    
    // Manage input strings locally to support direct typing
    var minutesText by remember { mutableStateOf((initialTotalSeconds / 60).toString()) }
    var secondsText by remember { mutableStateOf((initialTotalSeconds % 60).toString()) }
    var incrementText by remember { mutableStateOf((currentIncrementMs / 1000).toString()) }

    fun parseAndClamp(text: String, min: Int, max: Int, default: Int): Int {
        if (text.isEmpty()) return min
        val parsed = text.toIntOrNull() ?: default
        return parsed.coerceIn(min, max)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TIME CONTROL",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Presets Grid
                Text(
                    text = "Presets",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton("1+0", 60000L, 0L, onPresetSelected, Modifier.weight(1f))
                    PresetButton("3+0", 180000L, 0L, onPresetSelected, Modifier.weight(1f))
                    PresetButton("5+0", 300000L, 0L, onPresetSelected, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton("5+3", 300000L, 3000L, onPresetSelected, Modifier.weight(1f))
                    PresetButton("10+0", 600000L, 0L, onPresetSelected, Modifier.weight(1f))
                    PresetButton("30+0", 1800000L, 0L, onPresetSelected, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(color = Color(0xFF3F3F3F))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Custom",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Minutes selector
                CustomTimeRow(
                    label = "Minutes",
                    value = minutesText,
                    onValueChange = { minutesText = it },
                    onIncrement = {
                        val current = minutesText.toIntOrNull() ?: 0
                        minutesText = (current + 1).coerceAtMost(180).toString()
                    },
                    onDecrement = {
                        val current = minutesText.toIntOrNull() ?: 0
                        minutesText = (current - 1).coerceAtLeast(0).toString()
                    },
                    decrementEnabled = (minutesText.toIntOrNull() ?: 0) > 0,
                    incrementEnabled = (minutesText.toIntOrNull() ?: 0) < 180
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Seconds selector
                CustomTimeRow(
                    label = "Seconds",
                    value = secondsText,
                    onValueChange = { secondsText = it },
                    onIncrement = {
                        val current = secondsText.toIntOrNull() ?: 0
                        secondsText = (current + 1).coerceAtMost(59).toString()
                    },
                    onDecrement = {
                        val current = secondsText.toIntOrNull() ?: 0
                        secondsText = (current - 1).coerceAtLeast(0).toString()
                    },
                    decrementEnabled = (secondsText.toIntOrNull() ?: 0) > 0,
                    incrementEnabled = (secondsText.toIntOrNull() ?: 0) < 59
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Increment selector
                CustomTimeRow(
                    label = "Increment (s)",
                    value = incrementText,
                    onValueChange = { incrementText = it },
                    onIncrement = {
                        val current = incrementText.toIntOrNull() ?: 0
                        incrementText = (current + 1).coerceAtMost(60).toString()
                    },
                    onDecrement = {
                        val current = incrementText.toIntOrNull() ?: 0
                        incrementText = (current - 1).coerceAtLeast(0).toString()
                    },
                    decrementEnabled = (incrementText.toIntOrNull() ?: 0) > 0,
                    incrementEnabled = (incrementText.toIntOrNull() ?: 0) < 60
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save / Cancel Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val mins = parseAndClamp(minutesText, 0, 180, 5)
                            val secs = parseAndClamp(secondsText, 0, 59, 0)
                            val inc = parseAndClamp(incrementText, 0, 60, 0)
                            
                            // Ensure total initial time is at least 1 second
                            if (mins == 0 && secs == 0) {
                                onSave(0, 1, inc)
                            } else {
                                onSave(mins, secs, inc)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3388FF))
                    ) {
                        Text("Apply", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PresetButton(
    label: String,
    timeMs: Long,
    incMs: Long,
    onClick: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onClick(timeMs, incMs) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3E3E)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        modifier = modifier
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CustomTimeRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    decrementEnabled: Boolean,
    incrementEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 14.sp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                enabled = decrementEnabled,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color.DarkGray
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }

            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        onValueChange(newValue)
                    }
                },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier
                    .width(80.dp)
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3388FF),
                    unfocusedBorderColor = Color(0xFF3F3F3F),
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                )
            )

            IconButton(
                onClick = onIncrement,
                enabled = incrementEnabled,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}

fun formatTime(ms: Long): String {
    if (ms <= 0) return "0.0"

    // If more than 10 seconds, show MM:SS format (rounding up seconds to match chess standards)
    return if (ms > 10000L) {
        val totalSeconds = (ms + 999) / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    } else {
        // Less than or equal to 10s: show seconds and tenths for high-stakes feel
        val secs = ms / 1000
        val tenths = (ms % 1000) / 100
        String.format("%d.%d", secs, tenths)
    }
}
