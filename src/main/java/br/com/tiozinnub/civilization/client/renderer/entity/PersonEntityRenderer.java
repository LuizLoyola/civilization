package br.com.tiozinnub.civilization.client.renderer.entity;


import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import br.com.tiozinnub.civilization.entity.property.Gender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.List;

import static br.com.tiozinnub.civilization.entity.property.Gender.byGender;

@Environment(EnvType.CLIENT)
public class PersonEntityRenderer extends LivingEntityRenderer<PersonEntity, PlayerEntityModel<PersonEntity>> {
    public PersonEntityRenderer(EntityRendererFactory.Context ctx, Gender gender) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(byGender(gender, EntityModelLayers.PLAYER, EntityModelLayers.PLAYER_SLIM)), byGender(gender, false, true)), 0.5F);
        this.addFeature(new ArmorFeatureRenderer<>(
                this,
                new BipedEntityModel<>(ctx.getPart(byGender(gender, EntityModelLayers.PLAYER_INNER_ARMOR, EntityModelLayers.PLAYER_SLIM_INNER_ARMOR))),
                new BipedEntityModel<>(ctx.getPart(byGender(gender, EntityModelLayers.PLAYER_OUTER_ARMOR, EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR)))
        ));
        this.addFeature(new PersonHeldItemFeatureRenderer<>(this));
        this.addFeature(new StuckArrowsFeatureRenderer<>(ctx, this));
//        this.addFeature(new Deadmau5FeatureRenderer(this));
//        this.addFeature(new CapeFeatureRenderer(this));
        this.addFeature(new HeadFeatureRenderer<>(this, ctx.getModelLoader()));
        this.addFeature(new ElytraFeatureRenderer<>(this, ctx.getModelLoader()));
