package br.com.tiozinnub.civilization.resources;

import br.com.tiozinnub.civilization.core.blueprinting.BlueprintMaker;
import br.com.tiozinnub.civilization.registry.BlueprintRegistry;
import br.com.tiozinnub.civilization.utils.Constraints;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;
import static br.com.tiozinnub.civilization.utils.helper.StreamHelper.readInputStream;

public class StructureResource {
    private static final Map<String, String> BLUEPRINT_FILES = new HashMap<>();

    public static StructureResource loadFrom(ResourceManager manager) {
        StructureResource res = new StructureResource();

        var resources = manager.findResources("structures", path -> {
            if (!path.getNamespace().equals(Constraints.MOD_ID)) return false;
            return path.getPath().endsWith(".blueprint");
        });

        for (var entry : resources.entrySet()) {
            var id = entry.getKey();
            var resource = entry.getValue();
            try {
                BLUEPRINT_FILES.put(id.getPath(), readInputStream(resource.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    public void apply() {
        for (var entry : BLUEPRINT_FILES.entrySet()) {
            var blueprint = new BlueprintMaker.Reader().read(entry.getKey(), entry.getValue());
            var key = entry.getKey().replaceAll("structures/(.*)\\.blueprint", "$1");
            BlueprintRegistry.register(idFor(key), blueprint);
        }
    }

}

