package com.zincstate.cosmichoroscope

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// --- Constants & Optimization Flags ---

val CosmicBlack = Color(0xFF050508)
val CosmicDarkBlue = Color(0xFF0F172A)
val StarWhite = Color(0xFFE2E8F0)
val ElementFireColor = Color(0xFFFB923C)
val ElementEarthColor = Color(0xFF34D399)
val ElementAirColor = Color(0xFF7DD3FC)
val ElementWaterColor = Color(0xFFA78BFA)
val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)

// Pre-allocate Gradients to save allocation during composition
val FireGradient = Brush.linearGradient(listOf(Color(0xFFF97316).copy(0.15f), Color(0xFF7F1D1D).copy(0.15f)))
val EarthGradient = Brush.linearGradient(listOf(Color(0xFF10B981).copy(0.15f), Color(0xFF14532D).copy(0.15f)))
val AirGradient = Brush.linearGradient(listOf(Color(0xFF0EA5E9).copy(0.15f), Color(0xFF312E81).copy(0.15f)))
val WaterGradient = Brush.linearGradient(listOf(Color(0xFF8B5CF6).copy(0.15f), Color(0xFF1E3A8A).copy(0.15f)))

// Replacement for expensive blur effect
val GlowGradient = Brush.radialGradient(
    colors = listOf(
        Color(0xFF6366F1).copy(alpha = 0.25f),
        Color(0xFF6366F1).copy(alpha = 0.05f),
        Color.Transparent
    )
)

@Immutable
data class ZodiacSign(
    val name: String,
    val dates: String,
    val element: String,
    val symbol: String,
    val rulingPlanet: String,
    val compatibility: String,
    val color: Color,
    val bgBrush: Brush,
    val traits: List<String>
)

// Optimized: Moved list creation outside Composable
val ZODIAC_SIGNS = listOf(
    ZodiacSign("Aries", "Mar 21 - Apr 19", "Fire", "♈", "Mars", "Libra", ElementFireColor, FireGradient, listOf("Courageous", "Determined", "Optimistic")),
    ZodiacSign("Taurus", "Apr 20 - May 20", "Earth", "♉", "Venus", "Scorpio", ElementEarthColor, EarthGradient, listOf("Reliable", "Patient", "Devoted")),
    ZodiacSign("Gemini", "May 21 - Jun 20", "Air", "♊", "Mercury", "Sagittarius", ElementAirColor, AirGradient, listOf("Gentle", "Affectionate", "Curious")),
    ZodiacSign("Cancer", "Jun 21 - Jul 22", "Water", "♋", "Moon", "Capricorn", ElementWaterColor, WaterGradient, listOf("Tenacious", "Imaginative", "Loyal")),
    ZodiacSign("Leo", "Jul 23 - Aug 22", "Fire", "♌", "Sun", "Aquarius", ElementFireColor, FireGradient, listOf("Creative", "Passionate", "Warm-hearted")),
    ZodiacSign("Virgo", "Aug 23 - Sep 22", "Earth", "♍", "Mercury", "Pisces", ElementEarthColor, EarthGradient, listOf("Loyal", "Analytical", "Hardworking")),
    ZodiacSign("Libra", "Sep 23 - Oct 22", "Air", "♎", "Venus", "Aries", ElementAirColor, AirGradient, listOf("Cooperative", "Diplomatic", "Gracious")),
    ZodiacSign("Scorpio", "Oct 23 - Nov 21", "Water", "♏", "Pluto", "Taurus", ElementWaterColor, WaterGradient, listOf("Resourceful", "Brave", "Passionate")),
    ZodiacSign("Sagittarius", "Nov 22 - Dec 21", "Fire", "♐", "Jupiter", "Gemini", ElementFireColor, FireGradient, listOf("Generous", "Idealistic", "Humorous")),
    ZodiacSign("Capricorn", "Dec 22 - Jan 19", "Earth", "♑", "Saturn", "Cancer", ElementEarthColor, EarthGradient, listOf("Responsible", "Disciplined", "Self-control")),
    ZodiacSign("Aquarius", "Jan 20 - Feb 18", "Air", "♒", "Uranus", "Leo", ElementAirColor, AirGradient, listOf("Progressive", "Original", "Independent")),
    ZodiacSign("Pisces", "Feb 19 - Mar 20", "Water", "♓", "Neptune", "Virgo", ElementWaterColor, WaterGradient, listOf("Compassionate", "Artistic", "Intuitive")),
)

