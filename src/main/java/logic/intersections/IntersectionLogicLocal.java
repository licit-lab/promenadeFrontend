package logic.intersections;

import data.model.Intersection;

import javax.ejb.Local;
import java.util.ArrayList;

@Local
public interface IntersectionLogicLocal {
    public ArrayList<Intersection> getTopCritical(int topK);

    ArrayList<Intersection> getIntersectionsByArea(String areaName);

    ArrayList<ArrayList<Intersection>> updateBetweennessValue(ArrayList<ArrayList<Intersection>> intersections, Long endTimestamp, String normStrategy);
}
