package com.example.neoradio.ui.screen.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neoradio.ui.fragment.home.HomeFragment
import com.example.neoradio.ui.fragment.regions.RegionsFragment
import com.example.neoradio.ui.screen.main.component.FragmentNavigationBar
import com.example.neoradio.ui.screen.main.component.Miniplayer

val LocalBottomSheet = staticCompositionLocalOf<SheetState> {
    error("SheetState not provided")
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavHostController not provided")
}

@Composable
fun MainScreen() {
    val density = LocalDensity.current

    val navController = rememberNavController()
    val bottomSheetState = rememberStandardBottomSheetState()
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    var truePeekHeight by remember { mutableIntStateOf(85) }
    val animatedPeekHeight by animateDpAsState(targetValue = with(density) { truePeekHeight.toDp() })

    CompositionLocalProvider(LocalBottomSheet provides bottomSheetState) {
        CompositionLocalProvider(LocalNavController provides navController) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    FragmentNavigationBar()
                }
            ) { innerPadding ->
                BottomSheetScaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = animatedPeekHeight,
                    sheetDragHandle = null,
                    sheetContent = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        ) {
                            Miniplayer(
                                modifier = Modifier.onSizeChanged { size ->
                                    truePeekHeight = size.height
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeFragment()
                        }
                        composable("regions") {
                            RegionsFragment()
                        }
                    }
                }
            }
        }
    }
}