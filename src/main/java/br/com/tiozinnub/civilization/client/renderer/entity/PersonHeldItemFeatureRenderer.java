package br.com.tiozinnub.civilization.client.renderer.entity;

import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PersonHeldItemFeatureRenderer<T extends PersonEntity, M extends EntityModel<T> & ModelWithArms & ModelWithHead> extends HeldItemFeatureRenderer<T, M> {
    private final HeldItemRenderer personHeldItemRenderer;
    private static final float HEAD_YAW = -0.5235988F;
    private static final float HEAD_ROLL = 1.5707964F;

    public PersonHeldItemFeatureRenderer(FeatureRendererContext<T, M> featureRendererContext, HeldItemRenderer heldItemRenderer) {
        super(featureRendererContext, heldItemRenderer);
        this.personHeldItemRenderer = heldItemRenderer;
    }

    protected void renderItem(LivingEntity entity, ItemStack stack, ModelTransformation.Mode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (stack.isOf(Items.SPYGLASS) && entity.getActiveItem() == stack && entity.handSwingTicks == 0) {
            this.renderSpyglass(entity, stack, arm, matrices, vertexConsumers, light);
        } else {
            super.renderItem(entity, stack, transformationMode, arm, matrices, vertexConsumers, light);
        }

    }

    private void renderSpyglass(LivingEntity entity, ItemStack stack, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        ModelPart modelPart = this.getContextModel().getHead();
        float f = modelPart.pitch;
        modelPart.pitch = MathHelper.clamp(modelPart.pitch, HEAD_YAW, HEAD_ROLL);
        modelPart.rotate(matrices);
        modelPart.pitch = f;
        HeadFeatureRenderer.translate(matrices, false);
        boolean bl = arm == Arm.LEFT;
        matrices.translate((bl ? -2.5F : 2.5F) / 16.0F, -0.0625, 0.0);
        this.personHeldItemRenderer.renderItem(entity, stack, Mode.HEAD, false, matrices, vertexConsumers, light);
        matrices.pop();
    }
}
