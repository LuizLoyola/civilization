package br.com.tiozinnub.civilization.item;

import br.com.tiozinnub.civilization.core.blueprinting.BlueprintMaker;
import br.com.tiozinnub.civilization.registry.ItemGroupRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;
import static br.com.tiozinnub.civilization.utils.helper.StringHelper.getStringFromBlockPos;

public class BlueprintItem extends ItemBase {
    public BlueprintItem() {
        super(new Item.Settings()
                .group(ItemGroupRegistry.SPECIAL_ITEMS)
                .maxCount(1)
        );
    }

    private static void setBlueprintMaker(ItemStack itemStack, BlueprintMaker blueprintMaker) {
        itemStack.setNbt(blueprintMaker.toNbt());
    }

    private static BlueprintMaker getBlueprintMaker(ItemStack itemStack) {
        BlueprintMaker blueprintMaker;
        if (itemStack.hasNbt()) {
            blueprintMaker = BlueprintMaker.newFromNbt(itemStack.getNbt());
        } else {
            blueprintMaker = new BlueprintMaker();
        }
        return blueprintMaker;
    }

    @Override
    public Identifier getIdentifier() {
        return idFor("blueprint");
    }

    @Override
    public void inventoryTickClient(ItemStack stack, ClientWorld world, Entity entity, int slot, boolean selected) {
        if (!PlayerInventory.isValidHotbarIndex(slot)) return;

        if (!(entity instanceof PlayerEntity)) return;

        var blueprintMaker = getBlueprintMaker(stack);

        if (blueprintMaker.startPos == null) return;
        if (blueprintMaker.endPos == null) {
            drawParticleBox(world, new Box(blueprintMaker.startPos, blueprintMaker.startPos), ParticleTypes.FLAME);
            return;
        }

        if (blueprintMaker.direction == null) {
            drawParticleBox(world, new Box(blueprintMaker.startPos, blueprintMaker.endPos), ParticleTypes.SOUL_FIRE_FLAME);
            return;
        }

        drawParticleBox(world, new Box(blueprintMaker.startPos, blueprintMaker.endPos), ParticleTypes.SOUL_FIRE_FLAME, blueprintMaker.direction, ParticleTypes.FLAME);
    }


    @Override
    public ActionResult useOnBlockServer(ItemUsageContext context) {
        var player = context.getPlayer();
        if (player == null) {
            return super.useOnBlock(context);
        }

        var itemStack = context.getStack();

        var blueprintMaker = getBlueprintMaker(itemStack);
        var blockPos = context.getBlockPos();
        var direction = context.getSide();

        var result = this.handleUseOnBlock((ServerWorld) player.getWorld(), player, itemStack, blueprintMaker, blockPos, direction);

        setBlueprintMaker(itemStack, blueprintMaker);

        return result;
    }

    private ActionResult handleUseOnBlock(ServerWorld world, PlayerEntity player, ItemStack itemStack, BlueprintMaker blueprintMaker, BlockPos blockPos, Direction direction) {
        if (player.isSneaky()) {
            blueprintMaker.startPos = null;
            blueprintMaker.endPos = null;
            blueprintMaker.mainBlockPos = null;
            blueprintMaker.stages.clear();
            return ActionResult.CONSUME;
        }

        if (blueprintMaker.startPos == null) {
            blueprintMaker.startPos = blockPos;
            player.sendMessage(Text.of("Start at %s".formatted(getStringFromBlockPos(blueprintMaker.startPos, true))), true);
            return ActionResult.CONSUME;
        }

        if (blueprintMaker.endPos == null) {
            if (blueprintMaker.startPos.equals(blockPos)) {
                blueprintMaker.startPos = null;

                player.sendMessage(Text.of("Start cleared."), true);
                return ActionResult.CONSUME;
            }

            blueprintMaker.endPos = blockPos;
            player.sendMessage(Text.of("End at %s".formatted(getStringFromBlockPos(blueprintMaker.endPos, true))), true);
            return ActionResult.CONSUME;
        }

        var box = blueprintMaker.getBox();
        assert box != null;

        if (blueprintMaker.mainBlockPos == null) {
            if (blueprintMaker.endPos.equals(blockPos)) {
                blueprintMaker.endPos = null;

                player.sendMessage(Text.of("End cleared."), true);
                return ActionResult.CONSUME;
            }

            blueprintMaker.mainBlockPos = blockPos;

            player.sendMessage(Text.of("Main block at %s".formatted(getStringFromBlockPos(blueprintMaker.mainBlockPos, true))), true);
            return ActionResult.CONSUME;
        }

        if (blueprintMaker.mainBlockPos.equals(blockPos)) {
            blueprintMaker.mainBlockPos = null;

            player.sendMessage(Text.of("Main block cleared."), true);
            return ActionResult.CONSUME;
        }

        var stage = new BlueprintMaker.Stage(world, blueprintMaker.getBox(), blueprintMaker.getMinPos());

        if (blueprintMaker.stages.size() == 0 || !stage.isSame(blueprintMaker.stages.get(blueprintMaker.stages.size() - 1))) {
            blueprintMaker.stages.add(stage);

            player.sendMessage(Text.of("Stage %d saved".formatted(blueprintMaker.stages.size())), true);
            return ActionResult.CONSUME;
        }

        // finish
        player.sendMessage(Text.of(blueprintMaker.generateBlueprintText()), false);
        return ActionResult.CONSUME;
    }