// --- Networking & Caching Layer ---

@Immutable
data class ApiResponse(
    val success: Boolean,
    val data: HoroscopeData?
)

@Immutable
data class HoroscopeData(
    val date: String?,
    val week: String?,
    val month: String?,
    @SerializedName("horoscope_data") val horoscopeData: String,
    @SerializedName("standout_days") val standoutDays: String?,
    @SerializedName("challenging_days") val challengingDays: String?
)

interface HoroscopeApi {
    @GET("get-horoscope/daily")
    suspend fun getDailyHoroscope(
        @Query("sign") sign: String,
        @Query("day") day: String
    ): ApiResponse

    @GET("get-horoscope/weekly")
    suspend fun getWeeklyHoroscope(
        @Query("sign") sign: String
    ): ApiResponse

    @GET("get-horoscope/monthly")
    suspend fun getMonthlyHoroscope(
        @Query("sign") sign: String
    ): ApiResponse
}

object NetworkModule {
    private const val BASE_URL = "https://horoscope-app-api.vercel.app/api/v1/"

    // Optimized client for low-end devices with aggressive timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS) // Fail fast
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES)) // Reduce idle connections
        .build()

    val api: HoroscopeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HoroscopeApi::class.java)
    }
}

// Global In-Memory Cache to save internet
// Using a synchronized map to ensure thread safety
object DataRepository {
    private val cache = Collections.synchronizedMap(mutableMapOf<String, HoroscopeData>())

    fun getFromCache(key: String): HoroscopeData? = cache[key]
    fun saveToCache(key: String, data: HoroscopeData) { cache[key] = data }
}

// --- Logic Controller ---

