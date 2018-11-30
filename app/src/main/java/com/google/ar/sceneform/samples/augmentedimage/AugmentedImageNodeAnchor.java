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

package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNodeAnchor extends AnchorNode {

  private static final String TAG = "AugmentedImageNodeAnchor";

  // The augmented image represented by this node.
  private AugmentedImage image;
  private TransformableNode node;

  private CompletableFuture<ModelRenderable> model;

  public AugmentedImageNodeAnchor(Context context, TransformationSystem transformationSystem, String assetPath, TwoFingerDragGestureRecognizer twoFingerDragGestureRecognizer) {
    this.model = ModelRenderable.builder()
            .setSource(context, Uri.parse(assetPath))
            .build();

    node = new AugmentedImageTransformableNode(transformationSystem, twoFingerDragGestureRecognizer);
  }

  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  public void setImage(AugmentedImage image) {
    this.image = image;

    // If any of the models are not loaded, then recurse when all are loaded.
    if (!model.isDone()) {
      CompletableFuture.allOf(model)
          .thenAccept((Void aVoid) -> setImage(image))
          .exceptionally(
              throwable -> {
                Log.e(TAG, "Exception loading", throwable);
                return null;
              });
    }

    // Set the anchor based on the center of the image.
    setAnchor(image.createAnchor(image.getCenterPose()));
    Quaternion localRotation = new Quaternion();
    localRotation.set(new Vector3(-1f, 0f, 0f), 90);
    // setLocalRotation(localRotation);

    Renderable renderable = model.getNow(null);
    node.setParent(this);
    node.setRenderable(renderable);
    node.setLocalRotation(localRotation);
    // node.setLocalPosition(new Vector3(0f, 0.5f, 0f));
    if (renderable != null) {
      node.setCollisionShape(renderable.getCollisionShape());
    }
    node.select();
  }

  public AugmentedImage getImage() {
    return image;
  }

}