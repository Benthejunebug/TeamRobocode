package com.benthejunebug.robocode.twinDuel.Bobs;

import java.awt.geom.Point2D;
import java.io.Serializable;





public class Bullet
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private String firedFrom;
  private String target;
  private double absBearingOfTarget;
  private boolean virtual;
  private double sourceX;
  private double sourceY;
  private long timeOfFire;
  private double power;
  private double velocity;
  private int lateralDirectionOfTarget;
  private double angle;
  
  public Bullet(Bullet bullet)
  {
    this.firedFrom = bullet.getFiredFrom();
    this.target = bullet.getTarget();
    this.absBearingOfTarget = bullet.getAbsBearingOfTarget();
    this.virtual = bullet.isVirtual();
    this.sourceX = bullet.getSourceX();
    this.sourceY = bullet.getSourceY();
    this.timeOfFire = bullet.getTimeOfFire();
    this.power = bullet.getPower();
    this.velocity = bullet.getVelocity();
    this.lateralDirectionOfTarget = bullet.getLateralDirectionOfTarget();
    this.angle = bullet.getAngle();
  }
  
  public Bullet(String firedFrom, double sourceX, double sourceY, int lateralDirectionOfTarget, double power, long timeOfFire)
  {
    this.virtual = false;
    this.lateralDirectionOfTarget = lateralDirectionOfTarget;
    init(firedFrom, sourceX, sourceY, power, timeOfFire);
  }
  


  public Bullet(String firedFrom, String target, double absBearingOfTarget, int lateralDirectionOfTarget, boolean virtual, double sourceX, double sourceY, double power, long timeOfFire, double angle)
  {
    this.virtual = virtual;
    this.target = target;
    this.absBearingOfTarget = absBearingOfTarget;
    this.lateralDirectionOfTarget = lateralDirectionOfTarget;
    init(firedFrom, sourceX, sourceY, power, timeOfFire, angle);
  }
  
  private void init(String firedFrom, double sourceX, double sourceY, double power, long timeOfFire)
  {
    this.sourceX = sourceX;
    this.sourceY = sourceY;
    this.power = power;
    this.velocity = getBulletVelocity(power);
    this.timeOfFire = timeOfFire;
  }
  
  private void init(String firedFrom, double sourceX, double sourceY, double power, long timeOfFire, double angle)
  {
    init(firedFrom, sourceX, sourceY, power, timeOfFire);
    this.angle = angle;
  }
  
  public static double getBulletVelocity(double power)
  {
    return 20.0D - 3.0D * power;
  }
  
  public double getDistanceTraveled(long currentTime) {
    return (currentTime - getTimeOfFire()) * getVelocity();
  }
  
  public Point2D.Double getLocation(long currentTime) {
    double distance = getDistanceTraveled(currentTime);
    return new Point2D.Double(getSourceX() + distance * Math.sin(this.angle), getSourceY() + distance * Math.cos(this.angle));
  }
  
  public double getDistance(double observingX, double observingY, long currentTime) {
    return getLocation(currentTime).distance(observingX, observingY);
  }
  
  public String getFiredFrom()
  {
    return this.firedFrom;
  }
  
  public String getTarget() {
    return this.target;
  }
  
  public double getAbsBearingOfTarget() {
    return this.absBearingOfTarget;
  }
  
  public int getLateralDirectionOfTarget() {
    return this.lateralDirectionOfTarget;
  }
  
  public boolean isVirtual() {
    return this.virtual;
  }
  
  public double getSourceX() {
    return this.sourceX;
  }
  
  public double getSourceY() {
    return this.sourceY;
  }
  
  public long getTimeOfFire() {
    return this.timeOfFire;
  }
  
  public double getPower() {
    return this.power;
  }
  
  public double getVelocity() {
    return this.velocity;
  }
  
  public double getMaxEscapeAngle() {
    return Math.asin(8.0D / getVelocity());
  }
  
  public static double getMaxEscapeAngle(double power)
  {
    return Math.asin(8.0D / getBulletVelocity(power));
  }
  
  public double getAngle() {
    return this.angle;
  }
}
