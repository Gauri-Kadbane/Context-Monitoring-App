package com.example.heartrate

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadActivity : AppCompatActivity() {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_upload)
//        // setting up Room DB
//        val db = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "SymptomDB"
//        ).build()
//        val symptomRatingDao = db.symptomRatingDao()

        private lateinit var db: AppDatabase
        private lateinit var symptomRatingDao: SymptomRatingDao

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_upload)



            // Initialize the database in the background
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    // Initialize Room database
//                    val db = Room.databaseBuilder(
//                        applicationContext,
//                        AppDatabase::class.java, "SymptomDB"
//                    ).build()
//
//                    val symptomRatingDao = db.symptomRatingDao()
//
//                    // Perform database operations in the background thread
//                    // symptomRatingDao.insert(Symptom(...))
//
//                    // Switch to the main thread if you need to update UI
//                    withContext(Dispatchers.Main) {
//                        // Update UI (like displaying confirmation to the user)
//                    }
//                } catch (e: Exception) {
//                    // Log the exception to understand the issue
//                    Log.e("UploadActivity", "Error initializing database", e)
//                }
//            }

                var selectedSymptom: String = ""
        val heartRate = intent.getStringExtra("HeartRate")
        val respRate = intent.getStringExtra("RespRate")

        val symptom: Spinner = findViewById(R.id.symptom)
        val rates: RatingBar = findViewById(R.id.rating)

        val btnUploadSymptom: Button = findViewById(R.id.btnUploadSymptom)
        val symArray = resources.getStringArray(R.array.symptoms)

        // setting up spinner
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, symArray)
        symptom.adapter = adapter
        symptom.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                selectedSymptom = symArray[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // storing data to Room DB
        btnUploadSymptom.setOnClickListener{
            val symptomRating: SymptomRating = SymptomRating(selectedSymptom, heartRate, respRate, rates.rating.toString())
//            CoroutineScope(Dispatchers.IO).launch {
//                symptomRatingDao.insertAll(symptomRating)
//            }
            AppDatabase.getInstance(this)?.symptomRatingDao()?.insertAll(symptomRating)
            rates.rating = 0F
            var file_path = getDatabasePath("symptom.db").absolutePath
            Toast.makeText(this, "symptom saved successfully!"+getDatabasePath("symptom.db").absolutePath, Toast.LENGTH_LONG).show()

        }
    }

}

@Entity
data class SymptomRating(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "symptom") val symptom: String,
    @ColumnInfo(name = "heart_rate") var heartRate: String?,
    @ColumnInfo(name = "resp_rate") val respRate: String?,
    @ColumnInfo(name = "ratings") val ratings: String?
)
{
    constructor(symptom: String, heartRate: String?, respRate: String?, ratings: String?)   : this(null, symptom, heartRate, respRate, ratings)
}




@Database(entities = [SymptomRating::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {


        var instance: AppDatabase? = null
        fun getInstance(context: Context?): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    if (context != null) {
                        instance = Room.databaseBuilder(
                            context,

                            AppDatabase::class.java,
                            "symptom.db"
                        ).allowMainThreadQueries().build()
                    }
                }
            }
            return instance
        }
    }

    abstract fun symptomRatingDao(): SymptomRatingDao?
}

@Dao
interface SymptomRatingDao {
    @Query("SELECT * FROM SymptomRating")
    fun getAll(): List<SymptomRating>

//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User

    @Insert
    fun insertAll(vararg symptomRatings: SymptomRating)

    @Delete
    fun delete(symptomRating: SymptomRating)
}