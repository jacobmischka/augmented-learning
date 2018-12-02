package me.mischka.augmentedlearning;

import android.util.Log;

import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.BaseTransformableNode;
import com.google.ar.sceneform.ux.RotationController;
import com.google.ar.sceneform.ux.TwistGesture;
import com.google.ar.sceneform.ux.TwistGestureRecognizer;

public class AugmentedImageRotationController extends RotationController {
    private static String TAG = "AugmentedImageRotationController";

    public AugmentedImageRotationController(BaseTransformableNode transformableNode, TwistGestureRecognizer gestureRecognizer) {
        super(transformableNode, gestureRecognizer);
        Log.d(TAG, "constructor called");
    }

    @Override
    public void onContinueTransformation(TwistGesture gesture) {
        Log.d(TAG, "onContinueTransformation");
        float rotationAmount = gesture.getDeltaRotationDegrees() * getRotationRateDegrees();
        Quaternion rotationDelta = new Quaternion(getTransformableNode().worldToLocalDirection(Vector3.forward()), rotationAmount);
        Quaternion localrotation = getTransformableNode().getLocalRotation();
        localrotation = Quaternion.multiply(localrotation, rotationDelta);
        getTransformableNode().setLocalRotation(localrotation);
    }
}
