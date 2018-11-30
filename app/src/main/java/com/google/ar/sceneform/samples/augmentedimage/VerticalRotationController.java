package com.google.ar.sceneform.samples.augmentedimage;

import android.support.v4.math.MathUtils;
import android.util.Log;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.MathHelper;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.BaseTransformationController;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.schemas.lull.Quat;

public class VerticalRotationController extends BaseTransformationController<TwoFingerDragGesture> {
    private final TransformableNode node;
    private static final float DELTA_MULTIPLIER = 1f;
    public VerticalRotationController(
            TransformableNode transformableNode, TwoFingerDragGestureRecognizer gestureRecognizer) {
        super(transformableNode, gestureRecognizer);
        this.node = transformableNode;
    }
    @Override
    public boolean canStartTransformation(TwoFingerDragGesture gesture) {
        return getTransformableNode().isSelected();
    }
    @Override
    public void onContinueTransformation(TwoFingerDragGesture gesture) {
        float deltaY = gesture.getAverageDeltaPosition().y * DELTA_MULTIPLIER;
        Log.d("VerticalRotationController", String.valueOf(deltaY));
        MathUtils.clamp(deltaY, -360, 360);
        Quaternion localRotation = new Quaternion(new Vector3(1f, 0f, 0f), deltaY);
        Log.d("VerticalRotationController", localRotation.toString());
        node.setLocalRotation(Quaternion.multiply(node.getLocalRotation(), localRotation));
    }
    @Override
    public void onEndTransformation(TwoFingerDragGesture gesture) {
    }
}
