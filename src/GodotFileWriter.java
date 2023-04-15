import godot.GodotNode;
import godot.GodotResource;
import godot.GodotSubResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

public class GodotFileWriter {
    private ArrayList<String> lines;

    public GodotFileWriter() {
        this.lines = new ArrayList<>();
    }

    private void writeLine(String line) {
        lines.add(line);
        writeNewLine();
    }

    private void writeNewLine() {
        lines.add("\n");
    }

    public void writeScene(String filePath, ArrayList<GodotSprite> sprites) {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator();

        var resources = new ArrayList<GodotResource>();
        var subResources = new ArrayList<GodotSubResource>();
        var nodes = new ArrayList<GodotNode>();
        nodes.add(new GodotNode("Node2D", "Node2D", null, new ArrayList<>()));

        for (int i = 0; i < sprites.size(); i++) {
            var sprite = sprites.get(i);
            var nodeProperties = new ArrayList<AbstractMap.SimpleEntry<String, String>>();

            var resourceId = String.format("%s_%s", i + 1, randomStringGenerator.randomString(5));
            var spriteUid = randomStringGenerator.randomString(13);
            resources.add(new GodotResource(resourceId, "Texture2D", sprite.getResourcePath(), spriteUid));

            var shaderOption = sprite.getShaderOption();
            if (shaderOption != null) {
                var type = "ShaderMaterial";
                var shaderResourceId = String.format("%s_%s", i + 1, randomStringGenerator.randomString(5));
                var shaderMaterialId = String.format("%s_%s", type, randomStringGenerator.randomString(5));

                resources.add(new GodotResource(shaderResourceId, "Shader", shaderOption.getResourcePath(), randomStringGenerator.randomString(13)));

                var shaderProperties = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                shaderProperties.add(new AbstractMap.SimpleEntry<>("shader", String.format("ExtResource(\"%s\")", shaderResourceId)));
                shaderProperties.add(shaderOption.getParameter());

                subResources.add(new GodotSubResource(shaderMaterialId, type, shaderProperties));
                nodeProperties.add(new AbstractMap.SimpleEntry<>("material", String.format("SubResource(\"%s\")", shaderMaterialId)));
            }

            nodeProperties.add(new AbstractMap.SimpleEntry<>("position", String.format("Vector2(%s, %s)", sprite.getPosition().x, sprite.getPosition().y)));
            nodeProperties.add(new AbstractMap.SimpleEntry<>("texture", String.format("ExtResource(\"%s\")", resourceId)));
            nodes.add(new GodotNode(sprite.getName(), "Sprite2D", ".", nodeProperties));
        }

        var sceneUid = randomStringGenerator.randomString(13);
        writeLine(String.format("[gd_scene load_steps=%s format=3 uid=\"uid://%s\"]", nodes.size() + subResources.size() + 1, sceneUid));
        writeNewLine();

        for (GodotResource resource : resources) {
            writeLine(String.format("[ext_resource type=\"%s\" uid=\"uid://%s\" path=\"%s\" id=\"%s\"]", resource.getType(), resource.getUid(), resource.getPath(), resource.getId()));
        }
        writeNewLine();

        for (GodotSubResource subResource : subResources) {
            writeLine(String.format("[sub_resource type=\"%s\" id=\"%s\"]", subResource.getType(), subResource.getId()));
            for (var property : subResource.getProperties()) {
                writeLine(String.format("%s = %s", property.getKey(), property.getValue()));
            }
            writeNewLine();
        }

        for (GodotNode node : nodes) {
            var parent = node.getParent() != null ? String.format("parent=\"%s\"", node.getParent()) : "";
            writeLine(String.format("[node name=\"%s\" type=\"%s\" %s]", node.getName(), node.getType(), parent));
            for (var property : node.getProperties()) {
                writeLine(String.format("%s = %s", property.getKey(), property.getValue()));
            }
            writeNewLine();
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
