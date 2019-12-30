/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.disnodeteam.dogecv.detectors.skystone.SkystoneDetector;
import com.disnodeteam.dogecv.detectors.skystone.StoneDetector;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;

import java.util.Locale;

/**
 * This file illustrates the concept of driving a path based on encoder counts.
 * It uses the common Pushbot hardware class to define the drive on the robot.
 * The code is structured as a LinearOpMode
 *
 * The code REQUIRES that you DO have encoders on the wheels,
 *   otherwise you would use: PushbotAutoDriveByTime;
 *
 *  This code ALSO requires that the drive Motors have been configured such that a positive
 *  power command moves them forwards, and causes the encoders to count UP.
 *
 *   The desired path in this example is:
 *   - Drive forward for 48 inches
 *   - Spin right for 12 Inches
 *   - Drive Backwards for 24 inches
 *   - Stop and close the claw.
 *
 *  The code is written using a method called: encoderDrive(speed, leftInches, rightInches, timeoutS)
 *  that performs the actual movement.
 *  This methods assumes that each movement is relative to the last stopping place.
 *  There are other ways to perform encoder based moves, but this method is probably the simplest.
 *  This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@Autonomous(name="FoundationAutonomousBlueTesting", group="Pushbot")
//@Disabled
public class FoundationAutonomousBlueTesting extends LinearOpMode {
    private OpenCvCamera phoneCam;
    private SkystoneDetector skyStoneDetector;
    private StoneDetector stoneDetector;
    /* Declare OpMode members. */
    HardwareTest         robot   = new HardwareTest();   // Use a Pushbot's hardware
    private ElapsedTime     runtime = new ElapsedTime();

    static final double     COUNTS_PER_MOTOR_REV    = 480 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 0.585 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 3.5 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                                                      (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double     DRIVE_SPEED             = 0.5;
    static final double     DRIVE_SPEED4            = 0.75;
    static final double     DRIVE_SPEED2            = 0.45;
    static final double     DRIVE_SPEED3            = 0.2;

    @Override
    public void runOpMode() {

        /*
         * Initialize the drive system variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Resetting Encoders");    //
        telemetry.update();

        robot.frontleft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.frontright.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.downleft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.downright.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.frontleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.frontright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.downleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.downright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        // Send telemetry message to indicate successful Encoder reset
        telemetry.addData("Path0",  "Starting at %7d :%7d",
                          robot.frontleft.getCurrentPosition(),
                          robot.frontright.getCurrentPosition(),
                          robot.downleft.getCurrentPosition(),
                          robot.downright.getCurrentPosition());
        telemetry.update();
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        phoneCam = new OpenCvInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);

        // OR...  Do Not Activate the Camera Monitor View
        //phoneCam = new OpenCvInternalCamera(OpenCvInternalCamera.CameraDirection.BACK);

        /*
         * Open the connection to the camera device
         */
        phoneCam.openCameraDevice();

        /*
         * Specify the image processing pipeline we wish to invoke upon receipt
         * of a frame from the camera. Note that switching pipelines on-the-fly
         * (while a streaming session is in flight) *IS* supported.
         */
        skyStoneDetector = new SkystoneDetector();
        phoneCam.setPipeline(skyStoneDetector);
        stoneDetector = new StoneDetector();
        phoneCam.setPipeline(stoneDetector);

        /*
         * Tell the camera to start streaming images to us! Note that you must make sure
         * the resolution you specify is supported by the camera. If it is not, an exception
         * will be thrown.
         *
         * Also, we specify the rotation that the camera is used in. This is so that the image
         * from the camera sensor can be rotated such that it is always displayed with the image upright.
         * For a front facing camera, rotation is defined assuming the user is looking at the screen.
         * For a rear facing camera or a webcam, rotation is defined assuming the camera is facing
         * away from the user.
         */
        phoneCam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);

        /*
         * Wait for the user to press start on the Driver Station
         */

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // Step through each leg of the path,
        // Note: Reverse movement is obtained by setting a negative distance (not speed)
       /*encoderDrive(DRIVE_SPEED,30,-30,-30,30,5.0);
        encoderDrive(DRIVE_SPEED3,5,5,5,5,5.0);
        encoderDrive(DRIVE_SPEED,-45,-45,-45,-45,5.0);
        robot.FoundationGrabber1.setPosition(0.0);
        robot.FoundationGrabber2.setPosition(1.0);
        sleep(1000);
        encoderDrive(DRIVE_SPEED2,52,52,52,52,10.0);
        robot.FoundationGrabber1.setPosition(1.0);
        robot.FoundationGrabber2.setPosition(0.0);
        sleep(1000);
        encoderDrive(DRIVE_SPEED4,-98,98,98,-98,10.0);
        encoderDrive(DRIVE_SPEED3,5,5,5,5,5.0);
        encoderDrive(DRIVE_SPEED,-27,-27,-27,-27,5.0);*/


        if (skyStoneDetector.getScreenPosition().x <= 100){
            robot.FrontCollector.setPosition(0.0);
            sleep(100000);
        }else{
            if (skyStoneDetector.getScreenPosition().x<100){

            }else
                if (skyStoneDetector.getScreenPosition().x>150){

                }else{

                }

        }










        telemetry.addData("Path", "Complete");
        telemetry.update();
    }

    /*
     *  Method to perfmorm a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the opmode running.
     */
    public void encoderDrive(double speed,
                             double frontleftInches, double frontrightInches,double downleftInches, double downrightInches,
                             double timeoutS) {
        int newfrontleftTarget;
        int newfrontrightTarget;
        int newdownleftTarget;
        int newdownrightTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {


            // Determine new target position, and pass to motor controller
            newfrontleftTarget = robot.frontleft.getCurrentPosition() + (int)(frontleftInches * COUNTS_PER_INCH);
            newfrontrightTarget = robot.frontright.getCurrentPosition() + (int)(frontrightInches * COUNTS_PER_INCH);
            newdownleftTarget = robot.downleft.getCurrentPosition() + (int)(downleftInches * COUNTS_PER_INCH);
            newdownrightTarget = robot.downright.getCurrentPosition() + (int)(downrightInches * COUNTS_PER_INCH);
            robot.frontleft.setTargetPosition(newfrontleftTarget);
            robot.frontright.setTargetPosition(newfrontrightTarget);
            robot.downleft.setTargetPosition(newdownleftTarget);
            robot.downright.setTargetPosition(newdownrightTarget);

            // Turn On RUN_TO_POSITION
            robot.frontleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.frontright.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.downleft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.downright.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            // reset the timeout time and start motion.
            runtime.reset();
            robot.frontleft.setPower(Math.abs(speed));
            robot.frontright.setPower(Math.abs(speed));
            robot.downleft.setPower(Math.abs(speed));
            robot.downright.setPower(Math.abs(speed));


            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() &&
                   (runtime.seconds() < timeoutS) &&
                   (robot.frontleft.isBusy() && robot.frontright.isBusy()
                           && robot.downleft.isBusy() && robot.downright.isBusy()))

            {

                // Display it for the driver.
                telemetry.addData("Path1",  "Running to %7d :%7d", newfrontleftTarget,
                        newfrontrightTarget,newdownleftTarget,newdownrightTarget);
                telemetry.addData("Path2",  "Running at %7d :%7d",
                                            robot.frontleft.getCurrentPosition(),
                                            robot.frontright.getCurrentPosition(),
                                            robot.downleft.getCurrentPosition(),
                                            robot.downright.getCurrentPosition());
                telemetry.addData("Stone Position X", skyStoneDetector.getScreenPosition().x);
                telemetry.addData("Stone Position Y", skyStoneDetector.getScreenPosition().y);
                telemetry.addData("Frame Count", phoneCam.getFrameCount());
                telemetry.addData("FPS", String.format(Locale.US, "%.2f", phoneCam.getFps()));
                telemetry.addData("Total frame time ms", phoneCam.getTotalFrameTimeMs());
                telemetry.addData("Pipeline time ms", phoneCam.getPipelineTimeMs());
                telemetry.addData("Overhead time ms", phoneCam.getOverheadTimeMs());
                telemetry.addData("Theoretical max FPS", phoneCam.getCurrentPipelineMaxFps());
                telemetry.update();

                telemetry.update();
            }



            // Stop all motion;
            robot.frontleft.setPower(0);
            robot.frontright.setPower(0);
            robot.downleft.setPower(0);
            robot.downright.setPower(0);


            // Turn off RUN_TO_POSITION
            robot.frontleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.frontright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.downleft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.downright.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


            //  sleep(250);   // optional pause after each move
        }

    }

}