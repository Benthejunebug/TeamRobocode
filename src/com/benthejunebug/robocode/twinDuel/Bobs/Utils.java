package com.benthejunebug.robocode.twinDuel.Bobs;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class Utils
{
  public static int sign(double value)
  {
    if (value >= 0.0D) {
      return 1;
    }
    return -1;
  }
  

  public static double wallSmoothing(Rectangle2D.Double battleField, Point2D.Double botLocation, double angle, int orientation, int WALL_STICK)
  {
    while (!battleField.contains(projectPosition(botLocation, angle, WALL_STICK))) {
      angle += orientation * 0.05D;
    }
    return angle;
  }
  
  public static double limit(double min, double value, double max) {
    return Math.max(min, Math.min(value, max));
  }
  
  public static double getResultant(double x, double x2, double y, double y2) {
    return getResultant(x - x2, y - y2);
  }
  
  public static Point2D.Double projectPosition(Point2D.Double location, double distance, double angleInRadians)
  {
    return new Point2D.Double(location.x + distance * 
      Math.sin(angleInRadians), location.y + distance * 
      Math.cos(angleInRadians));
  }
  
  public static Point2D.Double projectPosition(double x, double y, double distance, double angleInRadians) {
    return projectPosition(new Point2D.Double(x, y), distance, angleInRadians);
  }
  
  public static double getResultant(double deltaX, double deltaY) {
    return Math.sqrt(Math.pow(deltaX, 2.0D) + Math.pow(deltaY, 2.0D));
  }
  
  public static boolean fallsOnLine(double x, double y, double hitRadius, double sourceX, double sourceY, double angle)
  {
    Ellipse2D.Double candidate = new Ellipse2D.Double(x, y, hitRadius, hitRadius);
    return candidate.contains(projectPosition(sourceX, sourceY, getResultant(x, sourceX, y, sourceY), angle));
  }
  


  
  //unused code
  public static <T> T[] insertToArray(T value, T[] array, int index)
  {
    ArrayList<T> newArray = new ArrayList();
    
    for (int i = 0; i < index; i++) {
      newArray.add(array[i]);
    }
    
    newArray.add(value);
    
    for (int i = index + 1; i < array.length; i++) {
      newArray.add(array[(i - 1)]);
    }
    
    return (T[]) newArray.toArray();
  }
  
  public static double[] insertToArray(double value, double[] array, int index) {
    double[] newArray = new double[array.length];
    
    for (int i = 0; i < index; i++) {
      newArray[i] = array[i];
    }
    
    newArray[index] = value;
    
    for (int i = index + 1; i < array.length; i++) {
      newArray[i] = array[(i - 1)];
    }
    
    return newArray;
  }
  
  public static long[] insertToArray(long value, long[] array, int index) {
    long[] newArray = new long[array.length];
    
    for (int i = 0; i < index; i++) {
      newArray[i] = array[i];
    }
    
    newArray[index] = value;
    
    for (int i = index + 1; i < array.length; i++) {
      newArray[i] = array[(i - 1)];
    }
    
    return newArray;
  }
}
