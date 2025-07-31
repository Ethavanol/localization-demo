package MAP;

import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.List;

public class LocalizationMap {
    private Integer width;
    private Integer height;
    private Integer nbAgts;
    private List<Location> agentStart;
    private List<MapMarker> markers;

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getNbAgts() {
        return nbAgts;
    }

    public List<Location> getAgentStart() {return agentStart;}

    public List<MapMarker> getMarkers() {
        return markers;
    }
}
