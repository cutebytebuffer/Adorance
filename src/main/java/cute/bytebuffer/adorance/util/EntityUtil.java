package cute.bytebuffer.adorance.util;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class EntityUtil implements IMinecraft{

    private static void spawnLocalCrystal(BlockPos pos) {
        ClientWorld world = mc.world;
        if (world == null) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        EndCrystalEntity crystal = new EndCrystalEntity(world, x, y, z);
        crystal.setUuid(UUID.randomUUID());
        crystal.setShowBottom(false);
        crystal.noClip = true;
        crystal.refreshPositionAndAngles(x, y, z, 0.0F, 0.0F);
        world.addEntity(crystal);
    }



}
