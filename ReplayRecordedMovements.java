package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import java.util.ArrayList;

@Autonomous(name="ReplayRecordedMovements", group="Autonomous")
public class ReplayRecordedMovements extends LinearOpMode {
    SleekClippaHardware robot = new SleekClippaHardware();
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        robot.init_robot(this);

        telemetry.addLine("Ready to replay recorded movements");
        telemetry.update();

        waitForStart();

        // Get the recorded states from the teleop class
        ArrayList<SleekClippaDrive.RobotStateRecorder.CompleteRobotState> recordedStates = SleekClippaDrive.getRecordedStates();

        // Replay the recorded movements
        if (recordedStates != null && !recordedStates.isEmpty()) {
            telemetry.addLine("Replaying " + recordedStates.size() + " recorded states");
            telemetry.update();

            for (SleekClippaDrive.RobotStateRecorder.CompleteRobotState state : recordedStates) {
                // Set drive motors
                robot.TopLeft.setPower(state.leftFront);
                robot.TopRight.setPower(state.rightFront);
                robot.BottomLeft.setPower(state.leftRear);
                robot.BottomRight.setPower(state.rightRear);

                // Set arm positions
                robot.ExtendLeft.setTargetPosition(state.extendLeftPos);
                robot.ExtendRight.setTargetPosition(state.extendRightPos);
                robot.ClipArm.setTargetPosition(state.clipArmPos);

                // Set servo positions
                robot.GameClaw.setPosition(state.gameClaw);
                robot.GameWrist.setPosition(state.gameWrist);
                robot.GameTwist.setPosition(state.gameTwist);
                robot.ClipWrist.setPosition(state.clipWrist);
                robot.ClipHold.setPosition(state.clipHold);
                robot.ClipCamLeft.setPosition(state.clipCamLeft);
                robot.ClipCamRight.setPosition(state.clipCamRight);

                sleep(20); // 20ms delay between states
            }

            // Stop motors at end
            robot.TopLeft.setPower(0);
            robot.TopRight.setPower(0);
            robot.BottomLeft.setPower(0);
            robot.BottomRight.setPower(0);
        } else {
            telemetry.addLine("No recorded states found to replay");
            telemetry.update();
        }
    }
}