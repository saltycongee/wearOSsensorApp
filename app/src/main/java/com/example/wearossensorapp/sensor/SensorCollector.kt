package com.example.wearossensorapp.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.example.wearossensorapp.MainActivity
import com.example.wearossensorapp.R

class   SensorCollector : SensorEventListener {

  val TAG = "SensorCollector"

  private var sensorManager: SensorManager
  //Sensors requested from the wearable
  private lateinit var heartRate: Sensor
  private lateinit var accelerometer: Sensor
  private lateinit var steps: Sensor
  private lateinit var temperature: Sensor
  private lateinit var heartBeat: Sensor

  //Sensor handlers for simulating delay
  private val heartRateHandler = Handler(Looper.getMainLooper())
  private val accelerometerHandler = Handler(Looper.getMainLooper())
  private val stepsHandler = Handler(Looper.getMainLooper())
  private val temperatureHandler = Handler(Looper.getMainLooper())
  private val heartBeatHandler = Handler(Looper.getMainLooper())

  //Exposes most recently read values to the foreground service
  var heartRateValues: MutableList<SensorData> = mutableListOf<SensorData>()
  var accelerometerValues: MutableList<SensorData> = mutableListOf<SensorData>()
  var stepValues: MutableList<SensorData> = mutableListOf<SensorData>()
  var temperatureValues: MutableList<SensorData> = mutableListOf<SensorData>()
  var heartBeatValues: MutableList<SensorData> = mutableListOf<SensorData>()

  //Float array of most recent values from the sensors
  var mostRecentValues = FloatArray(5)

  //Used to ignore results that have low accuracy
  private val sensorThreshhold = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM

  //Used to control sampling rate
  private val DELAY_IN_SEC = 5

  constructor(sensorManager: SensorManager) {
    this.sensorManager = sensorManager
  }

  //Sensor will equal null if the watch does not support the sensor
  fun start() {
    if (sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
      heartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
      sensorManager.registerListener(this, this.heartRate, SensorManager.SENSOR_DELAY_UI, 100000)
    }
    if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
      accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
      sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_UI, 100000)
    }
    if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
      steps = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
      sensorManager.registerListener(this, this.steps, SensorManager.SENSOR_DELAY_UI, 100000)
    }
    if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
      temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
      sensorManager.registerListener(this, this.temperature, SensorManager.SENSOR_DELAY_UI, 100000)
    }
    if (sensorManager.getDefaultSensor(Sensor.TYPE_HEART_BEAT) != null) {
      heartBeat = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_BEAT)
      sensorManager.registerListener(this, this.heartBeat, SensorManager.SENSOR_DELAY_UI, 100000)
    }
    Log.d(TAG,"Sensors Started")
    //for debugging
