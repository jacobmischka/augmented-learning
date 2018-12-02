/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.mischka.augmentedlearning;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 */
public class AugmentedImageActivity extends AppCompatActivity {

    private static final String TAG = "AugmentedImageActivity";

    private ArFragment arFragment;
    private ImageView fitToScanView;
    private FloatingActionButton fab;
    private TransformationSystem transformationSystem;

    private TwoFingerDragGestureRecognizer twoFingerDragGestureRecognizer;

    private Scene.OnPeekTouchListener peekTouchListener = new Scene.OnPeekTouchListener() {
        @Override
        public void onPeekTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
            arFragment.onPeekTouch(hitTestResult, motionEvent);
            twoFingerDragGestureRecognizer.onTouch(hitTestResult, motionEvent);
        }
    };

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, AugmentedImageNodeAnchor> augmentedImageMap = new HashMap<>();

    private final String[] assetPathMap = {
            "Orange.sfb",
            "green-apples",
            "banana.sfb",
            "pineapple.sfb",
            "Lemons.sfb",
            "pear_export.sfb",

            "TropicalFish01.sfb",
            "TropicalFish02.sfb",
            "TropicalFish06.sfb",
            "TropicalFish12.sfb",
            "TropicalFish11.sfb",

            "10067_Eiffel_Tower_v1_max2010_it1.sfb",
            "GOLDGATE.sfb",
            "pisa.sfb",
            "LibertStatue.sfb",
            "tajmahal.sfb",

            "dock(formats).sfb",
            "rpbridge(formats).sfb",
            "bridge-a.sfb",
            "bridge.sfb"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        fab = findViewById(R.id.clear_button);
        fab.setOnClickListener(view -> {
            Log.d(TAG, "fab tapped");
            resetView();
        });


        transformationSystem = arFragment.getTransformationSystem();
        twoFingerDragGestureRecognizer = new TwoFingerDragGestureRecognizer(transformationSystem.getGesturePointersUtility());

        Scene scene = arFragment.getArSceneView().getScene();
        scene.addOnUpdateListener(this::onUpdateFrame);
        scene.addOnPeekTouchListener(peekTouchListener);
        Log.d(TAG, "onCreate");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    // String text = "Detected Image " + augmentedImage.getIndex();
                    // SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);
                    fab.setVisibility(View.VISIBLE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        AugmentedImageNodeAnchor anchorNode = new AugmentedImageNodeAnchor(this, transformationSystem, assetPathMap[augmentedImage.getIndex()], twoFingerDragGestureRecognizer);
                        anchorNode.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, anchorNode);
                        arFragment.getArSceneView().getScene().addChild(anchorNode);
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    private void resetView() {
        Intent i = new Intent(AugmentedImageActivity.this, AugmentedImageActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

}
