package br.com.tiozinnub.civilization.registry;

import br.com.tiozinnub.civilization.core.blueprinting.Blueprint;
import br.com.tiozinnub.civilization.core.structure.StructureType;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueprintRegistry {
    private static final Map<Identifier, Blueprint> BLUEPRINTS = new HashMap<>();

    public static void register(Identifier id, Blueprint blueprint) {
        BLUEPRINTS.put(id, blueprint);
    }

    public static Blueprint getBlueprint(Identifier id) {
        return BLUEPRINTS.get(id);
    }

    public static List<Identifier> getBlueprintIdsForStructure(StructureType structureType) {
        return BLUEPRINTS.keySet().stream().filter(id -> id.getPath().startsWith(structureType.asString())).toList();
    }
}
