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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 */
public class AugmentedImageActivity extends AppCompatActivity {

    private static final String TAG = "AugmentedImageActivity";
    private static final Map<String, String> assetPathMap;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("apple", "green-apples.sfb");
        map.put("banana", "banana.sfb");
        map.put("orange", "Orange.sfb");
        map.put("pear", "pear_export.sfb");
        map.put("pineapple", "pineapple.sfb");

        map.put("blue-tang", "TropicalFish02.sfb");
        map.put("clarks-anemonefish", "TropicalFish11.sfb");
        map.put("clownfish", "TropicalFish12.sfb");
        map.put("convict-cichlid", "TropicalFish06.sfb");
        map.put("discus", "TropicalFish01.sfb");

        map.put("eiffel-tower", "10067_Eiffel_Tower_v1_max2010_it1.sfb");
        map.put("pisa", "pisa.sfb");
        map.put("statue-of-liberty", "LibertStatue.sfb");
        map.put("taj-mahal", "tajmahal.sfb");

        map.put("arch", "bridge-a.sfb");
        map.put("beam", "dock(formats).sfb");
        map.put("simple-suspension", "bridge.sfb");
        map.put("suspension", "GOLDGATE.sfb");

        assetPathMap = Collections.unmodifiableMap(map);
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        fab = findViewById(R.id.clear_button);
        fab.setOnClickListener(view -> {
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
            fab.setVisibility(View.GONE);
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
                        AugmentedImageNodeAnchor anchorNode = new AugmentedImageNodeAnchor(
                                this,
                                transformationSystem,
                                assetPathMap.get(augmentedImage.getName()),
                                twoFingerDragGestureRecognizer
                        );
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
