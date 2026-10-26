package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.entry.DailyVehicleEntryScreen
import com.example.ui.expense.ExpenditureScreen
import com.example.ui.report.ReportsScreen
import com.example.ui.theme.ExpenseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    var showLogoutConfirm by remember { mutableStateOf(false) }

    // Startup silent authentication check
    LaunchedEffect(Unit) {
        authViewModel.attemptAutoSignIn(context)
    }

    // Authentication Guard: Ensure unauthorized users cannot access protected routes
    LaunchedEffect(currentUser, currentRoute) {
        if (currentUser == null) {
            if (currentRoute != null && currentRoute != AppRoute.Login.route) {
                navController.navigate(AppRoute.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            if (currentRoute == AppRoute.Login.route) {
                navController.navigate(AppRoute.Dashboard.route) {
                    popUpTo(AppRoute.Login.route) { inclusive = true }
                }
            }
        }
    }

    val isAuthScreen = currentRoute == AppRoute.Login.route || currentUser == null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (!isAuthScreen) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.dash_puc_icon),
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Dash Pollution",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }
                    },
                    actions = {
                        // Authorized Operator User Badge
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentUser?.name ?: "Operator",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1
                                )
                            }
                        }

                        // Logout Action
                        IconButton(
                            onClick = { showLogoutConfirm = true },
                            modifier = Modifier.testTag("btn_logout")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Log Out",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // 1. Dashboard Tab
                    NavigationBarItem(
                        selected = currentRoute == AppRoute.Dashboard.route,
                        onClick = {
                            navController.navigate(AppRoute.Dashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    // 2. Daily Entry Tab
                    NavigationBarItem(
                        selected = currentRoute == AppRoute.DailyEntry.route,
                        onClick = {
                            navController.navigate(AppRoute.DailyEntry.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.EditNote, contentDescription = "Daily Entry") },
                        label = { Text("Daily Entry", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_daily_entry")
                    )

                    // 3. Expenses Tab
                    NavigationBarItem(
                        selected = currentRoute == AppRoute.Expenditures.route,
                        onClick = {
                            navController.navigate(AppRoute.Expenditures.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Expenses") },
                        label = { Text("Expenses", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_expenditures")
                    )

                    // 4. Reports Tab
                    NavigationBarItem(
                        selected = currentRoute == AppRoute.Reports.route,
                        onClick = {
                            navController.navigate(AppRoute.Reports.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                        label = { Text("Reports", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_reports")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser == null) AppRoute.Login.route else AppRoute.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Unauthenticated Route: Login Screen (Firebase Auth & Google Sign-In)
            composable(AppRoute.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    modifier = Modifier.fillMaxSize(),
                    onAuthSuccess = {
                        navController.navigate(AppRoute.Dashboard.route) {
                            popUpTo(AppRoute.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Authenticated Route: Main Dashboard
            composable(AppRoute.Dashboard.route) {
                if (currentUser != null) {
                    DashboardScreen(
                        viewModel = mainViewModel,
                        onNavigateToDailyEntry = {
                            navController.navigate(AppRoute.DailyEntry.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToReports = {
                            navController.navigate(AppRoute.Reports.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // Authenticated Route: Daily Vehicle Entry
            composable(AppRoute.DailyEntry.route) {
                if (currentUser != null) {
                    DailyVehicleEntryScreen(
                        viewModel = mainViewModel
                    )
                }
            }

            // Authenticated Route: Others Expenditure
            composable(AppRoute.Expenditures.route) {
                if (currentUser != null) {
                    ExpenditureScreen(
                        viewModel = mainViewModel
                    )
                }
            }

            // Authenticated Route: Reports
            composable(AppRoute.Reports.route) {
                if (currentUser != null) {
                    ReportsScreen(
                        viewModel = mainViewModel,
                        onNavigateToDailyEntry = {
                            navController.navigate(AppRoute.DailyEntry.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log Out?") },
            text = { Text("Are you sure you want to log out of ${currentUser?.email}? You will be redirected to the login screen.") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout()
                        showLogoutConfirm = false
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("btn_confirm_logout")
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
