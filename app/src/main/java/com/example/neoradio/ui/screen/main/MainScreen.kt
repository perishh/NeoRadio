package com.example.neoradio.ui.screen.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neoradio.ui.fragment.genres.GenresFragment
import com.example.neoradio.ui.fragment.home.HomeFragment
import com.example.neoradio.ui.fragment.regions.RegionsFragment
import com.example.neoradio.ui.screen.main.component.BottomSheet
import com.example.neoradio.ui.screen.main.component.FragmentNavigationBar
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

val LocalBottomSheet = staticCompositionLocalOf<SheetState> {
    error("SheetState not provided")
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavHostController not provided")
}

@Serializable
sealed class Fragments(
    @property:Transient val icon: ImageVector = Icons.Rounded.Home,
    @property:Transient val label: String = ""
) {
    @Serializable
    object Home : Fragments(Icons.Rounded.Home, "Αρχική")

    @Serializable
    object Regions : Fragments(Icons.Rounded.LocationOn, "Περιοχές")

    @Serializable
    object Genres : Fragments(Icons.Rounded.MusicNote, "Είδη")

    companion object {
        val all = listOf(Home, Regions, Genres)
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val density = LocalDensity.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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
                    sheetContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    sheetContent = {
                        BottomSheet { truePeekHeight = it }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Fragments.Home,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<Fragments.Home> {
                            HomeFragment()
                        }
                        composable<Fragments.Regions> {
                            RegionsFragment()
                        }
                        composable<Fragments.Genres> {
                            GenresFragment()
                        }
                    }
                }
            }
        }
    }
}