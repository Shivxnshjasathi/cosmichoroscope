package com.zincstate.cosmichoroscope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Constants & Data Models ---

val CosmicBlack = Color(0xFF050508)
val CosmicDarkBlue = Color(0xFF0F172A)
val StarWhite = Color(0xFFE2E8F0)
val ElementFireColor = Color(0xFFFB923C)
val ElementEarthColor = Color(0xFF34D399)
val ElementAirColor = Color(0xFF7DD3FC)
val ElementWaterColor = Color(0xFFA78BFA)
val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.15f)

data class ZodiacSign(
    val name: String,
    val dates: String,
    val element: String,
    val symbol: String,
    val color: Color,
    val bgBrush: Brush
)

val ZODIAC_SIGNS = listOf(
    ZodiacSign("Aries", "Mar 21 - Apr 19", "Fire", "♈", ElementFireColor, Brush.linearGradient(listOf(Color(0xFFF97316).copy(0.15f), Color(0xFF7F1D1D).copy(0.15f)))),
    ZodiacSign("Taurus", "Apr 20 - May 20", "Earth", "♉", ElementEarthColor, Brush.linearGradient(listOf(Color(0xFF10B981).copy(0.15f), Color(0xFF14532D).copy(0.15f)))),
    ZodiacSign("Gemini", "May 21 - Jun 20", "Air", "♊", ElementAirColor, Brush.linearGradient(listOf(Color(0xFF0EA5E9).copy(0.15f), Color(0xFF312E81).copy(0.15f)))),
    ZodiacSign("Cancer", "Jun 21 - Jul 22", "Water", "♋", ElementWaterColor, Brush.linearGradient(listOf(Color(0xFF8B5CF6).copy(0.15f), Color(0xFF1E3A8A).copy(0.15f)))),
    ZodiacSign("Leo", "Jul 23 - Aug 22", "Fire", "♌", ElementFireColor, Brush.linearGradient(listOf(Color(0xFFF97316).copy(0.15f), Color(0xFF7F1D1D).copy(0.15f)))),
    ZodiacSign("Virgo", "Aug 23 - Sep 22", "Earth", "♍", ElementEarthColor, Brush.linearGradient(listOf(Color(0xFF10B981).copy(0.15f), Color(0xFF14532D).copy(0.15f)))),
    ZodiacSign("Libra", "Sep 23 - Oct 22", "Air", "♎", ElementAirColor, Brush.linearGradient(listOf(Color(0xFF0EA5E9).copy(0.15f), Color(0xFF312E81).copy(0.15f)))),
    ZodiacSign("Scorpio", "Oct 23 - Nov 21", "Water", "♏", ElementWaterColor, Brush.linearGradient(listOf(Color(0xFF8B5CF6).copy(0.15f), Color(0xFF1E3A8A).copy(0.15f)))),
    ZodiacSign("Sagittarius", "Nov 22 - Dec 21", "Fire", "♐", ElementFireColor, Brush.linearGradient(listOf(Color(0xFFF97316).copy(0.15f), Color(0xFF7F1D1D).copy(0.15f)))),
    ZodiacSign("Capricorn", "Dec 22 - Jan 19", "Earth", "♑", ElementEarthColor, Brush.linearGradient(listOf(Color(0xFF10B981).copy(0.15f), Color(0xFF14532D).copy(0.15f)))),
    ZodiacSign("Aquarius", "Jan 20 - Feb 18", "Air", "♒", ElementAirColor, Brush.linearGradient(listOf(Color(0xFF0EA5E9).copy(0.15f), Color(0xFF312E81).copy(0.15f)))),
    ZodiacSign("Pisces", "Feb 19 - Mar 20", "Water", "♓", ElementWaterColor, Brush.linearGradient(listOf(Color(0xFF8B5CF6).copy(0.15f), Color(0xFF1E3A8A).copy(0.15f)))),
)

// --- Networking ---

data class ApiResponse(
    val success: Boolean,
    val data: HoroscopeData
)

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

object RetrofitClient {
    private const val BASE_URL = "https://horoscope-app-api.vercel.app/api/v1/"

    val api: HoroscopeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HoroscopeApi::class.java)
    }
}

// --- Utils & Custom Modifiers ---

