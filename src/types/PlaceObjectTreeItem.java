package types;

import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;

public class PlaceObjectTreeItem {
    private PlaceObject2Tag placeObject;
    private PlaceObject2Tag parent;

    public PlaceObjectTreeItem(PlaceObject2Tag placeObject) {
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

    public String getName() {
        String uniqueName = placeObject.getName();
        if (parent != null) uniqueName += " - " + parent.getName();
        return uniqueName;
    }
}