class HoroscopeStateHolder(private val scope: CoroutineScope) {
    var horoscopeData by mutableStateOf<HoroscopeData?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var fetchJob: Job? = null

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun loadData(sign: String, period: String, day: String, context: Context) {
        val cacheKey = "${sign.lowercase()}_${period}_${day.lowercase()}"

        // 1. Check Cache First (Instant Load, Zero Data)
        val cached = DataRepository.getFromCache(cacheKey)
        if (cached != null) {
            horoscopeData = cached
            error = null
            isLoading = false
            return
        }

        // 2. Check Internet Connectivity (Prevent crash on offline)
        if (!isInternetAvailable(context)) {
            error = "No internet connection available."
            isLoading = false
            return
        }

        fetchJob?.cancel()
        fetchJob = scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                error = null
                horoscopeData = null
            }
            try {
                val response = when (period) {
                    "daily" -> NetworkModule.api.getDailyHoroscope(sign.lowercase(), day)
                    "weekly" -> NetworkModule.api.getWeeklyHoroscope(sign.lowercase())
                    "monthly" -> NetworkModule.api.getMonthlyHoroscope(sign.lowercase())
                    else -> throw IllegalArgumentException("Unknown period")
                }

                withContext(Dispatchers.Main) {
                    if (response.success && response.data != null) {
                        horoscopeData = response.data
                        // Save to cache
                        DataRepository.saveToCache(cacheKey, response.data)
                    } else {
                        error = "Unable to fetch stars alignment."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Friendly error messages
                    error = when(e) {
                        is UnknownHostException -> "No internet access."
                        is SocketTimeoutException -> "Server took too long."
                        else -> "Connection error."
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}

// --- Helpers (Optimized) ---

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

fun calculateMoonPhase(): String {
    val c = Calendar.getInstance()
    // Simplified calculation for speed
    val day = c.get(Calendar.DAY_OF_MONTH)
    // Very rough approximation based on day of month (Not scientifically perfect but fast)
    // For a real app, passing the epoch math is fine, but reducing object creation is key
    return when (day % 30) {
        in 0..2 -> "New Moon"
        in 3..7 -> "Waxing Crescent"
        in 8..14 -> "First Quarter"
        in 15..17 -> "Full Moon"
        in 18..22 -> "Waning Gibbous"
        in 23..27 -> "Last Quarter"
        else -> "Waning Crescent"
    }
}

fun isCurrentSeason(dates: String): Boolean {
    // Wrapped in try-catch to prevent crashing on malformed strings
    try {
        val parts = dates.split("-").map { it.trim() }
        if (parts.size != 2) return false
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Fast fail optimization
        val dateString = SimpleDateFormat("MMM", Locale.US).format(calendar.time)
        if (!dates.contains(dateString) && !dates.contains("Dec") && !dates.contains("Jan")) return false

        val format = SimpleDateFormat("MMM dd", Locale.US)
        val start = format.parse(parts[0]) ?: return false
        val end = format.parse(parts[1]) ?: return false
        val startCal = Calendar.getInstance().apply { time = start }
        val endCal = Calendar.getInstance().apply { time = end }

        if (currentMonth == startCal.get(Calendar.MONTH)) return currentDay >= startCal.get(Calendar.DAY_OF_MONTH)
        if (currentMonth == endCal.get(Calendar.MONTH)) return currentDay <= endCal.get(Calendar.DAY_OF_MONTH)
        if (dates.startsWith("Dec")) {
            if (currentMonth == Calendar.DECEMBER && currentDay >= 22) return true
            if (currentMonth == Calendar.JANUARY && currentDay <= 19) return true
        }
        return false
    } catch (e: Exception) { return false }
}

fun calculateNumerology(signName: String): Int {
    val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val signValue = signName.length
    return (day + signValue) % 9 + 1
}

val TAROT_DECK = listOf(
    "The Fool" to "New Beginnings", "The Magician" to "Manifestation",
    "The Empress" to "Fertility", "The Emperor" to "Authority",
    "The Lovers" to "Partnership", "The Chariot" to "Victory",
    "Strength" to "Courage", "The Hermit" to "Introspection",
    "The Sun" to "Success", "The World" to "Completion"
)

fun getDailyTarot(signName: String): Pair<String, String> {
    val seed = (signName.hashCode() + Calendar.getInstance().get(Calendar.DAY_OF_YEAR)).toLong()
    return TAROT_DECK[Random(seed).nextInt(TAROT_DECK.size)]
}

val AFFIRMATIONS = listOf(
    "I attract good fortune.", "I am calm and centered.", "My potential is limitless.",
    "I choose peace.", "I am worthy of love.", "I embrace change."
)

fun getDailyAffirmation(signName: String): String {
    val seed = (signName.hashCode() * 3 + Calendar.getInstance().get(Calendar.DAY_OF_YEAR)).toLong()
    return AFFIRMATIONS[Random(seed).nextInt(AFFIRMATIONS.size)]
}

// --- UI Components ---

@Composable
fun StarField(modifier: Modifier = Modifier) {
    // Generate stars only once per composition lifecycle using remember
    val stars = remember {
        List(80) { // Reduced count for performance
            Star(x = Random.nextFloat(), y = Random.nextFloat(), size = Random.nextFloat() * 2f + 0.5f)
        }
    }

    // Canvas is lightweight, but we ensure it doesn't redraw unnecessarily
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        // Draw batching isn't strictly necessary for 80 points but good practice
        stars.forEach { star ->
            drawCircle(
                color = Color.White,
                radius = star.size / 2,
                center = Offset(star.x * width, star.y * height),
                alpha = 0.4f // Lower alpha for subtle effect
            )
        }
    }
}

data class Star(val x: Float, val y: Float, val size: Float)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    background = CosmicBlack,
                    surface = CosmicDarkBlue,
                    onBackground = StarWhite,
                    onSurface = StarWhite
                )
            ) {
                HoroscopeApp()
            }
        }
    }
}

