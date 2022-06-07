package logic.areas;

import data.model.Area;

import javax.ejb.Local;
import java.util.ArrayList;

@Local
public interface AreaLogicLocal {

    public ArrayList<Area> getAreaFromCorners(float upperLeftLon, float upperLeftLat, float lowerRightLon, float lowerRightLat);

}
