package com.rpn.blockblaster.core.di

import com.rpn.blockblaster.domain.usecase.game.*
import com.rpn.blockblaster.domain.usecase.score.*
import com.rpn.blockblaster.domain.usecase.settings.*
import com.rpn.blockblaster.feature.game.GameViewModel
import com.rpn.blockblaster.feature.gameover.GameOverViewModel
import com.rpn.blockblaster.feature.home.HomeViewModel
import com.rpn.blockblaster.feature.settings.SettingsViewModel
import com.rpn.blockblaster.service.SoundManager
import com.rpn.blockblaster.service.VibrationManager
import com.rpn.blockblaster.service.AdManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SoundManager(androidContext()) }
    single { VibrationManager(androidContext()) }
    single { AdManager(androidContext()) }

    factory { InitBoardUseCase() }
    factory { PlaceBlockUseCase() }
    factory { BlastLinesUseCase() }
    factory { SpawnBlocksUseCase() }
    factory { CheckGameOverUseCase(get()) }
    factory { CalculateScoreUseCase() }
    factory { SaveScoreUseCase(get()) }
    factory { GetBestScoreUseCase(get()) }
    factory { GetSettingsUseCase(get()) }
    factory { SaveSettingsUseCase(get()) }

    viewModel {
        GameViewModel(
            initBoard      = get(),
            placeBlock     = get(),
            blastLines     = get(),
            spawnBlocks    = get(),
            checkGameOver  = get(),
            calcScore      = get(),
            saveScore      = get(),
            getBestScore   = get(),
            getSettings    = get(),
            soundManager   = get(),
            vibrationManager = get()
        )
    }
    viewModel { HomeViewModel(getBestScore = get()) }
    viewModel { GameOverViewModel() }
    viewModel { SettingsViewModel(getSettings = get(), saveSettings = get()) }
}
