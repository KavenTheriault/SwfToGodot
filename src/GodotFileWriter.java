import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class GodotFileWriter {
    private ArrayList<String> lines;

    public GodotFileWriter() {
        this.lines = new ArrayList<>();
    }

    private void writeLine(String line) {
        lines.add(line);
        writeLine();
    }

    private void writeLine() {
        lines.add("\n");
    }

    public void writeScene(String filePath, ArrayList<GodotSprite> sprites) {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator();

        var sceneUid = randomStringGenerator.randomString(13);
        writeLine(String.format("[gd_scene load_steps=%s format=3 uid=\"uid://%s\"]", sprites.size() + 1, sceneUid));

        writeLine();

        var resourceIds = new ArrayList<String>();
        var shaderMaterialIds = new LinkedList<String>();

        for (int i = 0; i < sprites.size(); i++) {
            var resourceId = String.format("%s_%s", i + 1, randomStringGenerator.randomString(5));
            resourceIds.add(resourceId);
        }

        for (int i = 0; i < sprites.size(); i++) {
            var sprite = sprites.get(i);
            var resourceId = resourceIds.get(i);
            var spriteUid = randomStringGenerator.randomString(13);

            writeLine(String.format("[ext_resource type=\"Texture2D\" uid=\"uid://%s\" path=\"%s\" id=\"%s\"]", spriteUid, sprite.getResourcePath(), resourceId));

            var shaderOption = sprite.getShaderOption();
            if (shaderOption != null) {
                var shaderId = String.format("%s_%s", i + 1, randomStringGenerator.randomString(5));
                var shaderMaterialId = String.format("ShaderMaterial_%s", randomStringGenerator.randomString(5));
                shaderMaterialIds.add(shaderMaterialId);

                writeLine(String.format("[ext_resource type=\"Shader\" path=\"%s\" id=\"%s\"]", shaderOption.getResourcePath(), shaderId));

                writeLine();

                writeLine(String.format("[sub_resource type=\"ShaderMaterial\" id=\"%s\"]", shaderMaterialId));
                writeLine(String.format("shader = ExtResource(\"%s\")", shaderId));
                writeLine(shaderOption.getParameter());
            }
        }

        writeLine();
        writeLine("[node name=\"Node2D\" type=\"Node2D\"]");
        writeLine();

        for (int i = 0; i < sprites.size(); i++) {
            var sprite = sprites.get(i);
            var resourceId = resourceIds.get(i);

            writeLine(String.format("[node name=\"%s\" type=\"Sprite2D\" parent=\".\"]", sprite.getName()));
            var shaderOption = sprite.getShaderOption();
            if (shaderOption != null) {
                var shaderMaterialId = shaderMaterialIds.getFirst();
                writeLine(String.format("material = SubResource(\"%s\")", shaderMaterialId));
            }
            writeLine(String.format("position = Vector2(%s, %s)", sprite.getPosition().x, sprite.getPosition().y));
            writeLine(String.format("texture = ExtResource(\"%s\")", resourceId));
            writeLine();
        }

        writeFile(filePath);
    }

    public void writeFile(String filePath) {
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
