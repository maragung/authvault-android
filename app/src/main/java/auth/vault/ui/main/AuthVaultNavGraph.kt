package auth.vault.ui.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import auth.vault.presentation.viewmodel.LockScreenViewModel
import auth.vault.presentation.viewmodel.SettingsViewModel
import auth.vault.presentation.viewmodel.TokenDisplayItem
import auth.vault.presentation.viewmodel.UnlockResult
import auth.vault.presentation.viewmodel.VaultViewModel
import auth.vault.presentation.viewmodel.VaultUiState
import auth.vault.ui.screens.addtoken.AddTokenScreen
import auth.vault.ui.screens.auth.LockScreen
import auth.vault.ui.screens.auth.SetupScreen
import auth.vault.ui.screens.edittoken.EditTokenScreen
import auth.vault.ui.screens.home.VaultHomeScreen
import auth.vault.ui.screens.importer.ImportScreen
import auth.vault.ui.screens.onboarding.OnboardingScreen
import auth.vault.ui.screens.pingenerator.PasswordGeneratorScreen
import auth.vault.ui.screens.pingenerator.PinUnlockScreen
import auth.vault.ui.screens.qrgallery.QrGalleryImportScreen
import auth.vault.ui.screens.qscanner.QrScannerScreen
import auth.vault.ui.screens.recovery.RecoveryCodeScreen
import auth.vault.ui.screens.securenotes.SecureNotesScreen
import auth.vault.ui.screens.settings.SettingsScreen
import auth.vault.ui.screens.share.ShareTokenScreen
import auth.vault.ui.screens.stats.UsageStatsScreen
import auth.vault.ui.util.collectAsStateWrapper
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

