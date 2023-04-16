package types;

import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;

import java.util.ArrayList;

public class TagTreeItem {
    private PlaceObject2Tag tag;
    private PlaceObject2Tag parent;
    private ArrayList<TagTreeItem> children;

    public TagTreeItem(PlaceObject2Tag tag) {
        this.tag = tag;
    }

    public PlaceObject2Tag getTag() {
        return tag;
    }

    public void setTag(PlaceObject2Tag tag) {
        this.tag = tag;
    }

    public PlaceObject2Tag getParent() {
        return parent;
    }

    public void setParent(PlaceObject2Tag parent) {
        this.parent = parent;
    }

    public ArrayList<TagTreeItem> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<TagTreeItem> children) {
        this.children = children;
    }

    public String getName() {
        String uniqueName = tag.getName();
        if (parent != null) uniqueName += " - " + parent.getName();
        return uniqueName;
    }
}
