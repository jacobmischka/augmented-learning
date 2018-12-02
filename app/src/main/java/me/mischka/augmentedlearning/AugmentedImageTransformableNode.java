package me.mischka.augmentedlearning;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ScaleController;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

public class AugmentedImageTransformableNode extends TransformableNode {
    private final VerticalRotationController verticalRotationController;
    private final AugmentedImageRotationController rotationController;
    private final AugmentedImageTranslationController translationController;

    private Node node;

    public AugmentedImageTransformableNode(TransformationSystem transformationSystem, TwoFingerDragGestureRecognizer twoFingerDragGestureRecognizer) {
        super(transformationSystem);
        getRotationController().setEnabled(false);
        removeTransformationController(getRotationController());
        getTranslationController().setEnabled(false);
        removeTransformationController(getTranslationController());

        verticalRotationController = new VerticalRotationController(this, twoFingerDragGestureRecognizer);
        rotationController = new AugmentedImageRotationController(this, transformationSystem.getTwistRecognizer());
        translationController = new AugmentedImageTranslationController(this, transformationSystem.getDragRecognizer());

        addTransformationController(rotationController);
        addTransformationController(translationController);

        ScaleController scaleController = getScaleController();
        scaleController.setMinScale(0.1f);
        scaleController.setMaxScale(3f);


        node = new Node();
        node.setParent(this);
    }

    public boolean isTransforming() {
        return
                super.isTransforming()
                || verticalRotationController.isTransforming()
                || rotationController.isTransforming()
                || translationController.isTransforming();
    }

    @Override
    public void onActivate() {
        super.onActivate();
        verticalRotationController.onActivated(this);
        rotationController.onActivated(this);
        translationController.onActivated(this);
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        verticalRotationController.onDeactivated(this);
        rotationController.onDeactivated(this);
        translationController.onDeactivated(this);
    }

    @Override
    public void setRenderable(Renderable renderable) {
        node.setRenderable(renderable);
        if (renderable != null) {
            setCollisionShape(renderable.getCollisionShape());
        }

        setLookDirection(Vector3.left());
    }
}
