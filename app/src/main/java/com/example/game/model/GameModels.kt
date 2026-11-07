package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CapeRed
import com.example.ui.theme.NecroGreen
import com.example.ui.theme.ViolatorGold

enum class PlayerAction {
    IDLE,
    RUNNING,
    JUMPING,
    LIGHT_ATTACK,
    CHAIN_WHIP,
    CAPE_BLOCK,
    NECRO_BLAST,
    HELL_NOVA,
    HURT,
    DEAD
}

enum class EnemyType(
    val displayName: String,
    val maxHp: Float,
    val speed: Float,
    val damage: Float,
    val scoreValue: Int,
    val soulReward: Int,
    val width: Float,
    val height: Float
) {
    THUG("Rat City Thug", 50f, 120f, 10f, 100, 5, 40f, 65f),
    GARGOYLE("Gargoyle Fiend", 40f, 150f, 12f, 150, 8, 45f, 45f),
    HELL_HOUND("Hell Hound", 65f, 210f, 15f, 200, 10, 55f, 35f),
    NECRO_ENFORCER("Necro Knight", 140f, 80f, 22f, 400, 25, 55f, 80f),
    VIOLATOR_CLOWN("The Violator (Clown)", 350f, 110f, 20f, 1500, 100, 50f, 75f),
    VIOLATOR_DEMON("The Violator (Demon)", 650f, 140f, 35f, 3000, 250, 80f, 95f)
}

data class Player(
    var x: Float = 150f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isFacingRight: Boolean = true,
    var action: PlayerAction = PlayerAction.IDLE,
    var actionTimer: Float = 0f,
    var currentHp: Float = 100f,
    var maxHp: Float = 100f,
    var necroEnergy: Float = 9999f, // Classic 9999 countdown
    var necroSouls: Int = 0,
    var comboCount: Int = 0,
    var comboTimer: Float = 0f,
    var maxCombo: Int = 0,
    var invincibleTimer: Float = 0f,
    var width: Float = 45f,
    var height: Float = 75f,
    // Upgrades
    var gauntletLevel: Int = 1,
    var chainLevel: Int = 1,
    var capeLevel: Int = 1,
    var reservoirLevel: Int = 1,
    var novaLevel: Int = 1
) {
    val lightAttackDamage: Float
        get() = 18f + (gauntletLevel - 1) * 7f

    val heavyAttackDamage: Float
        get() = 35f + (chainLevel - 1) * 14f

    val heavyAttackRange: Float
        get() = 110f + (chainLevel - 1) * 25f

    val blastCost: Float
        get() = (150f - (reservoirLevel - 1) * 18f).coerceAtLeast(60f)

    val novaCost: Float
        get() = (1200f - (reservoirLevel - 1) * 100f).coerceAtLeast(600f)

    val isInvulnerable: Boolean
        get() = invincibleTimer > 0f || action == PlayerAction.CAPE_BLOCK || action == PlayerAction.HELL_NOVA
}

data class Enemy(
    val id: Long,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var hp: Float = type.maxHp,
    var maxHp: Float = type.maxHp,
    var isFacingRight: Boolean = false,
    var attackCooldown: Float = 1.0f,
    var isAttacking: Boolean = false,
    var hurtTimer: Float = 0f,
    var isDead: Boolean = false,
    var deathTimer: Float = 0f,
    var specialTimer: Float = 0f // Boss projectiles/teleport
)

data class Projectile(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val damage: Float,
    val isFromPlayer: Boolean,
    var lifetime: Float = 3.0f,
    val color: Color = if (isFromPlayer) NecroGreen else ViolatorGold
)

enum class ParticleType {
    SPARK,
    NECRO_FLAME,
    BLOOD_MIST,
    CHAIN_LINK,
    FLOATING_TEXT
}

data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float = 1f,
    val maxLifetime: Float,
    var lifetime: Float = maxLifetime,
    val color: Color,
    val type: ParticleType = ParticleType.SPARK,
    val text: String? = null
)

data class StageInfo(
    val stageNumber: Int,
    val name: String,
    val subtitle: String,
    val totalWaves: Int,
    val ambientColor: Color,
    val groundColor: Color,
    val hasBoss: Boolean = false
)

data class UpgradeItem(
    val id: String,
    val title: String,
    val description: String,
    val baseCost: Int,
    val currentLevel: Int,
    val maxLevel: Int = 5,
    val iconName: String
) {
    val nextCost: Int
        get() = baseCost * currentLevel
    val isMaxed: Boolean
        get() = currentLevel >= maxLevel
}
