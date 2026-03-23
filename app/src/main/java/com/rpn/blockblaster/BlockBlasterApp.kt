package com.rpn.blockblaster

import android.app.Application
import com.rpn.blockblaster.core.di.appModule
import com.rpn.blockblaster.core.di.databaseModule
import com.rpn.blockblaster.core.di.dataStoreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BlockBlasterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BlockBlasterApp)
            modules(appModule, databaseModule, dataStoreModule)
        }
    }
}
