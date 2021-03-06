package frc.robot.lib.controllers;

import frc.robot.RobotMap;
import frc.robot.lib.drivers.Limelight;
import edu.wpi.first.wpilibj.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/******************************************************************************************************************************** 
** VISION CONTROLLER CLASS
********************************************************************************************************************************/
public class Vision {

  public static enum State {
    kTurn,
    kPnP
  }

  public static enum Status {
    kReachedTarget,
    kLostTarget,
    kTargeting
  }

  Runnable mVisionProcessor = new Runnable() {
    @Override
    public void run() {
      synchronized(this) {
        // Process state changes
        if (mState != mDesiredState) {
          mLogger.info("Vision processing state change request: [{}]", mDesiredState);
          mState = mDesiredState;
          mStatus = Status.kTargeting;
        }

        // Process the current states
        if (mState == State.kTurn) {
          mTurningErrorDeg = mLimelight.horizontalToTargetDeg();
          mTurn = mTurningErrorDeg * RobotMap.kScaleHorizontalToTarget * RobotMap.kTurningGain;
          if (!mLimelight.foundTarget()) {
            mStatus = Status.kLostTarget;
          } else if (Math.abs(mTurningErrorDeg) < RobotMap.kStopTurningDeg) {
            mStatus = Status.kReachedTarget;
          } else {
            mStatus = Status.kTargeting;
          }
        }
      }
    }
  };

  private double mTurningErrorDeg = 0.0;
  private double mTurn = 0.0;
  private State mState = State.kTurn;
  private State mDesiredState = State.kTurn;
  private Status mStatus = Status.kLostTarget;

  private Limelight mLimelight;

  public Notifier mVisionThread = new Notifier(mVisionProcessor);

  private final Logger mLogger = LoggerFactory.getLogger(Vision.class);

  /****************************************************************************************************************************** 
  ** SETTERS AND GETTERS
  ******************************************************************************************************************************/
  public synchronized Status getStatus() {
    return mStatus;
  }

  public synchronized double getOutput() {
    return mTurn;
  }

  public synchronized State getState() {
    return mState;
  }

  public synchronized void setState(State state) {
    mDesiredState = state;
  }

  /****************************************************************************************************************************** 
  ** CONSTRUCTOR
  ******************************************************************************************************************************/
  public Vision(Limelight limelight) {
    mLimelight = limelight;
  }

  public static Vision create() {
    Limelight limelight = new Limelight();
    return new Vision(limelight);
  }

}
