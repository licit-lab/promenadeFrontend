package data;

import data.model.Area;
import data.model.Intersection;

import java.util.ArrayList;
import java.util.HashMap;

public interface BetweennessDAO {

    ArrayList<ArrayList<Intersection>> updateBetweennessValue(ArrayList<ArrayList<Intersection>> intersectionsList, Long endTimestamp, String normStrategy, HashMap<String, Area> allAreas);
}
