package br.com.tiozinnub.civilization.client.renderer.entity;

import br.com.tiozinnub.civilization.client.model.entity.PersonEntityModel;
import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

import java.util.List;

public class PersonEntityRenderer extends GeoEntityRenderer<PersonEntity> {

    private static final String HEAD = "head";
    private static final String BODY = "body";
    private static final String CHESTPLATE = "chestplate";
    private static final String BELT = "belt";
    private static final String LEG_LEFT = "legLeft";
    private static final String BOOT_LEFT = "bootLeft";
    private static final String LEG_RIGHT = "legRight";
    private static final String BOOT_RIGHT = "bootRight";
    private static final String ARM_LEFT = "armLeft";
    private static final String ARM_RIGHT = "armRight";
    private static final String HAND_RIGHT = "handRight";
    private static final String HAND_LEFT = "handLeft";

    protected ItemStack mainHandItem;
    protected ItemStack offhandItem;

    public PersonEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new PersonEntityModel());
        scaleHeight = 0.9375f;
        scaleWidth = 0.9375f;

        addRenderLayer(new ItemArmorGeoLayer<>(this) {
            @Override
            protected ItemStack getArmorItemForBone(GeoBone bone, PersonEntity animatable) {
                return switch (bone.getName()) {
                    case HEAD -> this.helmetStack;
                    case CHESTPLATE, ARM_LEFT, ARM_RIGHT -> this.chestplateStack;
                    case LEG_LEFT, LEG_RIGHT, BELT -> this.leggingsStack;
                    case BOOT_LEFT, BOOT_RIGHT -> this.bootsStack;
                    default -> null;
                };
            }

            @Override
            protected @NotNull EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, PersonEntity animatable) {
                return switch (bone.getName()) {
                    case HEAD -> EquipmentSlot.HEAD;
                    case CHESTPLATE, ARM_LEFT, ARM_RIGHT -> EquipmentSlot.CHEST;
                    case LEG_LEFT, LEG_RIGHT, BELT -> EquipmentSlot.LEGS;
                    case BOOT_LEFT, BOOT_RIGHT -> EquipmentSlot.FEET;
                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
                };
            }

            @Override
            protected @NotNull ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, PersonEntity animatable, BipedEntityModel<?> baseModel) {
                return switch (bone.getName()) {
                    case HEAD -> baseModel.head;
                    case CHESTPLATE, BELT -> baseModel.body;
                    case LEG_LEFT, BOOT_LEFT -> baseModel.leftLeg;
                    case LEG_RIGHT, BOOT_RIGHT -> baseModel.rightLeg;
                    case ARM_LEFT -> baseModel.leftArm;
                    case ARM_RIGHT -> baseModel.rightArm;
                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                };
            }

            @Override
            public void preRender(MatrixStack poseStack, PersonEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                super.preRender(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            }
        });

        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Override
            protected ItemStack getStackForBone(GeoBone bone, PersonEntity animatable) {
                return switch (bone.getName()) {
                    case HAND_RIGHT -> animatable.isLeftHanded() ? animatable.getOffHandStack() : animatable.getMainHandStack();
                    case HAND_LEFT -> animatable.isLeftHanded() ? animatable.getMainHandStack() : animatable.getOffHandStack();
                    default -> null;
                };
            }

            @Override
            protected ModelTransformation.Mode getTransformTypeForStack(GeoBone bone, ItemStack stack, PersonEntity animatable) {
                return switch (bone.getName()) {
                    case HAND_RIGHT -> ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND;
                    case HAND_LEFT -> ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND;
                    default -> null;
                };
            }

            @Override
            protected void renderStackForBone(MatrixStack matrices, GeoBone bone, ItemStack stack, PersonEntity animatable, VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay) {
                if (!stack.isEmpty()) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
                }

                super.renderStackForBone(matrices, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public void preRender(MatrixStack poseStack, PersonEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        this.mainHandItem = animatable.getMainHandStack();
        this.offhandItem = animatable.getOffHandStack();
    }

    @Override
    protected void renderLabelIfPresent(PersonEntity personEntity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        double d = this.dispatcher.getSquaredDistanceToCamera(personEntity);

        float margin = 0.675f;
        float padding = 0.325f;

        if (!(d > 4096.0D)) {
            boolean sneaky = !personEntity.isSneaky();

            List<Text> multilineNameplate = personEntity.getMultilineNameplate().stream().filter(t -> t.getString().length() > 0).toList();

            if (d > 100) {
                multilineNameplate = multilineNameplate.stream().findFirst().stream().toList();
            }

            for (int i = 0, multilineNameplateSize = multilineNameplate.size(); i < multilineNameplateSize; i++) {
                Text line = multilineNameplate.get(i);

                var height = personEntity.getHeight() + margin + ((multilineNameplateSize - i - 1) * padding);

                drawLabel(line, sneaky, height, matrixStack, vertexConsumerProvider, light);
            }
        }

        //super.renderLabelIfPresent(personEntity, text, matrixStack, vertexConsumerProvider, light);
    }


    private void drawLabel(Text text, boolean sneaky, float height, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0F, height, 0.0F);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        int j = (int) (g * 255.0F) << 24;
        TextRenderer textRenderer = this.getTextRenderer();
        float h = (float) (-textRenderer.getWidth(text) / 2);
        textRenderer.draw(text, h, 0, 553648127, false, matrix4f, vertexConsumers, sneaky, j, light);
        if (sneaky) {
            textRenderer.draw(text, h, 0, -1, false, matrix4f, vertexConsumers, false, 0, light);
        }

        matrices.pop();
    }

    @Override
    public boolean hasLabel(PersonEntity animatable) {
        return true;
    }


}
