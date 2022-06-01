package br.com.tiozinnub.civilization.resource.blueprinting;

import net.minecraft.resource.ResourceManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static br.com.tiozinnub.civilization.core.blueprinting.BlueprintDefs.registerRawBlueprints;
import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class BlueprintResource {
    public final List<RawBlueprint> rawBlueprints = new ArrayList<>();

    public static BlueprintResource loadFrom(ResourceManager manager) {
        var res = new BlueprintResource();

        for (var id : manager.findResources("blueprints", path -> path.endsWith(".txt"))) {
            try (var stream = manager.getResource(id).getInputStream()) {
                var text = IOUtils.toString(stream, StandardCharsets.UTF_8);
                var path = id.getPath();//.replace('/', '_');
                path = path.substring(0, path.indexOf('.'));
                var correctIdentifier = idFor(path);
                res.rawBlueprints.add(new RawBlueprint(correctIdentifier, text));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    public void apply() {
        registerRawBlueprints(rawBlueprints);
    }
}
