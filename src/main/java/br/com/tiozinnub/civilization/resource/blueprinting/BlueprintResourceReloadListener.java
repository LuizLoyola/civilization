package br.com.tiozinnub.civilization.resource.blueprinting;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class BlueprintResourceReloadListener implements SimpleResourceReloadListener<BlueprintResource> {
    @Override
    public Identifier getFabricId() {
        return idFor("resource_blueprints");
    }

    // thx to https://gist.github.com/quat1024/2645637708b9577a57671df0eab953e2
    @Override
    public CompletableFuture<BlueprintResource> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            //Do loading tasks (read files, grab things from the ResourceManager, etc)
            //You're off-thread in this method, so don't touch the game.
            return BlueprintResource.loadFrom(manager);
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(BlueprintResource data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            //Your loaded resource gets threaded into   ^^^ the first argument of this method.
            //Apply the loaded data to the game somehow (dump caches and refill them, set variables, etc)

            //noinspection Convert2MethodRef
            data.apply();
        }, executor);
    }
}
