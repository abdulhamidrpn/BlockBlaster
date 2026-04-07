package com.rpn.blockblaster

import android.app.Application
import com.rpn.blockblaster.core.di.appModule
import com.rpn.blockblaster.core.di.databaseModule
import com.rpn.blockblaster.core.di.dataStoreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade

class BlockBlasterApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BlockBlasterApp)
            modules(appModule, databaseModule, dataStoreModule)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
    }
}
