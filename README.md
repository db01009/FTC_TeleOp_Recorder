# 🤖 FTC TeleOp Recorder & Autonomous Replay System

This project enables **recording** of full robot states during TeleOp and **replaying** those actions in Autonomous mode for FIRST Tech Challenge (FTC) robots. Developed by a high school robotics team member to streamline autonomous development without manual path programming.

---

## 📌 Project Summary

This system consists of:

- **`SleekClippaDrive` (TeleOp)**  
  Records drive motor powers, encoder positions, and servo positions in real-time while manually controlling the robot.

- **`ReplayRecordedMovements` (Autonomous)**  
  Replays the recorded sequence with high fidelity by setting motor/servo values step-by-step using stored data.

---

## 🔍 Key Features

- **⚙️ Complete Robot State Capture**  
  Records all drive powers, arm positions, and servo settings (wrist, claw, twist, cams).

- **📏 Smart Change Detection**  
  Uses a custom method to log a new state *only* when a significant change is detected in motors or servos—minimizing unnecessary data.

- **⏱ Timestamped Playback**  
  Maintains execution timing during autonomous replay for realistic motion reproduction.

- **🎮 Gamepad-Controlled Trigger**  
  Start/stop recording from the gamepad (e.g., `X` button to stop recording).

---

## 💡 Highlighted Logic

The following logic ensures only meaningful state changes are recorded—highlighting this project’s efficiency:

```java
if (isRecording) {
    CompleteRobotState currentState = new CompleteRobotState(robot, leftFront, rightFront, leftRear, rightRear);
    if (lastState == null || hasSignificantChange(lastState, currentState)) {
        recordedStates.add(currentState);
        lastState = currentState;
    }
}

🧪 How It Works
1. TeleOp Phase (SleekClippaDrive)
Drive the robot as normal.


Robot state is automatically logged every loop.


Press X to stop recording.


2. Autonomous Phase (ReplayRecordedMovements)
Uses the saved list of robot states.


Replays all motor/servo positions in sequence with ~20ms intervals.



🛠 Technologies
Language: Java


Platform: Android Studio + FTC SDK


Hardware: REV Expansion Hub, Servo Motors, DcMotors


Control System: Gamepad + Encoders



🚀 Use Cases
Quick generation of autonomous routines by simply “driving a route”


Teaching tool for learning autonomous sequencing


Reducing overhead of script-based autonomous programming



📈 Planned Improvements
Persistent recording using file I/O for use across sessions


Visual timeline editor for trimming or modifying replay data


Real-time telemetry export to external analysis tools



👤 Author
Donovan Bruton
FTC Robotics 
Head Programmer | 2023–Present
UIL State Finalist & FIRST Premier International Invitee

📄 License
MIT License – Use freely and modify for your FTC team!

🌟 Star this repo if you find it helpful!

