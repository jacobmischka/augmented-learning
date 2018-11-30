package com.google.ar.sceneform.samples.augmentedimage;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.MathHelper;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.BaseTransformationController;
import com.google.ar.sceneform.ux.TransformableNode;

public class VerticalRotationController extends BaseTransformationController<TwoFingerDragGesture> {
    private final TransformableNode node;
    private static final float DELTA_MULTIPLIER = -0.0002f;
    private static final float MAX_HEIGHT = 0.25f;
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
        Vector3 localPos = node.getLocalPosition();
        float newY = localPos.y + (gesture.getAverageDeltaPosition().y * DELTA_MULTIPLIER);
        newY = MathHelper.clamp(newY, 0.0f, MAX_HEIGHT);
        localPos.y = newY;
        node.setLocalPosition(localPos);
    }
    @Override
    public void onEndTransformation(TwoFingerDragGesture gesture) {
    }
}
