package com.example.game.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.viewmodel.GameViewModel
import com.example.game.viewmodel.ScreenState
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CapeCrimson
import com.example.ui.theme.CapeRed
import com.example.ui.theme.CapeRedDark
import com.example.ui.theme.ChainSilver
import com.example.ui.theme.CryptStone
import com.example.ui.theme.CryptSurface
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.NecroGreen
import com.example.ui.theme.NecroGreenBright
import com.example.ui.theme.ViolatorGold
import com.example.ui.theme.VoidBlack

@Composable
fun TitleScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showStageSelectDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(VoidBlack, DeepCharcoal, Color(0xFF1B0C12))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Settings & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Score Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CryptSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CapeRedDark)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HIGH SCORE: ",
                            color = ChainSilver,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${viewModel.highScore.value}",
                            color = NecroGreenBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Souls Counter & Settings Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CryptSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NecroGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SOULS: ",
                                color = ChainSilver,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${viewModel.engine.player.necroSouls}",
                                color = NecroGreenBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = BoneWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game Art Card with PS1 Box Art
            Card(
                modifier = Modifier
                    .size(240.dp)
                    .shadow(16.dp, RoundedCornerShape(12.dp))
                    .border(2.dp, CapeRed.copy(alpha = glowAlpha), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CryptStone)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.spawn_cover),
                        contentDescription = "Spawn: The Eternal Game Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay at bottom of card
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, VoidBlack.copy(alpha = 0.7f))
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SPAWN",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    color = BoneWhite,
                    letterSpacing = 4.sp,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "THE ETERNAL",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CapeRed,
                    letterSpacing = 6.sp
                )
                Text(
                    text = "DARK REALM ARCADE BATTLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = NecroGreenBright.copy(alpha = glowAlpha),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play Campaign
                Button(
                    onClick = {
                        if (viewModel.maxStageUnlocked.value > 1) {
                            showStageSelectDialog = true
                        } else {
                            viewModel.startCampaign(1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_campaign_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CapeRed,
                        contentColor = BoneWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START CAMPAIGN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Hell Horde (Endless Mode)
                OutlinedButton(
                    onClick = { viewModel.startEndlessMode() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("hell_horde_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NecroGreenBright
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NecroGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NecroGreenBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HELL HORDE (ENDLESS)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Armory
                    OutlinedButton(
                        onClick = { viewModel.setScreenState(ScreenState.ARMORY) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("armory_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ViolatorGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ViolatorGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "ARMORY", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Archive Lore
                    OutlinedButton(
                        onClick = { viewModel.setScreenState(ScreenState.ARCHIVE) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("archive_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ChainSilver),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ChainSilver),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "ARCHIVE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer info
            Text(
                text = "Todd McFarlane's Spawn • 90s Retro PlayStation Homage",
                fontSize = 10.sp,
                color = ChainSilver.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showStageSelectDialog) {
        StageSelectDialog(
            viewModel = viewModel,
            onDismiss = { showStageSelectDialog = false }
        )
    }
}

@Composable
fun StageSelectDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CryptStone,
            border = androidx.compose.foundation.BorderStroke(2.dp, CapeRedDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SELECT STAGE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = BoneWhite
                )

                Spacer(modifier = Modifier.height(14.dp))

                viewModel.engine.stages.forEach { stage ->
                    val isUnlocked = stage.stageNumber <= viewModel.maxStageUnlocked.value
                    Button(
                        onClick = {
                            if (isUnlocked) {
                                onDismiss()
                                viewModel.startCampaign(stage.stageNumber)
                            }
                        },
                        enabled = isUnlocked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUnlocked) DeepCharcoal else VoidBlack,
                            contentColor = if (isUnlocked) BoneWhite else ChainSilver.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isUnlocked) CapeRed else Color.Transparent
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "STAGE ${stage.stageNumber}: ${stage.name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) BoneWhite else ChainSilver.copy(alpha = 0.4f)
                            )
                            Text(
                                text = stage.subtitle,
                                fontSize = 11.sp,
                                color = if (isUnlocked) NecroGreen else ChainSilver.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CapeCrimson),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CLOSE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
