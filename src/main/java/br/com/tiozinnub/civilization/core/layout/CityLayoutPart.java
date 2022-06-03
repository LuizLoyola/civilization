package br.com.tiozinnub.civilization.core.layout;

import br.com.tiozinnub.civilization.core.City;
import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public abstract class CityLayoutPart extends Serializable {
    private final City city;

    protected CityLayoutPart(City city) {
        this.city = city;
    }

    protected City getCity() {
        return this.city;
    }

    protected LayoutDimensions getLayoutDimensions() {
        return this.getCity().getLayoutDimensions();
    }

    public abstract Box getBox();

    public List<ChunkPos> getChunks() {
        var min = new BlockPos(this.getBox().minX, this.getBox().minY, this.getBox().minZ);
        var max = new BlockPos(this.getBox().maxX, this.getBox().maxY, this.getBox().maxZ);

        var minChunk = new ChunkPos(min);
        var maxChunk = new ChunkPos(max);

        var list = new ArrayList<ChunkPos>();
        for (var chunkX = minChunk.x; chunkX <= maxChunk.x; chunkX++) {
            for (var chunkZ = minChunk.z; chunkZ <= maxChunk.z; chunkZ++) {
                list.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        return list;
    }

    public abstract Text getDescription();
}
