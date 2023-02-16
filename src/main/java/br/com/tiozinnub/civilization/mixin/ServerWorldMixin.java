package br.com.tiozinnub.civilization.mixin;

import br.com.tiozinnub.civilization.core.PersonCatalog;
import br.com.tiozinnub.civilization.ext.IServerWorldExt;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static br.com.tiozinnub.civilization.utils.Constraints.MOD_ID;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements IServerWorldExt {
    protected PersonCatalog personCatalog;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructor(CallbackInfo ci) {
        ServerWorld serverWorld = (ServerWorld) (Object) this;
        this.personCatalog = (serverWorld).getPersistentStateManager().getOrCreate((nbt) -> PersonCatalog.fromNbt(serverWorld, nbt), () -> new PersonCatalog(serverWorld), "%s_personCatalog".formatted(MOD_ID));
    }

    @Override
    public PersonCatalog getPersonCatalog() {
        return this.personCatalog;
    }
}
