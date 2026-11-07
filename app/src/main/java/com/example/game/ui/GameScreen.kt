package com.example.game.ui

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.viewmodel.GameViewModel
import com.example.game.viewmodel.ScreenState
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CapeCrimson
import com.example.ui.theme.CapeRed
import com.example.ui.theme.CapeRedDark
import com.example.ui.theme.ChainSilver
import com.example.ui.theme.ChainSteel
import com.example.ui.theme.CryptStone
import com.example.ui.theme.CryptSurface
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.HellfireOrange
import com.example.ui.theme.NecroGreen
import com.example.ui.theme.NecroGreenBright
import com.example.ui.theme.ViolatorGold
import com.example.ui.theme.VoidBlack

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val engine = viewModel.engine
    // Trigger recomposition on each physics tick
    val tick by viewModel.tick.collectAsState()
    val crtScanlines by viewModel.crtScanlines.collectAsState()
    val screenState by viewModel.screenState.collectAsState()

    var isPaused by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(VoidBlack)) {
        // Game Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    engine.screenWidth = size.width.toFloat()
                    engine.screenHeight = size.height.toFloat()
                    engine.groundY = size.height.toFloat() * 0.72f
                }
        ) {
            // Read tick to guarantee recomposition
            @Suppress("UNUSED_VARIABLE")
            val currentTick = tick
            GameRenderer.drawWorld(this, engine, crtScanlines)
        }

        // Top HUD
        GameHud(
            viewModel = viewModel,
            onPauseClick = { isPaused = true },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )

        // Combo Announcement Banner
        if (engine.player.comboCount >= 2) {
            ComboBadge(
                comboCount = engine.player.comboCount,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp)
            )
        }

        // Bottom Arcade Controls
        ArcadeControls(
            onMove = { dir -> engine.movePlayer(dir) },
            onJump = { engine.jumpPlayer() },
            onLightAttack = { engine.executeLightAttack() },
            onChainWhip = { engine.executeChainWhip() },
            onCapeBlock = { isBlocking -> engine.setCapeBlock(isBlocking) },
            onNecroBlast = { engine.executeNecroBlast() },
            onHellNova = { engine.executeHellNova() },
            canAffordBlast = engine.player.necroEnergy >= engine.player.blastCost,
            canAffordNova = engine.player.necroEnergy >= engine.player.novaCost,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )

        // Pause Dialog
        if (isPaused) {
            PauseMenuDialog(
                viewModel = viewModel,
                onResume = { isPaused = false }
            )
        }

        // Stage Clear Overlay
        if (screenState == ScreenState.STAGE_CLEAR) {
            StageClearDialog(viewModel = viewModel)
        }

        // Game Over Overlay
        if (screenState == ScreenState.GAME_OVER) {
            GameOverDialog(viewModel = viewModel)
        }

        // Victory Overlay
        if (screenState == ScreenState.VICTORY_SCREEN) {
            VictoryDialog(viewModel = viewModel)
        }
    }
}