    private void drawParticleBox(ClientWorld world, Box box, ParticleEffect particle) {
        drawParticleBox(world, box, particle, Direction.DOWN, particle);
    }

    @SuppressWarnings("DuplicatedCode")
    private void drawParticleBox(ClientWorld world, Box box, ParticleEffect particle, Direction highlightDirection, ParticleEffect highlightParticle) {
        var minX = box.minX;
        var minY = box.minY;
        var minZ = box.minZ;
        var maxX = box.maxX + 1;
        var maxY = box.maxY + 1;
        var maxZ = box.maxZ + 1;

        drawParticleLine(world, new Vec3d(minX, minY, minZ), new Vec3d(minX, minY, maxZ), highlightDirection == Direction.DOWN || highlightDirection == Direction.WEST ? highlightParticle : particle, (int) ((maxZ - minZ) * 2));
        drawParticleLine(world, new Vec3d(minX, maxY, minZ), new Vec3d(minX, maxY, maxZ), highlightDirection == Direction.UP || highlightDirection == Direction.WEST ? highlightParticle : particle, (int) ((maxZ - minZ) * 2));
        drawParticleLine(world, new Vec3d(maxX, minY, minZ), new Vec3d(maxX, minY, maxZ), highlightDirection == Direction.DOWN || highlightDirection == Direction.EAST ? highlightParticle : particle, (int) ((maxZ - minZ) * 2));
        drawParticleLine(world, new Vec3d(maxX, maxY, minZ), new Vec3d(maxX, maxY, maxZ), highlightDirection == Direction.UP || highlightDirection == Direction.EAST ? highlightParticle : particle, (int) ((maxZ - minZ) * 2));

        drawParticleLine(world, new Vec3d(minX, minY, minZ), new Vec3d(minX, maxY, minZ), highlightDirection == Direction.NORTH || highlightDirection == Direction.WEST ? highlightParticle : particle, (int) ((maxY - minY) * 2));
        drawParticleLine(world, new Vec3d(minX, minY, maxZ), new Vec3d(minX, maxY, maxZ), highlightDirection == Direction.SOUTH || highlightDirection == Direction.WEST ? highlightParticle : particle, (int) ((maxY - minY) * 2));
        drawParticleLine(world, new Vec3d(maxX, minY, minZ), new Vec3d(maxX, maxY, minZ), highlightDirection == Direction.NORTH || highlightDirection == Direction.EAST ? highlightParticle : particle, (int) ((maxY - minY) * 2));
        drawParticleLine(world, new Vec3d(maxX, minY, maxZ), new Vec3d(maxX, maxY, maxZ), highlightDirection == Direction.SOUTH || highlightDirection == Direction.EAST ? highlightParticle : particle, (int) ((maxY - minY) * 2));

        drawParticleLine(world, new Vec3d(minX, minY, minZ), new Vec3d(maxX, minY, minZ), highlightDirection == Direction.DOWN || highlightDirection == Direction.NORTH ? highlightParticle : particle, (int) ((maxX - minX) * 2));
        drawParticleLine(world, new Vec3d(minX, maxY, minZ), new Vec3d(maxX, maxY, minZ), highlightDirection == Direction.UP || highlightDirection == Direction.NORTH ? highlightParticle : particle, (int) ((maxX - minX) * 2));
        drawParticleLine(world, new Vec3d(minX, minY, maxZ), new Vec3d(maxX, minY, maxZ), highlightDirection == Direction.DOWN || highlightDirection == Direction.SOUTH ? highlightParticle : particle, (int) ((maxX - minX) * 2));
        drawParticleLine(world, new Vec3d(minX, maxY, maxZ), new Vec3d(maxX, maxY, maxZ), highlightDirection == Direction.UP || highlightDirection == Direction.SOUTH ? highlightParticle : particle, (int) ((maxX - minX) * 2));
    }

    private void drawParticleLine(ClientWorld world, Vec3d pos1, Vec3d pos2, ParticleEffect particle, int particleCount) {
        if (world.getTime() % 10 != 0) return;

        var minX = Math.min(pos1.x, pos2.x);
        var minY = Math.min(pos1.y, pos2.y);
        var minZ = Math.min(pos1.z, pos2.z);
        var maxX = Math.max(pos1.x, pos2.x);
        var maxY = Math.max(pos1.y, pos2.y);
        var maxZ = Math.max(pos1.z, pos2.z);

        var deltaX = (maxX - minX) / particleCount;
        var deltaY = (maxY - minY) / particleCount;
        var deltaZ = (maxZ - minZ) / particleCount;

        for (int i = 0; i < particleCount; i++) {
            world.addParticle(particle, minX + deltaX * i, minY + deltaY * i, minZ + deltaZ * i, 0, 0, 0);
        }

        world.addParticle(particle, maxX, maxY, maxZ, 0, 0, 0);
    }

}
