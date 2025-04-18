package com.fajarxfce.feature.main.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fajarxfce.core.designsystem.theme.AppTheme
import com.fajarxfce.feature.account.navigation.AccountBaseRoute
import com.fajarxfce.feature.main.R
import com.fajarxfce.feature.main.navigation.AccountRoute
import com.fajarxfce.feature.main.navigation.MainNavHost
import com.fajarxfce.shopping.navigation.ShoppingBaseRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.SHOP) }
// Remember the navigation suite state
    val navigationSuiteState = rememberNavigationSuiteScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // Create a nested scroll connection to detect scroll direction
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Hide when scrolling down, show when scrolling up
                if (available.y < 0) {
                    coroutineScope.launch {
                        navigationSuiteState.hide()
                    }
                } else if (available.y > 0) {
                    coroutineScope.launch {
                        navigationSuiteState.show()
                    }
                }
                return Offset.Zero
            }
        }
    }
    val myNavigationSuiteItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.background,
            selectedIconColor = MaterialTheme.colorScheme.primary,
        ),
    )

    NavigationSuiteScaffold(
        state = navigationSuiteState,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface,
        ),
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = stringResource(it.contentDescription),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(it.label),
                        )
                    },
                    selected = it == currentDestination,
                    onClick = {
                        currentDestination = it
                        navController.navigate(it.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = myNavigationSuiteItemColors,
                )
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .nestedScroll(nestedScrollConnection)  // Connect the scroll events
            ) {
                MainNavHost(navController = navController)
            }
        },
    )
}

enum class AppDestinations(
    @StringRes val label: Int,
    val icon: ImageVector,
    @StringRes val contentDescription: Int,
    val route: Any,
) {
    SHOP(R.string.shop, Icons.Default.Shop, R.string.shop, ShoppingBaseRoute),
//    CART(R.string.cart, Icons.Default.ShoppingCart, R.string.cart, AccountBaseRoute),
//    FAVORITE(R.string.favorite, Icons.Default.FavoriteBorder, R.string.favorite, AccountBaseRoute),
    ACCOUNT(R.string.account, Icons.Default.AccountCircle, R.string.account, AccountBaseRoute),
}

@Preview
@Composable
private fun MainScreenPreview() {
    AppTheme {
        val navController = rememberNavController()
        MainScreen(navController = navController)
    }
}