package com.rpn.blockblaster.domain.engine

import com.rpn.blockblaster.core.designsystem.BlockColors
import com.rpn.blockblaster.domain.model.Block
import kotlin.math.max
import kotlin.random.Random

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
//  Shape matrix helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Rotate a boolean matrix 90° clockwise. */
private fun List<List<Boolean>>.rotate90(): List<List<Boolean>> {
    val rows = size
    val cols = this[0].size
    return List(cols) { c -> List(rows) { r -> this[rows - 1 - r][c] } }
}

/** Normalise a matrix: trim empty rows/cols and return a canonical copy. */
private fun List<List<Boolean>>.trim(): List<List<Boolean>> {
    val withRows  = filter { row -> row.any { it } }
    if (withRows.isEmpty()) return emptyList()
    val minCol    = withRows.minOf { row -> row.indexOfFirst { it } }
    val maxCol    = withRows.maxOf { row -> row.indexOfLast { it } }
    return withRows.map { row -> row.subList(minCol, maxCol + 1) }
}

/** Produce all distinct rotations (1–4) for a matrix. */
private fun List<List<Boolean>>.distinctRotations(): List<List<List<Boolean>>> {
    val seen    = mutableSetOf<List<List<Boolean>>>()
    val result  = mutableListOf<List<List<Boolean>>>()
    var current = this
    repeat(4) {
        val trimmed = current.trim()
        if (seen.add(trimmed)) result.add(trimmed)
        current = current.rotate90()
    }
    return result
}

/** Count filled cells in a matrix. */
private fun List<List<Boolean>>.cellCount(): Int = sumOf { row -> row.count { it } }

/** Bounding box (rows × cols) of the matrix. */
data class BoundingBox(val rows: Int, val cols: Int)
private fun List<List<Boolean>>.boundingBox() =
    BoundingBox(size, this[0].size)

// ─────────────────────────────────────────────────────────────────────────────
//  Difficulty tier
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Controls which shapes appear at a given score/level.
 *
 *  EASY   – singles, dominoes, small corners              (score 0–499)
 *  MEDIUM – triominoes, 2×2 square, L/J/T/S/Z, quad-line (score 500–1499)
 *  HARD   – pentominoes, big rectangles, complex specials  (score 1500+)
 */
@Serializable
enum class Difficulty { EASY, MEDIUM, HARD }

// ─────────────────────────────────────────────────────────────────────────────
//  Shape definition  (canonical = base rotation only)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * @param name       Human-readable identifier (debug / analytics only)
 * @param matrix     Canonical (un-rotated) shape matrix
 * @param weight     Relative spawn probability within its difficulty tier
 * @param difficulty Earliest tier at which this shape is allowed to appear
 * @param rotatable  If false, only the canonical rotation is used (e.g. single cell)
 */
data class ShapeDefinition(
    val name        : String,
    val matrix      : List<List<Boolean>>,
    val weight      : Int,
    val difficulty  : Difficulty,
    val rotatable   : Boolean = true
)

// ─────────────────────────────────────────────────────────────────────────────
//  Resolved spawn entry  (one concrete rotation)
// ─────────────────────────────────────────────────────────────────────────────

data class SpawnEntry(
    val name        : String,
    val matrix      : List<List<Boolean>>,
    val cellCount   : Int,
    val boundingBox : BoundingBox,
    val weight      : Int,
    val difficulty  : Difficulty
)

// ─────────────────────────────────────────────────────────────────────────────
//  Shape catalogue
// ─────────────────────────────────────────────────────────────────────────────

