package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import java.util.ArrayList;

@TeleOp(name="SleekClippaDrive", group = "TeleOp")
public class SleekClippaDrive extends OpMode {
    SleekClippaHardware r = new SleekClippaHardware();
    private ElapsedTime runtime = new ElapsedTime();
    private RobotStateRecorder recorder = new RobotStateRecorder();
    private boolean hasDisplayedStartRecording = false;
    private static ArrayList<RobotStateRecorder.CompleteRobotState> savedStates;

    //Clip Vars
    double holdOpenMax = .475;//.45
    double holdClose = .35;//.3
    double holdCloseTight = .3;//.2

    // Robot state recorder class
    public class RobotStateRecorder {
        private ArrayList<CompleteRobotState> recordedStates = new ArrayList<>();
        private boolean isRecording = true;
        private CompleteRobotState lastState = null;

        public class CompleteRobotState {
            // Drive states
            public double leftFront;
            public double rightFront;
            public double leftRear;
            public double rightRear;

            // Arm states
            public int extendLeftPos;
            public int extendRightPos;
            public int clipArmPos;

            // Servo positions
            public double gameClaw;
            public double gameWrist;
            public double gameTwist;
            public double clipWrist;
            public double clipHold;
            public double clipCamLeft;
            public double clipCamRight;

            public long timestamp;

            public CompleteRobotState(SleekClippaHardware robot, double lf, double rf, double lr, double rr) {
                // Drive
                this.leftFront = lf;
                this.rightFront = rf;
                this.leftRear = lr;
                this.rightRear = rr;

                // Motors
                this.extendLeftPos = robot.ExtendLeft.getCurrentPosition();
                this.extendRightPos = robot.ExtendRight.getCurrentPosition();
                this.clipArmPos = robot.ClipArm.getCurrentPosition();

                // Servos
                this.gameClaw = robot.GameClaw.getPosition();
                this.gameWrist = robot.GameWrist.getPosition();
                this.gameTwist = robot.GameTwist.getPosition();
                this.clipWrist = robot.ClipWrist.getPosition();
                this.clipHold = robot.ClipHold.getPosition();
                this.clipCamLeft = robot.ClipCamLeft.getPosition();
                this.clipCamRight = robot.ClipCamRight.getPosition();

                this.timestamp = System.currentTimeMillis();
            }
        }

        public void recordCurrentState(SleekClippaHardware robot, double leftFront, double rightFront, double leftRear, double rightRear) {
            if (isRecording) {
                CompleteRobotState currentState = new CompleteRobotState(robot, leftFront, rightFront, leftRear, rightRear);

                // Only record if this is the first state or if something has changed
                if (lastState == null || hasSignificantChange(lastState, currentState)) {
                    recordedStates.add(currentState);
                    lastState = currentState;
                }
            }
        }

        // Check if there's meaningful change between states
        private boolean hasSignificantChange(CompleteRobotState last, CompleteRobotState current) {
            // Check for movement (motors or driving)
            if (Math.abs(current.leftFront) > 0.05 || Math.abs(current.rightFront) > 0.05 ||
                    Math.abs(current.leftRear) > 0.05 || Math.abs(current.rightRear) > 0.05) {
                return true;
            }

            // Check for significant position changes in servos
            if (Math.abs(current.gameClaw - last.gameClaw) > 0.01 ||
                    Math.abs(current.gameWrist - last.gameWrist) > 0.01 ||
                    Math.abs(current.gameTwist - last.gameTwist) > 0.01 ||
                    Math.abs(current.clipWrist - last.clipWrist) > 0.01 ||
                    Math.abs(current.clipHold - last.clipHold) > 0.01 ||
                    Math.abs(current.clipCamLeft - last.clipCamLeft) > 0.01 ||
                    Math.abs(current.clipCamRight - last.clipCamRight) > 0.01) {
                return true;
            }

            // Check for arm movement
            if (Math.abs(current.clipArmPos - last.clipArmPos) > 5 ||
                    Math.abs(current.extendLeftPos - last.extendLeftPos) > 5 ||
                    Math.abs(current.extendRightPos - last.extendRightPos) > 5) {
                return true;
            }

            return false;
        }

