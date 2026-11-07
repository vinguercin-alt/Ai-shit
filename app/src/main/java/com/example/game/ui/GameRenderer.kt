package com.example.game.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.game.engine.GameEngine
import com.example.game.model.Enemy
import com.example.game.model.EnemyType
import com.example.game.model.Particle
import com.example.game.model.ParticleType
import com.example.game.model.Player
import com.example.game.model.PlayerAction
import com.example.game.model.Projectile
import com.example.ui.theme.AshGrey
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CapeCrimson
import com.example.ui.theme.CapeRed
import com.example.ui.theme.CapeRedDark
import com.example.ui.theme.ChainSilver
import com.example.ui.theme.ChainSteel
import com.example.ui.theme.CryptStone
import com.example.ui.theme.CryptSurface
import com.example.ui.theme.DemonPurple
import com.example.ui.theme.HellfireOrange
import com.example.ui.theme.NecroGreen
import com.example.ui.theme.NecroGreenBright
import com.example.ui.theme.NecroGreenDark
import com.example.ui.theme.ViolatorGold
import com.example.ui.theme.VoidBlack
import kotlin.math.cos
import kotlin.math.sin

object GameRenderer {

    fun drawWorld(
        scope: DrawScope,
        engine: GameEngine,
        crtScanlines: Boolean
    ) {
        val stage = engine.getCurrentStage()
        val width = scope.size.width
        val height = scope.size.height

        // Shake offset
        val shakeX = if (engine.screenShakeAmount > 0f) (Math.random().toFloat() - 0.5f) * engine.screenShakeAmount * 2f else 0f
        val shakeY = if (engine.screenShakeAmount > 0f) (Math.random().toFloat() - 0.5f) * engine.screenShakeAmount * 2f else 0f

        // Draw Gothic Sky & Background
        drawBackground(scope, stage.ambientColor, stage.stageNumber, width, height, engine.groundY)

        // Draw Stage Ground
        drawGround(scope, stage.groundColor, width, engine.groundY, height - engine.groundY)

        // Draw Projectiles
        for (proj in engine.projectiles) {
            drawProjectile(scope, proj, shakeX, shakeY)
        }

        // Draw Enemies
        for (enemy in engine.enemies) {
            drawEnemy(scope, enemy, shakeX, shakeY)
        }

        // Draw Spawn (Player)
        drawPlayer(scope, engine.player, shakeX, shakeY)

        // Draw Particles & Floating Text
        for (p in engine.particles) {
            drawParticle(scope, p, shakeX, shakeY)
        }

        // Retro CRT Scanlines
        if (crtScanlines) {
            drawCrtScanlines(scope, width, height)
        }
    }