private val SHAPE_DEFINITIONS: List<ShapeDefinition> = listOf(

    // ── EASY ─────────────────────────────────────────────────────────────────
    // Heavier weights keep small breather-pieces common at every tier,
    // so the player always has something placeable on the board.

    ShapeDefinition(
        name = "Single",
        matrix = listOf(listOf(true)),
        weight = 12,                          // ↑ from 8 – always placeable, breathing room
        difficulty = Difficulty.EASY,
        rotatable = false
    ),
    ShapeDefinition(
        name = "Domino",
        matrix = listOf(listOf(true, true)),
        weight = 15,                          // ↑ from 10 – rotatable gives H+V variants
        difficulty = Difficulty.EASY
    ),
    ShapeDefinition(
        name = "Corner-2",
        matrix = listOf(
            listOf(true, true),
            listOf(true, false)
        ),
        weight = 14,                          // ↑ from 12 – rotatable gives all 4 corners
        difficulty = Difficulty.EASY
    ),

    // ── MEDIUM ───────────────────────────────────────────────────────────────
    // Good variety of recognisable Tetris-like pieces plus new flavours.
    // Quad-Line is here (moved down from HARD) so line-clearing comes earlier.

    ShapeDefinition(
        name = "Tri-Line",
        matrix = listOf(listOf(true, true, true)),
        weight = 14,                          // ↑ from 10 – rotatable gives H+V tri-lines
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "Square-2",
        matrix = listOf(
            listOf(true, true),
            listOf(true, true)
        ),
        weight = 13,                          // ↑ from 9 – 2×2 is extremely satisfying
        difficulty = Difficulty.MEDIUM,
        rotatable = false
    ),
    ShapeDefinition(
        name = "L-Shape",
        matrix = listOf(
            listOf(true, false),
            listOf(true, false),
            listOf(true, true)
        ),
        weight = 11,                          // ↑ from 9 – classic L, all 4 rotations
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "J-Shape",
        matrix = listOf(
            listOf(false, true),
            listOf(false, true),
            listOf(true,  true)
        ),
        weight = 11,                          // ↑ from 9
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "T-Shape",
        matrix = listOf(
            listOf(true, true, true),
            listOf(false, true, false)
        ),
        weight = 10,                          // ↑ from 8
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "S-Shape",
        matrix = listOf(
            listOf(false, true, true),
            listOf(true,  true, false)
        ),
        weight = 8,                           // ↑ from 7
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "Z-Shape",
        matrix = listOf(
            listOf(true,  true, false),
            listOf(false, true, true)
        ),
        weight = 8,                           // ↑ from 7
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "Corner-3",
        matrix = listOf(
            listOf(true, true, true),
            listOf(true, false, false),
            listOf(true, false, false)
        ),
        weight = 9,                           // ↑ from 6 – big L-corner, very useful
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "Corner-2-Plus",
        matrix = listOf(
            listOf(false, true),
            listOf(true,  false)
        ),
        weight = 4,                           // ↑ from 2 – diagonal 2-cell, tricky but small
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "U-Shape",
        matrix = listOf(
            listOf(true, false, true),
            listOf(true, true,  true)
        ),
        weight = 6,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "X-Shape",
        matrix = listOf(
            listOf(true, false, true),
            listOf(false, true,  false)
        ),
        weight = 4,                           // ↑ from 3
        difficulty = Difficulty.MEDIUM
    ),

    // ── NEW MEDIUM: Quad-Line ─────────────────────────────────────────────────
    // Moved from HARD → MEDIUM so players get the satisfying 4-clear sooner.
    // Rotatable gives both horizontal (I-piece) and vertical variants.
    ShapeDefinition(
        name = "Quad-Line",
        matrix = listOf(listOf(true, true, true, true)),
        weight = 9,                           // strong weight – line-clear enabler
        difficulty = Difficulty.MEDIUM
    ),

    // ── NEW MEDIUM: L-Long ───────────────────────────────────────────────────
    // A 4-tall L (5 cells). Satisfying to nestle into board corners.
    // Rotatable gives all 4 orientations of the long-L.
    ShapeDefinition(
        name = "L-Long",
        matrix = listOf(
            listOf(true, false),
            listOf(true, false),
            listOf(true, false),
            listOf(true, true)
        ),
        weight = 7,
        difficulty = Difficulty.MEDIUM
    ),

    // ── NEW MEDIUM: Flag ─────────────────────────────────────────────────────
    // A 4-tall vertical line with a 2-wide cap – looks like a flag.
    // Rotatable gives 4 orientations (cap on each side, horizontal variants).
    ShapeDefinition(
        name = "Flag",
        matrix = listOf(
            listOf(true, true),
            listOf(true, false),
            listOf(true, false),
            listOf(true, false)
        ),
        weight = 6,
        difficulty = Difficulty.MEDIUM
    ),

    // ── NEW MEDIUM: Stair-3 ──────────────────────────────────────────────────
    // A 3-step staircase (4 cells). Distinctive silhouette; 2 distinct rotations.
    ShapeDefinition(
        name = "Stair-3",
        matrix = listOf(
            listOf(true,  false, false),
            listOf(true,  true,  false),
            listOf(false, true,  true)
        ),
        weight = 6,
        difficulty = Difficulty.MEDIUM
    ),

    // ── NEW MEDIUM: C-Shape ───────────────────────────────────────────────────
    // A 3-tall open bracket (5 cells). Fills tightly around 2×1 gaps.
    // Rotatable gives all 4 bracket orientations.
    ShapeDefinition(
        name = "C-Shape",
        matrix = listOf(
            listOf(true, true),
            listOf(true, false),
            listOf(true, true)
        ),
        weight = 5,
        difficulty = Difficulty.MEDIUM
    ),

    // ── NEW MEDIUM: Square-Plus ───────────────────────────────────────────────
    // 2×2 with one arm extending right (5 cells). Sits snugly in board corners.
    // Rotatable gives 4 orientations.
    ShapeDefinition(
        name = "Square-Plus",
        matrix = listOf(
            listOf(true, true, false),
            listOf(true, true, true)
        ),
        weight = 7,
        difficulty = Difficulty.MEDIUM
    ),

    // ── HARD ─────────────────────────────────────────────────────────────────

    ShapeDefinition(
        name = "Plus",
        matrix = listOf(
            listOf(false, true,  false),
            listOf(true,  true,  true),
            listOf(false, true,  false)
        ),
        weight = 4,                           // ↓ from 5 – complex, less frustrating frequency
        difficulty = Difficulty.HARD,
        rotatable = false
    ),
    ShapeDefinition(
        name = "P-Shape",
        matrix = listOf(
            listOf(true, true),
            listOf(true, true),
            listOf(true, false)
        ),
        weight = 5,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Penta-Line",
        matrix = listOf(listOf(true, true, true, true, true)),
        weight = 3,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Square-3",
        matrix = listOf(
            listOf(true, true, true),
            listOf(true, true, true),
            listOf(true, true, true)
        ),
        weight = 2,
        difficulty = Difficulty.HARD,
        rotatable = false
    ),
    ShapeDefinition(
        name = "W-Pentomino",
        matrix = listOf(
            listOf(true,  false, false),
            listOf(true,  true,  false),
            listOf(false, true,  true)
        ),
        weight = 3,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Arrow",
        matrix = listOf(
            listOf(false, false, true),
            listOf(false, true, false),
            listOf(true, false, false)
        ),
        weight = 3,                           // ↓ from 4 – sparse diagonal, tricky
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Z-Long",
        matrix = listOf(
            listOf(true,  true,  false, false),
            listOf(false, true,  true,  true)
        ),
        weight = 2,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Hollow-Square",
        matrix = listOf(
            listOf(true, true,  true),
            listOf(true, false, true),
            listOf(true, true,  true)
        ),
        weight = 1,
        difficulty = Difficulty.HARD,
        rotatable = false
    ),

    // ── NEW HARD: Rect-3x2 ───────────────────────────────────────────────────
    // A solid 2×3 rectangle (6 cells). Clears two full rows when placed perfectly.
    // Rotatable gives 3×2 and 2×3 variants.
    ShapeDefinition(
        name = "Rect-3x2",
        matrix = listOf(
            listOf(true, true, true),
            listOf(true, true, true)
        ),
        weight = 4,
        difficulty = Difficulty.HARD
    ),

    // ── NEW HARD: Big-T ──────────────────────────────────────────────────────
    // A T-shape with a longer stem (5 cells). More striking than regular T.
    // Rotatable gives 4 orientations.
    ShapeDefinition(
        name = "Big-T",
        matrix = listOf(
            listOf(true, true, true),
            listOf(false, true, false),
            listOf(false, true, false)
        ),
        weight = 3,
        difficulty = Difficulty.HARD
    ),

    // ── NEW HARD: S-Step ─────────────────────────────────────────────────────
    // A 4-tall S/Z staircase (5 cells). Very distinctive; tricky to place well.
    ShapeDefinition(
        name = "S-Step",
        matrix = listOf(
            listOf(false, true),
            listOf(true,  true),
            listOf(true,  false),
            listOf(true,  false)
        ),
        weight = 2,
        difficulty = Difficulty.HARD
    )
)

