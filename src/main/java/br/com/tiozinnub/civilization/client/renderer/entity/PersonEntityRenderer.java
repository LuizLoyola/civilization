package br.com.tiozinnub.civilization.client.renderer.entity;

import br.com.tiozinnub.civilization.client.model.entity.PersonEntityModel;
import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.List;

public class PersonEntityRenderer extends GeoEntityRenderer<PersonEntity> {
    public PersonEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new PersonEntityModel());
        scaleHeight = 0.9375f;
        scaleWidth = 0.9375f;
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
