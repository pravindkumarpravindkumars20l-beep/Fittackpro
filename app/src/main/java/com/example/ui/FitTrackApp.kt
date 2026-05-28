package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitTrackApp(viewModel: FitTrackViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    val todayWorkouts by viewModel.todayWorkouts.collectAsStateWithLifecycle()
    val todayNutrition by viewModel.todayNutrition.collectAsStateWithLifecycle()
    val weightLogs by viewModel.weightProgressLogs.collectAsStateWithLifecycle()
    val todayAct by viewModel.todayActivity.collectAsStateWithLifecycle()

    val wearableSyncSuccessMsg by viewModel.wearableSyncSuccessMsg.collectAsStateWithLifecycle()

    // Dialog state controllers
    var showWorkoutDialog by remember { mutableStateOf(false) }
    var showMealDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (currentUser != null) {
                FitTrackBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (currentUser == null) {
                OnboardingScreen(viewModel = viewModel)
            } else {
                val user = currentUser!!
                
                // Overlay global warning banner if we just finished wearable synchronizations
                Column(modifier = Modifier.fillMaxSize()) {
                    if (wearableSyncSuccessMsg != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Sync",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = wearableSyncSuccessMsg!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.dismissSyncGreeting() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Status",
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Dynamic Tab content container
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            "Dashboard" -> DashboardTab(
                                user = user,
                                workouts = todayWorkouts,
                                nutrition = todayNutrition,
                                act = todayAct,
                                viewModel = viewModel,
                                onAddWorkoutClick = { showWorkoutDialog = true },
                                onAddMealClick = { showMealDialog = true }
                            )
                            "Workout" -> WorkoutTab(
                                workouts = todayWorkouts,
                                viewModel = viewModel,
                                onAddWorkoutClick = { showWorkoutDialog = true }
                            )
                            "Diet" -> DietTab(
                                user = user,
                                nutrition = todayNutrition,
                                viewModel = viewModel,
                                onAddMealClick = { showMealDialog = true }
                            )
                            "Progress" -> ProgressTab(
                                weightLogs = weightLogs,
                                viewModel = viewModel,
                                onAddWeightClick = { showWeightDialog = true }
                            )
                            "AI Coach" -> CoachTab(
                                user = user,
                                workouts = todayWorkouts,
                                nutrition = todayNutrition,
                                act = todayAct,
                                viewModel = viewModel
                            )
                            else -> DashboardTab(
                                user = user,
                                workouts = todayWorkouts,
                                nutrition = todayNutrition,
                                act = todayAct,
                                viewModel = viewModel,
                                onAddWorkoutClick = { showWorkoutDialog = true },
                                onAddMealClick = { showMealDialog = true }
                            )
                        }
                    }
                }
            }

            // Global Logging Modals
            if (showWorkoutDialog) {
                AddWorkoutDialog(
                    onDismiss = { showWorkoutDialog = false },
                    onConfirm = { name, cat, sets, reps, wt, cals ->
                        viewModel.addWorkout(name, cat, sets, reps, wt, cals)
                        showWorkoutDialog = false
                    }
                )
            }

            if (showMealDialog) {
                AddMealDialog(
                    onDismiss = { showMealDialog = false },
                    onConfirm = { name, type, cals, prot, carbs, fats ->
                        viewModel.addMeal(name, type, cals, prot, carbs, fats)
                        showMealDialog = false
                    }
                )
            }

            if (showWeightDialog) {
                AddWeightDialog(
                    onDismiss = { showWeightDialog = false },
                    onConfirm = { wt, fat, muscle, notes ->
                        viewModel.addProgressLog(wt, fat, muscle, notes)
                        showWeightDialog = false
                    }
                )
            }
        }
    }
}

