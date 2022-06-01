package br.com.tiozinnub.civilization.core.blueprinting;

import br.com.tiozinnub.civilization.resource.blueprinting.RawBlueprint;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class BlueprintDefs {
    private static final Map<Identifier, Blueprint> blueprints = new HashMap<>();

    public static void registerRawBlueprints(List<RawBlueprint> rawBlueprints) {
        for (var rawBlueprint : rawBlueprints) {
            var blueprint = new Blueprint(rawBlueprint);
            blueprints.put(blueprint.getIdentifier(), blueprint);
        }
    }

//    public static Blueprint getBlueprint(BuildingType type, Race race, BuildingMaterial material, int level) {
//        return getBlueprint("blueprints/%s/%s/%s%s".formatted(type.asString(), race.asString(), material.asString(), (level == 0 ? "" : "_%d".formatted(level))));
//    }

    public static Blueprint getBlueprint(String path) {
        return getBlueprint(idFor(path));
    }

    public static Blueprint getBlueprint(Identifier identifier) {
        return blueprints.get(identifier);
    }
}
