package io.github.jumperonjava.imaginebook.fabric.client;

import io.github.jumperonjava.imaginebook.Imaginebook;
import net.fabricmc.api.ClientModInitializer;

public final class ImaginebookFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Imaginebook.init();
    }
}
