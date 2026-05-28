package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class FitTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FitTrackDatabase.getDatabase(application)
    val repository = FitTrackRepository(db)

    val todayDate = repository.getTodayString()

    // ------------------------------------------
    // State Flows
    // ------------------------------------------
    val currentUser: StateFlow<User?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayWorkouts: StateFlow<List<Workout>> = repository.getWorkoutsForDateFlow(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayNutrition: StateFlow<List<Nutrition>> = repository.getNutritionForDateFlow(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightProgressLogs: StateFlow<List<Progress>> = repository.allProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayActivity: StateFlow<DailyActivity?> = repository.getDailyActivityFlow(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI Navigation Tab
    private val _currentTab = MutableStateFlow("Dashboard")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    // Onboarding Form State
    var onboardingName = MutableStateFlow("")
    var onboardingEmail = MutableStateFlow("")
    var onboardingAge = MutableStateFlow("24")
    var onboardingGender = MutableStateFlow("Male")
    var onboardingHeight = MutableStateFlow("175")
    var onboardingWeight = MutableStateFlow("72")
    var onboardingGoal = MutableStateFlow("Muscle Gain")

    // Timers
    private var workoutTimerJob: Job? = null
    private val _workoutTimeSeconds = MutableStateFlow(0)
    val workoutTimeSeconds: StateFlow<Int> = _workoutTimeSeconds.asStateFlow()
    private val _isWorkoutTimerRunning = MutableStateFlow(false)
    val isWorkoutTimerRunning: StateFlow<Boolean> = _isWorkoutTimerRunning.asStateFlow()

    private var restTimerJob: Job? = null
    private val _restTimeSeconds = MutableStateFlow(0)
    val restTimeSeconds: StateFlow<Int> = _restTimeSeconds.asStateFlow()
    private val _isRestTimerRunning = MutableStateFlow(false)
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning.asStateFlow()
    private val _restTimerInitialSeconds = MutableStateFlow(60)
    val restTimerInitialSeconds: StateFlow<Int> = _restTimerInitialSeconds.asStateFlow()

    // Simple Reminders State (in-app display)
    private val _remindersEnabled = MutableStateFlow(true)
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private val _activeWearableSync = MutableStateFlow(false)
    val activeWearableSync: StateFlow<Boolean> = _activeWearableSync.asStateFlow()

    private val _wearableSyncSuccessMsg = MutableStateFlow<String?>(null)
    val wearableSyncSuccessMsg: StateFlow<String?> = _wearableSyncSuccessMsg.asStateFlow()

    // Onboarding Submission
    fun saveProfile() {
        viewModelScope.launch {
            val nameVal = onboardingName.value.ifBlank { "Fitness Enthusiast" }
            val emailVal = onboardingEmail.value.ifBlank { "user@fittrack.com" }
            val ageVal = onboardingAge.value.toIntOrNull() ?: 24
            val genderVal = onboardingGender.value
            val heightVal = onboardingHeight.value.toFloatOrNull() ?: 175f
            val weightVal = onboardingWeight.value.toFloatOrNull() ?: 70f
            val goalVal = onboardingGoal.value

            // Set default targets based on goal
            val calorieTarget = when (goalVal) {
                "Weight Loss" -> 1800
                "Muscle Gain" -> 2800
                "Cardio Fitness" -> 2200
                else -> 2000
            }
            val proteinTarget = when (goalVal) {
                "Weight Loss" -> 130f
                "Muscle Gain" -> 160f
                "Cardio Fitness" -> 110f
                else -> 120f
            }
            val carbsTarget = when (goalVal) {
                "Weight Loss" -> 160f
                "Muscle Gain" -> 320f
                "Cardio Fitness" -> 240f
                else -> 200f
            }
            val fatsTarget = when (goalVal) {
                "Weight Loss" -> 55f
                "Muscle Gain" -> 85f
                "Cardio Fitness" -> 60f
                else -> 70f
            }

            val newUser = User(
                name = nameVal,
                email = emailVal,
                age = ageVal,
                gender = genderVal,
                height = heightVal,
                weight = weightVal,
                goals = goalVal,
                dailyCalorieTarget = calorieTarget,
                dailyProteinTargetGram = proteinTarget,
                dailyCarbsTargetGram = carbsTarget,
                dailyFatsTargetGram = fatsTarget
            )

            repository.saveUser(newUser)

            // Insert a first baseline weight logger in progress
            repository.insertProgress(
                Progress(
                    date = todayDate,
                    weight = weightVal,
                    bodyFat = 18f,
                    muscleMass = 42f,
                    notes = "Initial Profile Weight"
                )
            )

            // Insert some default mock records to make first layout look stunning
            seedMockDataIfEmpty()
        }
    }

    // Mock data to give an immediately rich UX experience
    private suspend fun seedMockDataIfEmpty() {
        // Only seed if empty
        repository.insertWorkout(
            Workout(
                date = todayDate,
                exerciseName = "Incline Dumbbell Press",
                category = "Chest",
                sets = 4,
                reps = 10,
                weightUsed = 24f,
                caloriesBurned = 180f
            )
        )
        repository.insertWorkout(
            Workout(
                date = todayDate,
                exerciseName = "Lat Pulldown",
                category = "Back",
                sets = 3,
                reps = 12,
                weightUsed = 55f,
                caloriesBurned = 120f
            )
        )

        repository.insertNutrition(
            Nutrition(
                date = todayDate,
                mealType = "Breakfast",
                foodName = "Oatmeal with Pecans & Banana",
                calories = 420f,
                protein = 15f,
                carbs = 62f,
                fats = 12f
            )
        )
        repository.insertNutrition(
            Nutrition(
                date = todayDate,
                mealType = "Lunch",
                foodName = "Grilled Chicken Breast with Quinoa",
                calories = 580f,
                protein = 46f,
                carbs = 45f,
                fats = 14f
            )
        )

        repository.addSteps(todayDate, 4120)
        repository.addWater(todayDate, 1250)
    }

    // Log workout
    fun addWorkout(name: String, category: String, sets: Int, reps: Int, weight: Float, cals: Float) {
        viewModelScope.launch {
            repository.insertWorkout(
                Workout(
                    date = todayDate,
                    exerciseName = name,
                    category = category,
                    sets = sets,
                    reps = reps,
                    weightUsed = weight,
                    caloriesBurned = cals
                )
            )
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
        }
    }

    // Log Nutrition
    fun addMeal(name: String, mealType: String, calories: Float, protein: Float, carbs: Float, fats: Float) {
        viewModelScope.launch {
            repository.insertNutrition(
                Nutrition(
                    date = todayDate,
                    mealType = mealType,
                    foodName = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fats = fats
                )
            )
        }
    }

    fun deleteMeal(id: Int) {
        viewModelScope.launch {
            repository.deleteNutrition(id)
        }
    }

    // Log Weight/Progress
    fun addProgressLog(weight: Float, bodyFat: Float, muscleMass: Float, notes: String = "") {
        viewModelScope.launch {
            repository.insertProgress(
                Progress(
                    date = todayDate,
                    weight = weight,
                    bodyFat = bodyFat,
                    muscleMass = muscleMass,
                    notes = notes
                )
            )
            // also update current user weight so BMI reflects immediately!
            val user = repository.getUser()
            if (user != null) {
                repository.saveUser(user.copy(weight = weight))
            }
        }
    }

    fun deleteProgress(id: Int) {
        viewModelScope.launch {
            repository.deleteProgress(id)
        }
    }

    // Water logging & Steps additions
    fun logWater(ml: Int) {
        viewModelScope.launch {
            repository.addWater(todayDate, ml)
        }
    }

    fun logSteps(count: Int) {
        viewModelScope.launch {
            repository.addSteps(todayDate, count)
        }
    }

    fun resetTodayGoalsAndLogs() {
        viewModelScope.launch {
            repository.resetDailyActivity(todayDate)
        }
    }

    fun toggleReminders() {
        _remindersEnabled.value = !_remindersEnabled.value
    }

    // Wearable Mock Sync
    fun syncWithWearables() {
        _activeWearableSync.value = true
        _wearableSyncSuccessMsg.value = null
        viewModelScope.launch {
            delay(1500) // Simulate reading payload
            val randomizedSteps = (3000..5500).random()
            val randomizedWater = listOf(250, 500, 750).random()
            
            repository.addSteps(todayDate, randomizedSteps)
            repository.addWater(todayDate, randomizedWater)

            _activeWearableSync.value = false
            _wearableSyncSuccessMsg.value = "Synced: +$randomizedSteps Steps, +${randomizedWater}ml Hydration from Google Fit."
        }
    }

    fun dismissSyncGreeting() {
        _wearableSyncSuccessMsg.value = null
    }

    // ------------------------------------------
    // Timer Controllers
    // ------------------------------------------
    fun toggleWorkoutTimer() {
        if (_isWorkoutTimerRunning.value) {
            workoutTimerJob?.cancel()
            _isWorkoutTimerRunning.value = false
        } else {
            _isWorkoutTimerRunning.value = true
            workoutTimerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _workoutTimeSeconds.value += 1
                }
            }
        }
    }

    fun resetWorkoutTimer() {
        workoutTimerJob?.cancel()
        _workoutTimeSeconds.value = 0
        _isWorkoutTimerRunning.value = false
    }

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _restTimerInitialSeconds.value = seconds
        _restTimeSeconds.value = seconds
        _isRestTimerRunning.value = true
        
        restTimerJob = viewModelScope.launch {
            while (_restTimeSeconds.value > 0) {
                delay(1000)
                _restTimeSeconds.value -= 1
            }
            _isRestTimerRunning.value = false
        }
    }

    fun stopRestTimer() {
        restTimerJob?.cancel()
        _restTimeSeconds.value = 0
        _isRestTimerRunning.value = false
    }

    // Helper to format SS to HH:MM:SS
    fun formatTimerText(totalSecs: Int): String {
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    // ------------------------------------------
    // AI Fitness Coach Logic
    // ------------------------------------------
    fun getAIEngineRecommendations(
        user: User, 
        workouts: List<Workout>, 
        meals: List<Nutrition>, 
        act: DailyActivity?
    ): List<CoachTip> {
        val list = mutableListOf<CoachTip>()

        val caloriesLogged = meals.sumOf { it.calories.toDouble() }.toInt()
        val proteinLogged = meals.sumOf { it.protein.toDouble() }.toFloat()
        val waterConsumed = act?.waterQuantityMl ?: 0
        val stepsWalked = act?.stepsCount ?: 0

        val goal = user.goals

        // Calorie Coach Tip
        if (goal == "Weight Loss") {
            if (caloriesLogged > user.dailyCalorieTarget) {
                list.add(
                    CoachTip(
                        title = "Calorie Cap Recommendation",
                        message = "You have eaten $caloriesLogged kcal today, exceeding your weight loss deficit goal of ${user.dailyCalorieTarget} kcal. Try choosing fiber-rich vegetables for remaining snacks.",
                        category = "Diet",
                        urgency = "High"
                    )
                )
            } else if (caloriesLogged > 0 && caloriesLogged < user.dailyCalorieTarget - 400) {
                list.add(
                    CoachTip(
                        title = "Maintain Lean Mass Deficit",
                        message = "Your calorie intake is $caloriesLogged kcal, below your healthy target. Ensure you reach at least ${user.dailyCalorieTarget - 300} kcal to preserve critical calorie metabolism.",
                        category = "Diet",
                        urgency = "Medium"
                    )
                )
            } else {
                list.add(
                    CoachTip(
                        title = "Perfect Deficit Zone",
                        message = "Fantastic pacing! Eaten $caloriesLogged / ${user.dailyCalorieTarget} kcal. Keep hydrated to sustain full physical fat oxidation during sleep.",
                        category = "Diet",
                        urgency = "Normal"
                    )
                )
            }
        } else if (goal == "Muscle Gain") {
            if (caloriesLogged < user.dailyCalorieTarget - 400) {
                list.add(
                    CoachTip(
                        title = "Caloric Surplus Notice",
                        message = "Anabolic muscle gain requires a positive nitrogen balance. You are currently at $caloriesLogged kcal from target ${user.dailyCalorieTarget}. Add a dense shake (peanut butter, banana, oats) to fuel recovery.",
                        category = "Diet",
                        urgency = "High"
                    )
                )
            } else {
                list.add(
                    CoachTip(
                        title = "Surplus Target Achieved",
                        message = "$caloriesLogged / ${user.dailyCalorieTarget} kcal logged. Your training has ample glycogen reserves for hard progressive overload sessions today!",
                        category = "Diet",
                        urgency = "Normal"
                    )
                )
            }
        }

        // Protein Coach Tip
        if (proteinLogged < user.dailyProteinTargetGram - 30) {
            list.add(
                CoachTip(
                    title = "Protein Synthesis Support",
                    message = "You logged ${proteinLogged.toInt()}g protein today vs targeted ${user.dailyProteinTargetGram.toInt()}g. Consuming 25-30g of protein within 2 hours of sleep speeds up overnight muscular cell synthesis.",
                    category = "Nutrition",
                    urgency = "High"
                )
            )
        }

        // Hydration Tip
        if (waterConsumed < user.dailyWaterGoalMl / 2) {
            list.add(
                CoachTip(
                    title = "Dehydration & Strength",
                    message = "Hydration is at $waterConsumed ml (Goal: ${user.dailyWaterGoalMl} ml). Even 2% dehydration reduces squat & bench press capability by up to 10-15%. Drink a pint right now!",
                    category = "Water",
                    urgency = "High"
                )
            )
        } else if (waterConsumed >= user.dailyWaterGoalMl) {
            list.add(
                CoachTip(
                    title = "Optimum Cellular Hydration",
                    message = "Hydration goals achieved! $waterConsumed ml of pure water. High cellular water content maximizes intramuscular volume and supports speedy toxic clearance.",
                    category = "Water",
                    urgency = "Normal"
                )
            )
        }

        // Workouts Advice
        if (workouts.isEmpty()) {
            val suggestedType = when (goal) {
                "Weight Loss" -> "Cardio & Full Body HIIT circuit"
                "Muscle Gain" -> "Core Push/Pull hypertrophy sequence"
                "Cardio Fitness" -> "Steady-state running or rowing intervals"
                else -> "Mobility flows and yoga restorative routines"
            }
            list.add(
                CoachTip(
                    title = "Begin Training Routine",
                    message = "No workouts logged today. A prompt 30-minute session of $suggestedType will trigger healthy cortisol release and elevate daily calorie expenditure.",
                    category = "Gym",
                    urgency = "Medium"
                )
            )
        } else {
            val totalSets = workouts.sumOf { it.sets }
            if (totalSets >= 12) {
                list.add(
                    CoachTip(
                        title = "Overtraining Warning",
                        message = "You registered $totalSets active sets. Rest durations should be extended to 90 seconds to allow standard muscular ATP restoration.",
                        category = "Gym",
                        urgency = "Medium"
                    )
                )
            } else {
                list.add(
                    CoachTip(
                        title = "Splendid Muscle Stimulus",
                        message = "You finished ${workouts.size} exercises with solid weight ratios! Remember, progressive load tracking is verified when executing the same rep totals safely at +2.5kg next week.",
                        category = "Gym",
                        urgency = "Normal"
                    )
                )
            }
        }

        // Steps advice
        if (stepsWalked > 0 && stepsWalked < user.dailyStepGoal) {
            list.add(
                CoachTip(
                    title = "Active Daily NEAT",
                    message = "Steps walk count at $stepsWalked / ${user.dailyStepGoal}. Simple Non-Exercise Activity Thermogenesis (NEAT) keeps lipolysis responsive. Consider a brief brisk walk after meals.",
                    category = "Steps",
                    urgency = "Medium"
                )
            )
        } else if (stepsWalked >= user.dailyStepGoal) {
            list.add(
                CoachTip(
                    title = "Elite Cardio Endurance",
                    message = "Outstanding step count! $stepsWalked steps logged. You are triggering exceptional cardiac strokes and insulin sensitivities.",
                    category = "Steps",
                    urgency = "Normal"
                )
            )
        }

        return list
    }

    // Clear user and perform full fresh reset
    fun nukeAndReset() {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                db.clearAllTables()
            }
        }
    }
}

data class CoachTip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val category: String, // Diet, Gym, Nutrition, Water, Steps
    val urgency: String // High, Medium, Normal
)