// Bounce effect on click
fun Modifier.bouncyClickable(
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

// Typewriter Text Effect
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    var visibleCharCount by remember { mutableStateOf(0) }

    LaunchedEffect(text) {
        visibleCharCount = 0
        while (visibleCharCount < text.length) {
            visibleCharCount++
            delay(20) // Typing speed
        }
    }

    Text(
        text = text.take(visibleCharCount),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = FontFamily.Serif,
        textAlign = TextAlign.Center,
        lineHeight = 32.sp
    )
}

// Nebula/Aurora Background Animation
@Composable
fun NebulaBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "nebula")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 200f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse), label = "o1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 200f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse), label = "o2"
    )

    Canvas(modifier = modifier.fillMaxSize().blur(80.dp).alpha(0.3f)) {
        // Purple blob
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6366F1), Color.Transparent),
                center = Offset(size.width * 0.2f + offset1, size.height * 0.2f + offset2),
                radius = 500f
            ),
            radius = 500f,
            center = Offset(size.width * 0.2f + offset1, size.height * 0.2f + offset2)
        )
        // Blue blob
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0EA5E9), Color.Transparent),
                center = Offset(size.width * 0.8f - offset2, size.height * 0.5f + offset1),
                radius = 600f
            ),
            radius = 600f,
            center = Offset(size.width * 0.8f - offset2, size.height * 0.5f + offset1)
        )
        // Pink blob
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEC4899), Color.Transparent),
                center = Offset(size.width * 0.5f + offset2/2, size.height * 0.8f - offset1),
                radius = 450f
            ),
            radius = 450f,
            center = Offset(size.width * 0.5f + offset2/2, size.height * 0.8f - offset1)
        )
    }
}

@Composable
fun StarField(modifier: Modifier = Modifier) {
    val starCount = 100
    val stars = remember {
        List(starCount) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                blinkSpeed = Random.nextFloat() * 0.5f + 0.1f,
                offset = Random.nextFloat() * 2 * Math.PI.toFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "star_anim")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        stars.forEach { star ->
            val alpha = (sin(time * star.blinkSpeed + star.offset) + 1) / 2 * 0.7f + 0.3f
            drawCircle(
                color = Color.White,
                radius = star.size / 2,
                center = Offset(star.x * width, star.y * height),
                alpha = alpha
            )
        }
    }
}

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val blinkSpeed: Float,
    val offset: Float
)

// --- Main Activity ---

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
        // 1. Nebula & Stars Background
        NebulaBackground()
        StarField()

        // 2. Animated Transitions
        AnimatedContent(
            targetState = selectedSign,
            transitionSpec = {
                if (targetState != null) {
                    (slideInVertically { height -> height } + fadeIn() + scaleIn(initialScale = 0.9f)).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut()
                    )
                } else {
                    (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> height } + fadeOut()
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

@Composable
fun HomeScreen(onSignSelected: (ZodiacSign) -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -50 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier
                    .border(1.dp, Color(0xFF6366F1).copy(0.3f), RoundedCornerShape(50))
                    .background(Color(0xFF6366F1).copy(0.05f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DAILY CELESTIAL GUIDANCE", color = Color(0xFFC7D2FE), fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Gradient Title
                val brush = Brush.verticalGradient(listOf(Color.White, Color.LightGray, Color.Gray))
                Text(
                    text = "Horoscope",
                    fontSize = 52.sp,
                    fontFamily = FontFamily.Serif,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = brush
                    )
                )
                Text("Unveil the mysteries of your path", fontSize = 16.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontFamily = FontFamily.Serif)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(ZODIAC_SIGNS) { index, sign ->
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = index * 50)) +
                            slideInVertically(animationSpec = tween(600, delayMillis = index * 50)) { 50 }
                ) {
                    ZodiacCard(sign, onSignSelected)
                }
            }
        }
    }
}

