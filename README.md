# Context Monitoring App

An Android app that monitors health signs like heart rate and respiratory rate using smartphone sensors. Users can also log symptoms along with intensity ratings, and all data is stored locally for future reference.

[Live Demo Video](https://youtu.be/2rsI3k5fdDE)

---

## Features

- **Heart Rate Calculation** using the camera  
- **Respiratory Rate Calculation** using the accelerometer  
- **Symptom Logging** with intensity ratings  
- **Local Database Storage** using RoomDB  
- **Data Export** to view in SQLite DB Browser  

---

## Tech Stack

- **Language:** Kotlin  
- **IDE:** Android Studio (Android API 34)  
- **Database:** RoomDB (SQLite)  
- **Devices Tested:** Samsung A15 smartphone  
- **Libraries & Tools:** Android Camera2 API, Accelerometer Sensor, RatingBar, Spinner  

---

## How It Works

1. **Heart Rate Calculation:**  
   - Uses the camera to record video for 45 seconds  
   - Processes video frames to extract heart rate  
   - Stores result for database logging  

2. **Respiratory Rate Calculation:**  
   - Uses accelerometer to capture movement in x, y, z directions  
   - Calculates respiratory rate from movement patterns  
   - Stores result for database logging  

3. **Symptom Logging:**  
   - Users select symptoms from a dropdown (Spinner)  
   - Rate intensity using RatingBar  
   - Upload all values (heart rate, respiratory rate, symptoms) to RoomDB  

---

## Screenshots

**Home Screen:**  
<img width="236" height="465" alt="image" src="https://github.com/user-attachments/assets/1d0b9d67-f0cd-433e-9798-33cb58135b9f" />

**Symptom Logging:**  
<img width="236" height="464" alt="image" src="https://github.com/user-attachments/assets/94667190-03dd-4f2e-b6e2-7492fea81688" />
  
---

## Future Improvements

- Integrate external sensors for more accurate measurements  
- Add cloud syncing for backup and remote access  
- Visualize trends using charts and graphs  

---

## Run the App

1. Clone the repo:  
   ```bash
   git clone https://github.com/Gauri-Milind-Kadbane/Context-Monitoring-App.git