// ─────────────────────────────────────────────────────────────────────────────
//  Factory
// ─────────────────────────────────────────────────────────────────────────────

object BlockShapeFactory {

    // Score thresholds at which each tier unlocks (documentation – wired in ViewModel)
    private const val SCORE_MEDIUM = 500
    private const val SCORE_HARD   = 1500

    // Pre-built weighted pools per tier (includes all easier tiers too).
    // Each pool is built lazily once and reused — no allocations at spawn time.
    private val poolEasy  : List<SpawnEntry> by lazy { buildPool(Difficulty.EASY)   }
    private val poolMedium: List<SpawnEntry> by lazy { buildPool(Difficulty.MEDIUM) }
    private val poolHard  : List<SpawnEntry> by lazy { buildPool(Difficulty.HARD)   }

    /**
     * Build a weighted spawn pool that includes all tiers up to [maxDifficulty].
     * Each [ShapeDefinition] is expanded into all of its distinct rotations,
     * then repeated [weight] times so weighted-random is a simple index pick.
     */
    private fun buildPool(maxDifficulty: Difficulty): List<SpawnEntry> {
        val tierOrder = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)
        val maxIdx    = tierOrder.indexOf(maxDifficulty)

        return SHAPE_DEFINITIONS
            .filter { tierOrder.indexOf(it.difficulty) <= maxIdx }
            .flatMap { def ->
                val rotations = if (def.rotatable) def.matrix.distinctRotations()
                else               listOf(def.matrix.trim())
                rotations.flatMap { rot ->
                    val entry = SpawnEntry(
                        name        = def.name,
                        matrix      = rot,
                        cellCount   = rot.cellCount(),
                        boundingBox = rot.boundingBox(),
                        weight      = def.weight,
                        difficulty  = def.difficulty
                    )
                    List(def.weight) { entry }
                }
            }
    }

    // ── Pool selection ────────────────────────────────────────────────────────

    private fun poolForDifficulty(difficulty: Difficulty): List<SpawnEntry> = when (difficulty) {
        Difficulty.EASY   -> poolEasy
        Difficulty.MEDIUM -> poolMedium
        Difficulty.HARD   -> poolHard
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Pick a single random [SpawnEntry] from the pool appropriate for [difficulty].
     *
     * @param difficulty Current difficulty tier.
     * @param rng        Kotlin [Random] instance (injectable for determinism/tests).
     */
    fun randomEntry(
        difficulty: Difficulty = Difficulty.MEDIUM,
        rng: Random = Random.Default
    ): SpawnEntry {
        val pool = poolForDifficulty(difficulty)
        return pool[rng.nextInt(pool.size)]
    }

    /**
     * Build a [Block] from a random [SpawnEntry].
     *
     * @param colorIndex  Index into [BlockColors]; wraps automatically.
     * @param difficulty  Current difficulty tier.
     * @param rng         Random instance.
     */
    fun randomBlock(
        colorIndex: Int,
        difficulty: Difficulty = Difficulty.MEDIUM,
        rng: Random = Random.Default
    ): Block {
        val entry = randomEntry(difficulty, rng)
        val color = BlockColors[colorIndex % BlockColors.size]
        return Block(shape = entry.matrix, color = color, name = entry.name)
    }

    /**
     * Spawn [count] blocks (default 3) ensuring:
     *  • No two consecutive blocks share the same shape name (variety guarantee).
     *  • At least one block per batch is EASY-tier so there's always a safe move.
     *  • Colours are spread across the palette.
     *
     * @param count      How many blocks to spawn (typically 3).
     * @param difficulty Current difficulty tier.
     * @param rng        Random instance.
     */
    fun spawnBlocks(
        count: Int = 3,
        difficulty: Difficulty = Difficulty.MEDIUM,
        rng: Random = Random.Default
    ): List<Block> {
        val startColor = rng.nextInt(BlockColors.size)
        val blocks     = mutableListOf<Block>()
        var lastName   = ""

        // Guarantee at least one easy-tier piece per batch so there's always a move.
        val easySlot = if (difficulty != Difficulty.EASY) rng.nextInt(count) else -1

        repeat(count) { i ->
            val usedDifficulty = if (i == easySlot) Difficulty.EASY else difficulty
            var entry = randomEntry(usedDifficulty, rng)
            // Avoid repeating the same shape name back-to-back for variety
            repeat(4) { if (entry.name == lastName) entry = randomEntry(usedDifficulty, rng) }
            lastName = entry.name

            val color = BlockColors[(startColor + i) % BlockColors.size]
            blocks.add(Block(shape = entry.matrix, color = color, name = entry.name))
        }
        return blocks
    }

    /**
     * Deterministic spawn using a seed — useful for daily challenges or replays.
     *
     * @param seed       Long seed value.
     * @param count      Number of blocks.
     * @param difficulty Current difficulty tier.
     */
    fun spawnBlocksSeeded(
        seed: Long,
        count: Int = 3,
        difficulty: Difficulty = Difficulty.MEDIUM
    ): List<Block> = spawnBlocks(count = count, difficulty = difficulty, rng = Random(seed))

    // ── Diagnostics (debug/test only) ─────────────────────────────────────────

    /** Total entries in the pool for [difficulty] (after rotation expansion). */
    fun poolSize(difficulty: Difficulty = Difficulty.MEDIUM): Int = poolForDifficulty(difficulty).size

    /** All unique shape names available at [difficulty]. */
    fun availableShapeNames(difficulty: Difficulty = Difficulty.MEDIUM): List<String> =
        poolForDifficulty(difficulty).map { it.name }.distinct().sorted()
}

