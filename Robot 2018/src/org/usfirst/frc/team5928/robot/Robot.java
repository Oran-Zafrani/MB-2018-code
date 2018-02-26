/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5928.robot;

import edu.wpi.first.wpilibj.SampleRobot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the RobotDrive class. The
 * SampleRobot class is the base of a robot application that will automatically
 * call your Autonomous and OperatorControl methods at the right time as
 * controlled by the switches on the driver station or the field controls.
 *
 * <p>The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 *
 * <p>WARNING: While it may look like a good choice to use for your code if
 * you're inexperienced, don't. Unless you know what you are doing, complex code
 * will be much more difficult under this system. Use IterativeRobot or
 * Command-Based instead if you're new.
 */

public class Robot extends SampleRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";

	
	private Joystick Rstick = new Joystick(0), Lstick = new Joystick(1), sysjoystic= new Joystick(2);
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	private String data = DriverStation.getInstance().getGameSpecificMessage();
	private Ultrasonic ultra = new Ultrasonic(0, 1);
	private Encoder lift = new Encoder(0, 1);
	PIDController pidsonic, pidgyro, pidlift;
	AnalogGyro gyro = new AnalogGyro(0);
	
	WPI_TalonSRX canLB = new WPI_TalonSRX(1), 
				canLF = new WPI_TalonSRX(2), 
				canRB = new WPI_TalonSRX(3), 
				canRF = new WPI_TalonSRX(4), 
				left_intake = new WPI_TalonSRX(5),
				right_intake = new WPI_TalonSRX(6);
	
	
	SpeedControllerGroup Lside = new SpeedControllerGroup(canLB, canLF);
	SpeedControllerGroup Rside = new SpeedControllerGroup(canRB, canRF);
	SpeedControllerGroup Lift_group = new SpeedControllerGroup(new WPI_TalonSRX(7), new WPI_TalonSRX(8));
	
	
	Boolean flag = true;


	DoubleSolenoid left_sol = new DoubleSolenoid(0, 1), right_sol = new DoubleSolenoid(2, 3);
	Compressor comp = new Compressor();

	DifferentialDrive m_robotDrive = new DifferentialDrive(Lside, Rside);
		/**
		 * This function is run when the robot is first started up and should be)
		 * used for any initialization code.
		 */

	
	
	
	public Robot() {
		m_robotDrive.setExpiration(0.1);
		pidsonic = new PIDController(0.1, 0.001, 0, ultra, new MyPidOutput());
		pidgyro = new PIDController(0.1, 0.001, 0, gyro, new MyPidOutput());
		pidlift = new PIDController(0.1, 0.001, 0, lift, Lift_group );
		pidlift.setInputRange(0, 999); // change before enabling pid
	}

	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		SmartDashboard.putData("Auto modes", m_chooser);
		gyro.reset();
		lift.reset();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the if-else structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 *
	 * <p>If you wanted to run a similar autonomous mode with an IterativeRobot
	 * you would write:
	 *
	 * <blockquote><pre>{@code
	 * Timer timer = new Timer();
	 *
	 * // This function is run once each time the robot enters autonomous mode
	 * public void autonomousInit() {
	 *     timer.reset();
	 *     timer.start();
	 * }
	 *
	 * // This function is called periodically during autonomous
	 * public void autonomousPeriodic() {
	 * // Drive for 2 seconds
	 *     if (timer.get() < 2.0) {
	 *         myRobot.drive(-0.5, 0.0); // drive forwards half speed
	 *     } else if (timer.get() < 5.0) {
	 *         myRobot.drive(-1.0, 0.0); // drive forwards full speed
	 *     } else {
	 *         myRobot.drive(0.0, 0.0); // stop robot
	 *     }
	 * }
	 * }</pre></blockquote>
	 */
	@Override
	public void autonomous() {
	
		pidsonic.setInputRange(0, 20*0.125);
		pidsonic.setSetpoint(000);
		
		String autoSelected = m_chooser.getSelected();
		// String autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + autoSelected);

		// MotorSafety improves safety when motors are updated in loops
		// but is disabled here because motor updates are not looped in
		// this autonomous mode.
		m_robotDrive.setSafetyEnabled(false);

	
				if (data.charAt(0)=='L' && Rstick.getRawAxis(3)>0.5){


				}
				else if (data.charAt(0)=='R' && Rstick.getRawAxis(3)>0.5){


				}
		
	}

	/**
	 * Runs the motors with arcade steering.
	 *
	 * <p>If you wanted to run a similar teleoperated mode with an IterativeRobot
	 * you would write:
	 *
	 * <blockquote><pre>{@code
	 * // This function is called periodically during operator control
	 * public void teleopPeriodic() {
	 *     myRobot.arcadeDrive(stick);
	 * }
	 * }</pre></blockquote>
	 */
	@Override
	public void operatorControl() {
		m_robotDrive.setSafetyEnabled(true);
		lift.reset();
		while (isOperatorControl() && isEnabled()) {
			// Drive arcade style
			
				if(Rstick.getRawButton(2))
					m_robotDrive.tankDrive(-Lstick.getY()*0.3, -Rstick.getY()*0.3);
				else if(Rstick.getRawButton(1))
					m_robotDrive.tankDrive(-Lstick.getY(), -Rstick.getY());
				else
					m_robotDrive.tankDrive(-Lstick.getY()*0.7, -Rstick.getY()*0.7);

				
				if (sysjoystic.getRawButton(3))
				{
					openIntake();
				}
				/* test the tuning of the PID before enabling the robot!! */
				
				if (sysjoystic.getRawButton(7)) // opening lift for scale
					pidlift.setSetpoint(999);
				else if (sysjoystic.getRawButton(10)) //opening lift for switch
					pidlift.setSetpoint(999);
				else if (sysjoystic.getRawButton(11)) // closing lift
					pidlift.setSetpoint(999);
				
				
			// The motors will be updated every 5ms
			Timer.delay(0.005);
		}
	}

	/**
	 * Runs during test mode.
	 */
	private class MyPidOutput implements PIDOutput {
		@Override
		public void pidWrite(double output) {
			m_robotDrive.arcadeDrive(output, 0);
		}
	}
public void openIntake(){
		
		if(flag){ // true if cube inside.
			left_intake.set(-0.5);
			right_intake.set(0.5);
			Timer.delay(0.5);
			left_intake.set(0);
			right_intake.set(0);
			
			left_sol.set(Value.kReverse);
			right_sol.set(Value.kReverse);
			flag = false;
		}
		else {
			
			left_intake.set(0.5);
			right_intake.set(-0.5);
			Timer.delay(0.5);
			left_sol.set(Value.kForward);
			right_sol.set(Value.kForward);
			Timer.delay(0.5);

			left_intake.set(0);
			right_intake.set(0);
			flag = true;
			
		}
	}

	@Override
	public void test() {
	}
}