@Composable
fun ZodiacCard(sign: ZodiacSign, onClick: (ZodiacSign) -> Unit) {
    Card(
        modifier = Modifier
            .height(200.dp)
            .bouncyClickable { onClick(sign) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Brush.verticalGradient(listOf(Color.White.copy(0.2f), Color.Transparent)))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(sign.bgBrush))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(0.3f), CircleShape)
                        .border(1.dp, Color.White.copy(0.1f), CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(sign.symbol, fontSize = 32.sp, color = sign.color)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(sign.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Text(sign.dates, color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, sign.color.copy(0.3f), RoundedCornerShape(8.dp))
                        .background(sign.color.copy(0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(sign.element, color = sign.color, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(sign: ZodiacSign, onBack: () -> Unit) {
    var period by remember { mutableStateOf("daily") }
    var day by remember { mutableStateOf("TODAY") }

    var horoscopeData by remember { mutableStateOf<HoroscopeData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Animation Removed: Floating Symbol Effect was here

    fun loadData() {
        scope.launch {
            isLoading = true
            error = null
            horoscopeData = null
            try {
                delay(400)
                val response = when (period) {
                    "daily" -> RetrofitClient.api.getDailyHoroscope(sign.name.lowercase(), day)
                    "weekly" -> RetrofitClient.api.getWeeklyHoroscope(sign.name.lowercase())
                    "monthly" -> RetrofitClient.api.getMonthlyHoroscope(sign.name.lowercase())
                    else -> throw Exception("Unknown period")
                }
                if (response.success) {
                    horoscopeData = response.data
                } else {
                    error = "Failed to load data"
                }
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Connection error"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(sign, period, day) {
        loadData()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Gray)
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Card - Redesigned (Modern Center Layout)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(0.6f)),
                shape = RoundedCornerShape(32.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Brush.verticalGradient(listOf(Color.White.copy(0.3f), Color.Transparent)))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large Centered Symbol
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(sign.bgBrush, CircleShape)
                            .border(2.dp, sign.color.copy(0.5f), CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sign.symbol, fontSize = 60.sp, color = sign.color)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Centered Text Info
                    Text(
                        sign.name,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(sign.dates, color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.size(6.dp).background(sign.color, CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(sign.element.uppercase(), color = sign.color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Period Tabs (Updated visual style)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(0.4f), RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("daily", "weekly", "monthly").forEach { p ->
                            val isSelected = period == p
                            val targetColor = if (isSelected) Color.White else Color.Transparent
                            val targetTextColor = if (isSelected) Color.Black else Color.Gray

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(targetColor)
                                    .clickable { period = p }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    p.replaceFirstChar { it.uppercase() },
                                    color = targetTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = period == "daily") {
                Row(
                    modifier = Modifier
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    listOf("YESTERDAY", "TODAY", "TOMORROW").forEach { d ->
                        val isSelected = day == d
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF6366F1) else Color.Transparent)
                                .clickable { day = d }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(d, color = if (isSelected) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (period == "daily") Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(targetState = isLoading, label = "loading") { loading ->
                    if (loading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = sign.color, modifier = Modifier.size(48.dp))
                            Text("ALIGNING STARS...", Modifier.padding(top = 80.dp), color = Color.Gray, fontSize = 10.sp, letterSpacing = 2.sp)
                        }
                    } else if (error != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Connection Severed", color = Color(0xFFFCA5A5), fontSize = 18.sp)
                            Text(error!!, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { loadData() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                                Text("Retry Connection", color = Color.Black)
                            }
                        }
                    } else if (horoscopeData != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val dateText = horoscopeData!!.date ?: horoscopeData!!.week ?: horoscopeData!!.month ?: ""

                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically { 20 } + fadeIn()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        modifier = Modifier
                                            .background(Color(0xFF1E293B), RoundedCornerShape(50))
                                            .border(1.dp, Color.Gray.copy(0.3f), RoundedCornerShape(50))
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(dateText, color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }

                                    Spacer(modifier = Modifier.height(32.dp))

                                    Box {
                                        Text("“", fontSize = 60.sp, color = Color.White.copy(0.1f), fontFamily = FontFamily.Serif, modifier = Modifier.offset(x = (-20).dp, y = (-30).dp))

                                        // Typewriter effect applied here
                                        TypewriterText(
                                            text = horoscopeData!!.horoscopeData,
                                            color = Color.White.copy(0.95f)
                                        )

                                        Text("”", fontSize = 60.sp, color = Color.White.copy(0.1f), fontFamily = FontFamily.Serif, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 10.dp, y = 30.dp))
                                    }
                                }
                            }

                            if (period == "monthly") {
                                Spacer(modifier = Modifier.height(40.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    if (horoscopeData!!.standoutDays != null) {
                                        InfoCard("Power Days", horoscopeData!!.standoutDays!!, Color(0xFF10B981))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                if (horoscopeData!!.challengingDays != null) {
                                    InfoCard("Caution Needed", horoscopeData!!.challengingDays!!, Color(0xFFF43F5E))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun InfoCard(title: String, content: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title.uppercase(), color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(content, color = Color.White.copy(0.9f), fontSize = 16.sp)
        }
    }
}