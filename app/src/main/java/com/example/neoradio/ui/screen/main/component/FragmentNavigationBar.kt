package com.example.neoradio.ui.screen.main.component

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.neoradio.R
import com.example.neoradio.ui.screen.main.LocalBottomSheet
import com.example.neoradio.ui.screen.main.LocalNavController
import kotlinx.coroutines.launch

@Composable
fun FragmentNavigationBar() {
    val navController = LocalNavController.current
    val bottomSheet = LocalBottomSheet.current

    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = Modifier.height(84.dp)
    ) {
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
            onClick = {
                coroutineScope.launch {
                    bottomSheet.partialExpand()
                }
                navController.navigate("home") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(Icons.Rounded.Home, contentDescription = null)
            },
            label = {
                Text("Αρχική")
            }
        )
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.route == "regions" } == true,
            onClick = {
                coroutineScope.launch {
                    bottomSheet.partialExpand()
                }
                navController.navigate("regions") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(painterResource(R.drawable.globe), contentDescription = null)
            },
            label = {
                Text("Περιοχές")
            }
        )
    }
}