package com.blackout.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.blackout.app.data.datastore.FeatureToggles
import com.blackout.app.data.datastore.SettingsDataStore
import com.blackout.app.data.repository.QuotesRepository
import com.blackout.app.ui.favorites.FavoritesScreen
import com.blackout.app.ui.journal.JournalScreen
import com.blackout.app.ui.profile.ProfileScreen
import com.blackout.app.ui.journey.JourneyScreen
import com.blackout.app.ui.quiz.QuizRoute
import com.blackout.app.ui.today.TodayScreen
import com.blackout.app.ui.settings.SettingsScreen
import com.core.designsystem.components.CustomCollapsibleTopAppBar
import com.core.designsystem.components.FloatingBottomNavigationBar
import com.core.designsystem.components.NavigationItem
import com.core.designsystem.theme.CoreTheme
import com.materialkolor.PaletteStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@Serializable
data object Home
@Serializable
data object Settings
@Serializable
data object Journal
@Serializable
data object Favorites
@Serializable
data object Quiz

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var quotesRepository: QuotesRepository

    // Flipped once the first settings snapshot has been read; keeps the splash up until then
    // so users never see the default seed colour / language flash on cold start.
    private var settingsLoaded = false

    // Last dark/light value applied by the theme effect; re-applied after the splash is removed
    private var barsDark = false

    private fun applySystemBars(dark: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ) { dark },
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ) { dark }
        )
    }

    @SuppressLint("LocalContextConfigurationRead")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { !settingsLoaded }
        splash.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .setDuration(250L)
                .withEndAction {
                    provider.remove()
                    applySystemBars(barsDark) // the system resets bar icons when the splash goes away
                }
                .start()
            provider.iconView.animate().scaleX(1.15f).scaleY(1.15f).setDuration(250L).start()
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // sensible default (follows the system) before anything is drawn
        setContent {
            val settings by settingsDataStore.uiSettings.collectAsState(initial = null)
            val featureState by settingsDataStore.features.collectAsState(initial = null)
            val reminderTime by settingsDataStore.reminderTime.collectAsState(initial = null)

            // Nothing to draw until DataStore has delivered the first snapshot (splash covers this).
            val ui = settings ?: return@setContent
            val features = featureState ?: return@setContent
            SideEffect { settingsLoaded = true }

            val context = LocalContext.current

            LaunchedEffect(reminderTime) {
                reminderTime?.let {
                    com.blackout.app.domain.notification.WorkManagerScheduler.scheduleDailyReminder(context, it)
                }
            }
            LaunchedEffect(ui.language) {
                val targetLocales = if (ui.language == SettingsDataStore.LANGUAGE_SYSTEM) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(ui.language)
                }
                if (AppCompatDelegate.getApplicationLocales() != targetLocales) {
                    AppCompatDelegate.setApplicationLocales(targetLocales)
                }
            }

            val useDarkTheme = when (ui.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val permissionState = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle permission result if needed
                }
                LaunchedEffect(Unit) {
                    permissionState.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            DisposableEffect(useDarkTheme) {
                barsDark = useDarkTheme
                applySystemBars(useDarkTheme)
                onDispose { }
            }

            val paletteStyle = remember(ui.paletteStyle) {
                runCatching { PaletteStyle.valueOf(ui.paletteStyle) }.getOrDefault(PaletteStyle.TonalSpot)
            }

            CoreTheme(
                seedColorInt = ui.seedColor,
                darkTheme = useDarkTheme,
                dynamicColor = ui.dynamicColor,
                paletteStyle = paletteStyle
            ) {
                MainNavigation(
                    activityContext = this@MainActivity,
                    settingsDataStore = settingsDataStore,
                    quotesRepository = quotesRepository,
                    features = features,
                    languageCode = ui.language,
                )
            }
        }
    }
}

