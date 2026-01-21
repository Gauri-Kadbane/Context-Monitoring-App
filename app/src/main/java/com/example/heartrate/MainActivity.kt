package com.example.heartrate

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var videoCapture: VideoCapture<Recorder>? = null
    private val requestCode = 10
    lateinit var file: File

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    var accelValuesX: MutableList<Float> = mutableListOf<Float>()
    var accelValuesY: MutableList<Float> = mutableListOf<Float>()
    var accelValuesZ: MutableList<Float> = mutableListOf<Float>()


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        requestPermissions()

        val btnRecord = findViewById<Button>(R.id.btnRecord)
        btnRecord.setOnClickListener {
            openCameraWithFlashAndRecordVideo()
        }
        val btnResp = findViewById<Button>(R.id.btnResp)
        btnResp.setOnClickListener {
            startSensor()
        }
        val btnUpload = findViewById<Button>(R.id.btnUpload)
        btnUpload.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            val txtHeartRate: TextView = findViewById(R.id.txtHeartRate)
            val txtRespRate: TextView = findViewById(R.id.txtRespRate)
            intent.putExtra("HeartRate", txtHeartRate.text.toString())
            intent.putExtra("RespRate", txtRespRate.text.toString())
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun openCameraWithFlashAndRecordVideo() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.SD))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            try {
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )

                // Turn on the flash
                camera.cameraControl.enableTorch(true)

                // Start recording video
                startRecording()

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("RestrictedApi")
    private fun startRecording() {
        val outputOptions = FileOutputOptions.Builder(createVideoFile()).build()

        val activeRecording = videoCapture?.output
            ?.prepareRecording(this, outputOptions)
            ?.start(ContextCompat.getMainExecutor(this)) {
                if (it is VideoRecordEvent.Finalize) {
                    Toast.makeText(this, "Video saved successfully!", Toast.LENGTH_SHORT).show()
                    var txtHeartRate: TextView = findViewById(R.id.txtHeartRate)
                    txtHeartRate.text = heartRateCalculator().toString()
                }
            }

        // Stop recording after 45 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(this, "Stopping Camera!", Toast.LENGTH_SHORT).show()
            //videoCapture?.output?.stopRecording()
            activeRecording?.stop()
            // activeRecording?.close()
            cameraProviderFuture.get().shutdownAsync()
        }, 45000)
    }

    private fun createVideoFile(): File {
        val videoDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        file = File(videoDir, "video_${System.currentTimeMillis()}.mp4")
        return file
    }


    private val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )


    private fun requestPermissions() {
        if (permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            //startCamera()
        } else {
            requestPermissions(permissions, requestCode)
        }
    }
    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview =  androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            try {
                // Bind the preview and video capture to the lifecycle
                var camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun heartRateCalculator(): Int {
        val result: Int
        val retriever = MediaMetadataRetriever()
        val frameList = ArrayList<Bitmap>()
        try {
            retriever.setDataSource(file.absolutePath)
            val duration =
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT
                )
            val frameDuration = min(duration!!.toInt(), 425)
            var i = 10
            while (i < frameDuration) {
                val bitmap = retriever.getFrameAtIndex(i)
                bitmap?.let { frameList.add(it) }
                i += 15
            }
        } catch (e: Exception) {
            // Log.d("MediaPath", "convertMediaUriToPath: ${e.stackTrace} ")
        } finally {
            retriever.release()
            var redBucket: Long
            var pixelCount: Long = 0
            val a = mutableListOf<Long>()
            for (i in frameList) {
                redBucket = 0
                for (y in 350 until 450) {
                    for (x in 350 until 450) {
                        val c: Int = i.getPixel(x, y)
                        pixelCount++
                        redBucket += Color.red(c) + Color.blue(c) +
                                Color.green(c)
                    }
                }
                a.add(redBucket)
            }
            val b = mutableListOf<Long>()
            for (i in 0 until a.lastIndex - 5) {
                val temp =
                    (a.elementAt(i) + a.elementAt(i + 1) + a.elementAt(i + 2)
                            + a.elementAt(
                        i + 3
                    ) + a.elementAt(
                        i + 4
                    )) / 4
                b.add(temp)
            }
            var x = b.elementAt(0)
            var count = 0
            for (i in 1 until b.lastIndex) {
                val p = b.elementAt(i)
                if ((p - x) > 3500) {
                    count += 1
                }
                x = b.elementAt(i)
            }
            val rate = ((count.toFloat()) * 60).toInt()
            result = (rate / 4)
        }
        return result
    }

    private fun startSensor() {
        Toast.makeText(this, "Sensor Started!", Toast.LENGTH_SHORT).show()
        accelValuesX.clear()
        accelValuesY.clear()
        accelValuesZ.clear()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Stop the accelerometer after 45 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(this, "Sensor Stopped!", Toast.LENGTH_SHORT).show()
            sensorManager.unregisterListener(this)
            var txtRespRate: TextView = findViewById(R.id.txtRespRate)
            txtRespRate.text = respiratoryRateCalculator().toString()

        }, 45000) // 45000 milliseconds = 45 seconds
    }

    fun respiratoryRateCalculator(): Int {
        var previousValue: Float
        var currentValue: Float
        previousValue = 10f
        var k = 0
        for (i in 11..<accelValuesY.size) {
            currentValue = kotlin.math.sqrt(
                accelValuesZ[i].toDouble().pow(2.0) + accelValuesX[i].toDouble()
                    .pow(2.0) + accelValuesY[i].toDouble().pow(2.0)
            ).toFloat()
            if (abs(x = previousValue - currentValue) > 0.15) {
                k++
            }
            previousValue = currentValue
        }
        val ret = (k.toDouble() / 45.00)
        return (ret * 30).toInt()
    }

    override fun onSensorChanged(evt: SensorEvent?) {
        if (evt?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = evt.values[0] // X-axis value
            val y = evt.values[1] // Y-axis value
            val z = evt.values[2] // Z-axis value
            accelValuesX.add(x)
            accelValuesY.add(y)
            accelValuesZ.add(z)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}

