package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.audio.ArcadeSoundSynth
import com.example.game.model.Enemy
import com.example.game.model.EnemyType
import com.example.game.model.Particle
import com.example.game.model.ParticleType
import com.example.game.model.Player
import com.example.game.model.PlayerAction
import com.example.game.model.Projectile
import com.example.game.model.StageInfo
import com.example.ui.theme.CapeRed
import com.example.ui.theme.ChainSilver
import com.example.ui.theme.DemonPurple
import com.example.ui.theme.HellfireOrange
import com.example.ui.theme.NecroGreen
import com.example.ui.theme.NecroGreenBright
import com.example.ui.theme.ViolatorGold
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    val soundSynth: ArcadeSoundSynth,
    val triggerHaptic: (intensity: Int) -> Unit = {}
) {
    var screenWidth: Float = 800f
    var screenHeight: Float = 500f
    var groundY: Float = 420f

    val player = Player()
    val enemies = mutableListOf<Enemy>()
    val projectiles = mutableListOf<Projectile>()
    val particles = mutableListOf<Particle>()

    var score: Int = 0
    var currentStageIndex: Int = 1
    var isEndlessMode: Boolean = false
    var currentWave: Int = 1
    var waveSpawnTimer: Float = 0f
    var waveRemainingToSpawn: Int = 0
    var isStageComplete: Boolean = false
    var isGameOver: Boolean = false
    var isVictory: Boolean = false
    var screenShakeAmount: Float = 0f
    var hitPauseTimer: Float = 0f
    var idCounter: Long = 1000L

    val stages = listOf(
        StageInfo(
            stageNumber = 1,
            name = "RAT CITY ALLEYS",
            subtitle = "The Bowery & Street Demons",
            totalWaves = 3,
            ambientColor = Color(0xFF13111C),
            groundColor = Color(0xFF1E1C2B),
            hasBoss = false
        ),
        StageInfo(
            stageNumber = 2,
            name = "FORGOTTEN CRYPTS",
            subtitle = "Necro Catacombs of the Damned",
            totalWaves = 4,
            ambientColor = Color(0xFF0F1A15),
            groundColor = Color(0xFF16251E),
            hasBoss = false
        ),
        StageInfo(
            stageNumber = 3,
            name = "THRONE OF MALEBOLGIA",
            subtitle = "Showdown with The Violator",
            totalWaves = 3,
            ambientColor = Color(0xFF220D12),
            groundColor = Color(0xFF33141B),
            hasBoss = true
        )
    )

    fun getCurrentStage(): StageInfo {
        return if (isEndlessMode) {
            StageInfo(
                stageNumber = 99,
                name = "BLOOD COLOSSEUM",
                subtitle = "Infinite Demon Horde",
                totalWaves = 999,
                ambientColor = Color(0xFF1F0D1D),
                groundColor = Color(0xFF2E152B),
                hasBoss = true
            )
        } else {
            stages.getOrElse(currentStageIndex - 1) { stages.first() }
        }
    }

    fun startStage(stageIndex: Int, endless: Boolean = false) {
        currentStageIndex = stageIndex
        isEndlessMode = endless
        currentWave = 1
        isStageComplete = false
        isGameOver = false
        isVictory = false
        enemies.clear()
        projectiles.clear()
        particles.clear()

        player.x = screenWidth * 0.25f
        player.y = groundY - player.height
        player.vx = 0f
        player.vy = 0f
        player.action = PlayerAction.IDLE
        player.currentHp = player.maxHp
        player.necroEnergy = 9999f
        player.invincibleTimer = 1.0f
        player.comboCount = 0

        prepareWave(currentWave)
    }

    private fun prepareWave(wave: Int) {
        val count = if (isEndlessMode) {
            4 + wave * 2
        } else {
            when (currentStageIndex) {
                1 -> 3 + wave * 2
                2 -> 4 + wave * 2
                3 -> if (wave == 3) 1 else 3 + wave * 2
                else -> 5
            }
        }
        waveRemainingToSpawn = count
        waveSpawnTimer = 0.5f
    }

    fun update(dt: Float) {
        if (isGameOver || isStageComplete || isVictory) return

        if (hitPauseTimer > 0f) {
            hitPauseTimer -= dt
            return
        }

        // Screen shake decay
        if (screenShakeAmount > 0f) {
            screenShakeAmount = (screenShakeAmount - dt * 25f).coerceAtLeast(0f)
        }

        // Combo timer
        if (player.comboCount > 0) {
            player.comboTimer -= dt
            if (player.comboTimer <= 0f) {
                player.comboCount = 0
            }
        }

        // Necro energy slow regeneration
        if (player.necroEnergy < 9999f) {
            player.necroEnergy = (player.necroEnergy + dt * 15f * player.reservoirLevel).coerceAtMost(9999f)
        }

        // Invincible timer
        if (player.invincibleTimer > 0f) {
            player.invincibleTimer -= dt
        }

        // Update Player
        updatePlayer(dt)

        // Wave Spawning
        updateWaveSpawning(dt)

        // Update Enemies
        updateEnemies(dt)

        // Update Projectiles
        updateProjectiles(dt)

        // Update Particles
        updateParticles(dt)

        // Check Stage progress
        checkWaveAndStageProgression()
    }

    private fun updatePlayer(dt: Float) {
        val gravity = 980f

        // Handle action states
        if (player.actionTimer > 0f) {
            player.actionTimer -= dt
            if (player.actionTimer <= 0f) {
                if (player.action != PlayerAction.DEAD) {
                    player.action = if (player.y < groundY - player.height - 2f) PlayerAction.JUMPING else PlayerAction.IDLE
                }
            }
        }

        // Apply physics
        player.x += player.vx * dt
        player.y += player.vy * dt
        player.vy += gravity * dt

        // Ground constraint
        val targetGround = groundY - player.height
        if (player.y >= targetGround) {
            player.y = targetGround
            player.vy = 0f
            if (player.action == PlayerAction.JUMPING) {
                player.action = PlayerAction.IDLE
            }
        }

        // Screen boundary
        player.x = player.x.coerceIn(20f, screenWidth - player.width - 20f)
    }

    private fun updateWaveSpawning(dt: Float) {
        if (waveRemainingToSpawn > 0) {
            waveSpawnTimer -= dt
            if (waveSpawnTimer <= 0f) {
                spawnNextEnemy()
                waveRemainingToSpawn--
                waveSpawnTimer = 1.2f
            }
        }
    }

    private fun spawnNextEnemy() {
        val id = idCounter++
        val spawnLeft = Random.nextBoolean()
        val spawnX = if (spawnLeft) -50f else screenWidth + 50f

        val type: EnemyType = if (isEndlessMode) {
            val roll = Random.nextInt(100)
            when {
                currentWave >= 3 && roll < 20 -> EnemyType.VIOLATOR_CLOWN
                roll < 45 -> EnemyType.THUG
                roll < 70 -> EnemyType.HELL_HOUND
                roll < 88 -> EnemyType.GARGOYLE
                else -> EnemyType.NECRO_ENFORCER
            }
        } else {
            when (currentStageIndex) {
                1 -> if (Random.nextInt(100) < 65) EnemyType.THUG else EnemyType.GARGOYLE
                2 -> when (Random.nextInt(100)) {
                    in 0..45 -> EnemyType.HELL_HOUND
                    in 46..80 -> EnemyType.NECRO_ENFORCER
                    else -> EnemyType.GARGOYLE
                }
                3 -> {
                    if (currentWave == 3) {
                        EnemyType.VIOLATOR_CLOWN
                    } else {
                        when (Random.nextInt(100)) {
                            in 0..40 -> EnemyType.NECRO_ENFORCER
                            in 41..75 -> EnemyType.HELL_HOUND
                            else -> EnemyType.GARGOYLE
                        }
                    }
                }
                else -> EnemyType.THUG
            }
        }

        val enemyY = if (type == EnemyType.GARGOYLE) {
            groundY - 140f - Random.nextFloat() * 80f
        } else {
            groundY - type.height
        }

        enemies.add(
            Enemy(
                id = id,
                type = type,
                x = spawnX,
                y = enemyY,
                isFacingRight = spawnX < player.x
            )
        )
    }

    private fun updateEnemies(dt: Float) {
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()

            if (enemy.isDead) {
                enemy.deathTimer += dt
                if (enemy.deathTimer > 0.6f) {
                    iterator.remove()
                }
                continue
            }

            if (enemy.hurtTimer > 0f) {
                enemy.hurtTimer -= dt
                enemy.x += enemy.vx * dt
                continue
            }

            // Direction to player
            val dx = player.x - enemy.x
            enemy.isFacingRight = dx > 0
            val distance = abs(dx)

            enemy.attackCooldown -= dt
            enemy.specialTimer += dt

            // Enemy AI by type
            when (enemy.type) {
                EnemyType.THUG -> {
                    val attackDist = 55f
                    if (distance > attackDist) {
                        enemy.x += (if (dx > 0) 1 else -1) * enemy.type.speed * dt
                    } else if (enemy.attackCooldown <= 0f) {
                        performEnemyMeleeAttack(enemy, attackDist)
                    }
                }
                EnemyType.GARGOYLE -> {
                    // Hover and sinusoidal swoop
                    val swoop = sin(enemy.specialTimer * 3.5f) * 60f
                    enemy.y = (groundY - 150f + swoop).coerceAtMost(groundY - enemy.type.height)
                    if (distance > 60f) {
                        enemy.x += (if (dx > 0) 1 else -1) * enemy.type.speed * dt
                    } else if (enemy.attackCooldown <= 0f) {
                        performEnemyMeleeAttack(enemy, 70f)
                    }
                }
                EnemyType.HELL_HOUND -> {
                    // Fast dash and leap
                    val attackDist = 70f
                    if (distance > attackDist) {
                        enemy.x += (if (dx > 0) 1 else -1) * enemy.type.speed * dt
                    } else if (enemy.attackCooldown <= 0f) {
                        // Leap at player!
                        enemy.vx = (if (dx > 0) 1 else -1) * 220f
                        performEnemyMeleeAttack(enemy, attackDist)
                    }
                }
                EnemyType.NECRO_ENFORCER -> {
                    val attackDist = 75f
                    if (distance > attackDist) {
                        enemy.x += (if (dx > 0) 1 else -1) * enemy.type.speed * dt
                    } else if (enemy.attackCooldown <= 0f) {
                        performEnemyMeleeAttack(enemy, attackDist)
                    }
                }
                EnemyType.VIOLATOR_CLOWN, EnemyType.VIOLATOR_DEMON -> {
                    updateViolatorBoss(enemy, dt, distance, dx)
                }
            }

            // Clamp on screen if alive
            if (!enemy.isDead && enemy.x > -80f && enemy.x < screenWidth + 80f) {
                enemy.x = enemy.x.coerceIn(-60f, screenWidth + 60f)
            }
        }
    }

    private fun updateViolatorBoss(boss: Enemy, dt: Float, distance: Float, dx: Float) {
        // Boss phase check
        if (boss.type == EnemyType.VIOLATOR_CLOWN && boss.hp <= boss.maxHp * 0.45f) {
            // Transform into Demon form!
            soundSynth.playDemonRoar()
            triggerHaptic(3)
            createExplosionParticles(boss.x + boss.type.width / 2, boss.y + boss.type.height / 2, DemonPurple, 30)
            spawnFloatingText("TRANSFORMATION!", boss.x, boss.y - 20f, ViolatorGold)
            val newType = EnemyType.VIOLATOR_DEMON
            val transformed = Enemy(
                id = boss.id,
                type = newType,
                x = boss.x,
                y = groundY - newType.height,
                hp = newType.maxHp,
                maxHp = newType.maxHp,
                isFacingRight = boss.isFacingRight
            )
            val idx = enemies.indexOf(boss)
            if (idx != -1) {
                enemies[idx] = transformed
            }
            return
        }

        // Special attacks: Bouncing Clown bombs or Fire Spit
        if (boss.specialTimer > 3.0f) {
            boss.specialTimer = 0f
            val isDemon = boss.type == EnemyType.VIOLATOR_DEMON
            val projSpeed = if (isDemon) 320f else 220f
            val dir = if (boss.isFacingRight) 1f else -1f
            projectiles.add(
                Projectile(
                    id = idCounter++,
                    x = boss.x + (if (boss.isFacingRight) boss.type.width else 0f),
                    y = boss.y + boss.type.height * 0.4f,
                    vx = dir * projSpeed,
                    vy = if (isDemon) 0f else -120f,
                    radius = if (isDemon) 18f else 12f,
                    damage = if (isDemon) 26f else 16f,
                    isFromPlayer = false,
                    color = if (isDemon) HellfireOrange else ViolatorGold
                )
            )
            soundSynth.playNecroBlast()
            spawnFloatingText(if (isDemon) "HELLFIRE BURST!" else "LAUGHING BOMB!", boss.x, boss.y - 15f, ViolatorGold)
        }

        // Move towards player
        val meleeDist = if (boss.type == EnemyType.VIOLATOR_DEMON) 90f else 65f
        if (distance > meleeDist) {
            boss.x += (if (dx > 0) 1 else -1) * boss.type.speed * dt
        } else if (boss.attackCooldown <= 0f) {
            performEnemyMeleeAttack(boss, meleeDist + 15f)
        }
    }

    private fun performEnemyMeleeAttack(enemy: Enemy, reach: Float) {
        enemy.attackCooldown = 1.3f
        val dx = player.x - enemy.x
        if (abs(dx) <= reach && abs(player.y - enemy.y) < 70f) {
            damagePlayer(enemy.type.damage)
        }
    }

    private fun damagePlayer(amount: Float) {
        if (player.isInvulnerable) {
            if (player.action == PlayerAction.CAPE_BLOCK) {
                // Cape block sound and deflect sparks!
                soundSynth.playCapeDeflect()
                triggerHaptic(1)
                createDeflectSparks(player.x + (if (player.isFacingRight) player.width else 0f), player.y + player.height * 0.4f)
                spawnFloatingText("DEFLECTED!", player.x, player.y - 10f, ChainSilver)
            }
            return
        }

        player.currentHp -= amount
        player.invincibleTimer = 0.8f
        player.action = PlayerAction.HURT
        player.actionTimer = 0.35f
        soundSynth.playDemonRoar()
        triggerHaptic(2)
        screenShakeAmount = 14f

        spawnFloatingText("-${amount.toInt()}", player.x, player.y - 10f, CapeRed)
        createExplosionParticles(player.x + player.width / 2, player.y + player.height / 2, CapeRed, 12)

        if (player.currentHp <= 0f) {
            player.currentHp = 0f
            player.action = PlayerAction.DEAD
            isGameOver = true
            triggerHaptic(3)
        }
    }

    private fun updateProjectiles(dt: Float) {
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val proj = iterator.next()
            proj.x += proj.vx * dt
            proj.y += proj.vy * dt
            if (!proj.isFromPlayer) {
                // Bouncing clown bombs have gravity
                proj.vy += 200f * dt
            }
            proj.lifetime -= dt

            // Check screen bounds or expiration
            if (proj.lifetime <= 0f || proj.x < -40f || proj.x > screenWidth + 40f || proj.y > groundY + 20f) {
                createExplosionParticles(proj.x, proj.y, proj.color, 8)
                iterator.remove()
                continue
            }

            if (proj.isFromPlayer) {
                // Check enemy collision
                for (enemy in enemies) {
                    if (!enemy.isDead && checkOverlap(proj.x, proj.y, proj.radius, enemy.x, enemy.y, enemy.type.width, enemy.type.height)) {
                        damageEnemy(enemy, proj.damage, isCritical = true)
                        createExplosionParticles(proj.x, proj.y, NecroGreen, 14)
                        soundSynth.playPunch()
                        iterator.remove()
                        break
                    }
                }
            } else {
                // Enemy projectile hitting Spawn
                if (checkOverlap(proj.x, proj.y, proj.radius, player.x, player.y, player.width, player.height)) {
                    if (player.action == PlayerAction.CAPE_BLOCK) {
                        // Reflect projectile back to enemy!
                        proj.vx = -proj.vx * 1.3f
                        proj.vy = -50f
                        soundSynth.playCapeDeflect()
                        triggerHaptic(1)
                        createDeflectSparks(player.x, player.y + player.height * 0.4f)
                        spawnFloatingText("REFLECTED!", player.x, player.y - 15f, NecroGreenBright)
                    } else {
                        damagePlayer(proj.damage)
                        createExplosionParticles(proj.x, proj.y, proj.color, 10)
                        iterator.remove()
                    }
                }
            }
        }
    }

    private fun updateParticles(dt: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.lifetime -= dt
            if (p.lifetime <= 0f) {
                iterator.remove()
                continue
            }
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.alpha = (p.lifetime / p.maxLifetime).coerceIn(0f, 1f)
        }
    }

    private fun checkWaveAndStageProgression() {
        val allEnemiesDefeated = enemies.isEmpty() && waveRemainingToSpawn == 0
        if (allEnemiesDefeated && !isStageComplete && !isVictory && !isGameOver) {
            val stage = getCurrentStage()
            if (currentWave < stage.totalWaves) {
                currentWave++
                prepareWave(currentWave)
                spawnFloatingText("WAVE $currentWave!", screenWidth / 2f, 120f, NecroGreenBright)
                soundSynth.playVictory()
            } else {
                // Completed stage!
                soundSynth.playVictory()
                triggerHaptic(3)
                if (isEndlessMode) {
                    currentWave++
                    prepareWave(currentWave)
                    spawnFloatingText("WAVE $currentWave - HORDE INTENSIFIES!", screenWidth / 2f, 120f, ViolatorGold)
                } else if (currentStageIndex < stages.size) {
                    isStageComplete = true
                } else {
                    isVictory = true
                }
            }
        }
    }

    // Player Combat Input Handlers
    fun executeLightAttack() {
        if (player.action == PlayerAction.DEAD || player.action == PlayerAction.HURT) return
        player.action = PlayerAction.LIGHT_ATTACK
        player.actionTimer = 0.28f
        soundSynth.playPunch()
        triggerHaptic(1)

        val reach = 70f
        val attackX = if (player.isFacingRight) player.x + player.width else player.x - reach
        val attackY = player.y + 10f

        var hitAny = false
        for (enemy in enemies) {
            if (!enemy.isDead && checkRectOverlap(attackX, attackY, reach, player.height - 20f, enemy.x, enemy.y, enemy.type.width, enemy.type.height)) {
                damageEnemy(enemy, player.lightAttackDamage, isCritical = false)
                hitAny = true
            }
        }

        if (hitAny) {
            addCombo()
            createHitSparks(attackX + reach / 2, attackY + 20f, NecroGreen)
        }
    }

    fun executeChainWhip() {
        if (player.action == PlayerAction.DEAD || player.action == PlayerAction.HURT) return
        player.action = PlayerAction.CHAIN_WHIP
        player.actionTimer = 0.38f
        soundSynth.playChainWhip()
        triggerHaptic(2)
        screenShakeAmount = 6f

        val reach = player.heavyAttackRange
        val attackX = if (player.isFacingRight) player.x + player.width else player.x - reach
        val attackY = player.y

        var hitCount = 0
        for (enemy in enemies) {
            if (!enemy.isDead && checkRectOverlap(attackX, attackY, reach, player.height, enemy.x, enemy.y, enemy.type.width, enemy.type.height)) {
                damageEnemy(enemy, player.heavyAttackDamage, isCritical = true)
                // Knockback
                enemy.vx = (if (player.isFacingRight) 1f else -1f) * 180f
                hitCount++
            }
        }

        if (hitCount > 0) {
            addCombo(hitCount)
            createExplosionParticles(attackX + reach / 2, attackY + 30f, ChainSilver, hitCount * 6)
        }
    }

    fun setCapeBlock(isBlocking: Boolean) {
        if (player.action == PlayerAction.DEAD) return
        if (isBlocking) {
            player.action = PlayerAction.CAPE_BLOCK
            player.actionTimer = 999f
        } else if (player.action == PlayerAction.CAPE_BLOCK) {
            player.action = PlayerAction.IDLE
            player.actionTimer = 0f
        }
    }

    fun executeNecroBlast() {
        if (player.action == PlayerAction.DEAD || player.necroEnergy < player.blastCost) return
        player.necroEnergy -= player.blastCost
        player.action = PlayerAction.NECRO_BLAST
        player.actionTimer = 0.3f
        soundSynth.playNecroBlast()
        triggerHaptic(2)

        val speed = 480f
        val dir = if (player.isFacingRight) 1f else -1f
        projectiles.add(
            Projectile(
                id = idCounter++,
                x = player.x + (if (player.isFacingRight) player.width + 10f else -10f),
                y = player.y + player.height * 0.35f,
                vx = dir * speed,
                vy = 0f,
                radius = 16f,
                damage = 45f + (player.reservoirLevel - 1) * 12f,
                isFromPlayer = true,
                color = NecroGreen
            )
        )
    }

    fun executeHellNova() {
        if (player.action == PlayerAction.DEAD || player.necroEnergy < player.novaCost) return
        player.necroEnergy -= player.novaCost
        player.action = PlayerAction.HELL_NOVA
        player.actionTimer = 1.0f
        soundSynth.playHellNova()
        triggerHaptic(3)
        screenShakeAmount = 24f
        hitPauseTimer = 0.15f

        createScreenWipeParticles()
        spawnFloatingText("HELL NOVA!", screenWidth / 2f, 140f, NecroGreenBright)

        val novaDamage = 160f + (player.novaLevel - 1) * 60f
        for (enemy in enemies) {
            if (!enemy.isDead) {
                damageEnemy(enemy, novaDamage, isCritical = true)
                createExplosionParticles(enemy.x + enemy.type.width / 2, enemy.y + enemy.type.height / 2, NecroGreen, 20)
            }
        }
        addCombo(5)
    }

    fun movePlayer(direction: Float) {
        if (player.action == PlayerAction.CAPE_BLOCK || player.action == PlayerAction.DEAD) return
        val speed = 210f
        player.vx = direction * speed
        if (direction != 0f) {
            player.isFacingRight = direction > 0
            if (player.action == PlayerAction.IDLE) {
                player.action = PlayerAction.RUNNING
            }
        } else if (player.action == PlayerAction.RUNNING) {
            player.action = PlayerAction.IDLE
        }
    }

    fun jumpPlayer() {
        if (player.action == PlayerAction.DEAD || player.action == PlayerAction.CAPE_BLOCK) return
        if (player.y >= groundY - player.height - 5f) {
            player.vy = -480f
            player.action = PlayerAction.JUMPING
            createCapeTrails(player.x + player.width / 2, player.y + player.height)
        }
    }

    private fun damageEnemy(enemy: Enemy, amount: Float, isCritical: Boolean) {
        enemy.hp -= amount
        enemy.hurtTimer = 0.25f
        soundSynth.playDemonRoar()
        triggerHaptic(if (isCritical) 2 else 1)

        val textColor = if (isCritical) ViolatorGold else NecroGreenBright
        val text = if (isCritical) "CRIT ${amount.toInt()}" else "${amount.toInt()}"
        spawnFloatingText(text, enemy.x, enemy.y - 10f, textColor)

        if (enemy.hp <= 0f) {
            enemy.hp = 0f
            enemy.isDead = true
            enemy.deathTimer = 0f
            score += enemy.type.scoreValue
            player.necroSouls += enemy.type.soulReward
            spawnFloatingText("+${enemy.type.soulReward} SOULS", enemy.x, enemy.y - 25f, NecroGreen)
            createExplosionParticles(enemy.x + enemy.type.width / 2, enemy.y + enemy.type.height / 2, CapeRed, 18)
        }
    }

    private fun addCombo(count: Int = 1) {
        player.comboCount += count
        player.comboTimer = 2.4f
        if (player.comboCount > player.maxCombo) {
            player.maxCombo = player.comboCount
        }

        // Announcer threshold callouts
        when (player.comboCount) {
            5 -> spawnFloatingText("BRUTAL!", screenWidth / 2f, 90f, ViolatorGold)
            10 -> spawnFloatingText("UNHOLY!", screenWidth / 2f, 90f, CapeRed)
            15 -> spawnFloatingText("NECRO-SLAG!", screenWidth / 2f, 90f, NecroGreenBright)
            25 -> spawnFloatingText("HELLBOUND GOD!", screenWidth / 2f, 90f, CapeRed)
        }
    }

    // Collision math
    private fun checkOverlap(px: Float, py: Float, radius: Float, rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        val closestX = px.coerceIn(rx, rx + rw)
        val closestY = py.coerceIn(ry, ry + rh)
        val dx = px - closestX
        val dy = py - closestY
        return (dx * dx + dy * dy) <= (radius * radius)
    }

    private fun checkRectOverlap(r1x: Float, r1y: Float, r1w: Float, r1h: Float, r2x: Float, r2y: Float, r2w: Float, r2h: Float): Boolean {
        return r1x < r2x + r2w && r1x + r1w > r2x && r1y < r2y + r2h && r1y + r1h > r2y
    }

    // Particle Emitters
    private fun spawnFloatingText(text: String, x: Float, y: Float, color: Color) {
        particles.add(
            Particle(
                id = idCounter++,
                x = x,
                y = y,
                vx = (Random.nextFloat() - 0.5f) * 20f,
                vy = -45f,
                size = 18f,
                maxLifetime = 0.9f,
                color = color,
                type = ParticleType.FLOATING_TEXT,
                text = text
            )
        )
    }

    private fun createHitSparks(x: Float, y: Float, color: Color) {
        for (i in 0 until 8) {
            val angle = Random.nextFloat() * 6.28f
            val spd = 60f + Random.nextFloat() * 120f
            particles.add(
                Particle(
                    id = idCounter++,
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    size = 4f + Random.nextFloat() * 4f,
                    maxLifetime = 0.35f,
                    color = color,
                    type = ParticleType.SPARK
                )
            )
        }
    }

    private fun createDeflectSparks(x: Float, y: Float) {
        for (i in 0 until 12) {
            val angle = Random.nextFloat() * 6.28f
            val spd = 80f + Random.nextFloat() * 140f
            particles.add(
                Particle(
                    id = idCounter++,
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    size = 5f,
                    maxLifetime = 0.4f,
                    color = ChainSilver,
                    type = ParticleType.SPARK
                )
            )
        }
    }

    private fun createExplosionParticles(x: Float, y: Float, color: Color, count: Int) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 6.28f
            val spd = 50f + Random.nextFloat() * 180f
            particles.add(
                Particle(
                    id = idCounter++,
                    x = x,
                    y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    size = 5f + Random.nextFloat() * 6f,
                    maxLifetime = 0.5f + Random.nextFloat() * 0.3f,
                    color = color,
                    type = ParticleType.NECRO_FLAME
                )
            )
        }
    }

    private fun createCapeTrails(x: Float, y: Float) {
        for (i in 0 until 6) {
            particles.add(
                Particle(
                    id = idCounter++,
                    x = x + (Random.nextFloat() - 0.5f) * 20f,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 30f,
                    vy = 40f + Random.nextFloat() * 40f,
                    size = 8f,
                    maxLifetime = 0.4f,
                    color = CapeRed,
                    type = ParticleType.BLOOD_MIST
                )
            )
        }
    }

    private fun createScreenWipeParticles() {
        for (i in 0 until 40) {
            particles.add(
                Particle(
                    id = idCounter++,
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight,
                    vx = (Random.nextFloat() - 0.5f) * 250f,
                    vy = -100f - Random.nextFloat() * 200f,
                    size = 8f + Random.nextFloat() * 12f,
                    maxLifetime = 0.8f,
                    color = NecroGreenBright,
                    type = ParticleType.NECRO_FLAME
                )
            )
        }
    }
}
