package com.bnw.voip.di

import android.content.Context
import com.bnw.voip.voip.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.linphone.core.AVPFMode
import org.linphone.core.Core
import org.linphone.core.Factory
import org.linphone.core.TransportType
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LinphoneModule {

    @Provides
    @Singleton
    fun provideLinphoneCore(@ApplicationContext context: Context): Core {
        val factory = Factory.instance()
        factory.setDebugMode(true, "Linphone")
        val core = factory.createCore(null, null, context)

        // Configure transports
        core.transports?.apply {
            udpPort = 0
            tcpPort = 0
            tlsPort = -1
        }

        // Configure audio settings
        core.isEchoCancellationEnabled = true
        core.isAdaptiveRateControlEnabled = true

        // Configure network settings
        core.isIpv6Enabled = false
        core.setUserAgent("T", core.version)
        core.isKeepAliveEnabled = true
        core.guessHostname = true

        return core
    }
}