@Composable
private fun GameHud(
    viewModel: GameViewModel,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = viewModel.engine
    val player = engine.player
    val stage = engine.getCurrentStage()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left: Spawn Health Bar
        Column(modifier = Modifier.width(140.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SPAWN HP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = CapeRed
                )
                Text(
                    text = "${player.currentHp.toInt()}/${player.maxHp.toInt()}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BoneWhite
                )
            }
            LinearProgressIndicator(
                progress = { (player.currentHp / player.maxHp).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .shadow(4.dp, RoundedCornerShape(4.dp)),
                color = if (player.currentHp > 30f) NecroGreenBright else CapeRed,
                trackColor = CryptStone
            )
            // Souls small badge
            Text(
                text = "SOULS: ${player.necroSouls}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NecroGreen,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Center: Classic 9999 Necro-Meter
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = CryptStone,
            border = BorderStroke(1.dp, NecroGreen.copy(alpha = 0.8f)),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NECRO-METER",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChainSilver,
                    letterSpacing = 1.sp
                )
                Text(
                    text = String.format("%04d", player.necroEnergy.toInt()),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (player.necroEnergy > 1000f) NecroGreenBright else CapeRed,
                    letterSpacing = 2.sp
                )
            }
        }

        // Right: Score, Wave & Pause
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SCORE: ${engine.score}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = ViolatorGold
                )
                Text(
                    text = if (engine.isEndlessMode) "WAVE ${engine.currentWave}" else "STG ${engine.currentStageIndex} • W${engine.currentWave}/${stage.totalWaves}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BoneWhite
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(CryptStone, CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = BoneWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ComboBadge(
    comboCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "comboScale")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(220),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val label = when {
        comboCount >= 20 -> "HELLBOUND!"
        comboCount >= 15 -> "NECRO-SLAG!"
        comboCount >= 10 -> "UNHOLY!"
        comboCount >= 5 -> "BRUTAL!"
        else -> "HIT COMBO!"
    }

    val badgeColor = when {
        comboCount >= 15 -> NecroGreenBright
        comboCount >= 10 -> CapeRed
        comboCount >= 5 -> ViolatorGold
        else -> BoneWhite
    }

    Surface(
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = CryptStone.copy(alpha = 0.9f),
        border = BorderStroke(2.dp, badgeColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${comboCount}x ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = badgeColor
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = BoneWhite,
                letterSpacing = 1.sp
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ArcadeControls(
    onMove: (Float) -> Unit,
    onJump: () -> Unit,
    onLightAttack: () -> Unit,
    onChainWhip: () -> Unit,
    onCapeBlock: (Boolean) -> Unit,
    onNecroBlast: () -> Unit,
    onHellNova: () -> Unit,
    canAffordBlast: Boolean,
    canAffordNova: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // D-Pad Directional Controls (Left Side)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Left Button
            ArcadeHoldButton(
                icon = Icons.Default.ChevronLeft,
                testTag = "btn_move_left",
                onPress = { onMove(-1f) },
                onRelease = { onMove(0f) }
            )

            // Right Button
            ArcadeHoldButton(
                icon = Icons.Default.ChevronRight,
                testTag = "btn_move_right",
                onPress = { onMove(1f) },
                onRelease = { onMove(0f) }
            )

            // Jump / Glide Button
            ArcadeTapButton(
                icon = Icons.Default.KeyboardArrowUp,
                label = "JUMP",
                color = ChainSteel,
                testTag = "btn_jump",
                onClick = onJump
            )
        }

        // Action Combat Buttons (Right Side)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top action row: Cape Shield & Necro Blast & Hell Nova
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Cape Shield Block (Hold to maintain block)
                ArcadeHoldActionButton(
                    icon = Icons.Default.Shield,
                    label = "BLOCK",
                    activeColor = CapeCrimson,
                    testTag = "btn_block",
                    onPress = { onCapeBlock(true) },
                    onRelease = { onCapeBlock(false) }
                )

                // Necro Blast Fireball
                ArcadeTapButton(
                    icon = Icons.Default.FlashOn,
                    label = "BLAST",
                    color = if (canAffordBlast) NecroGreenBright else CryptStone,
                    enabled = canAffordBlast,
                    testTag = "btn_blast",
                    onClick = onNecroBlast
                )

                // Hell Nova (Ultimate)
                val novaAnim = rememberInfiniteTransition(label = "novaPulse")
                val novaPulse by novaAnim.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
                    label = "pulse"
                )

                Surface(
                    modifier = Modifier
                        .scale(if (canAffordNova) novaPulse else 1f)
                        .size(54.dp),
                    shape = CircleShape,
                    color = if (canAffordNova) CapeRed else CryptStone,
                    border = BorderStroke(2.dp, if (canAffordNova) NecroGreenBright else ChainSteel.copy(alpha = 0.3f))
                ) {
                    IconButton(
                        onClick = onHellNova,
                        enabled = canAffordNova,
                        modifier = Modifier.fillMaxSize().testTag("btn_hell_nova")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Hell Nova",
                                tint = if (canAffordNova) BoneWhite else ChainSilver.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "NOVA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (canAffordNova) NecroGreenBright else ChainSilver.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // Bottom action row: Light Attack & Chain Whip
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Light Claw/Punch
                ArcadeTapButton(
                    icon = Icons.Default.PanTool,
                    label = "LIGHT",
                    color = CapeRed,
                    size = 56,
                    testTag = "btn_light_attack",
                    onClick = onLightAttack
                )

                // Heavy Chain Whip
                ArcadeTapButton(
                    icon = Icons.Default.Link,
                    label = "CHAIN",
                    color = ViolatorGold,
                    size = 56,
                    testTag = "btn_chain_whip",
                    onClick = onChainWhip
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ArcadeHoldButton(
    icon: ImageVector,
    testTag: String,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        shape = CircleShape,
        color = if (isPressed) CapeRedDark else CryptStone,
        border = BorderStroke(1.5.dp, if (isPressed) NecroGreenBright else ChainSteel),
        modifier = Modifier
            .size(52.dp)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onPress()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onRelease()
                        true
                    }
                    else -> false
                }
            }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPressed) BoneWhite else ChainSilver,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ArcadeHoldActionButton(
    icon: ImageVector,
    label: String,
    activeColor: Color,
    testTag: String,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        shape = CircleShape,
        color = if (isPressed) activeColor else CryptStone,
        border = BorderStroke(1.5.dp, if (isPressed) NecroGreenBright else ChainSteel),
        modifier = Modifier
            .size(50.dp)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onPress()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onRelease()
                        true
                    }
                    else -> false
                }
            }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPressed) BoneWhite else ChainSilver,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = if (isPressed) BoneWhite else ChainSilver
            )
        }
    }
}

