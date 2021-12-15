package com.example.wearossensorapp.health

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.wearossensorapp.MainActivity
import com.example.wearossensorapp.sensor.SensorCollector
import com.example.wearossensorapp.utils.AWSViewModel
import com.google.android.gms.location.*
import com.google.common.math.DoubleMath.roundToInt
import com.google.common.primitives.UnsignedBytes.toInt
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class HealthService : Service() {
    // Binder given to clients (notice class declaration below)
    private val mBinder: IBinder = ServiceBinder()
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private val TAG = "HealthService"
    lateinit var AWSviewModel : AWSViewModel
    lateinit var clientID : String
    // Channel ID for notification
    val CHANNEL_ID = "1"

    lateinit var mostRecentHealthData : HealthData

    private lateinit var sensorCollector: SensorCollector

    private var check : Boolean = false

    //Float array of most recent values from the sensors
    var mostRecentValues = FloatArray(5)

    companion object {
        private val UPDATE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(20)
        private val FASTEST_INTERVAL_MS = TimeUnit.SECONDS.toMillis(10)
    }


    inner class ServiceBinder : Binder() {
        // Return this instance of MyService so clients can call public methods
        val service: HealthService
            get() =// Return this instance of MyService so clients can call public methods
                this@HealthService
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
        startNotification()

        setupSensorCollector()
        setupLocationMonitoring()
        requestLocation()
        mostRecentValues = sensorCollector.mostRecentValues
    }

    /**
     * Used for creating and starting notification
     * whenever we start our Bound service
     */
    private fun startNotification() {
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                    CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("A service is running in the background")
                .build()
        startForeground(1, notification)
    }


    private fun setupSensorCollector(){
        Log.d(TAG, "setupSensorCollector: ")
        sensorCollector = SensorCollector(
                applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        )
        sensorCollector.start()
    }

    private fun setupLocationMonitoring(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d(TAG, "onLocationResult: ")
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    Log.d(TAG, "onLocationChanged() : $location")
                    val heartRateValues = sensorCollector.heartRateValues
                    val accelerometerValues = sensorCollector.accelerometerValues
                    val stepValues = sensorCollector.stepValues
                    val temperatureValues = sensorCollector.temperatureValues
                    val heartBeatValues = sensorCollector.heartBeatValues
                    if(heartRateValues.size > 0 && accelerometerValues.size > 0 && stepValues.size > 0 && temperatureValues.size > 0 && heartBeatValues.size > 0) {
                        val lastHeartRate = heartRateValues.last().value
                        val heartRateAccuracy = heartRateValues.last().accuracy
                        val lastAccelerometer = accelerometerValues.last().value
                        val accelerometerAccuracy = accelerometerValues.last().accuracy
                        val lastStepCount = stepValues.last().value
                        val stepCountAccuracy = stepValues.last().accuracy
                        val lastTemperatureCount = temperatureValues.last().value
                        val temperatureAccuracy = temperatureValues.last().accuracy
                        val lastHeartBeat = heartBeatValues.last().value
                        val heartBeatAccuracy = heartBeatValues.last().accuracy
                        mostRecentHealthData =
                                HealthData(
                                        longitude = location.longitude,
                                        latitude = location.latitude,
                                        heartRate = lastHeartRate,
                                        accelerometer = lastAccelerometer,
                                        steps = lastStepCount,
                                        temperature = lastTemperatureCount,
                                        heartBeat = lastHeartBeat,
                                        heartRateAccuracy = heartRateAccuracy,
                                        accelerometerAccuracy = accelerometerAccuracy,
                                        stepsAccuracy = stepCountAccuracy,
                                        temperatureAccuracy = temperatureAccuracy,
                                        heartBeatAccuracy = heartBeatAccuracy,
                                        locationAccuracy = location.accuracy
                                )
                        check = true
                        sendData(mostRecentHealthData)
                        Log.d(TAG, "lat : ${location.latitude}, lon = ${location.longitude}, hr : $lastHeartRate, accel : $lastAccelerometer, steps: $lastStepCount, temp : $lastStepCount, hb : $lastHeartBeat")
                        //Toast.makeText(applicationContext, "lat : ${location.latitude}, lon = ${location.longitude}, hr : $lastHeartRate", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }



    private fun requestLocation() {
        Log.d(TAG, "requestLocation()")

        /*
             * mGpsPermissionApproved covers 23+ (M+) style permissions. If that is already approved or
             * the device is pre-23, the app uses mSaveGpsLocation to save the user's location
             * preference.
             */
        val mGpsPermissionApproved = (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                === PackageManager.PERMISSION_GRANTED)
        if (mGpsPermissionApproved) {
            Log.d(TAG, "requestLocation: make request")
            val locationRequest: LocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL_MS)
                    .setFastestInterval(FASTEST_INTERVAL_MS)
            try {
                fusedLocationProviderClient?.requestLocationUpdates(
                        locationRequest, locationCallback, Looper.getMainLooper())
            } catch (unlikely: SecurityException) {
                Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
            }
        }
    }

    /**
     * @param : HealthData
     * Sends heartRate, latitude, longitude of the wearable and
     * publishes them to an IoT topic.
     * Additionally sends deviceID, deviceOS and time.
     */
    private fun sendData(healthData : HealthData) {
        val jsnObject = JSONObject()
        val release = java.lang.Double.parseDouble(java.lang.String(Build.VERSION.RELEASE).replaceAll("(\\d+[.]\\d+)(.*)", "$1"))
        val isoString = getISOString()
        Log.d(MainActivity.TAG, "sendData: date " + isoString)
        jsnObject.put("heart_rate", healthData.heartRate)
        jsnObject.put("accelerometer", healthData.accelerometer)
        jsnObject.put("steps", healthData.steps)
        jsnObject.put("temperature", healthData.temperature)
        jsnObject.put("heart_beat", healthData.heartBeat)
        jsnObject.put("latitude", healthData.latitude)
        jsnObject.put("longitude", healthData.longitude)
        jsnObject.put("deviceID", clientID)
        jsnObject.put("deviceOS", "Android ${release}")
        jsnObject.put("time", isoString)
        Log.d(MainActivity.TAG, "sendData: json " + jsnObject.toString())
        AWSviewModel.publish(jsnObject.toString(), AWSViewModel.TOPIC_NAME)
    }

    /**
     * Gets the UTC time in ISO format.
     */
    private fun getISOString() : String {
        val date = Date(System.currentTimeMillis())
        val sdf: SimpleDateFormat
        sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss")
        val tz = TimeZone.getTimeZone("UTC")
        sdf.setTimeZone(tz)
        val text: String = sdf.format(date) + "Z"
        Log.d(MainActivity.TAG, "getISOString: " + text)
        return text
    }
    public fun checkStatus() : Boolean {
        return check
    }
}