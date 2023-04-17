package godot.parameters;

import java.util.ArrayList;

public class GodotWriterAnimation {
    private Double length;
    private Double step;
    private ArrayList<GodotWriterAnimationTrack> tracks;

    public GodotWriterAnimation(Double length, Double step, ArrayList<GodotWriterAnimationTrack> tracks) {
        this.length = length;
        this.step = step;
        this.tracks = tracks;
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