/**
package com.rpn.blockblaster.domain.engine

import com.rpn.blockblaster.core.designsystem.BlockColors
import com.rpn.blockblaster.domain.model.Block
import kotlin.math.max
import kotlin.random.Random

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
//  Shape matrix helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Rotate a boolean matrix 90° clockwise. */
private fun List<List<Boolean>>.rotate90(): List<List<Boolean>> {
    val rows = size
    val cols = this[0].size
    return List(cols) { c -> List(rows) { r -> this[rows - 1 - r][c] } }
}

/** Normalise a matrix: trim empty rows/cols and return a canonical copy. */
private fun List<List<Boolean>>.trim(): List<List<Boolean>> {
    val withRows  = filter { row -> row.any { it } }
    if (withRows.isEmpty()) return emptyList()
    val minCol    = withRows.minOf { row -> row.indexOfFirst { it } }
    val maxCol    = withRows.maxOf { row -> row.indexOfLast { it } }
    return withRows.map { row -> row.subList(minCol, maxCol + 1) }
}

/** Produce all distinct rotations (1–4) for a matrix. */
private fun List<List<Boolean>>.distinctRotations(): List<List<List<Boolean>>> {
    val seen    = mutableSetOf<List<List<Boolean>>>()
    val result  = mutableListOf<List<List<Boolean>>>()
    var current = this
    repeat(4) {
        val trimmed = current.trim()
        if (seen.add(trimmed)) result.add(trimmed)
        current = current.rotate90()
    }
    return result
}

