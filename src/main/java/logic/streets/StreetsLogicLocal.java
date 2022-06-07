package logic.streets;

import data.model.Intersection;
import data.model.Street;

import javax.ejb.Local;
import java.util.ArrayList;
import java.util.Set;

@Local
public interface StreetsLogicLocal {
    public ArrayList<Street> getStreetsFromArea(String areaname, int zoom, int decimateSkip, String epochTimestamp);


    public ArrayList<Street> getStreetsFromLinkIds(Set<Long> streetIdsSet);

    public void decimateStreets(ArrayList<Street> streets, int decimateSkip);

}