//        this.addFeature(new ShoulderParrotFeatureRenderer<>(this, ctx.getModelLoader()));
        this.addFeature(new TridentRiptideFeatureRenderer<>(this, ctx.getModelLoader()));
        this.addFeature(new StuckStingersFeatureRenderer<>(this));
    }

    private static BipedEntityModel.ArmPose getArmPose(PersonEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isEmpty()) {
            return BipedEntityModel.ArmPose.EMPTY;
        } else {
            if (player.getActiveHand() == hand && player.getItemUseTimeLeft() > 0) {
                UseAction useAction = itemStack.getUseAction();
                if (useAction == UseAction.BLOCK) {
                    return BipedEntityModel.ArmPose.BLOCK;
                }

                if (useAction == UseAction.BOW) {
                    return BipedEntityModel.ArmPose.BOW_AND_ARROW;
                }

                if (useAction == UseAction.SPEAR) {
                    return BipedEntityModel.ArmPose.THROW_SPEAR;
                }

                if (useAction == UseAction.CROSSBOW && hand == player.getActiveHand()) {
                    return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useAction == UseAction.SPYGLASS) {
                    return BipedEntityModel.ArmPose.SPYGLASS;
                }
            } else if (!player.handSwinging && itemStack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack)) {
                return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
            }

            return BipedEntityModel.ArmPose.ITEM;
        }
    }

    private static float getLabelHeight(PersonEntity person, int index, int max) {
        var marginTop = 0.5f;
        return person.getHeight() + marginTop + ((max - index - 1) * 0.25f);
    }

    public void render(PersonEntity personEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.setModelPose(personEntity);
        super.render(personEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    public Vec3d getPositionOffset(PersonEntity personEntity, float f) {
        return personEntity.isInSneakingPose() ? new Vec3d(0.0, -0.125, 0.0) : super.getPositionOffset(personEntity, f);
    }

    private void setModelPose(PersonEntity player) {
        PlayerEntityModel<PersonEntity> playerEntityModel = this.getModel();
        if (player.isSpectator()) {
            playerEntityModel.setVisible(false);
            playerEntityModel.head.visible = true;
            playerEntityModel.hat.visible = true;
        } else {
            playerEntityModel.setVisible(true);
            playerEntityModel.hat.visible = true; //player.isPartVisible(PlayerModelPart.HAT);
            playerEntityModel.jacket.visible = true; //player.isPartVisible(PlayerModelPart.JACKET);
            playerEntityModel.leftPants.visible = true; //player.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
            playerEntityModel.rightPants.visible = true; //player.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
            playerEntityModel.leftSleeve.visible = true; //player.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
            playerEntityModel.rightSleeve.visible = true; //player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE);
            playerEntityModel.sneaking = player.isInSneakingPose();
            BipedEntityModel.ArmPose armPose = getArmPose(player, Hand.MAIN_HAND);
            BipedEntityModel.ArmPose armPose2 = getArmPose(player, Hand.OFF_HAND);
            if (armPose.isTwoHanded()) {
                armPose2 = player.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
            }

            if (player.getMainArm() == Arm.RIGHT) {
                playerEntityModel.rightArmPose = armPose;
                playerEntityModel.leftArmPose = armPose2;
            } else {
                playerEntityModel.rightArmPose = armPose2;
                playerEntityModel.leftArmPose = armPose;
            }
        }

    }

    public Identifier getTexture(PersonEntity personEntity) {
        return personEntity.getSkinTexture();
    }

    protected void scale(PersonEntity personEntity, MatrixStack matrixStack, float f) {
        float g = 0.9375F;
        matrixStack.scale(g, g, g);
    }

    protected void renderLabelIfPresent(PersonEntity person, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        double d = this.dispatcher.getSquaredDistanceToCamera(person);
        matrices.push();

        boolean sneaky = !person.isSneaky();

        List<Text> multilineNameplate = person.getMultilineNameplate().stream().filter(t -> t.getString().length() > 0).toList();

        var closeEnough = d <= 100;

        // TODO: See if player is allowed to see this stuff
        var showHealth = closeEnough;
        var showHunger = closeEnough;

        var lineCount = multilineNameplate.size() + 1;

        if (showHealth) lineCount++;
        if (showHunger) lineCount++;

        if (!closeEnough) {
            multilineNameplate = multilineNameplate.stream().findFirst().stream().toList();
            lineCount = 1;
        }

        for (int i = 0; i < multilineNameplate.size(); i++) {
            Text line = multilineNameplate.get(i);
            drawLabel(matrices, vertexConsumers, line, getLabelHeight(person, i, lineCount), sneaky, light);
        }

        if (showHealth) {
            var labelHeight = getLabelHeight(person, multilineNameplate.size(), lineCount);
            var health = person.getHealth();
            var maxHealth = person.getMaxHealth();

            var hearts = Math.ceil(health / 2);
            var maxHearts = maxHealth / 2;
            var heartChar = "\u2764";

            var maxHeartsBeforeConcat = 10;

            if (maxHearts > maxHeartsBeforeConcat) {
                drawLabel(matrices, vertexConsumers, Text.of("%s%s%s x%.0f".formatted(Formatting.RED, heartChar, Formatting.RESET, hearts)), labelHeight, sneaky, light);
            } else {
                var healthTextBuilder = new StringBuilder();

                healthTextBuilder.append(Formatting.RED);
                for (int i = 0; i < maxHearts; i++) {
                    if (i == hearts) {
                        healthTextBuilder.append(Formatting.GRAY);
                    }
                    healthTextBuilder.append(heartChar);
                }

                drawLabel(matrices, vertexConsumers, Text.of(healthTextBuilder.toString()), labelHeight, sneaky, light);
            }
        }
//        super.renderLabelIfPresent(person, text, matrices, vertexConsumers, light);
        matrices.pop();
    }

    private void drawLabel(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Text text, float height, boolean sneaky, int light) {
        var i = "deadmau5".equals(text.getString()) ? -10 : 0;
        matrices.push();
        matrices.translate(0.0D, height, 0.0D);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        var matrix4f = matrices.peek().getPositionMatrix();
        var g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        var j = (int) (g * 255.0F) << 24;
        var textRenderer = this.getTextRenderer();
        var h = (float) (-textRenderer.getWidth(text) / 2);
        textRenderer.draw(text, h, (float) i, 0x20ffffff, false, matrix4f, vertexConsumers, sneaky, j, light);
        if (sneaky) {
            textRenderer.draw(text, h, (float) i, -1, false, matrix4f, vertexConsumers, false, 0, light);
        }
        matrices.pop();
    }
//
//    private void renderHealthBar(MatrixStack matrices, PersonEntity person, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking) {
//        var heartType = HeartType.fromPersonState(person);
//
//        int i = 0;
//        int j = MathHelper.ceil((double)maxHealth / 2.0);
//        int k = MathHelper.ceil((double)absorption / 2.0);
//        int l = j * 2;
//
//        for(int m = j + k - 1; m >= 0; --m) {
//            int n = m / 10;
//            int o = m % 10;
//            int p = x + o * 8;
//            int q = y - n * lines;
//
//            if (m < j && m == regeneratingHeartIndex) {
//                q -= 2;
//            }
//
//            this.drawHeart(matrices, HeartType.CONTAINER, p, q, i, blinking, false);
//            int r = m * 2;
//            boolean bl = m >= j;
//            if (bl) {
//                int s = r - l;
//                if (s < absorption) {
//                    boolean bl2 = s + 1 == absorption;
//                    this.drawHeart(matrices, heartType == HeartType.WITHERED ? heartType : HeartType.ABSORBING, p, q, i, false, bl2);
//                }
//            }
//
//            boolean bl3;
//            if (blinking && r < health) {
//                bl3 = r + 1 == health;
//                this.drawHeart(matrices, heartType, p, q, i, true, bl3);
//            }
//
//            if (r < lastHealth) {
//                bl3 = r + 1 == lastHealth;
//                this.drawHeart(matrices, heartType, p, q, i, false, bl3);
//            }
//        }
//    }
//
////    private void drawHeart(MatrixStack matrices, InGameHud.HeartType type, int x, int y, int v, boolean blinking, boolean halfHeart) {
////        DrawableHelper.drawTexture(matrices, x, y, type.getU(halfHeart, blinking), v, 9, 9);
////    }

    public void renderRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PersonEntity player) {
        this.renderArm(matrices, vertexConsumers, light, player, this.model.rightArm, this.model.rightSleeve);
    }

    public void renderLeftArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PersonEntity player) {
        this.renderArm(matrices, vertexConsumers, light, player, this.model.leftArm, this.model.leftSleeve);
    }

    private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PersonEntity player, ModelPart arm, ModelPart sleeve) {
        PlayerEntityModel<PersonEntity> playerEntityModel = this.getModel();
        this.setModelPose(player);
        playerEntityModel.handSwingProgress = 0.0F;
        playerEntityModel.sneaking = false;
        playerEntityModel.leaningPitch = 0.0F;
        playerEntityModel.setAngles(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        arm.pitch = 0.0F;
        arm.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(player.getSkinTexture())), light, OverlayTexture.DEFAULT_UV);
        sleeve.pitch = 0.0F;
        sleeve.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(player.getSkinTexture())), light, OverlayTexture.DEFAULT_UV);
    }

    protected void setupTransforms(PersonEntity personEntity, MatrixStack matrixStack, float f, float g, float h) {
        float i = personEntity.getLeaningPitch(h);
        float j;
        float k;
        if (personEntity.isFallFlying()) {
            super.setupTransforms(personEntity, matrixStack, f, g, h);
            j = (float) personEntity.getRoll() + h;
            k = MathHelper.clamp(j * j / 100.0F, 0.0F, 1.0F);
            if (!personEntity.isUsingRiptide()) {
                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k * (-90.0F - personEntity.getPitch())));
            }

            Vec3d vec3d = personEntity.getRotationVec(h);
            Vec3d vec3d2 = personEntity.getVelocity();
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((float) (Math.signum(m) * Math.acos(l))));
            }
        } else if (i > 0.0F) {
            super.setupTransforms(personEntity, matrixStack, f, g, h);
            j = personEntity.isTouchingWater() ? -90.0F - personEntity.getPitch() : -90.0F;
            k = MathHelper.lerp(i, 0.0F, j);
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k));
            if (personEntity.isInSwimmingPose()) {
                matrixStack.translate(0.0, -1.0, 0.30000001192092896);
            }
        } else {
            super.setupTransforms(personEntity, matrixStack, f, g, h);
        }

    }

    @Environment(EnvType.CLIENT)
    enum HeartType {
        CONTAINER(0, false),
        NORMAL(2, true),
        POISONED(4, true),
        WITHERED(6, true),
        ABSORBING(8, false),
        FROZEN(9, false);

        private final int textureIndex;
        private final boolean hasBlinkingTexture;

        HeartType(int textureIndex, boolean hasBlinkingTexture) {
            this.textureIndex = textureIndex;
            this.hasBlinkingTexture = hasBlinkingTexture;
        }

        static HeartType fromPersonState(PersonEntity person) {
            if (person.hasStatusEffect(StatusEffects.POISON)) return POISONED;
            if (person.hasStatusEffect(StatusEffects.WITHER)) return WITHERED;
            if (person.isFrozen()) return FROZEN;
            return NORMAL;
        }

        public int getU(boolean halfHeart, boolean blinking) {
            int i;
            if (this == CONTAINER) {
                i = blinking ? 1 : 0;
            } else {
                int j = halfHeart ? 1 : 0;
                int k = this.hasBlinkingTexture && blinking ? 2 : 0;
                i = j + k;
            }

            return 16 + (this.textureIndex * 2 + i) * 9;
        }
    }
}