/** Count filled cells in a matrix. */
private fun List<List<Boolean>>.cellCount(): Int = sumOf { row -> row.count { it } }

/** Bounding box (rows × cols) of the matrix. */
data class BoundingBox(val rows: Int, val cols: Int)
private fun List<List<Boolean>>.boundingBox() =
    BoundingBox(size, this[0].size)

// ─────────────────────────────────────────────────────────────────────────────
//  Difficulty tier
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Controls which shapes appear at a given score/level.
 *
 *  EASY   – singles, dominoes, small corners         (score 0+)
 *  MEDIUM – triominoes, 2×2 square, L/J/T shapes     (score 500+)
 *  HARD   – tetrominoes, S/Z, pentominoes, specials   (score 1500+)
 */
@Serializable
enum class Difficulty { EASY, MEDIUM, HARD }

// ─────────────────────────────────────────────────────────────────────────────
//  Shape definition  (canonical = base rotation only)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * @param name       Human-readable identifier (debug / analytics only)
 * @param matrix     Canonical (un-rotated) shape matrix
 * @param weight     Relative spawn probability within its difficulty tier
 * @param difficulty Earliest tier at which this shape is allowed to appear
 * @param rotatable  If false, only the canonical rotation is used (e.g. single cell)
 */
data class ShapeDefinition(
    val name        : String,
    val matrix      : List<List<Boolean>>,
    val weight      : Int,
    val difficulty  : Difficulty,
    val rotatable   : Boolean = true
)

// ─────────────────────────────────────────────────────────────────────────────
//  Resolved spawn entry  (one concrete rotation)
// ─────────────────────────────────────────────────────────────────────────────

data class SpawnEntry(
    val name        : String,
    val matrix      : List<List<Boolean>>,
    val cellCount   : Int,
    val boundingBox : BoundingBox,
    val weight      : Int,
    val difficulty  : Difficulty
)

// ─────────────────────────────────────────────────────────────────────────────
//  Shape catalogue
// ─────────────────────────────────────────────────────────────────────────────

