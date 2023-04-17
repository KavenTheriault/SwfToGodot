package godot.parameters;

import java.util.ArrayList;

public class GodotWriterAnimation {
    private Double length;
    private Double step;
    private ArrayList<GodotWriterAnimationTrack> tracks;

    public GodotWriterAnimation(Double length, Double step) {
        this.length = length;
        this.step = step;
        this.tracks = new ArrayList<>();
    }

    public Double getLength() {
        return length;
    }

    public Double getStep() {
        return step;
    }

    public ArrayList<GodotWriterAnimationTrack> getTracks() {
        return tracks;
    }
}
