package ai.fritz.vision.poseestimation;

import android.graphics.PointF;
import android.util.Size;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Keypoint indicating detected part on Pose.
 */
public class Keypoint {
    String name;
    private int id;
    private PointF position;
    private float score;
    private Size bounds;

    public Keypoint(int id, String name, PointF position, float score, Size bounds) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.score = score;
        this.bounds = bounds;
    }

    public PointF getPosition() {
        return position;
    }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public float getScore() {
        return score;
    }

    /**
     * The name of the keypoint.
     *
     * @return the name from the skeleton.
     */
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    /**
     * Scale the keypoint for a given size.
     *
     * @param newBoundsSize - the bounds to scale the keypoint position for.
     * @return a new, scaled keypoint
     */
    public Keypoint scaled(Size newBoundsSize) {
        float scaleX = ((float) newBoundsSize.getWidth()) / bounds.getWidth();
        float scaleY = ((float) newBoundsSize.getHeight()) / bounds.getHeight();
        return new Keypoint(id, name, new PointF(position.x * scaleX, position.y * scaleY), score, newBoundsSize);
    }

    public float calculateSquaredDistanceFromCoordinates(PointF coordinates) {
        float dx = position.x - coordinates.x;
        float dy = position.y - coordinates.y;
        return dx * dx + dy * dy;
    }

    public JSONArray getPointsAsJsonArray() {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(id);
            jsonArray.put(position.x);
            jsonArray.put(position.y);
            jsonArray.put(score);
            return jsonArray;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
