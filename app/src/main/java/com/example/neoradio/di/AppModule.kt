package com.example.neoradio.di

import com.example.neoradio.repository.PlayerRepository
import com.example.neoradio.ui.miniplayer.MiniplayerViewModel
import com.example.neoradio.ui.screen.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.onClose

val appModule = module {
    single {
        PlayerRepository(
            androidContext(),
            CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
    } onClose { it?.release() }

    viewModelOf(::HomeViewModel)
    viewModelOf(::MiniplayerViewModel)
}