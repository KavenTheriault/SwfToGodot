package types;

import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;

public class FindPlaceObjectResult {
    private PlaceObject2Tag placeObject;
    private PlaceObject2Tag parent;

    public FindPlaceObjectResult(PlaceObject2Tag placeObject) {
        this.placeObject = placeObject;
    }

    public PlaceObject2Tag getPlaceObject() {
        return placeObject;
    }

    public void setPlaceObject(PlaceObject2Tag placeObject) {
        this.placeObject = placeObject;
    }

    public PlaceObject2Tag getParent() {
        return parent;
    }

    public void setParent(PlaceObject2Tag parent) {
        this.parent = parent;
    }
}
