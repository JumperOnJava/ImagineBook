package io.github.jumperonjava.imaginebook.mixin;

import io.github.jumperonjava.imaginebook.Imaginebook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    void setScreen(Screen screen, CallbackInfo ci) {
        if(Imaginebook.cancelledFinalize && screen==null){
            ci.cancel();
        }
        if(screen!=null){
            Imaginebook.cancelledFinalize = false;
        }
    }
}
