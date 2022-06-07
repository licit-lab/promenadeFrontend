package data;

import data.model.Area;

import java.util.ArrayList;

public interface AreaDAO {
    public abstract ArrayList<Area> findAreasfromCorners(float upperLeftLon, float upperLeftLat, float lowerRightLon, float lowerRightLat);
}