@Composable
fun AuthVaultNavGraph(
    navController: NavHostController,
    uiState: VaultUiState,
    windowSizeClass: WindowSizeClass,
    vaultViewModel: VaultViewModel = hiltViewModel(),
    lockViewModel: LockScreenViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isWideScreen = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    var showOnboarding by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vaultViewModel.checkVaultTimeout()
    }

    NavHost(
        navController = navController,
        startDestination = if (showOnboarding) "onboarding" else if (uiState.isLocked) "lock" else "home"
    ) {
        composable("onboarding", enterTransition = { fadeIn(tween(300)) }, exitTransition = { fadeOut(tween(300)) }) {
            OnboardingScreen(onComplete = { showOnboarding = false; navController.navigate("lock") { popUpTo("onboarding") { inclusive = true } } })
        }

        composable("lock", enterTransition = { fadeIn(tween(300)) }, exitTransition = { fadeOut(tween(300)) }, popEnterTransition = { fadeIn(tween(300)) }, popExitTransition = { fadeOut(tween(300)) }) {
            val lockUiState by lockViewModel.uiState.collectAsStateWrapper()
            LockScreen(
                uiState = lockUiState,
                onUnlock = { password -> vaultViewModel.unlockVault(password) { result -> if (result is UnlockResult.Success || result is UnlockResult.FirstTimeSetup) { navController.navigate("home") { popUpTo("lock") { inclusive = true } } } } },
                onNavigateToSetup = { navController.navigate("setup") { popUpTo("lock") { inclusive = true } } },
                onBiometricAuth = { (context as? Activity)?.finish() }
            )
        }

        composable("setup", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, exitTransition = { fadeOut(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            SetupScreen(onSetupComplete = { navController.navigate("home") { popUpTo("setup") { inclusive = true } } })
        }

        composable("home", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, exitTransition = { slideOutHorizontally { -it } + fadeOut(tween(300)) }, popEnterTransition = { slideInHorizontally { -it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            BackHandler { (context as? Activity)?.finish() }
            VaultHomeScreen(
                uiState = uiState,
                isWideScreen = isWideScreen,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToAddToken = { navController.navigate("add_token") },
                onNavigateToEditToken = { token -> navController.navigate("edit_token/${token.tokenId}/${token.serviceLabel}/${token.accountName}") },
                onNavigateToImport = { navController.navigate("import") },
                onDeleteToken = { vaultViewModel.deleteToken(it) },
                onSearchQueryChange = { vaultViewModel.setSearchQuery(it) },
                onLockVault = { vaultViewModel.lockVault() },
                onTogglePin = { vaultViewModel.togglePinToken(it) },
                onCategorySelected = { vaultViewModel.setActiveCategory(it) },
                onTagSelected = { vaultViewModel.setActiveTag(it) }
            )
        }

        composable("settings", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("add_token", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            AddTokenScreen(
                onTokenAdded = { s, a, sec, alg, dig, ts -> vaultViewModel.addToken(s, a, sec, alg, dig, ts); navController.popBackStack() },
                onNavigateToQrScanner = { navController.navigate("qr_scanner") },
                onNavigateToGallery = { navController.navigate("qr_gallery_import") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("qr_scanner", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            QrScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                onQrScanned = { scannedSecret -> navController.previousBackStackEntry?.savedStateHandle?.set("scanned_secret", scannedSecret); navController.popBackStack() }
            )
        }

        composable("qr_gallery_import", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            QrGalleryImportScreen(
                onImported = { uri -> vaultViewModel.importTokensFromUri(uri); navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("edit_token/{tokenId}/{serviceLabel}/{accountName}", arguments = listOf(navArgument("tokenId") { type = NavType.LongType }, navArgument("serviceLabel") { type = NavType.StringType }, navArgument("accountName") { type = NavType.StringType }), enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) { backStackEntry ->
            val tokenId = backStackEntry.arguments?.getLong("tokenId") ?: 0L
            val serviceLabel = backStackEntry.arguments?.getString("serviceLabel") ?: ""
            val accountName = backStackEntry.arguments?.getString("accountName") ?: ""
            EditTokenScreen(
                initialServiceLabel = serviceLabel, initialAccountName = accountName, initialSecretKey = "", initialAlgorithm = "SHA1", initialDigitCount = 6, initialTimeStep = 30L,
                onSave = { svc, acc, secret, algo, digits, timeStep -> vaultViewModel.updateToken(tokenId, svc, acc, secret, algo, digits, timeStep); navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("import", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            ImportScreen(
                onImportUri = { vaultViewModel.importTokensFromUri(it); navController.popBackStack() },
                onImportFile = { vaultViewModel.importTokensFromUri(it.toString()); navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("recovery_codes", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            RecoveryCodeScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("secure_notes", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            SecureNotesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("password_generator", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            PasswordGeneratorScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("usage_stats", enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) {
            UsageStatsScreen(
                stats = uiState.tokens.map { auth.vault.ui.screens.stats.UsageStatsItem(it.serviceLabel, it.accountName, it.usageCount, System.currentTimeMillis()) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("pin_unlock", enterTransition = { fadeIn(tween(300)) }, exitTransition = { fadeOut(tween(300)) }) {
            PinUnlockScreen(
                onUnlocked = { navController.navigate("home") { popUpTo("pin_unlock") { inclusive = true } } },
                onBackToPassword = { navController.navigate("lock") { popUpTo("pin_unlock") { inclusive = true } } }
            )
        }

        composable("share_token/{serviceLabel}/{secretKey}/{algorithm}/{digitCount}/{timeStep}", arguments = listOf(navArgument("serviceLabel") { type = NavType.StringType }, navArgument("secretKey") { type = NavType.StringType }, navArgument("algorithm") { type = NavType.StringType }, navArgument("digitCount") { type = NavType.IntType }, navArgument("timeStep") { type = NavType.LongType }), enterTransition = { slideInHorizontally { it } + fadeIn(tween(300)) }, popExitTransition = { slideOutHorizontally { it } + fadeOut(tween(300)) }) { backStackEntry ->
            val serviceLabel = backStackEntry.arguments?.getString("serviceLabel") ?: ""
            val secretKey = backStackEntry.arguments?.getString("secretKey") ?: ""
            val algorithm = backStackEntry.arguments?.getString("algorithm") ?: "SHA1"
            val digitCount = backStackEntry.arguments?.getInt("digitCount") ?: 6
            val timeStep = backStackEntry.arguments?.getLong("timeStep") ?: 30L
            ShareTokenScreen(
                serviceLabel = serviceLabel, secretKey = secretKey, algorithm = algorithm, digitCount = digitCount, timeStep = timeStep,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
