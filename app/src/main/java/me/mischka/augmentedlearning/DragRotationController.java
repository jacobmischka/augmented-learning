package me.mischka.augmentedlearning;

import android.util.Log;
import com.google.ar.sceneform.ux.*;

public class DragRotationController extends TranslationController {
    DragRotationController(BaseTransformableNode transformableNode, DragGestureRecognizer gestureRecognizer) {
        super(transformableNode, gestureRecognizer);
    }

    @Override
    public void onGestureStarted(DragGesture gesture) {
        Log.d("getGestureStarted", gesture.getDelta().toString());
    }
}
