package com.example.game.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.viewmodel.GameViewModel
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CapeCrimson
import com.example.ui.theme.CapeRedDark
import com.example.ui.theme.ChainSilver
import com.example.ui.theme.CryptStone
import com.example.ui.theme.CryptSurface
import com.example.ui.theme.NecroGreen
import com.example.ui.theme.NecroGreenBright
import com.example.ui.theme.VoidBlack

@Composable
fun SettingsDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val crt by viewModel.crtScanlines.collectAsState()
    val sound by viewModel.soundEnabled.collectAsState()
    val haptics by viewModel.hapticsEnabled.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "GAME SETTINGS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = BoneWhite,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // CRT Scanlines Toggle
                SettingRow(
                    title = "CRT Monitor Scanlines",
                    subtitle = "90s Arcade PS1 Television scanlines & vignette",
                    checked = crt,
                    onCheckedChange = { viewModel.toggleCrtScanlines() },
                    testTag = "toggle_crt"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Arcade Audio SFX Toggle
                SettingRow(
                    title = "Arcade Sound Synthesizer",
                    subtitle = "Procedural combat sounds, strikes, and roar effects",
                    checked = sound,
                    onCheckedChange = { viewModel.toggleSound() },
                    testTag = "toggle_sound"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Haptics Rumble Toggle
                SettingRow(
                    title = "Tactile Rumble Haptics",
                    subtitle = "Vibrations on chain whips, hits, and blasts",
                    checked = haptics,
                    onCheckedChange = { viewModel.toggleHaptics() },
                    testTag = "toggle_haptics"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CapeCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("close_settings_button")
                ) {
                    Text(
                        text = "DONE",
                        color = BoneWhite,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CryptSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BoneWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = ChainSilver.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NecroGreenBright,
                    checkedTrackColor = NecroGreen.copy(alpha = 0.4f),
                    uncheckedThumbColor = ChainSilver,
                    uncheckedTrackColor = VoidBlack
                )
            )
        }
    }
}
