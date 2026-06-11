# VESPER Somatic Input - Real Hardware Build

## Goal: Anyone can replicate this for <$50

### Tier 1: Phone Only (Free)
- iPhone/Android with Health app
- Export HRV: Apple Watch/Fitbit -> Health -> Export RMSSD
- Sleep: Use "Sleep Cycle" (free) -> export movement variance
- Manual 60Hz: use phone magnetometer app near outlet

### Tier 2: Recommended ($35 total)
1. ESP32 Dev Board - $8
2. MPU6050 accelerometer - $3 (sleep movement)
3. GQ EMF-390 USB meter - $25 (measures 60Hz accurately)

Wiring: ESP32 reads MPU6050 via I2C, sends data over BLE. EMF meter plugs into laptop, Python reads serial.

### Tier 3: Wearable Integration
- Oura Ring, Apple Watch, Garmin: export via API
- Use HealthKit or Google Fit API
- Script polls every 5 min

### Safety Note
This measures environmental stress correlates, not medical diagnosis. For oncology applications, this is a research simulator only. Consult healthcare professionals for medical decisions.

### Output
Creates somatic_field_log.jsonl with local_entropy 0-2.0
Feed directly into oncology_somatics_simulator.braid