@Composable
private fun ArcadeTapButton(
    icon: ImageVector,
    label: String,
    color: Color,
    size: Int = 50,
    enabled: Boolean = true,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (enabled) CryptStone else VoidBlack,
        border = BorderStroke(1.5.dp, if (enabled) color else ChainSteel.copy(alpha = 0.2f)),
        modifier = Modifier.size(size.dp)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxSize().testTag(testTag)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled) color else ChainSteel.copy(alpha = 0.3f),
                    modifier = Modifier.size(if (size > 50) 22.dp else 18.dp)
                )
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = if (enabled) BoneWhite else ChainSteel.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun PauseMenuDialog(
    viewModel: GameViewModel,
    onResume: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CryptStone,
            border = BorderStroke(2.dp, CapeRedDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME PAUSED",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = BoneWhite,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().testTag("resume_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CapeRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("RESUME BATTLE", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onResume()
                        viewModel.restartCurrentStage()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("restart_stage_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CryptSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("RESTART STAGE", color = BoneWhite, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onResume()
                        viewModel.setScreenState(ScreenState.TITLE)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("quit_to_title_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("RETURN TO TITLE", color = ChainSilver, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StageClearDialog(viewModel: GameViewModel) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CryptStone,
            border = BorderStroke(2.dp, NecroGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "STAGE CLEARED!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = NecroGreenBright,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "THE DAMNED HAVE FALLEN",
                    fontSize = 11.sp,
                    color = ChainSilver,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CryptSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL SCORE:", color = ChainSilver, fontSize = 13.sp)
                            Text("${viewModel.engine.score}", color = ViolatorGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SOULS HARVESTED:", color = ChainSilver, fontSize = 13.sp)
                            Text("${viewModel.engine.player.necroSouls}", color = NecroGreenBright, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("HIGHEST COMBO:", color = ChainSilver, fontSize = 13.sp)
                            Text("${viewModel.engine.player.maxCombo}x", color = CapeRed, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.proceedToNextStage() },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("next_stage_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CapeRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("NEXT STAGE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.setScreenState(ScreenState.ARMORY) },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("clear_to_armory_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CryptSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("VISIT ARMORY", color = ViolatorGold, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GameOverDialog(viewModel: GameViewModel) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CryptStone,
            border = BorderStroke(2.dp, CapeRedDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FALLEN",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = CapeRed,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "HELL RECLAIMS ITS SOLDIER",
                    fontSize = 11.sp,
                    color = ChainSilver,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "FINAL SCORE: ${viewModel.engine.score}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = ViolatorGold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.restartCurrentStage() },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("retry_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CapeRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("RETRY BATTLE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.setScreenState(ScreenState.TITLE) },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("game_over_title_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CryptSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("RETURN TO TITLE", color = ChainSilver, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VictoryDialog(viewModel: GameViewModel) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CryptStone,
            border = BorderStroke(2.dp, ViolatorGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VICTORY!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = ViolatorGold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "THE VIOLATOR DEFEATED • MALEBOLGIA REPELLED",
                    fontSize = 10.sp,
                    color = NecroGreenBright,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FINAL CAMPAIGN SCORE",
                    fontSize = 11.sp,
                    color = ChainSilver
                )
                Text(
                    text = "${viewModel.engine.score}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = BoneWhite
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.startEndlessMode() },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("victory_endless_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CapeRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("PLAY ENDLESS HORDE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.setScreenState(ScreenState.TITLE) },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("victory_title_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CryptSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("MAIN MENU", color = ChainSilver, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