    private fun drawBackground(
        scope: DrawScope,
        ambient: Color,
        stageNum: Int,
        width: Float,
        height: Float,
        groundY: Float
    ) {
        // Sky Gradient
        scope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(VoidBlack, ambient, CryptStone)
            ),
            size = Size(width, groundY)
        )

        // Moon / Hell Orb
        val moonX = width * 0.82f
        val moonY = 80f
        val moonColor = when (stageNum) {
            1 -> Color(0xFFE2E8F0) // Full silver moon in Rat City
            2 -> NecroGreenBright.copy(alpha = 0.8f) // Necro moon in crypts
            else -> HellfireOrange // Blood moon in Hell
        }
        scope.drawCircle(
            color = moonColor.copy(alpha = 0.15f),
            radius = 55f,
            center = Offset(moonX, moonY)
        )
        scope.drawCircle(
            color = moonColor,
            radius = 35f,
            center = Offset(moonX, moonY)
        )

        // Silhouette Architecture
        when (stageNum) {
            1 -> {
                // Rat City Skyscrapers & Gargoyle roofs
                drawCitySilhouettes(scope, width, groundY)
            }
            2 -> {
                // Crypt pillars & hanging chains
                drawCryptCatacombs(scope, width, groundY)
            }
            else -> {
                // Malebolgia hell pillars & lava cracks
                drawHellThrone(scope, width, groundY)
            }
        }
    }

    private fun drawCitySilhouettes(scope: DrawScope, width: Float, groundY: Float) {
        val bldgColor = Color(0xFF0C0A12)
        var curX = 10f
        val heights = floatArrayOf(180f, 260f, 210f, 320f, 190f, 280f, 230f, 310f, 170f)
        for (h in heights) {
            val bWidth = 70f
            scope.drawRect(
                color = bldgColor,
                topLeft = Offset(curX, groundY - h),
                size = Size(bWidth, h)
            )
            // Little yellow/green lit windows
            for (wy in (groundY - h + 20f).toInt()..groundY.toInt() - 30 step 35) {
                scope.drawRect(
                    color = if (curX.toInt() % 2 == 0) ViolatorGold.copy(alpha = 0.3f) else NecroGreen.copy(alpha = 0.25f),
                    topLeft = Offset(curX + 15f, wy.toFloat()),
                    size = Size(10f, 14f)
                )
            }
            curX += bWidth + 12f
            if (curX > width) break
        }
    }

    private fun drawCryptCatacombs(scope: DrawScope, width: Float, groundY: Float) {
        val pillarColor = Color(0xFF0B1410)
        for (px in 60..width.toInt() step 140) {
            // Pillars
            scope.drawRect(
                color = pillarColor,
                topLeft = Offset(px.toFloat(), 40f),
                size = Size(36f, groundY - 40f)
            )
            // Hanging Chains
            scope.drawLine(
                color = ChainSteel.copy(alpha = 0.4f),
                start = Offset(px + 18f, 0f),
                end = Offset(px + 18f, 150f),
                strokeWidth = 3f
            )
        }
    }

    private fun drawHellThrone(scope: DrawScope, width: Float, groundY: Float) {
        val lavaColor = CapeRedDark
        // Spiked gothic Hell pillars
        for (px in 40..width.toInt() step 160) {
            val path = Path().apply {
                moveTo(px.toFloat(), groundY)
                lineTo(px + 20f, 50f)
                lineTo(px + 40f, groundY)
                close()
            }
            scope.drawPath(path, color = Color(0xFF19060B))
        }
        // Glowing lava river at ground edge
        scope.drawRect(
            brush = Brush.horizontalGradient(listOf(CapeRed, HellfireOrange, CapeRed)),
            topLeft = Offset(0f, groundY - 6f),
            size = Size(width, 6f)
        )
    }

    private fun drawGround(scope: DrawScope, groundColor: Color, width: Float, groundY: Float, groundHeight: Float) {
        scope.drawRect(
            brush = Brush.verticalGradient(listOf(groundColor, VoidBlack)),
            topLeft = Offset(0f, groundY),
            size = Size(width, groundHeight)
        )
        // Stone boundary line
        scope.drawLine(
            color = CapeRedDark,
            start = Offset(0f, groundY),
            end = Offset(width, groundY),
            strokeWidth = 3f
        )
    }

    private fun drawPlayer(scope: DrawScope, player: Player, shakeX: Float, shakeY: Float) {
        val px = player.x + shakeX
        val py = player.y + shakeY
        val facingRight = player.isFacingRight

        // Invincibility flicker
        if (player.invincibleTimer > 0f && (player.invincibleTimer * 20).toInt() % 2 == 0) {
            return
        }

        // If Cape Block action: Draw impenetrable spiked red cocoon shield!
        if (player.action == PlayerAction.CAPE_BLOCK) {
            drawCapeBlockShield(scope, px, py, player.width, player.height)
            return
        }

        // Draw Flowing Red Cape behind/around Spawn
        drawSpawnCape(scope, px, py, player.width, player.height, facingRight, player.action)

        // Body: Spiked Black Suit
        val bodyColor = VoidBlack
        val chestWidth = player.width * 0.7f
        val chestHeight = player.height * 0.45f
        val chestX = px + (player.width - chestWidth) / 2

        scope.drawRoundRect(
            color = bodyColor,
            topLeft = Offset(chestX, py + 22f),
            size = Size(chestWidth, chestHeight),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // White M-shaped chest cowl pattern (Signature Spawn emblem)
        val emblemPath = Path().apply {
            val cx = chestX + chestWidth / 2
            val cy = py + 26f
            moveTo(cx, cy + 18f)
            lineTo(cx - 10f, cy + 4f)
            lineTo(cx - 16f, cy + 14f)
            lineTo(cx - 8f, cy + 28f)
            lineTo(cx, cy + 22f)
            lineTo(cx + 8f, cy + 28f)
            lineTo(cx + 16f, cy + 14f)
            lineTo(cx + 10f, cy + 4f)
            close()
        }
        scope.drawPath(emblemPath, color = BoneWhite)

        // Skull Belt Buckle with Chains
        val beltY = py + 22f + chestHeight
        scope.drawCircle(
            color = BoneWhite,
            radius = 5.5f,
            center = Offset(chestX + chestWidth / 2, beltY)
        )
        // Orbiting chains
        scope.drawLine(
            color = ChainSilver,
            start = Offset(chestX + 2f, beltY),
            end = Offset(chestX + chestWidth - 2f, beltY + 4f),
            strokeWidth = 2.5f
        )

        // Legs & Spiked Greaves
        val legWidth = 8f
        val legHeight = player.height - chestHeight - 22f
        val legLeftX = chestX + 4f
        val legRightX = chestX + chestWidth - 12f
        scope.drawRect(
            color = bodyColor,
            topLeft = Offset(legLeftX, beltY),
            size = Size(legWidth, legHeight)
        )
        scope.drawRect(
            color = bodyColor,
            topLeft = Offset(legRightX, beltY),
            size = Size(legWidth, legHeight)
        )
        // Spiked Boot Greaves
        scope.drawRect(
            color = CapeRedDark,
            topLeft = Offset(legLeftX - 2f, beltY + legHeight - 12f),
            size = Size(legWidth + 4f, 12f)
        )
        scope.drawRect(
            color = CapeRedDark,
            topLeft = Offset(legRightX - 2f, beltY + legHeight - 12f),
            size = Size(legWidth + 4f, 12f)
        )

        // Head & Mask
        val headRadius = 11f
        val headCenter = Offset(chestX + chestWidth / 2, py + 12f)
        scope.drawCircle(
            color = bodyColor,
            radius = headRadius,
            center = headCenter
        )

        // Glowing Necro Eyes (Spawn signature angular white/green)
        val eyeY = headCenter.y - 2f
        val eyeDir = if (facingRight) 1 else -1
        val eyeX = headCenter.x + (eyeDir * 3f)

        // Left eye
        val eyePath = Path().apply {
            moveTo(eyeX - 5f, eyeY - 3f)
            lineTo(eyeX + 1f, eyeY)
            lineTo(eyeX - 4f, eyeY + 3f)
            close()
        }
        scope.drawPath(eyePath, color = NecroGreenBright)
        // Eye Glow
        scope.drawCircle(
            color = NecroGreen.copy(alpha = 0.5f),
            radius = 6f,
            center = Offset(eyeX, eyeY)
        )

        // Spiked Gauntlet Arms & Attacks
        drawSpawnArmsAndWeapons(scope, px, py, player, facingRight)
    }

    private fun drawSpawnCape(
        scope: DrawScope,
        px: Float,
        py: Float,
        width: Float,
        height: Float,
        facingRight: Boolean,
        action: PlayerAction
    ) {
        val capeCol = CapeRed
        val capeDark = CapeCrimson
        val shoulderY = py + 20f

        // High jagged collar
        val collarPath = Path().apply {
            moveTo(px + width / 2 - 14f, shoulderY - 14f)
            lineTo(px + width / 2 - 6f, shoulderY)
            lineTo(px + width / 2 + 6f, shoulderY)
            lineTo(px + width / 2 + 14f, shoulderY - 14f)
            lineTo(px + width / 2 + 10f, shoulderY + 8f)
            lineTo(px + width / 2 - 10f, shoulderY + 8f)
            close()
        }
        scope.drawPath(collarPath, color = capeDark)

        // Flowing Main Cape billowing backward
        val capePath = Path().apply {
            val topX = px + width / 2
            val capeDir = if (facingRight) -1f else 1f
            moveTo(topX, shoulderY)
            cubicTo(
                topX + (capeDir * 40f), shoulderY + 20f,
                topX + (capeDir * 60f), shoulderY + 50f,
                topX + (capeDir * 50f), py + height
            )
            // Jagged tattered cape bottom edges
            lineTo(topX + (capeDir * 38f), py + height - 10f)
            lineTo(topX + (capeDir * 26f), py + height + 2f)
            lineTo(topX + (capeDir * 14f), py + height - 8f)
            lineTo(topX, shoulderY + 24f)
            close()
        }
        scope.drawPath(capePath, color = capeCol)
    }

    private fun drawCapeBlockShield(scope: DrawScope, px: Float, py: Float, width: Float, height: Float) {
        // Full Spiked Cocoon
        val centerX = px + width / 2
        val centerY = py + height / 2
        scope.drawCircle(
            brush = Brush.radialGradient(listOf(CapeRed, CapeCrimson, VoidBlack)),
            radius = height * 0.55f,
            center = Offset(centerX, centerY)
        )
        // Spikes on shield
        for (i in 0 until 8) {
            val angle = i * (3.14159f / 4f)
            val sx = centerX + cos(angle) * (height * 0.55f)
            val sy = centerY + sin(angle) * (height * 0.55f)
            val tx = centerX + cos(angle) * (height * 0.7f)
            val ty = centerY + sin(angle) * (height * 0.7f)
            scope.drawLine(
                color = ChainSilver,
                start = Offset(sx, sy),
                end = Offset(tx, ty),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
        // Glowing necro rune in center
        scope.drawCircle(
            color = NecroGreenBright,
            radius = 6f,
            center = Offset(centerX, centerY)
        )
    }

    private fun drawSpawnArmsAndWeapons(
        scope: DrawScope,
        px: Float,
        py: Float,
        player: Player,
        facingRight: Boolean
    ) {
        val armY = py + 30f
        val shoulderX = px + player.width / 2

        when (player.action) {
            PlayerAction.LIGHT_ATTACK -> {
                // Extended spiked claw fist with green trails
                val dir = if (facingRight) 1f else -1f
                val fistX = shoulderX + dir * 45f
                val fistY = armY
                scope.drawLine(
                    color = VoidBlack,
                    start = Offset(shoulderX, armY),
                    end = Offset(fistX, fistY),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
                // Spiked red gauntlet
                scope.drawCircle(
                    color = CapeRed,
                    radius = 8f,
                    center = Offset(fistX, fistY)
                )
                // Necro slash arc
                val arcPath = Path().apply {
                    moveTo(fistX, fistY - 24f)
                    cubicTo(fistX + dir * 18f, fistY - 10f, fistX + dir * 18f, fistY + 10f, fistX, fistY + 24f)
                }
                scope.drawPath(arcPath, color = NecroGreenBright, style = Stroke(width = 4f))
            }

            PlayerAction.CHAIN_WHIP -> {
                // Segmented Spiked Chain lash forward
                val dir = if (facingRight) 1f else -1f
                val chainLen = player.heavyAttackRange
                var lastX = shoulderX
                var lastY = armY
                for (s in 1..8) {
                    val progress = s / 8f
                    val waveY = sin(progress * 3.14f) * -25f
                    val curX = shoulderX + dir * (progress * chainLen)
                    val curY = armY + waveY
                    scope.drawLine(
                        color = if (s % 2 == 0) ChainSilver else ChainSteel,
                        start = Offset(lastX, lastY),
                        end = Offset(curX, curY),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                    lastX = curX
                    lastY = curY
                }
                // Spiked skull ball at the end of chain
                scope.drawCircle(
                    color = BoneWhite,
                    radius = 9f,
                    center = Offset(lastX, lastY)
                )
                scope.drawCircle(
                    color = NecroGreenBright,
                    radius = 4f,
                    center = Offset(lastX, lastY)
                )
            }

            PlayerAction.NECRO_BLAST -> {
                // Thrust hands forward with necro aura
                val dir = if (facingRight) 1f else -1f
                val handX = shoulderX + dir * 30f
                scope.drawLine(
                    color = VoidBlack,
                    start = Offset(shoulderX, armY),
                    end = Offset(handX, armY),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
                scope.drawCircle(
                    color = NecroGreenBright,
                    radius = 12f,
                    center = Offset(handX, armY)
                )
                scope.drawCircle(
                    color = NecroGreen.copy(alpha = 0.4f),
                    radius = 20f,
                    center = Offset(handX, armY)
                )
            }

            PlayerAction.HELL_NOVA -> {
                // Arms raised up, summoning orbiting hell fire
                scope.drawLine(
                    color = VoidBlack,
                    start = Offset(shoulderX - 10f, armY),
                    end = Offset(shoulderX - 18f, armY - 25f),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
                scope.drawLine(
                    color = VoidBlack,
                    start = Offset(shoulderX + 10f, armY),
                    end = Offset(shoulderX + 18f, armY - 25f),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
                // Orbiting ring
                scope.drawCircle(
                    color = NecroGreenBright,
                    radius = 65f,
                    center = Offset(shoulderX, armY - 10f),
                    style = Stroke(width = 3f)
                )
            }

            else -> {
                // Idle / running arms
                val dir = if (facingRight) 1f else -1f
                scope.drawLine(
                    color = VoidBlack,
                    start = Offset(shoulderX, armY),
                    end = Offset(shoulderX + dir * 14f, armY + 18f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                scope.drawCircle(
                    color = CapeRedDark,
                    radius = 6f,
                    center = Offset(shoulderX + dir * 14f, armY + 18f)
                )
            }
        }
    }

    private fun drawEnemy(scope: DrawScope, enemy: Enemy, shakeX: Float, shakeY: Float) {
        val ex = enemy.x + shakeX
        val ey = enemy.y + shakeY

        if (enemy.isDead) {
            // Fading ash
            val alpha = (1f - enemy.deathTimer / 0.6f).coerceIn(0f, 1f)
            scope.drawCircle(
                color = AshGrey.copy(alpha = alpha),
                radius = 16f,
                center = Offset(ex + enemy.type.width / 2, ey + enemy.type.height / 2)
            )
            return
        }

        // Hurt flash (red/white)
        val isFlashing = enemy.hurtTimer > 0f

        when (enemy.type) {
            EnemyType.THUG -> drawThug(scope, ex, ey, enemy.type.width, enemy.type.height, enemy.isFacingRight, isFlashing)
            EnemyType.GARGOYLE -> drawGargoyle(scope, ex, ey, enemy.type.width, enemy.type.height, isFlashing)
            EnemyType.HELL_HOUND -> drawHellHound(scope, ex, ey, enemy.type.width, enemy.type.height, enemy.isFacingRight, isFlashing)
            EnemyType.NECRO_ENFORCER -> drawNecroEnforcer(scope, ex, ey, enemy.type.width, enemy.type.height, enemy.isFacingRight, isFlashing)
            EnemyType.VIOLATOR_CLOWN -> drawViolatorClown(scope, ex, ey, enemy.type.width, enemy.type.height, enemy.isFacingRight, isFlashing)
            EnemyType.VIOLATOR_DEMON -> drawViolatorDemon(scope, ex, ey, enemy.type.width, enemy.type.height, enemy.isFacingRight, isFlashing)
        }

        // Draw Health Bar above Enemy
        drawEnemyHpBar(scope, ex, ey - 12f, enemy.type.width, enemy.hp, enemy.maxHp)
    }

    private fun drawThug(scope: DrawScope, x: Float, y: Float, w: Float, h: Float, facingRight: Boolean, isFlashing: Boolean) {
        val baseColor = if (isFlashing) BoneWhite else Color(0xFF334155)
        // Body
        scope.drawRoundRect(
            color = baseColor,
            topLeft = Offset(x + 8f, y + 20f),
            size = Size(w - 16f, h - 20f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Red Bandana Head
        scope.drawCircle(
            color = if (isFlashing) BoneWhite else CapeRed,
            radius = 10f,
            center = Offset(x + w / 2, y + 10f)
        )
        // Steel Pipe Weapon
        val dir = if (facingRight) 1f else -1f
        scope.drawLine(
            color = ChainSilver,
            start = Offset(x + w / 2, y + 30f),
            end = Offset(x + w / 2 + dir * 25f, y + 15f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }

    private fun drawGargoyle(scope: DrawScope, x: Float, y: Float, w: Float, h: Float, isFlashing: Boolean) {
        val col = if (isFlashing) BoneWhite else CryptStone
        // Stone Bat Body
        scope.drawCircle(
            color = col,
            radius = 16f,
            center = Offset(x + w / 2, y + h / 2)
        )
        // Glowing red eyes
        scope.drawCircle(color = CapeRed, radius = 3.5f, center = Offset(x + w / 2 - 5f, y + h / 2 - 2f))
        scope.drawCircle(color = CapeRed, radius = 3.5f, center = Offset(x + w / 2 + 5f, y + h / 2 - 2f))
        // Stone Wings
        val wingPath = Path().apply {
            moveTo(x + w / 2, y + h / 2)
            lineTo(x - 18f, y)
            lineTo(x - 5f, y + h / 2 + 10f)
            close()
        }
        scope.drawPath(wingPath, color = col)
        val wingPath2 = Path().apply {
            moveTo(x + w / 2, y + h / 2)
            lineTo(x + w + 18f, y)
            lineTo(x + w + 5f, y + h / 2 + 10f)
            close()
        }
        scope.drawPath(wingPath2, color = col)
    }

    private fun drawHellHound(scope: DrawScope, x: Float, y: Float, w: Float, h: Float, facingRight: Boolean, isFlashing: Boolean) {
        val col = if (isFlashing) BoneWhite else Color(0xFF1E0B0F)
        // Low quadruped body
        scope.drawRoundRect(
            color = col,
            topLeft = Offset(x, y + 8f),
            size = Size(w, h - 8f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Spines on back
        for (i in 0 until 4) {
            val sx = x + 10f + i * 10f
            scope.drawLine(
                color = CapeRedDark,
                start = Offset(sx, y + 8f),
                end = Offset(sx + 2f, y - 2f),
                strokeWidth = 3f
            )
        }
        // Head with glowing jaws
        val headX = if (facingRight) x + w else x
        scope.drawCircle(color = CapeRed, radius = 5f, center = Offset(headX, y + 14f))
    }

    private fun drawNecroEnforcer(scope: DrawScope, x: Float, y: Float, w: Float, h: Float, facingRight: Boolean, isFlashing: Boolean) {
        val col = if (isFlashing) BoneWhite else Color(0xFF0F172A)
        // Heavy Armor
        scope.drawRoundRect(
            color = col,
            topLeft = Offset(x + 5f, y + 15f),
            size = Size(w - 10f, h - 15f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Horned skull helmet
        scope.drawCircle(color = BoneWhite, radius = 12f, center = Offset(x + w / 2, y + 12f))
        // Horns
        scope.drawLine(color = ChainSilver, start = Offset(x + w / 2 - 8f, y + 8f), end = Offset(x + w / 2 - 16f, y - 4f), strokeWidth = 3f)
        scope.drawLine(color = ChainSilver, start = Offset(x + w / 2 + 8f, y + 8f), end = Offset(x + w / 2 + 16f, y - 4f), strokeWidth = 3f)
        // Spiked Hammer
        val dir = if (facingRight) 1f else -1f
        scope.drawRect(color = CapeRedDark, topLeft = Offset(x + w / 2 + dir * 20f, y + 25f), size = Size(18f, 18f))
    }

    private fun drawViolatorClown(scope: DrawScope, x: Float, y: Float, w: Float, h: Float, facingRight: Boolean, isFlashing: Boolean) {
        // Yellow suit, suspenders, blue clown face
        val suitColor = if (isFlashing) BoneWhite else ViolatorGold
        scope.drawRoundRect(
            color = suitColor,
            topLeft = Offset(x + 5f, y + 25f),
            size = Size(w - 10f, h - 25f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Blue face
        scope.drawCircle(
            color = if (isFlashing) BoneWhite else Color(0xFF38BDF8),
            radius = 14f,
            center = Offset(x + w / 2, y + 14f)
        )
        // Red bulb nose & evil grin
        scope.drawCircle(color = CapeRed, radius = 4f, center = Offset(x + w / 2, y + 14f))
        val smilePath = Path().apply {
            moveTo(x + w / 2 - 8f, y + 18f)
            lineTo(x + w / 2, y + 23f)
            lineTo(x + w / 2 + 8f, y + 18f)
        }
        scope.drawPath(smilePath, color = VoidBlack, style = Stroke(width = 2.5f))
    }

    private fun drawViolatorDemon(scope: DrawScope, x: Float, y: Float, w: Float, h: Float, facingRight: Boolean, isFlashing: Boolean) {
        val demonColor = if (isFlashing) BoneWhite else DemonPurple
        // Monstrous spiky demonic silhouette
        scope.drawRoundRect(
            color = demonColor,
            topLeft = Offset(x + 10f, y + 25f),
            size = Size(w - 20f, h - 25f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        // Gaping Maw with jagged teeth
        val mawPath = Path().apply {
            val cx = x + w / 2
            moveTo(cx - 20f, y + 10f)
            lineTo(cx, y - 8f)
            lineTo(cx + 20f, y + 10f)
            lineTo(cx + 12f, y + 28f)
            lineTo(cx - 12f, y + 28f)
            close()
        }
        scope.drawPath(mawPath, color = VoidBlack)
        // Jagged White Fangs
        for (f in 0..4) {
            val fx = x + w / 2 - 14f + f * 7f
            scope.drawLine(color = BoneWhite, start = Offset(fx, y + 10f), end = Offset(fx + 2f, y + 18f), strokeWidth = 3f)
        }
        // Flaming Eyes
        scope.drawCircle(color = HellfireOrange, radius = 5f, center = Offset(x + w / 2 - 10f, y + 2f))
        scope.drawCircle(color = HellfireOrange, radius = 5f, center = Offset(x + w / 2 + 10f, y + 2f))
        // Long Spiked Demon Tail
        val tailDir = if (facingRight) -1f else 1f
        val tailPath = Path().apply {
            moveTo(x + w / 2, y + h - 15f)
            cubicTo(x + tailDir * 40f, y + h - 10f, x + tailDir * 60f, y + h - 40f, x + tailDir * 75f, y + h - 50f)
        }
        scope.drawPath(tailPath, color = CapeRedDark, style = Stroke(width = 6f, cap = StrokeCap.Round))
    }

    private fun drawEnemyHpBar(scope: DrawScope, x: Float, y: Float, width: Float, hp: Float, maxHp: Float) {
        val barW = width.coerceAtLeast(35f)
        val barH = 5f
        scope.drawRect(
            color = VoidBlack,
            topLeft = Offset(x, y),
            size = Size(barW, barH)
        )
        val fillW = (hp / maxHp).coerceIn(0f, 1f) * barW
        scope.drawRect(
            color = CapeRed,
            topLeft = Offset(x, y),
            size = Size(fillW, barH)
        )
    }

    private fun drawProjectile(scope: DrawScope, proj: Projectile, shakeX: Float, shakeY: Float) {
        val px = proj.x + shakeX
        val py = proj.y + shakeY

        // Glowing outer circle
        scope.drawCircle(
            color = proj.color.copy(alpha = 0.35f),
            radius = proj.radius * 1.8f,
            center = Offset(px, py)
        )
        // Core
        scope.drawCircle(
            color = proj.color,
            radius = proj.radius,
            center = Offset(px, py)
        )
        // White-hot center
        scope.drawCircle(
            color = BoneWhite,
            radius = proj.radius * 0.45f,
            center = Offset(px, py)
        )
    }

    private fun drawParticle(scope: DrawScope, p: Particle, shakeX: Float, shakeY: Float) {
        val px = p.x + shakeX
        val py = p.y + shakeY

        if (p.type == ParticleType.FLOATING_TEXT && p.text != null) {
            scope.drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (p.alpha * 255).toInt(),
                        (p.color.red * 255).toInt(),
                        (p.color.green * 255).toInt(),
                        (p.color.blue * 255).toInt()
                    )
                    textSize = p.size * 1.4f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                drawText(p.text, px, py, paint)
            }
            return
        }

        scope.drawCircle(
            color = p.color.copy(alpha = p.alpha),
            radius = p.size * p.alpha,
            center = Offset(px, py)
        )
    }

    private fun drawCrtScanlines(scope: DrawScope, width: Float, height: Float) {
        val scanlineColor = Color.Black.copy(alpha = 0.18f)
        for (y in 0..height.toInt() step 4) {
            scope.drawLine(
                color = scanlineColor,
                start = Offset(0f, y.toFloat()),
                end = Offset(width, y.toFloat()),
                strokeWidth = 1.2f
            )
        }
        // CRT Edge Vignette
        scope.drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                center = Offset(width / 2, height / 2),
                radius = width * 0.65f
            ),
            size = Size(width, height)
        )
    }
}
