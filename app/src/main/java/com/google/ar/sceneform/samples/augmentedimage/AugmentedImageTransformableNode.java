package com.google.ar.sceneform.samples.augmentedimage;

import android.util.Log;
import android.view.MotionEvent;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

public class AugmentedImageTransformableNode extends TransformableNode {
    private final VerticalRotationController verticalRotationController;

    public AugmentedImageTransformableNode(TransformationSystem transformationSystem, TwoFingerDragGestureRecognizer twoFingerDragGestureRecognizer) {
        super(transformationSystem);
        // removeTransformationController(getTranslationController());
        verticalRotationController = new VerticalRotationController(this, twoFingerDragGestureRecognizer);
    }

    public boolean isTransforming() {
        return super.isTransforming() || verticalRotationController.isTransforming();
    }

    @Override
    public void onActivate() {
        super.onActivate();
        verticalRotationController.onActivated(this);
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        verticalRotationController.onDeactivated(this);
    }
}
