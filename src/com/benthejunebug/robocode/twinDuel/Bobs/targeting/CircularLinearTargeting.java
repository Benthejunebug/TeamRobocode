package com.benthejunebug.robocode.twinDuel.Bobs.targeting;

import java.awt.geom.Point2D;

import com.benthejunebug.robocode.twinDuel.Bobs.Bob;
import com.benthejunebug.robocode.twinDuel.Bobs.Bullet;
import com.benthejunebug.robocode.twinDuel.Bobs.OtherRobot;
import com.benthejunebug.robocode.twinDuel.Bobs.Utils;







public class CircularLinearTargeting extends TargetingMethod {
	
  private static final long serialVersionUID = 1L;
  
  private static final String METHOD_NAME = "Circular Linear Targeting";
  private static final int ADDITIONAL_TICKS = 0;
  
  public CircularLinearTargeting(String nameOfTarget) {
    super(nameOfTarget);
  }
  
  public CircularLinearTargeting(CircularLinearTargeting method) {
    super(method);
  }
  
  
  public Point2D.Double targetReturningLocation(OtherRobot observingRobot, OtherRobot targetedRobot, double power) {
    Point2D.Double predictedLocation = new Point2D.Double(targetedRobot.getX(), targetedRobot.getY());
    int elapsedTime = 0;
    
    double bulletVelocity = Bullet.getBulletVelocity(power);
    
    double robotHeading = targetedRobot.getHeading();
    
    while (elapsedTime * bulletVelocity < Utils.getResultant(observingRobot.getX(), predictedLocation.x, observingRobot.getX(), predictedLocation.y)) {
      robotHeading += targetedRobot.getAnglularVelociy();
      predictedLocation = Utils.projectPosition(predictedLocation, targetedRobot.getVelocity(), robotHeading);
      elapsedTime++;
      
      if ((predictedLocation.x < 18) || (predictedLocation.y < 18) || (predictedLocation.x > Bob.BATTLE_FIELD_WIDTH - 18) || (predictedLocation.y > Bob.BATTLE_FIELD_HEIGHT - 18)) {
        predictedLocation.x = Math.min(Math.max(18, predictedLocation.x), Bob.BATTLE_FIELD_WIDTH - 18);
        predictedLocation.y = Math.min(Math.max(18, predictedLocation.y), Bob.BATTLE_FIELD_HEIGHT - 18);
        break;
      }
    }
    
    return predictedLocation;
  }
  


  void additionalMerge(TargetingMethod method) {}
  


  public String getNameOfMethod() {
    return METHOD_NAME;
  }
  
  void additionalLogForVirtualCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {}
  
  void additionalLogForRealCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {}
  
  void additionalDeleteUnsharedData() {}
}