@Composable
fun HoroscopeApp() {
    var selectedSign by remember { mutableStateOf<ZodiacSign?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(CosmicBlack)) {
        StarField()

        // OPTIMIZED: Replaced expensive Modifier.blur() with a radial gradient
        Box(modifier = Modifier
            .align(Alignment.TopCenter)
            .size(400.dp)
            .background(GlowGradient) // Much faster rendering
        )

        AnimatedContent(
            targetState = selectedSign,
            transitionSpec = {
                if (targetState != null) {
                    (slideInVertically { height -> height / 2 } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height / 2 } + fadeOut()
                    )
                } else {
                    (slideInVertically { height -> -height / 2 } + fadeIn()).togetherWith(
                        slideOutVertically { height -> height / 2 } + fadeOut()
                    )
                }
            }, label = "ScreenTransition"
        ) { sign ->
            if (sign == null) {
                HomeScreen(onSignSelected = { selectedSign = it })
            } else {
                DetailScreen(sign = sign, onBack = { selectedSign = null })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSignSelected: (ZodiacSign) -> Unit) {
    val greeting = remember { getGreeting() }
    val moonPhase = remember { calculateMoonPhase() }
    var selectedElement by remember { mutableStateOf("All") }

    val filteredSigns = remember(selectedElement) {
        if (selectedElement == "All") ZODIAC_SIGNS
        else ZODIAC_SIGNS.filter { it.element == selectedElement }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(greeting, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Serif)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NightsStay, contentDescription = null, tint = Color(0xFFFDE047), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$moonPhase • Daily Guidance", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Fire", "Earth", "Air", "Water").forEach { element ->
                        val isSelected = selectedElement == element
                        // Simplify color logic to reduce object creation
                        val color = when(element) {
                            "Fire" -> ElementFireColor
                            "Earth" -> ElementEarthColor
                            "Air" -> ElementAirColor
                            "Water" -> ElementWaterColor
                            else -> Color.White
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) color.copy(0.2f) else Color.White.copy(0.05f))
                                .border(1.dp, if (isSelected) color.copy(0.5f) else Color.White.copy(0.1f), RoundedCornerShape(20.dp))
                                .clickable { selectedElement = element }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                element,
                                color = if (isSelected) color else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Optimized: Added key for list state preservation
            itemsIndexed(
                items = filteredSigns,
                key = { _, sign -> sign.name }
            ) { _, sign ->
                ZodiacCard(sign, onSignSelected)
            }
        }
    }
}

