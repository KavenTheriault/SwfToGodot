import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GodotFileWriter {
    public void writeScene(String filePath, ArrayList<GodotSprite> sprites) {
        ArrayList<String> lines = new ArrayList<>();
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator();

        var sceneUid = randomStringGenerator.randomString(13);
        lines.add(String.format("[gd_scene load_steps=4 format=3 uid=\"uid://%s\"]", sceneUid));

        lines.add("\n");
        lines.add("\n");

        var resourceIds = new ArrayList<String>();

        for (int i = 0; i < sprites.size(); i++) {
            var resourceId = String.format("%s_%s", i + 1, randomStringGenerator.randomString(5));
            resourceIds.add(resourceId);
        }

        for (int i = 0; i < sprites.size(); i++) {
            var sprite = sprites.get(i);
            var resourceId = resourceIds.get(i);
            var spriteUid = randomStringGenerator.randomString(13);

            lines.add(String.format("[ext_resource type=\"Texture2D\" uid=\"uid://%s\" path=\"%s\" id=\"%s\"]", spriteUid, sprite.getResourcePath(), resourceId));
            lines.add("\n");
        }

        lines.add("\n");
        lines.add("[node name=\"Node2D\" type=\"Node2D\"]");
        lines.add("\n");
        lines.add("\n");

        for (int i = 0; i < sprites.size(); i++) {
            var sprite = sprites.get(i);
            var resourceId = resourceIds.get(i);

            lines.add(String.format("[node name=\"%s\" type=\"Sprite2D\" parent=\".\"]", sprite.getName()));
            lines.add("\n");
            lines.add(String.format("position = Vector2(%s, %s)", sprite.getPosition().x, sprite.getPosition().y));
            lines.add("\n");
            lines.add(String.format("texture = ExtResource(\"%s\")", resourceId));
            lines.add("\n");
            lines.add("\n");
        }

        writeFile(filePath, lines);
    }

    public void writeFile(String filePath, ArrayList<String> lines) {
        try {
            File file = new File(filePath);
            if (file.exists()) file.delete();

            FileWriter writer = new FileWriter(file);
            for (String line : lines) {
                writer.write(line);
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filePath + " " + e.getMessage());
        }
    }
}
