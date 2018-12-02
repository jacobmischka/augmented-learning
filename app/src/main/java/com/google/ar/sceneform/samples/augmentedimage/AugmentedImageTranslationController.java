package com.google.ar.sceneform.samples.augmentedimage;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.MathHelper;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.utilities.Preconditions;
import com.google.ar.sceneform.ux.BaseTransformableNode;
import com.google.ar.sceneform.ux.DragGesture;
import com.google.ar.sceneform.ux.DragGestureRecognizer;
import com.google.ar.sceneform.ux.TranslationController;

import java.util.EnumSet;
import java.util.List;

public class AugmentedImageTranslationController extends TranslationController {

    private static final String TAG = "AugmentedImageTranslationController";

    public AugmentedImageTranslationController(
        BaseTransformableNode transformableNode,
        DragGestureRecognizer gestureRecognizer
    ) {
        super(transformableNode, gestureRecognizer);
    }

    @Nullable private HitResult lastArHitResult;
    @Nullable private Vector3 desiredWorldPosition;

    private EnumSet<Plane.Type> allowedPlaneTypes = EnumSet.allOf(Plane.Type.class);

    private static final float LERP_SPEED = 12.0f;
    private static final float POSITION_LENGTH_THRESHOLD = 0.01f;

    /** Sets which types of ArCore Planes this TranslationController is allowed to translate on. */
    public void setAllowedPlaneTypes(EnumSet<Plane.Type> allowedPlaneTypes) {
        this.allowedPlaneTypes = allowedPlaneTypes;
    }

    /**
     * Gets a reference to the EnumSet that determines which types of ArCore Planes this
     * TranslationController is allowed to translate on.
     */
    public EnumSet<Plane.Type> getAllowedPlaneTypes() {
        return allowedPlaneTypes;
    }

    @Override
    public void onUpdated(Node node, FrameTime frameTime) {
        updatePosition(frameTime);
    }

    @Override
    public boolean isTransforming() {
        // As long as the transformable node is still interpolating towards the final pose, this
        // controller is still transforming.
        return super.isTransforming() || desiredWorldPosition != null;
    }

    @Override
    public boolean canStartTransformation(DragGesture gesture) {
        Node targetNode = gesture.getTargetNode();
        if (targetNode == null) {
            return false;
        }

        BaseTransformableNode transformableNode = getTransformableNode();
        if (targetNode != transformableNode && !targetNode.isDescendantOf(transformableNode)) {
            return false;
        }

        if (!transformableNode.isSelected() && !transformableNode.select()) {
            return false;
        }

        return true;
    }

    @Override
    public void onContinueTransformation(DragGesture gesture) {
        Log.d(TAG, "onContinueTransformation");
        Scene scene = getTransformableNode().getScene();
        if (scene == null) {
            return;
        }

        Frame frame = ((ArSceneView) scene.getView()).getArFrame();
        if (frame == null) {
            return;
        }

        Camera arCamera = frame.getCamera();
        if (arCamera.getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        Vector3 position = gesture.getPosition();
        List<HitResult> hitResultList = frame.hitTest(position.x, position.y);
        for (int i = 0; i < hitResultList.size(); i++) {
            HitResult hit = hitResultList.get(i);
            Trackable trackable = hit.getTrackable();
            Pose pose = hit.getHitPose();
            if (trackable instanceof Plane) {
                Plane plane = (Plane) trackable;
                if (allowedPlaneTypes.contains(plane.getType())) {
                    desiredWorldPosition = new Vector3(pose.tx(), pose.ty(), pose.tz());

                    lastArHitResult = hit;
                    break;
                }
            }
        }
    }

    @Override
    public void onEndTransformation(DragGesture gesture) {
        HitResult hitResult = lastArHitResult;
        if (hitResult == null) {
            return;
        }

        if (hitResult.getTrackable().getTrackingState() == TrackingState.TRACKING) {
            AnchorNode anchorNode = getAnchorNodeOrDie();

            Anchor oldAnchor = anchorNode.getAnchor();
            if (oldAnchor != null) {
                oldAnchor.detach();
            }

            Anchor newAnchor = hitResult.createAnchor();

            Vector3 worldPosition = getTransformableNode().getWorldPosition();
            Quaternion worldRotation = getTransformableNode().getWorldRotation();

            anchorNode.setAnchor(newAnchor);


            getTransformableNode().setWorldPosition(worldPosition);
            getTransformableNode().setWorldRotation(worldRotation);
        }

        desiredWorldPosition = null;
    }

    private AnchorNode getAnchorNodeOrDie() {
        Node parent = getTransformableNode().getParent();
        if (!(parent instanceof AnchorNode)) {
            throw new IllegalStateException("TransformableNode must have an AnchorNode as a parent.");
        }

        return (AnchorNode) parent;
    }

    private void updatePosition(FrameTime frameTime) {
        // Store in local variable for nullness static analysis.
        Vector3 desiredWorldPosition = this.desiredWorldPosition;
        if (desiredWorldPosition == null) {
            return;
        }

        Vector3 worldPosition = getTransformableNode().getWorldPosition();
        float lerpFactor = MathHelper.clamp(frameTime.getDeltaSeconds() * LERP_SPEED, 0, 1);
        worldPosition = Vector3.lerp(worldPosition, desiredWorldPosition, lerpFactor);

        float lengthDiff = Math.abs(Vector3.subtract(desiredWorldPosition, worldPosition).length());
        if (lengthDiff <= POSITION_LENGTH_THRESHOLD) {
            worldPosition = desiredWorldPosition;
            this.desiredWorldPosition = null;
        }

        getTransformableNode().setWorldPosition(worldPosition);
    }

    private static float dotQuaternion(Quaternion lhs, Quaternion rhs) {
        return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z + lhs.w * rhs.w;
    }
}