@Composable
fun ZodiacCard(sign: ZodiacSign, onClick: (ZodiacSign) -> Unit) {
    // Cache calculation
    val isCurrent = remember(sign.dates) { isCurrentSeason(sign.dates) }

    Card(
        modifier = Modifier
            .height(200.dp)
            .clickable { onClick(sign) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCurrent) 2.dp else 1.dp,
            color = if (isCurrent) sign.color.copy(0.8f) else GlassBorder
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(sign.bgBrush))

            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .background(sign.color.copy(0.2f), RoundedCornerShape(4.dp))
                        .border(1.dp, sign.color.copy(0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("CURRENT SEASON", fontSize = 8.sp, color = sign.color, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(sign.symbol, fontSize = 48.sp, color = sign.color) // Reduced nesting complexity
                Spacer(modifier = Modifier.height(16.dp))
                Text(sign.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Text(sign.dates, color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(sign.element.uppercase(), color = sign.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(sign: ZodiacSign, onBack: () -> Unit) {
    var period by remember { mutableStateOf("daily") }
    var day by remember { mutableStateOf("TODAY") }
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val stateHolder = remember { HoroscopeStateHolder(scope) }

    val luckyNumber = remember(sign) { calculateNumerology(sign.name) }
    val affirmation = remember(sign) { getDailyAffirmation(sign.name) }
    val (tarotCard, tarotMeaning) = remember(sign) { getDailyTarot(sign.name) }

    LaunchedEffect(sign, period, day) @androidx.annotation.RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) {
        stateHolder.loadData(sign.name, period, day, context)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(sign.symbol, fontSize = 24.sp, color = sign.color)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(sign.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Gray)
                    }
                },
                actions = {
                    if (stateHolder.horoscopeData != null) {
                        IconButton(onClick = {
                            val shareText = "My ${sign.name} horoscope: ${stateHolder.horoscopeData?.horoscopeData?.take(100)}..."
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Period Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("daily", "weekly", "monthly").forEach { p ->
                    val isSelected = period == p
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { period = p }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            p.uppercase(),
                            color = if (isSelected) Color.Black else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (period == "daily") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf("YESTERDAY", "TODAY", "TOMORROW").forEach { d ->
                        val isSelected = day == d
                        Text(
                            text = d,
                            color = if (isSelected) sign.color else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .clickable { day = d }
                        )
                    }
                }
            }

            // Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                if (stateHolder.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = sign.color, modifier = Modifier.size(32.dp))
                    }
                } else if (stateHolder.error != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp).fillMaxWidth()) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
                        Text(stateHolder.error ?: "Error", color = Color(0xFFFCA5A5), fontSize = 14.sp, modifier = Modifier.padding(top=8.dp))
                        Button(
                            onClick = { stateHolder.loadData(sign.name, period, day, context) },
                            modifier = Modifier.padding(top=16.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f))
                        ) { Text("Try Again") }
                    }
                } else if (stateHolder.horoscopeData != null) {
                    val data = stateHolder.horoscopeData!!
                    val dateText = data.date ?: data.week ?: data.month ?: ""

                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(dateText, color = Color.LightGray, fontSize = 11.sp)
                    }

                    Text(
                        data.horoscopeData,
                        color = Color.White.copy(0.9f),
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Simplified Profile Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileItem(Icons.Default.Public, "Ruler", sign.rulingPlanet, sign.color, Modifier.weight(1f))
                        ProfileItem(Icons.Default.Favorite, "Match", sign.compatibility, sign.color, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simplified Affirmation Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("DAILY AFFIRMATION", fontSize = 10.sp, color = sign.color, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("\"$affirmation\"", fontSize = 14.sp, color = Color.White, fontStyle = FontStyle.Italic)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarot Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B).copy(0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Style, contentDescription = null, tint = Color(0xFFA78BFA))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("TAROT CARD", fontSize = 10.sp, color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold)
                            Text("$tarotCard: $tarotMeaning", fontSize = 14.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Traits
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sign.traits.forEach { trait ->
                            TraitChip(text = trait, color = sign.color)
                        }
                    }

                    if (period == "monthly") {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (data.standoutDays != null) InfoCard("Best Days", data.standoutDays, Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(8.dp))
                        if (data.challengingDays != null) InfoCard("Caution", data.challengingDays, Color(0xFFF43F5E))
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileItem(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFF1E293B).copy(0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color.copy(0.8f), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label.uppercase(), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
fun TraitChip(text: String, color: Color) {
    Text(
        text = text,
        color = Color.White.copy(0.9f),
        fontSize = 11.sp,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun InfoCard(title: String, content: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = color, modifier = Modifier.size(14.dp).padding(top=2.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title.uppercase(), color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(content, color = Color.White.copy(0.9f), fontSize = 13.sp)
        }
    }
}