// ==========================================
// Bottom Navigation Component
// ==========================================
@Composable
fun FitTrackBottomBar(currentTab: String, onTabSelected: (String) -> Unit) {
    val items = listOf(
        TabItem("Dashboard", "Dash", Icons.Default.Dashboard, Icons.Outlined.Dashboard, "dashboard_tab"),
        TabItem("Workout", "Gym", Icons.Default.FitnessCenter, Icons.Outlined.FitnessCenter, "workout_tab"),
        TabItem("Diet", "Diet", Icons.Default.Restaurant, Icons.Outlined.Restaurant, "diet_tab"),
        TabItem("Progress", "Progress", Icons.Default.TrendingUp, Icons.Outlined.TrendingUp, "progress_tab"),
        TabItem("AI Coach", "AI Coach", Icons.Default.Psychology, Icons.Outlined.Psychology, "ai_coach_tab")
    )

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Draw elegant top border mimicking border-t border-white/5
                drawLine(
                    color = Color(0x0EFFFFFF),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentTab == item.title
                Column(
                    modifier = Modifier
                        .clickable { onTabSelected(item.title) }
                        .testTag(item.testTag)
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        // Tiny dot indicator from theme
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    } else {
                        Spacer(modifier = Modifier.height(7.dp))
                    }

                    Icon(
                        imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.label.uppercase(),
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

data class TabItem(
    val title: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val testTag: String
)

// ==========================================
// Onboarding Screen (Local Sign Up)
// ==========================================
@Composable
fun OnboardingScreen(viewModel: FitTrackViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("25") }
    var height by remember { mutableStateOf("178") }
    var weight by remember { mutableStateOf("75") }
    var selectedGender by remember { mutableStateOf("Male") }
    var selectedGoal by remember { mutableStateOf("Muscle Gain") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "FitTrack Pro",
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "YOUR PERSONAL HEALTH & GYM PARTNER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g., Jane Doe") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_name_input")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g., jane@email.com") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age (yrs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Gender selector
                    Column {
                        Text(
                            text = "Gender",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Male", "Female", "Other").forEach { g ->
                                val active = selectedGender == g
                                Button(
                                    onClick = { selectedGender = g },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(text = g, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Goals
                    Column {
                        Text(
                            text = "Primary Fitness Target",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val goalOptions = listOf("Weight Loss", "Muscle Gain", "Cardio Fitness", "Yoga & Flexibility")
                        goalOptions.forEach { target ->
                            val active = selectedGoal == target
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedGoal = target }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = active,
                                    onClick = { selectedGoal = target },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = target,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.onboardingName.value = name
                            viewModel.onboardingEmail.value = email
                            viewModel.onboardingAge.value = age
                            viewModel.onboardingGender.value = selectedGender
                            viewModel.onboardingHeight.value = height
                            viewModel.onboardingWeight.value = weight
                            viewModel.onboardingGoal.value = selectedGoal
                            viewModel.saveProfile()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("onboarding_submit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Build My Dashboard",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. Dashboard Tab Component
// ==========================================
@Composable
fun DashboardTab(
    user: User,
    workouts: List<Workout>,
    nutrition: List<Nutrition>,
    act: DailyActivity?,
    viewModel: FitTrackViewModel,
    onAddWorkoutClick: () -> Unit,
    onAddMealClick: () -> Unit
) {
    val caloriesConsumed = nutrition.sumOf { it.calories.toDouble() }.toInt()
    val caloriesGoal = user.dailyCalorieTarget
    val waterQuantity = act?.waterQuantityMl ?: 0
    val waterGoal = user.dailyWaterGoalMl
    val stepsWalked = act?.stepsCount ?: 0
    val stepsGoal = user.dailyStepGoal

    val totalCalsBurned = workouts.sumOf { it.caloriesBurned.toDouble() }.toInt()

    val progressVal = if (caloriesGoal > 0) (caloriesConsumed.toFloat() / caloriesGoal).coerceIn(0f, 1f) else 0f

    // Calculate BMI
    // bmi = weight_kg / (height_m ^ 2)
    val heightMeters = user.height / 100f
    val bmi = if (heightMeters > 0) user.weight / (heightMeters * heightMeters) else 0f
    val bmiCategory = when {
        bmi < 18.5f -> "Underweight"
        bmi < 24.9f -> "Normal"
        bmi < 29.9f -> "Overweight"
        else -> "Obese"
    }
    val bmiColor = when (bmiCategory) {
        "Normal" -> Color(0xFF10B981) // Green
        "Underweight" -> Color(0xFF3B82F6) // Blue
        else -> Color(0xFFEF4444) // Red
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Dashboard Header (Vibrant Palette Theme)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WELCOME BACK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Custom Rounded-2xl avatar card with Sync integration on click!
                val initials = remember(user.name) {
                    user.name.split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .map { it.first().uppercase() }
                        .joinToString("")
                        .ifEmpty { "AR" }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { viewModel.syncWithWearables() },
                    contentAlignment = Alignment.Center
                ) {
                    val activeSync by viewModel.activeWearableSync.collectAsStateWithLifecycle()
                    if (activeSync) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = initials,
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Calorie Card Progress Builder - Daily Activity style
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0x0EFFFFFF)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Daily Activity",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%,d", caloriesConsumed),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "/ $caloriesGoal kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Line
                        val barFraction = if (caloriesGoal > 0) (caloriesConsumed.toFloat() / caloriesGoal).coerceIn(0f, 1f) else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color(0x1BFFFFFF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barFraction)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    // Circle Progress Visual side by side (from design template!)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(76.dp)
                    ) {
                        val ringPercent = if (caloriesGoal > 0) (caloriesConsumed.toFloat() / caloriesGoal).coerceIn(0f, 1f) else 0f
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color(0x11FFFFFF),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = PrimaryActiveLime,
                                startAngle = -90f,
                                sweepAngle = ringPercent * 360f,
                                useCenter = false,
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${(ringPercent * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Circular steps ring and water quick logger columns mapped perfectly to Grid style
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Steps Ring Block
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0x0EFFFFFF)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22F97316)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥", fontSize = 16.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Steps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%,d", stepsWalked),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "/$stepsGoal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // manual add steps buttons
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.logSteps(500) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.weight(1f).height(28.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+500", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.logSteps(1500) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.weight(1f).height(28.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+1.5k", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Water Fluid Block
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0x0EFFFFFF)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x223B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💧", fontSize = 16.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Hydration",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%.1fL", waterQuantity / 1000f),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("/%.1fL", waterGoal / 1000f),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Quick taps for drink
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.logWater(250) },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal.copy(alpha = 0.15f)),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.weight(1f).height(28.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+250ml", fontSize = 9.sp, color = SecondaryTeal, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.logWater(500) },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal.copy(alpha = 0.15f)),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.weight(1f).height(28.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("+500ml", fontSize = 9.sp, color = SecondaryTeal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Today's Routine (Direct interactive push-day sporty highlight)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Routine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.selectTab("Workout") }
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectTab("Workout") },
                    colors = CardDefaults.cardColors(containerColor = PrimaryActiveLime),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (workouts.isEmpty()) "MORNING SESSION" else "SESSION IN PROGRESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (workouts.isEmpty()) "STRENGTH PUSH DAY" else "LOGGED WORKOUT",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (workouts.isEmpty()) "Chest • Shoulders • Triceps" else "${workouts.size} exercises completed today",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black.copy(alpha = 0.8f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Go",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Post-workout meal placeholder block style
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0x0EFFFFFF)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x0EFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍏", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Post-workout Meal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Scheduled for 12:30 PM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // BMI Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0x0EFFFFFF)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BMI Index Summary",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Height: ${user.height.roundToInt()} cm | Weight: ${user.weight} kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        // visual ribbon indicator
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(modifier = Modifier.weight(18.5f).fillMaxHeight().background(Color(0xFF3B82F6))) // Under
                            Box(modifier = Modifier.weight(6.4f).fillMaxHeight().background(Color(0xFF10B981))) // Normal
                            Box(modifier = Modifier.weight(5f).fillMaxHeight().background(Color(0xFFF59E0B))) // Over
                            Box(modifier = Modifier.weight(10f).fillMaxHeight().background(Color(0xFFEF4444))) // Obese
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = bmiColor.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, bmiColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format("%.1f", bmi),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = bmiColor
                            )
                            Text(
                                text = bmiCategory,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = bmiColor
                            )
                        }
                    }
                }
            }
        }

        // Quick Entry Portal Actions Dashboard
        item {
            Column {
                Text(
                    text = "Quick Logging Shortcuts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAddWorkoutClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Workout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAddMealClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Meal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Mini AI Tips Panel
        item {
            val tips = viewModel.getAIEngineRecommendations(user, workouts, nutrition, act)
            if (tips.isNotEmpty()) {
                val bestTip = tips.first()
                val activeColored = when (bestTip.urgency) {
                    "High" -> Color(0xFFEF4444)
                    "Medium" -> Color(0xFFF97316)
                    else -> MaterialTheme.colorScheme.primary
                }

                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = "AI Fitness Coach Advice",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = activeColored.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, activeColored.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = activeColored,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = bestTip.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = activeColored
                                    )
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = activeColored),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = bestTip.urgency,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bestTip.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CircularStepsProgressIndicator(
    current: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.surfaceVariant
    val activeColor = CardioBlue

    Canvas(modifier = modifier) {
        val sweepAngle = if (target > 0) (current.toFloat() / target * 360f).coerceAtMost(360f) else 0f
        
        // Draw Grey Outline Background Ring
        drawArc(
            color = outlineColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw active Blue Sports Ring
        drawArc(
            color = activeColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

// ==========================================
// 2. Workout Tab Component (Workout Tracker)
// ==========================================
@Composable
fun WorkoutTab(
    workouts: List<Workout>,
    viewModel: FitTrackViewModel,
    onAddWorkoutClick: () -> Unit
) {
    val isTimerRunning by viewModel.isWorkoutTimerRunning.collectAsStateWithLifecycle()
    val timerAmt by viewModel.workoutTimeSeconds.collectAsStateWithLifecycle()
    
    val isRestRunning by viewModel.isRestTimerRunning.collectAsStateWithLifecycle()
    val restTimerVal by viewModel.restTimeSeconds.collectAsStateWithLifecycle()
    val initialRestLimit by viewModel.restTimerInitialSeconds.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Gym Routine Tracker",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Stopwatch / Routine timers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Exercise Timer",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTimerRunning) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (isTimerRunning) "Recording Session" else "Paused",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = viewModel.formatTimerText(timerAmt),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.toggleWorkoutTimer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimerRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTimerRunning) "Pause Clock" else "Start Gym Timer")
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetWorkoutTimer() },
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset")
                        }
                    }
                }
            }
        }

        // Rest countdown timer module
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rest Interval Timer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isRestRunning) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { restTimerVal.toFloat() / initialRestLimit },
                                        color = SecondaryTeal,
                                        strokeWidth = 4.dp
                                    )
                                    Text(
                                        text = restTimerVal.toString(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Reps Segment Completed", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Oxygenating target cells...", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            Button(
                                onClick = { viewModel.stopRestTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Skip", fontSize = 11.sp)
                            }
                        }
                    } else {
                        // Show Quick Select interval times presets
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(30, 45, 60, 90).forEach { s ->
                                Button(
                                    onClick = { viewModel.startRestTimer(s) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("${s}s", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Workouts exercises logged today
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Workouts Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddWorkoutClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Lift", fontSize = 12.sp)
                }
            }
        }

        if (workouts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No lifts logged today yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Create workout routines to fuel muscle hypertrophy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(workouts) { workout ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category visual tag icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .background(AccentOrange.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = when (workout.category) {
                                    "Cardio" -> Icons.Default.DirectionsRun
                                    "Yoga" -> Icons.Default.SelfImprovement
                                    else -> Icons.Default.FitnessCenter
                                },
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = workout.exerciseName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(workout.category, fontSize = 9.sp) },
                                    modifier = Modifier.height(20.dp)
                                )
                                Text(
                                    text = "${workout.sets} sets x ${workout.reps} reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                if (workout.weightUsed > 0) {
                                    Text(
                                        text = "@ ${workout.weightUsed} kg",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "-${workout.caloriesBurned.roundToInt()} kcal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = AccentOrange
                            )
                            IconButton(
                                onClick = { viewModel.deleteWorkout(workout.workoutId) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 3. Diet Tab Component (Diet & Nutrition Tracker)
// ==========================================
@Composable
fun DietTab(
    user: User,
    nutrition: List<Nutrition>,
    viewModel: FitTrackViewModel,
    onAddMealClick: () -> Unit
) {
    val totalCalories = nutrition.sumOf { it.calories.toDouble() }.toFloat()
    val totalProtein = nutrition.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs = nutrition.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFats = nutrition.sumOf { it.fats.toDouble() }.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Diet & Macrological Metrics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Full Interactive Macros Graph Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Macronutrients distribution",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Protein
                    MacroRowProgress(
                        label = "Protein (Cell repair)",
                        current = totalProtein,
                        target = user.dailyProteinTargetGram,
                        color = MaterialTheme.colorScheme.primary,
                        units = "g"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Carbs
                    MacroRowProgress(
                        label = "Carbohydrates (ATP Glycogen)",
                        current = totalCarbs,
                        target = user.dailyCarbsTargetGram,
                        color = AccentOrange,
                        units = "g"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fats
                    MacroRowProgress(
                        label = "Essential Fats (Hormonal)",
                        current = totalFats,
                        target = user.dailyFatsTargetGram,
                        color = SecondaryTeal,
                        units = "g"
                    )
                }
            }
        }

        // Meal logging actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meals Logged Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddMealClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Food", fontSize = 12.sp)
                }
            }
        }

        if (nutrition.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No food items recorded today",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Log breakfast, lunches, dinners to watch caloric loads",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(nutrition) { meal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = when (meal.mealType) {
                                    "Breakfast" -> Icons.Default.Coffee
                                    "Lunch" -> Icons.Default.LunchDining
                                    "Dinner" -> Icons.Default.DinnerDining
                                    else -> Icons.Default.Fastfood
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = meal.foodName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = meal.mealType,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "P: ${meal.protein.roundToInt()}g | C: ${meal.carbs.roundToInt()}g | F: ${meal.fats.roundToInt()}g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${meal.calories.roundToInt()} kcal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { viewModel.deleteMeal(meal.nutritionId) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Meal Record",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MacroRowProgress(
    label: String,
    current: Float,
    target: Float,
    color: Color,
    units: String
) {
    val fraction = if (target > 0) (current / target).coerceIn(0f, 1f) else 0f
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Text(
                text = "${current.roundToInt()} / ${target.roundToInt()} $units",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// ==========================================
// 4. Progress Tab Component (Weight Graphs)
// ==========================================
@Composable
fun ProgressTab(
    weightLogs: List<Progress>,
    viewModel: FitTrackViewModel,
    onAddWeightClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Progress & Anthropometric Chart",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Custom Weight Progression Canvas Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weight log flow (Last entries)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (weightLogs.size >= 2) {
                        CustomWeightLineChart(
                            logs = weightLogs,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "Log weight on 2 different days to generate history chart",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Add Log buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight Log Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddWeightClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Weight", fontSize = 12.sp)
                }
            }
        }

        if (weightLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No metrics logged",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(weightLogs.reversed()) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.date,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Fat: ${log.bodyFat}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "Muscle: ${log.muscleMass}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (log.notes.isNotBlank()) {
                                Text(
                                    text = log.notes,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${log.weight} kg",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { viewModel.deleteProgress(log.progressId) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Progress record",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CustomWeightLineChart(logs: List<Progress>, modifier: Modifier = Modifier) {
    val activeColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40f
        val paddingRight = 20f
        val paddingTop = 20f
        val paddingBottom = 40f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        val weights = logs.map { it.weight }
        val maxWeight = (weights.maxOrNull() ?: 100f) + 1.5f
        val minWeight = (weights.minOrNull() ?: 0f) - 1.5f
        val weightRange = maxWeight - minWeight

        val points = logs.mapIndexed { index, progress ->
            val x = paddingLeft + (index.toFloat() / (logs.size - 1)) * chartWidth
            val y = paddingTop + chartHeight - ((progress.weight - minWeight) / weightRange) * chartHeight
            Offset(x, y)
        }

        // Draw Axes Lines
        drawLine(
            color = labelColor.copy(alpha = 0.3f),
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, height - paddingBottom),
            strokeWidth = 2f
        )
        drawLine(
            color = labelColor.copy(alpha = 0.3f),
            start = Offset(paddingLeft, height - paddingBottom),
            end = Offset(width - paddingRight, height - paddingBottom),
            strokeWidth = 2f
        )

        // Draw Line Joining Points
        val path = Path().apply {
            points.forEachIndexed { idx, offset ->
                if (idx == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
            }
        }
        drawPath(
            path = path,
            color = activeColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Circle Nodes and values above
        points.forEachIndexed { idx, offset ->
            drawCircle(
                color = activeColor,
                radius = 6.dp.toPx(),
                center = offset
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = offset
            )
        }
    }
}

// ==========================================
// 5. Coach Tab Component (Recommendations)
// ==========================================
@Composable
fun CoachTab(
    user: User,
    workouts: List<Workout>,
    nutrition: List<Nutrition>,
    act: DailyActivity?,
    viewModel: FitTrackViewModel
) {
    val tips = viewModel.getAIEngineRecommendations(user, workouts, nutrition, act)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "AI Fitness Coach & Reminders",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // In-app Reminders controller
        item {
            val alertsOn by viewModel.remindersEnabled.collectAsStateWithLifecycle()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (alertsOn) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = if (alertsOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Personal Goal Reminders",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Remind to Log hydration, meals, work lifts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Switch(
                        checked = alertsOn,
                        onCheckedChange = { viewModel.toggleReminders() }
                    )
                }
            }
        }

        item {
            Text(
                text = "Coach Recommendation Engine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (tips.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Calculating personalized insights based on water, workout targets, steps progression...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(tips) { tip ->
                val activeColored = when (tip.urgency) {
                    "High" -> Color(0xFFEF4444)
                    "Medium" -> Color(0xFFF97316)
                    else -> MaterialTheme.colorScheme.primary
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = activeColored.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, activeColored.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(activeColored.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = when (tip.category) {
                                    "Diet" -> Icons.Default.Spa
                                    "Gym" -> Icons.Default.FitnessCenter
                                    "Nutrition" -> Icons.Default.Egg
                                    "Water" -> Icons.Default.LocalDrink
                                    else -> Icons.Default.DirectionsWalk
                                },
                                contentDescription = null,
                                tint = activeColored,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tip.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = activeColored
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = activeColored),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = tip.urgency,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tip.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        // Factory reset button
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.nukeAndReset() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Text("Nuke database (Fresh Profile onboarding)", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

// ==========================================
// 6. Logging Popup Dialogs
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, Float, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Chest") }
    var sets by remember { mutableStateOf("4") }
    var reps by remember { mutableStateOf("10") }
    var weightInput by remember { mutableStateOf("20") }

    val categoriesList = listOf("Chest", "Back", "Legs", "Shoulder", "Arms", "Cardio", "Yoga")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Gym Exercise", fontWeight = FontWeight.Black) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    placeholder = { Text("e.g. Bench Press") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("exercise_name_input")
                )

                // Category selector chip row scrolling
                Column {
                    Text("Exercise Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.forEach { category ->
                            val active = selectedCategory == category
                            FilterChip(
                                selected = active,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text("Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val setsInt = sets.toIntOrNull() ?: 4
                    val repsInt = reps.toIntOrNull() ?: 10
                    val weightFl = weightInput.toFloatOrNull() ?: 0f
                    val workoutName = name.ifBlank { "Strength Activity" }
                    
                    // Simple dynamic formula for burned calories
                    val multiplier = when (selectedCategory) {
                        "Cardio" -> 9.0f
                        "Legs" -> 7.5f
                        "Back", "Chest" -> 6.5f
                        "Shoulder", "Arms" -> 5.5f
                        else -> 4.5f
                    }
                    val calculatedCals = multiplier * setsInt * repsInt * (if (weightFl > 0) 1.15f else 1.0f)

                    onConfirm(workoutName, selectedCategory, setsInt, repsInt, weightFl, calculatedCals)
                },
                modifier = Modifier.testTag("dialog_workout_submit")
            ) {
                Text("Record Lift")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Float, Float, Float, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatsStr by remember { mutableStateOf("") }

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Food Intake", fontWeight = FontWeight.Black) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        // Intelligent calorie & macros autodetector!
                        when (it.lowercase().trim()) {
                            "banana" -> {
                                caloriesStr = "105"; proteinStr = "1"; carbsStr = "27"; fatsStr = "0"
                            }
                            "apple" -> {
                                caloriesStr = "95"; proteinStr = "0"; carbsStr = "25"; fatsStr = "0"
                            }
                            "egg", "boiled egg" -> {
                                caloriesStr = "78"; proteinStr = "6"; carbsStr = "0"; fatsStr = "5"
                            }
                            "chicken", "chicken breast" -> {
                                caloriesStr = "165"; proteinStr = "31"; carbsStr = "0"; fatsStr = "3.6"
                            }
                            "protein shake" -> {
                                caloriesStr = "150"; proteinStr = "25"; carbsStr = "4"; fatsStr = "2"
                            }
                            "peanut butter spoon" -> {
                                caloriesStr = "94"; proteinStr = "4"; carbsStr = "3"; fatsStr = "8"
                            }
                        }
                    },
                    label = { Text("Food Name") },
                    placeholder = { Text("e.g. Protein shake (with autodetector)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("meal_name_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mealTypes.forEach { type ->
                        val active = selectedMealType == type
                        FilterChip(
                            selected = active,
                            onClick = { selectedMealType = type },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = caloriesStr,
                    onValueChange = { caloriesStr = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatsStr,
                        onValueChange = { fatsStr = it },
                        label = { Text("Fats (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cals = caloriesStr.toFloatOrNull() ?: 150f
                    val prot = proteinStr.toFloatOrNull() ?: 5f
                    val carb = carbsStr.toFloatOrNull() ?: 15f
                    val fat = fatsStr.toFloatOrNull() ?: 2f
                    val foodName = name.ifBlank { "Snack Meal" }

                    onConfirm(foodName, selectedMealType, cals, prot, carb, fat)
                },
                modifier = Modifier.testTag("dialog_meal_submit")
            ) {
                Text("Log Food")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Float, Float, Float, String) -> Unit
) {
    var weight by remember { mutableStateOf("75") }
    var fat by remember { mutableStateOf("15") }
    var muscle by remember { mutableStateOf("40") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Metrics", fontWeight = FontWeight.Black) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Current Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("weight_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Body Fat %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = muscle,
                        onValueChange = { muscle = it },
                        label = { Text("Muscle Mass %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Progress Notes") },
                    placeholder = { Text("e.g., Morning weigh-in, post fasting") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wt = weight.toFloatOrNull() ?: 75f
                    val ft = fat.toFloatOrNull() ?: 15f
                    val ms = muscle.toFloatOrNull() ?: 40f
                    onConfirm(wt, ft, ms, notes)
                },
                modifier = Modifier.testTag("dialog_weight_submit")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
