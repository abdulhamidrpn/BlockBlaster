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
            listOf(true,true, true)), 3)
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
