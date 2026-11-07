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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class LoreEntry(
    val title: String,
    val subtitle: String,
    val description: String,
    val accentTag: String
)

val LORE_DATABASE = listOf(
    LoreEntry(
        title = "AL SIMMONS (SPAWN)",
        subtitle = "Hellspawn & Reluctant Anti-Hero",
        description = "Betrayed and murdered by his own government agency, covert operative Al Simmons made a pact with the devil Malebolgia to see his wife Wanda once more. Reborn five years later as a Hellspawn with charred flesh and a symbiotic necroplasmic suit, he broke his chains to wage war on both Heaven and Hell.",
        accentTag = "HERO / HELLSPAWN"
    ),
    LoreEntry(
        title = "THE K7-LEETHA SYMBIOTE",
        subtitle = "Living Costume & Spiked Chains",
        description = "Spawn's armor is a living, neural parasite born in the Stygian depths of the Seventh Circle. The flowing red cape, spiked arm gauntlets, and razor-sharp chains act instinctively to defend him, shifting mass into shields, lethal whips, and gliding wings.",
        accentTag = "SYMBIOTE SUIT"
    ),
    LoreEntry(
        title = "THE 9:9:9:9 NECRO-METER",
        subtitle = "Finite Unholy Power Reserve",
        description = "Every Hellspawn is endowed with an exact finite reserve of magical necroplasmic energy—initially measured at 9,999 units. Each resurrection, hellfire eruption, and matter transmutation burns away this essence. In this arcade tribute, your energy powers your lethal Blasts and Hell Novas!",
        accentTag = "LORE MECHANIC"
    ),
    LoreEntry(
        title = "THE VIOLATOR / THE CLOWN",
        subtitle = "Oldest of the Phlebiac Brothers",
        description = "A shapeshifting demon of immense cruelty sent to Earth to groom Simmons into leading Hell's army. When masquerading as an obese, disgusting clown with blue makeup, he mocks and torments Spawn before ripping open his true monstrous demon form with razor fangs and spiked tail.",
        accentTag = "ARCH-NEMESIS"
    ),
    LoreEntry(
        title = "RAT CITY & THE BOWERY",
        subtitle = "Kingdom of the Outcast",
        description = "Spawn makes his dark sanctuary in the grimy alleyways of New York City, protecting the homeless and forgotten street people from criminal syndicates, corrupt cyborg assassins, and encroaching demonic cabals.",
        accentTag = "TERRITORY"
    ),
    LoreEntry(
        title = "MALEBOLGIA",
        subtitle = "Lord of the Eighth Sphere",
        description = "The supreme architect of the Hellspawn program. Malebolgia trades earthly souls for cursed immortality, crafting generals for the prophesied Armageddon. Confront his minions in the final stage!",
        accentTag = "HELL LORD"
    )
)

@Composable
fun ArchiveScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setScreenState(ScreenState.TITLE) },
                    modifier = Modifier.testTag("archive_back_button")
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
                        text = "SPAWN ARCHIVE",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = BoneWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "COMIC LORE & BESTIARY DATABASE",
                        fontSize = 10.sp,
                        color = NecroGreenBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lore Entries
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(LORE_DATABASE) { entry ->
                    LoreCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun LoreCard(entry: LoreEntry) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CryptSurface),
        border = BorderStroke(1.dp, CapeRedDark.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = BoneWhite,
                    letterSpacing = 0.5.sp
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DeepCharcoal,
                    border = BorderStroke(1.dp, NecroGreen.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = entry.accentTag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NecroGreenBright,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = entry.subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ViolatorGold,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )

            Text(
                text = entry.description,
                fontSize = 12.sp,
                color = ChainSilver.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )
        }
    }
}