@Composable
fun MainNavigation(
    activityContext: android.content.Context,
    settingsDataStore: SettingsDataStore,
    quotesRepository: QuotesRepository,
    features: FeatureToggles,
    languageCode: String = "en",
) {
    val navController = rememberNavController()

    var selectedDestination by remember { mutableStateOf(AppDestinations.TODAY) }

    NavHost(
        navController = navController,
        startDestination = Home,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) }
    ) {
        composable<Home> {
            BlackoutApp(
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it },
                features = features,
                quotesRepository = quotesRepository,
                languageCode = languageCode,
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToJournalEntries = { navController.navigate(Journal) },
                onNavigateToFavoriteQuotes = { navController.navigate(Favorites) },
                onStartQuiz = { navController.navigate(Quiz) { launchSingleTop = true } },
            )
        }

        composable<Settings> {
            SettingsScreen(
                settingsDataStore = settingsDataStore,
                activityContext = activityContext,
                features = features,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Journal> {
            JournalScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Favorites> {
            FavoritesScreen(
                quotesRepository = quotesRepository,
                languageCode = languageCode,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Quiz> {
            QuizRoute(
                onFinished = { navController.popBackStack() },
                onClose = { navController.popBackStack() },
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlackoutApp(
    selectedDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit,
    features: FeatureToggles,
    quotesRepository: QuotesRepository,
    languageCode: String = "en",
    onNavigateToFavoriteQuotes: () -> Unit,
    onNavigateToJournalEntries: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onStartQuiz: () -> Unit,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomCollapsibleTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = selectedDestination,
                        modifier = Modifier.fillMaxWidth(),
                        transitionSpec = {
                            val isMovingRight = targetState.ordinal > initialState.ordinal
                            val duration = 600

                            val slideIn = slideInVertically(
                                initialOffsetY = { fullHeight -> if (isMovingRight) fullHeight else -fullHeight },
                                animationSpec = tween(duration)
                            )
                            val slideOut = slideOutVertically(
                                targetOffsetY = { fullHeight -> if (isMovingRight) -fullHeight else fullHeight },
                                animationSpec = tween(duration)
                            )

                            (slideIn togetherWith slideOut).using(SizeTransform(clip = false))
                        },
                        contentAlignment = Alignment.CenterStart,
                        label = "FlipCardTitle"
                    ) { dest ->
                        Text(
                            text = dest.getLabel(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = stringResource(R.string.streak_description),
                                    tint =
//                                        if (uiState.hasWorkedOutToday) {
                                        Color(0xFFFFA726),
//                                        }
//                                        else {
//                                            MaterialTheme.colorScheme.outline
//                                        },
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
//                                    text = uiState.currentStreak.toString(),
                                    text = "3", // TODO: PLACEHOLDER
                                    fontWeight = FontWeight.Bold,
                                    color =
//                                        if (uiState.hasWorkedOutToday) {
                                        Color(0xFFFFA726),
//                                        }
//                                        else {
//                                            MaterialTheme.colorScheme.outline
//                                        },
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            // The design-system bar is generic, so map our enum into NavigationItems.
            // getLabel() is @Composable; that's fine inside the inline `map`.
            val navItems = AppDestinations.entries.map { dest ->
                NavigationItem(
                    destination = dest,
                    icon = dest.icon,
                    selectedIcon = dest.selectedIcon,
                    label = dest.getLabel()
                )
            }
            FloatingBottomNavigationBar(
                items = navItems,
                selectedDestination = selectedDestination,
                onDestinationSelected = { dest ->
                    if (dest != selectedDestination) {
                        onDestinationSelected(dest)
                    }
                }
            )
        }
    ) { innerPadding ->
        val bottomBarPadding = innerPadding.calculateBottomPadding()
        AnimatedContent(
            targetState = selectedDestination,
            transitionSpec = {
                val isMovingRight = targetState.ordinal > initialState.ordinal
                if (isMovingRight) {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(300)
                    )
                } else {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    )
                }
            },
            modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
            label = "ScreenTransition"
        ) { destination ->
            when (destination) {
                AppDestinations.JOURNEY -> JourneyScreen(

                )

                AppDestinations.TODAY -> TodayScreen(
                    quotesRepository = quotesRepository,
                    languageCode = languageCode,
                    onStartQuiz = onStartQuiz,
                )

                AppDestinations.PROFILE -> ProfileScreen(
                    onNavigateToFavoriteQuotes = onNavigateToFavoriteQuotes,
                    onNavigateToJournalEntries = onNavigateToJournalEntries,
                    onNavigateToSettings = onNavigateToSettings,
                    features = features,
                    bottomPadding = bottomBarPadding,
                )
            }
        }
    }
}

enum class AppDestinations(
    @StringRes val labelRes: Int?,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    JOURNEY(R.string.nav_journey, Icons.Outlined.Map, Icons.Filled.Map),
    TODAY(null, Icons.Outlined.WbSunny, Icons.Filled.WbSunny),
    PROFILE(R.string.nav_profile, Icons.Outlined.Person, Icons.Filled.Person)
}

@Composable
fun AppDestinations.getLabel(): String {
    return when (this) {
        AppDestinations.TODAY -> {
            val dayOfWeek = LocalDate.now()
                .dayOfWeek
                .getDisplayName(TextStyle.FULL, Locale.getDefault())
                .lowercase()
            "$dayOfWeek."
        }
        else -> this.labelRes?.let { stringResource(id = it) }.orEmpty()
    }
}