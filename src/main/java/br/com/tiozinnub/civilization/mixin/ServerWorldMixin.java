package br.com.tiozinnub.civilization.mixin;

import br.com.tiozinnub.civilization.core.CityManager;
import br.com.tiozinnub.civilization.ext.IServerWorldExt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements IServerWorldExt {
    protected CityManager cityManager;

    @SuppressWarnings({"rawtypes"})
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructor(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, CallbackInfo ci) {
        ServerWorld serverWorld = (ServerWorld) (Object) this;
        this.cityManager = (serverWorld).getPersistentStateManager().getOrCreate((nbt) -> CityManager.fromNbt(serverWorld, nbt), () -> new CityManager(serverWorld), CityManager.nameFor());
    }

    // tick
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        this.getCityManager().tick();
    }

    @Override
    public CityManager getCityManager() {
        return this.cityManager;
    }
}