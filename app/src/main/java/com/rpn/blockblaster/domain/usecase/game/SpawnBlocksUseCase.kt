package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.engine.BlockShapeFactory
import com.rpn.blockblaster.domain.model.Block
import java.util.Random

class SpawnBlocksUseCase(private val rng: Random = Random()) {
    operator fun invoke(): List<Block?> =
        BlockShapeFactory.spawnThree(rng).map { it as Block? }
}
