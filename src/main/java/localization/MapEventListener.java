package localization;

import localization.models.MapEvent;

public interface MapEventListener {
    void agentMoved(MapEvent mapEvent);
}