//    this.sensor = sensor ?: sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
//    sensorManager.registerListener(this, this.sensor, SensorManager.SENSOR_DELAY_UI, 100000)
  }

  override fun onSensorChanged(event: SensorEvent?) {
    val eventSensor = event?.sensor ?: return
    if (eventSensor.type == heartRate.type) {
      Log.d(TAG, "Value: " + event.values[0] + " accuracy: " + event.accuracy)
      if (event.accuracy >= sensorThreshhold) {
        if(this.heartRateValues.size > 0){
          //Simulating delay
          sensorManager.unregisterListener(this, this.heartRate)
          this.heartRateValues.clear()
          heartRateHandler.postDelayed(object : Runnable {
            override fun run() {
              Log.d(MainActivity.TAG, "Heart Rate Handler")
              sensorManager.registerListener(this@SensorCollector, heartRate, SensorManager.SENSOR_DELAY_UI, 100000)
            }
          }, DELAY_IN_SEC * 1000L)
        }
        this.heartRateValues.add(SensorData(event.accuracy, event.timestamp, event.values[0]))
        mostRecentValues[0] = event.values[0]
      } else{
        Log.d(TAG, "onSensorChanged: sensor data accuracy is not accurate enough")
      }
    } else if (eventSensor.type == accelerometer.type) {
      Log.d(TAG, "Value: " + event.values[0] + " accuracy: " + event.accuracy)
      if (event.accuracy >= sensorThreshhold) {
        if(this.accelerometerValues.size > 0){
          //Simulating delay
          sensorManager.unregisterListener(this, this.accelerometer)
          this.accelerometerValues.clear()
          accelerometerHandler.postDelayed(object : Runnable {
            override fun run() {
              Log.d(MainActivity.TAG, "Accelerometer Handler")
              sensorManager.registerListener(this@SensorCollector, accelerometer, SensorManager.SENSOR_DELAY_UI, 100000)
            }
          }, DELAY_IN_SEC * 1000L)
        }
        this.accelerometerValues.add(SensorData(event.accuracy, event.timestamp, event.values[0]))
        mostRecentValues[1] = event.values[0]
      } else{
        Log.d(TAG, "onSensorChanged: sensor data accuracy is not accurate enough")
      }
    } else if (eventSensor.type == steps.type) {
      Log.d(TAG, "Value: " + event.values[0] + " accuracy: " + event.accuracy)
      if (event.accuracy >= sensorThreshhold) {
        if(this.stepValues.size > 0){
          //Simulating delay
          sensorManager.unregisterListener(this, this.steps)
          this.stepValues.clear()
          stepsHandler.postDelayed(object : Runnable {
            override fun run() {
              Log.d(MainActivity.TAG, "Steps Handler")
              sensorManager.registerListener(this@SensorCollector, steps, SensorManager.SENSOR_DELAY_UI, 100000)
            }
          }, DELAY_IN_SEC * 1000L)
        }
        this.stepValues.add(SensorData(event.accuracy, event.timestamp, event.values[0]))
        mostRecentValues[2] = event.values[0]
      } else{
        Log.d(TAG, "onSensorChanged: sensor data accuracy is not accurate enough")
      }
    } else if (eventSensor.type == temperature.type) {
      Log.d(TAG, "Value: " + event.values[0] + " accuracy: " + event.accuracy)
      if (event.accuracy >= sensorThreshhold) {
        if(this.temperatureValues.size > 0){
          //Simulating delay
          sensorManager.unregisterListener(this, this.temperature)
          this.temperatureValues.clear()
          temperatureHandler.postDelayed(object : Runnable {
            override fun run() {
              Log.d(MainActivity.TAG, "Temperature Handler")
              sensorManager.registerListener(this@SensorCollector, temperature, SensorManager.SENSOR_DELAY_UI, 100000)
            }
          }, DELAY_IN_SEC * 1000L)
        }
        this.temperatureValues.add(SensorData(event.accuracy, event.timestamp, event.values[0]))
        mostRecentValues[3] = event.values[0]
      } else{
        Log.d(TAG, "onSensorChanged: sensor data accuracy is not accurate enough")
      }
    } else if (eventSensor.type == heartBeat.type) {
      Log.d(TAG, "Value: " + event.values[0] + " accuracy: " + event.accuracy)
      if (event.accuracy >= sensorThreshhold) {
        if(this.heartBeatValues.size > 0){
          //Simulating delay
          sensorManager.unregisterListener(this, this.heartBeat)
          this.heartBeatValues.clear()
          heartBeatHandler.postDelayed(object : Runnable {
            override fun run() {
              Log.d(MainActivity.TAG, "Heart Beat Handler")
              sensorManager.registerListener(this@SensorCollector, heartBeat, SensorManager.SENSOR_DELAY_UI, 100000)
            }
          }, DELAY_IN_SEC * 1000L)
        }
        this.heartBeatValues.add(SensorData(event.accuracy, event.timestamp, event.values[0]))
        mostRecentValues[4] = event.values[0]
      } else{
        Log.d(TAG, "onSensorChanged: sensor data accuracy is not accurate enough")
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}