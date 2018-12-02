package me.mischka.augmentedlearning;

import android.util.Log;

import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.BaseTransformationController;
import com.google.ar.sceneform.ux.TransformableNode;

public class VerticalRotationController extends BaseTransformationController<TwoFingerDragGesture> {
    private static final String TAG = "VerticalRotationController";

    private static final float DELTA_MULTIPLIER = 1f;
    public VerticalRotationController(
            TransformableNode transformableNode, TwoFingerDragGestureRecognizer gestureRecognizer) {
        super(transformableNode, gestureRecognizer);
    }
    @Override
    public boolean canStartTransformation(TwoFingerDragGesture gesture) {
        return getTransformableNode().isSelected();
    }
    @Override
    public void onContinueTransformation(TwoFingerDragGesture gesture) {
        float deltaY = gesture.getAverageDeltaPosition().y * DELTA_MULTIPLIER;
        Log.d(TAG, String.valueOf(deltaY));
        Quaternion rotationDelta = new Quaternion(getTransformableNode().worldToLocalDirection(Vector3.right()), deltaY);
        getTransformableNode().setLocalRotation(Quaternion.multiply(getTransformableNode().getLocalRotation(), rotationDelta));
    }
    @Override
    public void onEndTransformation(TwoFingerDragGesture gesture) {
    }
}
