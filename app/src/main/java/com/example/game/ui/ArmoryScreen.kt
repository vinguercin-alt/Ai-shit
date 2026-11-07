package com.example.game.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PanTool
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.UpgradeItem
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
fun ArmoryScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val upgrades by viewModel.upgrades.collectAsState()
    val availableSouls = viewModel.engine.player.necroSouls

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(VoidBlack, DeepCharcoal, CryptStone)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.setScreenState(ScreenState.TITLE) },
                        modifier = Modifier.testTag("armory_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BoneWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "HELL ARMORY",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = BoneWhite,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "SYMBIOTE & WEAPON ENHANCEMENTS",
                            fontSize = 10.sp,
                            color = CapeRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Souls Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CryptSurface,
                    border = BorderStroke(1.dp, NecroGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOULS: ",
                            color = ChainSilver,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$availableSouls",
                            color = NecroGreenBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Upgrade Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(upgrades) { item ->
                    UpgradeCard(
                        item = item,
                        availableSouls = availableSouls,
                        onUpgrade = { viewModel.purchaseUpgrade(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    item: UpgradeItem,
    availableSouls: Int,
    onUpgrade: () -> Unit
) {
    val canAfford = !item.isMaxed && availableSouls >= item.nextCost
    val icon: ImageVector = when (item.iconName) {
        "fist" -> Icons.Default.PanTool
        "link" -> Icons.Default.Link
        "shield" -> Icons.Default.Shield
        "bolt" -> Icons.Default.Bolt
        else -> Icons.Default.Flare
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CryptSurface),
        border = BorderStroke(1.dp, if (canAfford) NecroGreen.copy(alpha = 0.6f) else CapeRedDark.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DeepCharcoal,
                border = BorderStroke(1.dp, if (canAfford) NecroGreen else CapeRedDark),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (canAfford) NecroGreenBright else ViolatorGold,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BoneWhite
                    )
                    Text(
                        text = if (item.isMaxed) "MAX" else "LVL ${item.currentLevel}/${item.maxLevel}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (item.isMaxed) ViolatorGold else NecroGreen
                    )
                }

                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = ChainSilver.copy(alpha = 0.75f),
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Progress Bar
                LinearProgressIndicator(
                    progress = { item.currentLevel.toFloat() / item.maxLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (item.isMaxed) ViolatorGold else NecroGreen,
                    trackColor = VoidBlack
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Upgrade Button
            Button(
                onClick = onUpgrade,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) CapeRed else CryptStone,
                    contentColor = if (canAfford) BoneWhite else ChainSilver.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("upgrade_${item.id}_button")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (item.isMaxed) "MAXED" else "UPGRADE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!item.isMaxed) {
                        Text(
                            text = "${item.nextCost} SOULS",
                            fontSize = 9.sp,
                            color = if (canAfford) NecroGreenBright else CapeCrimson
                        )
                    }
                }
            }
        }
    }
}
