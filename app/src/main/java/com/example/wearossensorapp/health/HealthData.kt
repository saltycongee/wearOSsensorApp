package com.example.wearossensorapp.health

data class HealthData (
        var longitude : Double,
        var latitude : Double,
        var heartRate : Float,
        var accelerometer : Float,
        var steps : Float,
        var temperature : Float,
        var heartBeat : Float,
        var heartRateAccuracy : Int,
        var accelerometerAccuracy: Int,
        var stepsAccuracy : Int,
        var temperatureAccuracy : Int,
        var heartBeatAccuracy : Int,
        var locationAccuracy : Float) {

        override fun toString(): String {
            return "HealthData(longitude=$longitude, latitude=$latitude, heartRate=$heartRate, " +
                    "accelerometer=$accelerometer, steps=$steps, temperature=$temperature, " +
                    "heartBeat=$heartBeat, heartRateAccuracy=$heartRateAccuracy, " +
                    "accelerometerAccuracy=$accelerometerAccuracy, stepsAccuracy=$stepsAccuracy, " +
                    "temperatureAccuracy=$temperatureAccuracy, heartBeatAccuracy=$heartBeatAccuracy, " +
                    "locationAccuracy=$locationAccuracy)"
        }
}