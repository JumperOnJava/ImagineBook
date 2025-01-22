package io.github.jumperonjava.imaginebook.platform;

import io.github.jumperonjava.imaginebook.Imaginebook;
/*? if fabric {*/
import net.fabricmc.api.ClientModInitializer;

public final class PlatformEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Imaginebook.init();
    }
}
/*?} elif neoforge {*/
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Imaginebook.MOD_ID, dist = Dist.CLIENT)
public final class PlatformEntrypoint {
    public PlatformEntrypoint() {
        Imaginebook.init();
    }
}
*//*?}*/