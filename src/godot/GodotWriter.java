package godot;

import utils.RandomStringGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

public class GodotWriter {
    private ArrayList<String> lines;
    private ArrayList<GodotResource> resources;
    private ArrayList<GodotSubResource> subResources;
    private ArrayList<GodotNode> nodes;
    private RandomStringGenerator randomStringGenerator;

    public GodotWriter() {
        this.lines = new ArrayList<>();
        this.resources = new ArrayList<>();
        this.subResources = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.randomStringGenerator = new RandomStringGenerator();
    }

    private void writeLine(String line) {
        lines.add(line);
        writeNewLine();
    }

    private void writeNewLine() {
        lines.add("\n");
    }

    public void writeScene(String filePath, ArrayList<GodotWriterItem> writerItems) {
        nodes.add(new GodotNode("Node2D", "Node2D", null, new ArrayList<>()));

        writeItems(writerItems, ".");
        generateLines();
        writeFile(filePath);
    }

    private void writeItems(ArrayList<GodotWriterItem> writerItems, String parent) {
        for (var writerItem : writerItems) {
            if (writerItem instanceof GodotWriterSprite writerSprite) writeSprite(writerSprite, parent);
            if (writerItem instanceof GodotWriterGroup writerGroup) {
                nodes.add(new GodotNode(writerGroup.getName(), "Node2D", parent, new ArrayList<>()));
                writeItems(writerGroup.getItems(), writerGroup.getName());
            }
        }
    }

    private void writeSprite(GodotWriterSprite godotWriterSprite, String parent) {
        var nodeProperties = new ArrayList<AbstractMap.SimpleEntry<String, String>>();

        var resourceId = String.format("%s_%s", resources.size() + 1, randomStringGenerator.randomString(5));
        var spriteUid = randomStringGenerator.randomString(13);
        resources.add(new GodotResource(resourceId, "Texture2D", godotWriterSprite.getResourcePath(), spriteUid));

        var shaderOption = godotWriterSprite.getShaderOption();
        if (shaderOption != null) {
            var type = "ShaderMaterial";
            var shaderResourceId = String.format("%s_%s", resources.size() + 1, randomStringGenerator.randomString(5));
            var shaderMaterialId = String.format("%s_%s", type, randomStringGenerator.randomString(5));

            resources.add(new GodotResource(shaderResourceId, "Shader", shaderOption.getResourcePath(), randomStringGenerator.randomString(13)));

            var shaderProperties = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
            shaderProperties.add(new AbstractMap.SimpleEntry<>("shader", String.format("ExtResource(\"%s\")", shaderResourceId)));
            shaderProperties.add(shaderOption.getParameter());

            subResources.add(new GodotSubResource(shaderMaterialId, type, shaderProperties));
            nodeProperties.add(new AbstractMap.SimpleEntry<>("material", String.format("SubResource(\"%s\")", shaderMaterialId)));
        }

        nodeProperties.add(new AbstractMap.SimpleEntry<>("position", String.format("Vector2(%s, %s)", godotWriterSprite.getPosition().x, godotWriterSprite.getPosition().y)));
        nodeProperties.add(new AbstractMap.SimpleEntry<>("texture", String.format("ExtResource(\"%s\")", resourceId)));
        nodes.add(new GodotNode(godotWriterSprite.getName(), "Sprite2D", parent, nodeProperties));
    }

    private void generateLines() {
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