private val SHAPE_DEFINITIONS: List<ShapeDefinition> = listOf(

    // ── EASY ─────────────────────────────────────────────────────────────────

    ShapeDefinition(
        name = "Single",
        matrix = listOf(listOf(true)),
        weight = 8,
        difficulty = Difficulty.EASY,
        rotatable = false
    ),
    ShapeDefinition(
        name = "Domino",
        matrix = listOf(listOf(true, true)),
        weight = 10,
        difficulty = Difficulty.EASY
    ),
    ShapeDefinition(
        name = "Corner-2",
        matrix = listOf(
            listOf(true, true),
            listOf(true, false)
        ),
        weight = 12,
        difficulty = Difficulty.EASY
    ),

    // ── MEDIUM ───────────────────────────────────────────────────────────────

    ShapeDefinition(
        name = "Tri-Line",
        matrix = listOf(listOf(true, true, true)),
        weight = 10,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "Square-2",
        matrix = listOf(
            listOf(true, true),
            listOf(true, true)
        ),
        weight = 9,
        difficulty = Difficulty.MEDIUM,
        rotatable = false
    ),
    ShapeDefinition(
        name = "L-Shape",
        matrix = listOf(
            listOf(true, false),
            listOf(true, false),
            listOf(true, true)
        ),
        weight = 9,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "J-Shape",
        matrix = listOf(
            listOf(false, true),
            listOf(false, true),
            listOf(true,  true)
        ),
        weight = 9,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "T-Shape",
        matrix = listOf(
            listOf(true, true, true),
            listOf(false, true, false)
        ),
        weight = 8,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "S-Shape",
        matrix = listOf(
            listOf(false, true, true),
            listOf(true,  true, false)
        ),
        weight = 7,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "Z-Shape",
        matrix = listOf(
            listOf(true,  true, false),
            listOf(false, true, true)
        ),
        weight = 7,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "Corner-3",
        matrix = listOf(
            listOf(true, true, true),
            listOf(true, false, false),
            listOf(true, false, false)
        ),
        weight = 6,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "Corner-2-Plus",
        matrix = listOf(
            listOf(false, true),
            listOf(true,  false),
        ),
        weight = 2,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "U-Shape",
        matrix = listOf(
            listOf(true, false, true),
            listOf(true, true,  true)
        ),
        weight = 5,
        difficulty = Difficulty.MEDIUM
    ),
    ShapeDefinition(
        name = "X-Shape",
        matrix = listOf(
            listOf(true, false, true),
            listOf(false, true,  false)
        ),
        weight = 3,
        difficulty = Difficulty.MEDIUM
    ),

    // ── HARD ─────────────────────────────────────────────────────────────────

    ShapeDefinition(
        name = "Plus",
        matrix = listOf(
            listOf(false, true,  false),
            listOf(true,  true,  true),
            listOf(false, true,  false)
        ),
        weight = 5,
        difficulty = Difficulty.HARD,
        rotatable = false
    ),
    ShapeDefinition(
        name = "P-Shape",
        matrix = listOf(
            listOf(true, true),
            listOf(true, true),
            listOf(true, false)
        ),
        weight = 6,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Quad-Line",
        matrix = listOf(listOf(true, true, true, true)),
        weight = 6,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Penta-Line",
        matrix = listOf(listOf(true, true, true, true, true)),
        weight = 3,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Square-3",
        matrix = listOf(
            listOf(true, true, true),
            listOf(true, true, true),
            listOf(true, true, true)
        ),
        weight = 2,
        difficulty = Difficulty.HARD,
        rotatable = false
    ),
    ShapeDefinition(
        name = "W-Pentomino",
        matrix = listOf(
            listOf(true,  false, false),
            listOf(true,  true,  false),
            listOf(false, true,  true)
        ),
        weight = 3,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Arrow",
        matrix = listOf(
            listOf(false, false, true),
            listOf(false, true, false),
            listOf(true, false, false)

        ),
        weight = 4,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Z-Long",
        matrix = listOf(
            listOf(true,  true,  false, false),
            listOf(false, true,  true,  true)
        ),
        weight = 2,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Hollow-Square",
        matrix = listOf(
            listOf(true, true,  true),
            listOf(true, false, true),
            listOf(true, true,  true)
        ),
        weight = 1,
        difficulty = Difficulty.HARD,
        rotatable = false
    )
)

// ─────────────────────────────────────────────────────────────────────────────
//  Factory
// ─────────────────────────────────────────────────────────────────────────────

object BlockShapeFactory {

    private const val SCORE_MEDIUM = 0
    private const val SCORE_HARD   = 0

    // Pre-built weighted pools per tier (includes all easier tiers too).
    // Each pool is built lazily once and reused — no allocations at spawn time.
    private val poolEasy  : List<SpawnEntry> by lazy { buildPool(Difficulty.EASY)   }
    private val poolMedium: List<SpawnEntry> by lazy { buildPool(Difficulty.MEDIUM) }
    private val poolHard  : List<SpawnEntry> by lazy { buildPool(Difficulty.HARD)   }

    /**
     * Build a weighted spawn pool that includes all tiers up to [maxDifficulty].
     * Each [ShapeDefinition] is expanded into all of its distinct rotations,
     * then repeated [weight] times so weighted-random is a simple index pick.
     */
    private fun buildPool(maxDifficulty: Difficulty): List<SpawnEntry> {
        val tierOrder = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)
        val maxIdx    = tierOrder.indexOf(maxDifficulty)