        public void stopRecording() {
            isRecording = false;
            savedStates = recordedStates;
        }

        public int getRecordedStatesCount() {
            return recordedStates.size();
        }

        public boolean isCurrentlyRecording() {
            return isRecording;
        }

        public ArrayList<CompleteRobotState> getRecordedStates() {
            return recordedStates;
        }
    }

    public static ArrayList<RobotStateRecorder.CompleteRobotState> getRecordedStates() {
        return savedStates;
    }

    @Override
    public void init() {
        r.init_robot(this);
    }

    @Override
    public void loop() {

        /*
         *
         *Gamepad 1
         *
         */

        // Keep existing drive code unchanged
        double deflator = 0.7;
        double y = -gamepad1.left_stick_y;
        double x = gamepad1.left_stick_x;
        double rotation = gamepad1.right_stick_x;
        double angle = Math.atan2(y, x);
        double power = Math.hypot(x, y);
        double sin = Math.sin(angle - Math.PI / 4);
        double cos = Math.cos(angle - Math.PI / 4);
        double max = Math.max(Math.abs(sin), Math.abs(cos));
        double leftFront = power * cos / max + rotation;
        double rightFront = power * sin / max - rotation;
        double leftRear = power * sin / max + rotation;
        double rightRear = power * cos / max - rotation;

        if (power + Math.abs(rotation) > 1) {
            double scale = power + Math.abs(rotation);
            leftFront /= scale;
            rightFront /= scale;
            leftRear /= scale;
            rightRear /= scale;
        }

        r.TopLeft.setPower(leftFront * deflator);
        r.TopRight.setPower(rightFront * deflator);
        r.BottomLeft.setPower(leftRear * deflator);
        r.BottomRight.setPower(rightRear * deflator);

        //Extend or retract arm
        if (gamepad1.right_trigger > 0 && r.ExtendRight.getCurrentPosition() < 2525) {
            r.ExtendLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            r.ExtendLeft.setPower(-1 * gamepad1.right_trigger);
            r.ExtendRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            r.ExtendRight.setPower(gamepad1.right_trigger);
        }
        else if (gamepad1.left_trigger > 0 && r.ExtendRight.getCurrentPosition() > 0) {
            r.ExtendLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            r.ExtendLeft.setPower(gamepad1.left_trigger);
            r.ExtendRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            r.ExtendRight.setPower(-1 * gamepad1.left_trigger);
        }
        else {
            //r.ExtendRight.setTargetPosition(r.ExtendRight.getCurrentPosition());
            //r.ExtendLeft.setTargetPosition(r.ExtendLeft.getCurrentPosition());
            r.ExtendRight.setPower(0);
            r.ExtendLeft.setPower(0);

        }

        //Close or Open Claw
        if (gamepad1.right_bumper) {
            //Close
            r.GameClaw.setPosition(.45);//.46
        }
        else if (gamepad1.left_bumper) {
            r.GameClaw.setPosition(.7);
        }

        //Wrist motions
        if (gamepad1.dpad_up) {
            r.GameWrist.setPosition(.45 + .25);//.4before
        }
        else if (gamepad1.dpad_down) {
            r.GameWrist.setPosition(.75 + .25);//.8
        }
        if(gamepad1.a){
            r.ClipWrist.setPosition(0.15);
            r.ClipArm.setTargetPosition(-895 + 1493);
        }

        //Twist motions
        if (gamepad1.dpad_left && r.GameTwist.getPosition() <= .99) {
            r.GameTwist.setPosition(r.GameTwist.getPosition() + .01);
        }
        else if (gamepad1.dpad_right && r.GameTwist.getPosition() >= 0.01) {
            r.GameTwist.setPosition(r.GameTwist.getPosition() - .01);
        }
        else if (gamepad1.b) {
            //Reset to straight
            r.GameTwist.setPosition(.625);
        }


        /*
         *
         *Gamepad2 Stuff
         *
         */


        //Move Arm to Clip Rack
        if (gamepad2.dpad_right){
            r.ClipWrist.setPosition(.65);
            r.ClipHold.setPosition(holdOpenMax);
            r.ClipArm.setTargetPosition(0 + 1493);
            r.ClipArm.setPower(.3);
            r.ClipArm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }

        //Take Clip Claw off rack
        if (gamepad2.dpad_left) {
            r.ClipWrist.setPosition(.6);
            r.ClipHold.setPosition(holdCloseTight);
            double time = runtime.time();
            while (runtime.time() - time <= .25) {
                continue;
            } if (runtime.time() - time > .25) {
                r.ClipArm.setTargetPosition(-300 + 1493);
                r.ClipArm.setPower(.4);
            }
        }

        //Score Position
        if (gamepad2.dpad_up) {
            r.ClipArm.setTargetPosition(-670 + 1493);
            r.ClipArm.setPower(.7);
            r.ClipHold.setPosition(0);
            r.ClipArm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }

        //Turn Wrist to pass rack position
        if (gamepad2.x) {
            r.ClipWrist.setPosition(.825);//.8
            // ADDED: Stop recording when X is pressed
            recorder.stopRecording();
        }

        //CLip Arm to Clip Position
        if (gamepad2.dpad_down) {
            r.ClipArm.setTargetPosition(55 + 1493);
        }

        //Grab or release clip
        if (gamepad2.right_bumper) {
            //Grab
            r.ClipWrist.setPosition(.5);
            double time = runtime.time();
            while (runtime.time() - time <= .25){
                continue;
            } if (runtime.time() - time > .25) {
                r.ClipHold.setPosition(holdClose);
            }
        }
        else if (gamepad2.left_bumper) {
            //Release
            r.ClipHold.setPosition(holdOpenMax);
            r.ClipWrist.setPosition(.65);
        }

        //Collect Clips
        if (gamepad2.right_trigger > .5){
            r.ClipCamLeft.setPosition(0);
            r.ClipCamRight.setPosition(1);
        }
        else if (gamepad2.left_trigger > .5){
            r.ClipCamLeft.setPosition(0.7);
            r.ClipCamRight.setPosition(0.4);
        }

        if (gamepad2.right_stick_button){
            r.ClipWrist.setPosition(0.1);
        }

        //CLIP FUNCTION
        if (gamepad2.y) {
            r.GameClaw.setPosition(.39);//.4

            double time = runtime.time();
            while (runtime.time() - time <= .5) {
                continue;
            }
            if (runtime.time() - time > .5) {
                r.ClipWrist.setPosition(1);
            }
            while (runtime.time() - time <= .75){
                continue;
            }
            if (runtime.time() - time > .75) { //1
                r.ClipHold.setPosition(holdCloseTight);
                r.GameWrist.setPosition(.3);//.4?

                r.ExtendLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                r.ExtendLeft.setPower(-.4);
                r.ExtendRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                r.ExtendRight.setPower(.4);
            }
            while (runtime.time() - time <= 1.1){
                continue;
            }
            if (runtime.time() - time > 1.1) { //1
                r.ExtendLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                r.ExtendLeft.setPower(0);
                r.ExtendRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                r.ExtendRight.setPower(0);
            }
        }

        //RELEASE FUNCTION
        if (gamepad2.a) {
            r.GameClaw.setPosition(.6);
            r.GameWrist.setPosition(.7);
            r.ClipArm.setTargetPosition(100 + 1493);
            r.ClipArm.setPower(.6);
            r.ClipArm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }

        // Record current state after all servo and motor operations
        recorder.recordCurrentState(r, leftFront, rightFront, leftRear, rightRear);

        // Add telemetry for recording status
        telemetry.addLine("Arm Position: " + String.valueOf(r.ClipArm.getCurrentPosition()) );
        telemetry.addLine("Extendo Position: " + String.valueOf(r.ExtendRight.getCurrentPosition()) );
        telemetry.addLine("Twist Position: " + String.valueOf(r.GameTwist.getPosition()) );

        // Recording status telemetry
        if (recorder.isCurrentlyRecording()) {
            telemetry.addLine("RECORDING IN PROGRESS");
        } else {
            telemetry.addLine("RECORDING STOPPED");
        }
        telemetry.addLine("States Recorded: " + recorder.getRecordedStatesCount());
        telemetry.addLine("Press X to stop recording");
    }
}