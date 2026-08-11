package com.example.neoradio.di

import com.example.neoradio.controller.PlayerController
import com.example.neoradio.repository.HomeRepository
import com.example.neoradio.ui.fragment.home.HomeViewModel
import com.example.neoradio.ui.fragment.regions.RegionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.onClose

val appModule = module {
    single {
        PlayerController(
            androidContext(),
            CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
    } onClose { it?.release() }
    singleOf(::HomeRepository)

    viewModelOf(::HomeViewModel)
    viewModelOf(::RegionsViewModel)
}