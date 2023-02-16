package br.com.tiozinnub.civilization.utils.helper;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleHelper {
    public static void drawParticleBox(World world, Box box, ParticleEffect particle, Direction highlightDirection, ParticleEffect highlightParticle) {
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

    public static void drawParticleBox(World world, Box box, ParticleEffect particle) {
        drawParticleBox(world, box, particle, Direction.DOWN, particle);
    }

    public static void drawParticleBox(World world, BlockPos pos, ParticleEffect particle) {
        drawParticleBox(world, new Box(pos, pos), particle, Direction.DOWN, particle);
    }

    public static void drawParticleLine(World world, Vec3d pos1, Vec3d pos2, ParticleEffect particle, double particlesPerBlock) {
        drawParticleLine(world, pos1, pos2, particle, particlesPerBlock, 10);
    }

    public static void drawParticleLine(World world, Vec3d pos1, Vec3d pos2, ParticleEffect particle, double particlesPerBlock, int customFrequency) {
        var distance = pos1.distanceTo(pos2);
        drawParticleLine(world, pos1, pos2, particle, Math.max((int) (distance * particlesPerBlock), 3), customFrequency);
    }

    public static void drawParticleLine(World world, Vec3d pos1, Vec3d pos2, ParticleEffect particle, int particleCount) {
        drawParticleLine(world, pos1, pos2, particle, particleCount, 10);
    }

    public static void drawParticleLine(World world, Vec3d pos1, Vec3d pos2, ParticleEffect particle, int particleCount, int customFrequency) {
        if (world.getTime() % customFrequency != 0) return;

        var deltaX = (pos2.x - pos1.x) / particleCount;
        var deltaY = (pos2.y - pos1.y) / particleCount;
        var deltaZ = (pos2.z - pos1.z) / particleCount;

        if (world.isClient()) {
            var clientWorld = (ClientWorld) world;

            for (int i = 0; i < particleCount; i++) {
                clientWorld.addParticle(particle, pos1.x + deltaX * i, pos1.y + deltaY * i, pos1.z + deltaZ * i, 0, 0, 0);
            }

            clientWorld.addParticle(particle, pos2.x, pos2.y, pos2.z, 0, 0, 0);
        } else {
            var serverWorld = (ServerWorld) world;

            for (int i = 0; i < particleCount; i++) {
                serverWorld.spawnParticles(particle, pos1.x + deltaX * i, pos1.y + deltaY * i, pos1.z + deltaZ * i, 1, 0, 0, 0, 0);
            }

            serverWorld.spawnParticles(particle, pos2.x, pos2.y, pos2.z, 1, 0, 0, 0, 0);
        }
    }

}
