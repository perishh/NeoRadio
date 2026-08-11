package com.example.neoradio.di

import com.example.neoradio.controller.PlayerController
import com.example.neoradio.repository.HomeRepository
import com.example.neoradio.repository.RegionsRepository
import com.example.neoradio.repository.StreamRepository
import com.example.neoradio.ui.fragment.home.HomeViewModel
import com.example.neoradio.ui.fragment.regions.RegionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.onClose

val appModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    singleOf(::RegionsRepository)
    singleOf(::StreamRepository)
    singleOf(::HomeRepository)

    singleOf(::PlayerController) onClose { it?.release() }

    viewModelOf(::HomeViewModel)
    viewModelOf(::RegionsViewModel)
}