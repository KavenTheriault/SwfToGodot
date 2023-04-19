package godot;

import godot.parameters.GodotWriterAnimation;
import godot.parameters.GodotWriterGroup;
import godot.parameters.GodotWriterNode;
import godot.parameters.GodotWriterSprite;
import godot.schema.*;
import utils.RandomStringGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;

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

    public void writeScene(String folderPath, String name, Collection<GodotWriterNode> writerItems, GodotWriterAnimation animation) {
        nodes.add(new GodotNode(name, "Node2D", null, new ArrayList<>()));

        writeItems(writerItems, ".");
        if (animation != null) writeAnimation(animation);
        generateLines();

        var sceneFilePath = String.format("%s\\%s.tscn", folderPath, name);
        writeFile(sceneFilePath);
    }

    private void writeItems(Collection<GodotWriterNode> writerNodes, String parent) {
        for (var writerNode : writerNodes) {
            if (writerNode instanceof GodotWriterSprite writerSprite) writeSprite(writerSprite, parent);
            if (writerNode instanceof GodotWriterGroup writerGroup) {
                var nodeProperties = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                nodeProperties.add(new AbstractMap.SimpleEntry<>("position", String.format("Vector2(%s, %s)", writerGroup.getPosition().x, writerGroup.getPosition().y)));

                nodes.add(new GodotNode(writerGroup.getName(), "Node2D", parent, nodeProperties));
                writeItems(writerGroup.getNodes(), writerGroup.getName());
            }
        }
    }

    private void writeAnimation(GodotWriterAnimation godotWriterAnimation) {
        var animationName = "generated";
        var animationProperties = new ArrayList<AbstractMap.SimpleEntry<String, String>>();

        animationProperties.add(new AbstractMap.SimpleEntry<>("resource_name", String.format("\"%s\"", animationName)));
        animationProperties.add(new AbstractMap.SimpleEntry<>("length", godotWriterAnimation.getLength().toString()));
        animationProperties.add(new AbstractMap.SimpleEntry<>("loop_mode", "1"));
        animationProperties.add(new AbstractMap.SimpleEntry<>("step", godotWriterAnimation.getStep().toString()));

        for (int i = 0; i < godotWriterAnimation.getTracks().size(); i++) {
            var track = godotWriterAnimation.getTracks().get(i);
            var prefix = String.format("tracks/%s", i);

            animationProperties.add(new AbstractMap.SimpleEntry<>(String.format("%s/type", prefix), "\"value\""));
            animationProperties.add(new AbstractMap.SimpleEntry<>(String.format("%s/imported", prefix), "false"));
            animationProperties.add(new AbstractMap.SimpleEntry<>(String.format("%s/enabled", prefix), "true"));
            animationProperties.add(new AbstractMap.SimpleEntry<>(String.format("%s/path", prefix), String.format("NodePath(\"%s:%s\")", track.getTargetName(), track.getTargetProperty())));
            animationProperties.add(new AbstractMap.SimpleEntry<>(String.format("%s/interp", prefix), "1"));
            animationProperties.add(new AbstractMap.SimpleEntry<>(String.format("%s/loop_wrap", prefix), "true"));

            var times = new ArrayList<String>();
            var transitions = new ArrayList<String>();
            var values = new ArrayList<String>();

            if (track.getVectorValues() != null) {
                for (int j = 0; j < track.getVectorValues().size(); j++) {
                    var value = track.getVectorValues().get(j);
                    values.add(String.format("Vector2(%.4f, %.4f)", value.x, value.y));
                }
            }

            if (track.getDoubleValues() != null) {
                for (int j = 0; j < track.getDoubleValues().size(); j++) {
                    var value = track.getDoubleValues().get(j);
                    values.add(String.format("%.4f", value));
                }
            }

            for (int j = 0; j < values.size(); j++) {
                var time = godotWriterAnimation.getStep() * j;
                times.add(Double.toString(time));
                transitions.add("1");
            }

            var timesStr = String.join(", ", times);
            var transitionsStr = String.join(", ", transitions);
            var valuesStr = String.join(", ", values);

            var keys = String.format("""
{
    "times": PackedFloat32Array(%s),
    "transitions": PackedFloat32Array(%s),
    "update": 0,
    "values": [%s]
}""", timesStr, transitionsStr, valuesStr);

            animationProperties.add(new AbstractMap.SimpleEntry<>(String.format("%s/keys", prefix), keys));
        }

        var type = "Animation";
        var animationId = String.format("%s_%s", type, randomStringGenerator.randomString(5));
        GodotSubResource animationSubResource = new GodotSubResource(animationId, type, animationProperties);
        subResources.add(animationSubResource);

        var typeLibrary = "AnimationLibrary";
        var animationLibraryId = String.format("%s_%s", typeLibrary, randomStringGenerator.randomString(5));
        var animationLibraryProperties = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
        animationLibraryProperties.add(new AbstractMap.SimpleEntry<>("_data", String.format("{ \"%s\": SubResource(\"%s\") }", animationName, animationId)));

        GodotSubResource animationLibrarySubResource = new GodotSubResource(animationLibraryId, typeLibrary, animationLibraryProperties);
        subResources.add(animationLibrarySubResource);

        var animationPlayerProperties = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
        animationPlayerProperties.add(new AbstractMap.SimpleEntry<>("libraries", String.format("{ \"\": SubResource(\"%s\") }", animationLibraryId)));
        nodes.add(new GodotNode("AnimationPlayer", "AnimationPlayer", ".", animationPlayerProperties));
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
