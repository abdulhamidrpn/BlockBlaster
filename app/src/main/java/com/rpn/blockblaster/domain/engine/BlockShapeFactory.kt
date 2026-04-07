package com.rpn.blockblaster.domain.engine

import com.rpn.blockblaster.core.designsystem.BlockColors
import com.rpn.blockblaster.domain.model.Block
import kotlin.math.max
import kotlin.random.Random

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
        name = "Corner-3",
        matrix = listOf(
            listOf(true, true, true),
            listOf(true, false, false),
            listOf(true, false, false)
        ),
        weight = 6,
        difficulty = Difficulty.MEDIUM
    ),

    // ── HARD ─────────────────────────────────────────────────────────────────

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
        name = "Z-Shape",
        matrix = listOf(
            listOf(true,  true, false),
            listOf(false, true, true)
        ),
        weight = 6,
        difficulty = Difficulty.HARD
    ),
    ShapeDefinition(
        name = "Plus",
        matrix = listOf(
            listOf(false, true,  false),
            listOf(true,  true,  true),
            listOf(false, true,  false)
        ),
        weight = 4,
        difficulty = Difficulty.HARD,
        rotatable = false
    ),
    ShapeDefinition(
        name = "U-Shape",
        matrix = listOf(
            listOf(true, false, true),
            listOf(true, true,  true)
        ),
        weight = 4,
        difficulty = Difficulty.HARD
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

    private fun poolForScore(score: Int): List<SpawnEntry> = poolHard

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Pick a single random [SpawnEntry] from the pool appropriate for [score].
     *
     * @param score      Current player score — drives difficulty progression.
     * @param rng        Kotlin [Random] instance (injectable for determinism/tests).
     */
    fun randomEntry(
        score : Int    = 0,
        rng   : Random = Random.Default
    ): SpawnEntry {
        val pool = poolForScore(score)
        return pool[rng.nextInt(pool.size)]
    }

    /**
     * Build a [Block] from a random [SpawnEntry].
     *
     * @param colorIndex  Index into [BlockColors]; wraps automatically.
     * @param score       Current player score.
     * @param rng         Random instance.
     */
    fun randomBlock(
        colorIndex : Int,
        score      : Int    = 0,
        rng        : Random = Random.Default
    ): Block {
        val entry = randomEntry(score, rng)
        val color = BlockColors[colorIndex % BlockColors.size]
        return Block(shape = entry.matrix, color = color, name = entry.name)
    }

    /**
     * Spawn [count] blocks (default 3) ensuring no two consecutive blocks share
     * the same shape name, and colours are spread across the palette.
     *
     * @param count  How many blocks to spawn (typically 3).
     * @param score  Current player score.
     * @param rng    Random instance.
     */
    fun spawnBlocks(
        count : Int    = 3,
        score : Int    = 0,
        rng   : Random = Random.Default
    ): List<Block> {
        val startColor = rng.nextInt(BlockColors.size)
        val blocks     = mutableListOf<Block>()
        var lastName   = ""

        repeat(count) { i ->
            var entry = randomEntry(score, rng)
            // Avoid repeating the same shape name back-to-back for variety
            repeat(3) { if (entry.name == lastName) entry = randomEntry(score, rng) }
            lastName = entry.name

            val color = BlockColors[(startColor + i) % BlockColors.size]
            blocks.add(Block(shape = entry.matrix, color = color, name = entry.name))
        }
        return blocks
    }

    /**
     * Deterministic spawn using a seed — useful for daily challenges or replays.
     *
     * @param seed   Long seed value.
     * @param count  Number of blocks.
     * @param score  Current player score.
     */
    fun spawnBlocksSeeded(
        seed  : Long,
        count : Int = 3,
        score : Int = 0
    ): List<Block> = spawnBlocks(count = count, score = score, rng = Random(seed))

    // ── Diagnostics (debug/test only) ─────────────────────────────────────────

    /** Total entries in the pool for [score] (after rotation expansion). */
    fun poolSize(score: Int = 0): Int = poolForScore(score).size

    /** All unique shape names available at [score]. */
    fun availableShapeNames(score: Int = 0): List<String> =
        poolForScore(score).map { it.name }.distinct().sorted()
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