        return SHAPE_DEFINITIONS
            .filter { tierOrder.indexOf(it.difficulty) <= maxIdx }
            .flatMap { def ->
                val rotations = if (def.rotatable) def.matrix.distinctRotations()
                else               listOf(def.matrix.trim())
                rotations.flatMap { rot ->
                    val entry = SpawnEntry(
                        name        = def.name,
                        matrix      = rot,
                        cellCount   = rot.cellCount(),
                        boundingBox = rot.boundingBox(),
                        weight      = def.weight,
                        difficulty  = def.difficulty
                    )
                    List(def.weight) { entry }
                }
            }
    }

    // ── Pool selection ────────────────────────────────────────────────────────

    private fun poolForDifficulty(difficulty: Difficulty): List<SpawnEntry> = when (difficulty) {
        Difficulty.EASY   -> poolEasy
        Difficulty.MEDIUM -> poolMedium
        Difficulty.HARD   -> poolHard
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Pick a single random [SpawnEntry] from the pool appropriate for [difficulty].
     *
     * @param difficulty Current difficulty tier.
     * @param rng        Kotlin [Random] instance (injectable for determinism/tests).
     */
    fun randomEntry(
        difficulty: Difficulty = Difficulty.MEDIUM,
        rng: Random = Random.Default
    ): SpawnEntry {
        val pool = poolForDifficulty(difficulty)
        return pool[rng.nextInt(pool.size)]
    }

    /**
     * Build a [Block] from a random [SpawnEntry].
     *
     * @param colorIndex  Index into [BlockColors]; wraps automatically.
     * @param difficulty  Current difficulty tier.
     * @param rng         Random instance.
     */
    fun randomBlock(
        colorIndex: Int,
        difficulty: Difficulty = Difficulty.MEDIUM,
        rng: Random = Random.Default
    ): Block {
        val entry = randomEntry(difficulty, rng)
        val color = BlockColors[colorIndex % BlockColors.size]
        return Block(shape = entry.matrix, color = color, name = entry.name)
    }

    /**
     * Spawn [count] blocks (default 3) ensuring no two consecutive blocks share
     * the same shape name, and colours are spread across the palette.
     *
     * @param count      How many blocks to spawn (typically 3).
     * @param difficulty Current difficulty tier.
     * @param rng        Random instance.
     */
    fun spawnBlocks(
        count: Int = 3,
        difficulty: Difficulty = Difficulty.MEDIUM,
        rng: Random = Random.Default
    ): List<Block> {
        val startColor = rng.nextInt(BlockColors.size)
        val blocks     = mutableListOf<Block>()
        var lastName   = ""

        repeat(count) { i ->
            var entry = randomEntry(difficulty, rng)
            // Avoid repeating the same shape name back-to-back for variety
            repeat(3) { if (entry.name == lastName) entry = randomEntry(difficulty, rng) }
            lastName = entry.name

            val color = BlockColors[(startColor + i) % BlockColors.size]
            blocks.add(Block(shape = entry.matrix, color = color, name = entry.name))
        }
        return blocks
    }

    /**
     * Deterministic spawn using a seed — useful for daily challenges or replays.
     *
     * @param seed       Long seed value.
     * @param count      Number of blocks.
     * @param difficulty Current difficulty tier.
     */
    fun spawnBlocksSeeded(
        seed: Long,
        count: Int = 3,
        difficulty: Difficulty = Difficulty.MEDIUM
    ): List<Block> = spawnBlocks(count = count, difficulty = difficulty, rng = Random(seed))

    // ── Diagnostics (debug/test only) ─────────────────────────────────────────

    /** Total entries in the pool for [difficulty] (after rotation expansion). */
    fun poolSize(difficulty: Difficulty = Difficulty.MEDIUM): Int = poolForDifficulty(difficulty).size

    /** All unique shape names available at [difficulty]. */
    fun availableShapeNames(difficulty: Difficulty = Difficulty.MEDIUM): List<String> =
        poolForDifficulty(difficulty).map { it.name }.distinct().sorted()
}
/*
package com.rpn.blockblaster.domain.engine

import com.rpn.blockblaster.core.designsystem.BlockColors
import com.rpn.blockblaster.domain.model.Block

data class ShapeTemplate(
    val name:   String,
    val matrix: List<List<Boolean>>,
    val weight: Int   // higher = more common
)

object BlockShapeFactory {

    private val templates: List<ShapeTemplate> = listOf(
        // ── Singles & dominoes ───────────────────────────────────────────────
        ShapeTemplate("Single",       listOf(listOf(true)), 10),
        ShapeTemplate("Domino-H",     listOf(listOf(true,true)), 10),
        ShapeTemplate("Domino-V",     listOf(listOf(true),listOf(true)), 10),
        // ── Tri lines ────────────────────────────────────────────────────────
        ShapeTemplate("Tri-H",        listOf(listOf(true,true,true)), 10),
        ShapeTemplate("Tri-V",        listOf(listOf(true),listOf(true),listOf(true)), 10),
        // ── Quad lines ───────────────────────────────────────────────────────
        ShapeTemplate("Quad-H",       listOf(listOf(true,true,true,true)), 6),
        ShapeTemplate("Quad-V",       listOf(listOf(true),listOf(true),listOf(true),listOf(true)), 6),
        // ── Penta lines ──────────────────────────────────────────────────────
        ShapeTemplate("Penta-H",      listOf(listOf(true,true,true,true,true)), 3),
        ShapeTemplate("Penta-V",      listOf(listOf(true),listOf(true),listOf(true),listOf(true),listOf(true)), 3),
        // ── Squares ──────────────────────────────────────────────────────────
        ShapeTemplate("Square-2",     listOf(listOf(true,true),listOf(true,true)), 8),
        ShapeTemplate("Square-3",     listOf(
            listOf(true,true,true),
            listOf(true,true,true),
            listOf(true,true,true)), 2),
        // ── L shapes ─────────────────────────────────────────────────────────
        ShapeTemplate("L",            listOf(listOf(true,false),listOf(true,false),listOf(true,true)), 6),
        ShapeTemplate("L-flip",       listOf(listOf(false,true),listOf(false,true),listOf(true,true)), 6),
        ShapeTemplate("L-rot90",      listOf(listOf(true,true,true),listOf(true,false,false)), 6),
        ShapeTemplate("L-rot270",     listOf(listOf(false,false,true),listOf(true,true,true)), 6),
        // ── J shapes ─────────────────────────────────────────────────────────
        ShapeTemplate("J",            listOf(listOf(true,true),listOf(true,false),listOf(true,false)), 6),
        ShapeTemplate("J-flip",       listOf(listOf(true,true),listOf(false,true),listOf(false,true)), 6),
        // ── T shapes ─────────────────────────────────────────────────────────
        ShapeTemplate("T-up",         listOf(listOf(true,true,true),listOf(false,true,false)), 5),
        ShapeTemplate("T-down",       listOf(listOf(false,true,false),listOf(true,true,true)), 5),
        ShapeTemplate("T-left",       listOf(listOf(true,false),listOf(true,true),listOf(true,false)), 5),
        ShapeTemplate("T-right",      listOf(listOf(false,true),listOf(true,true),listOf(false,true)), 5),
        // ── S/Z ──────────────────────────────────────────────────────────────
        ShapeTemplate("S",            listOf(listOf(false,true,true),listOf(true,true,false)), 5),
        ShapeTemplate("Z",            listOf(listOf(true,true,false),listOf(false,true,true)), 5),
        // ── Corners ──────────────────────────────────────────────────────────
        ShapeTemplate("Corner-TL",    listOf(listOf(true,true),listOf(true,false)), 8),
        ShapeTemplate("Corner-TR",    listOf(listOf(true,true),listOf(false,true)), 8),
        ShapeTemplate("Corner-BL",    listOf(listOf(true,false),listOf(true,true)), 8),
        ShapeTemplate("Corner-BR",    listOf(listOf(false,true),listOf(true,true)), 8),
        // ── Plus ─────────────────────────────────────────────────────────────
        ShapeTemplate("Plus",         listOf(
            listOf(false,true,false),
            listOf(true, true,true),
            listOf(false,true,false)), 2),
        // ── U shape ──────────────────────────────────────────────────────────
        ShapeTemplate("U",            listOf(
            listOf(true,false,true),
            listOf(true,true, true)), 3),
        // ── Hollow Square ────────────────────────────────────────────────────
        ShapeTemplate("Hollow-Sq",    listOf(
            listOf(true,true, true),
            listOf(true,false,true),
            listOf(true,true, true)), 1),
        // ── Big Corner ───────────────────────────────────────────────────────
        ShapeTemplate("BigCorner-TL", listOf(
            listOf(true,true,true),
            listOf(true,false,false),
            listOf(true,false,false)), 3),
        ShapeTemplate("BigCorner-TR", listOf(
            listOf(true,true,true),
            listOf(false,false,true),
            listOf(false,false,true)), 3),
        // ── W Pentomino ──────────────────────────────────────────────────────
        ShapeTemplate("W-shape",      listOf(
            listOf(true,false,false),
            listOf(true,true, false),
            listOf(false,true, true)), 2),
        // ── F Pentomino ──────────────────────────────────────────────────────
        ShapeTemplate("F-shape",      listOf(
            listOf(false,true,true),
            listOf(true,true, false),
            listOf(false,true, false)), 2),
        // ── P Pentomino ──────────────────────────────────────────────────────
        ShapeTemplate("P-shape",      listOf(
            listOf(true,true),
            listOf(true,true),
            listOf(true,false)), 3),
        // ── Z-Long ───────────────────────────────────────────────────────────
        ShapeTemplate("Z-long",       listOf(
            listOf(true,true,false,false),
            listOf(false,true,true,true)), 2)
    )

    private val weightedPool: List<ShapeTemplate> by lazy {
        templates.flatMap { t -> List(t.weight) { t } }
    }

    fun randomBlock(colorIndex: Int, rng: java.util.Random = java.util.Random()): Block {
        val template = weightedPool[rng.nextInt(weightedPool.size)]
        val color    = BlockColors[colorIndex % BlockColors.size]
        return Block(shape = template.matrix, color = color, name = template.name)
    }

    fun spawnThree(rng: java.util.Random = java.util.Random()): List<Block> {
        val startColor = rng.nextInt(BlockColors.size)
        return List(3) { i -> randomBlock((startColor + i) % BlockColors.size, rng) }
    }
}
*/


 */