package utils;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;

public class TagUtils {
    public static Tag getTagById(SWF swf, int id) throws Exception {
        for (Tag tag : swf.getTags()) {
            if (tag instanceof CharacterIdTag characterIdTag && characterIdTag.getCharacterId() == id) {
                return tag;
            }
        }
        throw new Exception("Tag not found");
    }

    public static DefineSpriteTag getSprite(SWF swf, int id) throws Exception {
        for (Tag t : swf.getTags()) {
            if (t instanceof DefineSpriteTag sprite && sprite.getCharacterId() == id) {
                return sprite;
            }
        }
        throw new Exception("Sprite not found");
    }

    public static DefineShape2Tag getShape(SWF swf, int id) throws Exception {
        for (Tag t : swf.getTags()) {
            if (t instanceof DefineShape2Tag shape && shape.getCharacterId() == id) {
                return shape;
            }
        }
        throw new Exception("Shape not found");
    }

    public static void printTag(Tag t) {
        if (t instanceof CharacterIdTag) {
            System.out.println("Tag " + t.getTagName() + " (" + ((CharacterIdTag) t).getCharacterId() + ")");
        } else {
            System.out.println("Tag " + t.getTagName());
        }
    }
}
