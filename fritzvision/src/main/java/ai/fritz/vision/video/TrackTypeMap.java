package ai.fritz.vision.video;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An utility class for storing data relative to a single {@link TrackType} in a map.
 * Adapted from https://github.com/natario1/Transcoder
 *
 * @param <T> The map type.
 */
class TrackTypeMap<T> {

    public TrackTypeMap() {

    }

    public TrackTypeMap(T videoValue, T audioValue) {
        set(TrackType.AUDIO, audioValue);
        set(TrackType.VIDEO, videoValue);
    }

    private Map<TrackType, T> map = new HashMap<>();

    public void set(TrackType type, T value) {
        map.put(type, value);
    }

    public void setAudio(T value) {
        set(TrackType.AUDIO, value);
    }

    public void setVideo(T value) {
        set(TrackType.VIDEO, value);
    }

    public T get(TrackType type) {
        return map.get(type);
    }

    public T getAudio() {
        return get(TrackType.AUDIO);
    }

    public T getVideo() {
        return get(TrackType.VIDEO);
    }

    public Collection<T> values() {
        return map.values();
    }

    public boolean has(TrackType type) {
        return map.containsKey(type);
    }

    public boolean hasAudio() {
        return has(TrackType.AUDIO);
    }

    public boolean hasVideo() {
        return has(TrackType.VIDEO);
    }

    public int size() {
        return map.size();
    }
}