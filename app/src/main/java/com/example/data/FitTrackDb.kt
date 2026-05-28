package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 1. Entities
// ==========================================

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1, // Single-user profile setup
    val name: String,
    val email: String,
    val age: Int,
    val gender: String,
    val height: Float, // in cm
    val weight: Float, // in kg
    val goals: String, // e.g., "Weight Loss", "Muscle Gain", "Cardio Fitness", "Yoga & Flexibility"
    val dailyCalorieTarget: Int = 2000,
    val dailyProteinTargetGram: Float = 120f,
    val dailyCarbsTargetGram: Float = 200f,
    val dailyFatsTargetGram: Float = 70f,
    val dailyStepGoal: Int = 8000,
    val dailyWaterGoalMl: Int = 2500
)

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val workoutId: Int = 0,
    val date: String, // YYYY-MM-DD
    val exerciseName: String,
    val category: String, // Chest, Back, Legs, Shoulder, Arms, Cardio, Yoga
    val sets: Int,
    val reps: Int,
    val weightUsed: Float, // in kg
    val caloriesBurned: Float,
    val durationMinutes: Int = 15,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "nutrition")
data class Nutrition(
    @PrimaryKey(autoGenerate = true) val nutritionId: Int = 0,
    val date: String, // YYYY-MM-DD
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val foodName: String,
    val calories: Float,
    val protein: Float, // grams
    val carbs: Float, // grams
    val fats: Float // grams
)

@Entity(tableName = "progress")
data class Progress(
    @PrimaryKey(autoGenerate = true) val progressId: Int = 0,
    val date: String, // YYYY-MM-DD
    val weight: Float, // kg
    val bodyFat: Float, // percentage
    val muscleMass: Float, // percentage
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_activity")
data class DailyActivity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val stepsCount: Int = 0,
    val waterQuantityMl: Int = 0
)

// ==========================================
// 2. DAOs (Data Access Objects)
// ==========================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUserFlow(): Flow<User?>

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    suspend fun getUserSync(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    fun getAllWorkoutsFlow(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE date = :date ORDER BY timestamp DESC")
    fun getWorkoutsByDateFlow(date: String): Flow<List<Workout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Query("DELETE FROM workouts WHERE workoutId = :workoutId")
    suspend fun deleteWorkout(workoutId: Int)
}

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition ORDER BY nutritionId DESC")
    fun getAllNutritionFlow(): Flow<List<Nutrition>>

    @Query("SELECT * FROM nutrition WHERE date = :date ORDER BY nutritionId DESC")
    fun getNutritionByDateFlow(date: String): Flow<List<Nutrition>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: Nutrition)

    @Query("DELETE FROM nutrition WHERE nutritionId = :nutritionId")
    suspend fun deleteNutrition(nutritionId: Int)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress ORDER BY timestamp ASC")
    fun getAllProgressFlow(): Flow<List<Progress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: Progress)

    @Query("DELETE FROM progress WHERE progressId = :progressId")
    suspend fun deleteProgress(progressId: Int)
}

@Dao
interface DailyActivityDao {
    @Query("SELECT * FROM daily_activity WHERE date = :date LIMIT 1")
    fun getDailyActivityFlow(date: String): Flow<DailyActivity?>

    @Query("SELECT * FROM daily_activity WHERE date = :date LIMIT 1")
    suspend fun getDailyActivitySync(date: String): DailyActivity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyActivity(activity: DailyActivity)
}

// ==========================================
// 3. Database
// ==========================================

@Database(
    entities = [User::class, Workout::class, Nutrition::class, Progress::class, DailyActivity::class],
    version = 1,
    exportSchema = false
)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun progressDao(): ProgressDao
    abstract fun dailyActivityDao(): DailyActivityDao

    companion object {
        @Volatile
        private var INSTANCE: FitTrackDatabase? = null

        fun getDatabase(context: android.content.Context): FitTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    FitTrackDatabase::class.java,
                    "fittrack_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ==========================================
// 4. Repository Pattern
// ==========================================

class FitTrackRepository(private val database: FitTrackDatabase) {
    private val userDao = database.userDao()
    private val workoutDao = database.workoutDao()
    private val nutritionDao = database.nutritionDao()
    private val progressDao = database.progressDao()
    private val dailyActivityDao = database.dailyActivityDao()

    // Date Helper
    fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // User Profile
    val userFlow: Flow<User?> = userDao.getUserFlow()
    suspend fun getUser(): User? = userDao.getUserSync()
    suspend fun saveUser(user: User) = userDao.insertUser(user)

    // Workouts
    val allWorkoutsFlow: Flow<List<Workout>> = workoutDao.getAllWorkoutsFlow()
    fun getWorkoutsForDateFlow(date: String): Flow<List<Workout>> = workoutDao.getWorkoutsByDateFlow(date)
    suspend fun insertWorkout(workout: Workout) = workoutDao.insertWorkout(workout)
    suspend fun deleteWorkout(workoutId: Int) = workoutDao.deleteWorkout(workoutId)

    // Nutrition
    val allNutritionFlow: Flow<List<Nutrition>> = nutritionDao.getAllNutritionFlow()
    fun getNutritionForDateFlow(date: String): Flow<List<Nutrition>> = nutritionDao.getNutritionByDateFlow(date)
    suspend fun insertNutrition(nutrition: Nutrition) = nutritionDao.insertNutrition(nutrition)
    suspend fun deleteNutrition(nutritionId: Int) = nutritionDao.deleteNutrition(nutritionId)

    // Progress Weight logs
    val allProgressFlow: Flow<List<Progress>> = progressDao.getAllProgressFlow()
    suspend fun insertProgress(progress: Progress) = progressDao.insertProgress(progress)
    suspend fun deleteProgress(progressId: Int) = progressDao.deleteProgress(progressId)

    // Daily Activity (Steps and Water)
    fun getDailyActivityFlow(date: String): Flow<DailyActivity?> = dailyActivityDao.getDailyActivityFlow(date)
    
    suspend fun addSteps(date: String, steps: Int) {
        val current = dailyActivityDao.getDailyActivitySync(date) ?: DailyActivity(date = date)
        dailyActivityDao.insertDailyActivity(current.copy(stepsCount = current.stepsCount + steps))
    }

    suspend fun addWater(date: String, ml: Int) {
        val current = dailyActivityDao.getDailyActivitySync(date) ?: DailyActivity(date = date)
        dailyActivityDao.insertDailyActivity(current.copy(waterQuantityMl = current.waterQuantityMl + ml))
    }

    suspend fun resetDailyActivity(date: String) {
        dailyActivityDao.insertDailyActivity(DailyActivity(date = date, stepsCount = 0, waterQuantityMl = 0))
    }
}
