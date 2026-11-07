package com.example.game.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.ArcadeSoundSynth
import com.example.game.engine.GameEngine
import com.example.game.model.UpgradeItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenState {
    TITLE,
    PLAYING,
    STAGE_CLEAR,
    GAME_OVER,
    VICTORY_SCREEN,
    ARMORY,
    ARCHIVE
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("spawn_game_prefs", Context.MODE_PRIVATE)
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val soundSynth = ArcadeSoundSynth()

    val engine = GameEngine(
        soundSynth = soundSynth,
        triggerHaptic = { intensity -> triggerVibration(intensity) }
    )

    private val _screenState = MutableStateFlow(ScreenState.TITLE)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _highScore = MutableStateFlow(prefs.getInt("high_score", 0))
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private val _maxStageUnlocked = MutableStateFlow(prefs.getInt("max_stage", 1))
    val maxStageUnlocked: StateFlow<Int> = _maxStageUnlocked.asStateFlow()

    private val _crtScanlines = MutableStateFlow(prefs.getBoolean("crt_scanlines", true))
    val crtScanlines: StateFlow<Boolean> = _crtScanlines.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean("haptics_enabled", true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    // Upgrades
    private val _upgrades = MutableStateFlow(loadUpgrades())
    val upgrades: StateFlow<List<UpgradeItem>> = _upgrades.asStateFlow()

    // Game loop ticker
    private val _tick = MutableStateFlow(0L)
    val tick: StateFlow<Long> = _tick.asStateFlow()

    private var gameLoopJob: Job? = null

    init {
        soundSynth.isEnabled = _soundEnabled.value
        loadSavedPlayerStats()
    }

    private fun loadUpgrades(): List<UpgradeItem> {
        return listOf(
            UpgradeItem(
                id = "gauntlets",
                title = "Necro Gauntlets",
                description = "Sharpens spiked claws and increases light attack damage.",
                baseCost = 25,
                currentLevel = prefs.getInt("upgrade_gauntlets", 1),
                iconName = "fist"
            ),
            UpgradeItem(
                id = "chains",
                title = "Symbiote Chains",
                description = "Lengthens heavy chain whip reach and increases armor cleaving.",
                baseCost = 35,
                currentLevel = prefs.getInt("upgrade_chains", 1),
                iconName = "link"
            ),
            UpgradeItem(
                id = "cape",
                title = "Shadow Shroud",
                description = "Fortifies cape block defense and deflects projectiles with extra force.",
                baseCost = 30,
                currentLevel = prefs.getInt("upgrade_cape", 1),
                iconName = "shield"
            ),
            UpgradeItem(
                id = "reservoir",
                title = "Necro Reservoir",
                description = "Accelerates necro energy regeneration and reduces power costs.",
                baseCost = 45,
                currentLevel = prefs.getInt("upgrade_reservoir", 1),
                iconName = "bolt"
            ),
            UpgradeItem(
                id = "nova",
                title = "Armageddon Nova",
                description = "Expands screen wipe detonation radius and incinerates tough demons.",
                baseCost = 60,
                currentLevel = prefs.getInt("upgrade_nova", 1),
                iconName = "flare"
            )
        )
    }

    private fun loadSavedPlayerStats() {
        engine.player.necroSouls = prefs.getInt("necro_souls", 50)
        applyUpgradeLevelsToPlayer()
    }

    private fun applyUpgradeLevelsToPlayer() {
        for (item in _upgrades.value) {
            when (item.id) {
                "gauntlets" -> engine.player.gauntletLevel = item.currentLevel
                "chains" -> engine.player.chainLevel = item.currentLevel
                "cape" -> engine.player.capeLevel = item.currentLevel
                "reservoir" -> engine.player.reservoirLevel = item.currentLevel
                "nova" -> engine.player.novaLevel = item.currentLevel
            }
        }
    }

    fun purchaseUpgrade(id: String) {
        val list = _upgrades.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = list[index]
            if (!item.isMaxed && engine.player.necroSouls >= item.nextCost) {
                engine.player.necroSouls -= item.nextCost
                val updated = item.copy(currentLevel = item.currentLevel + 1)
                list[index] = updated
                _upgrades.value = list
                prefs.edit()
                    .putInt("upgrade_$id", updated.currentLevel)
                    .putInt("necro_souls", engine.player.necroSouls)
                    .apply()
                applyUpgradeLevelsToPlayer()
                soundSynth.playVictory()
                triggerVibration(2)
            }
        }
    }

    fun startCampaign(stageNumber: Int = 1) {
        engine.startStage(stageNumber, endless = false)
        _screenState.value = ScreenState.PLAYING
        startGameLoop()
    }

    fun startEndlessMode() {
        engine.startStage(1, endless = true)
        _screenState.value = ScreenState.PLAYING
        startGameLoop()
    }

    fun setScreenState(state: ScreenState) {
        _screenState.value = state
        soundSynth.playMenuClick()
        if (state != ScreenState.PLAYING) {
            stopGameLoop()
        }
    }

    fun restartCurrentStage() {
        if (engine.isEndlessMode) {
            startEndlessMode()
        } else {
            startCampaign(engine.currentStageIndex)
        }
    }

    fun proceedToNextStage() {
        val next = engine.currentStageIndex + 1
        if (next > _maxStageUnlocked.value) {
            _maxStageUnlocked.value = next
            prefs.edit().putInt("max_stage", next).apply()
        }
        startCampaign(next)
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val currentTime = System.nanoTime()
                val dt = ((currentTime - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastTime = currentTime

                engine.update(dt)

                // Check game events
                if (engine.isGameOver && _screenState.value == ScreenState.PLAYING) {
                    saveStats()
                    _screenState.value = ScreenState.GAME_OVER
                } else if (engine.isStageComplete && _screenState.value == ScreenState.PLAYING) {
                    saveStats()
                    _screenState.value = ScreenState.STAGE_CLEAR
                } else if (engine.isVictory && _screenState.value == ScreenState.PLAYING) {
                    saveStats()
                    _screenState.value = ScreenState.VICTORY_SCREEN
                }

                _tick.value = currentTime
                delay(16) // ~60 FPS
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun saveStats() {
        if (engine.score > _highScore.value) {
            _highScore.value = engine.score
            prefs.edit().putInt("high_score", engine.score).apply()
        }
        prefs.edit().putInt("necro_souls", engine.player.necroSouls).apply()
    }

    fun toggleCrtScanlines() {
        val newCrt = !_crtScanlines.value
        _crtScanlines.value = newCrt
        prefs.edit().putBoolean("crt_scanlines", newCrt).apply()
        soundSynth.playMenuClick()
    }

    fun toggleSound() {
        val newSound = !_soundEnabled.value
        _soundEnabled.value = newSound
        soundSynth.isEnabled = newSound
        prefs.edit().putBoolean("sound_enabled", newSound).apply()
        if (newSound) soundSynth.playMenuClick()
    }

    fun toggleHaptics() {
        val newHaptic = !_hapticsEnabled.value
        _hapticsEnabled.value = newHaptic
        prefs.edit().putBoolean("haptics_enabled", newHaptic).apply()
        if (newHaptic) triggerVibration(1)
    }

    private fun triggerVibration(intensity: Int) {
        if (!_hapticsEnabled.value || vibrator == null) return
        try {
            val duration = when (intensity) {
                1 -> 25L
                2 -> 65L
                else -> 140L
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = when (intensity) {
                    1 -> 80
                    2 -> 160
                    else -> 255
                }
                vibrator.vibrate(VibrationEffect.createOneShot(duration, amp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}
