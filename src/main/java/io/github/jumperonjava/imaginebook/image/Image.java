package io.github.jumperonjava.imaginebook.image;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.jumperonjava.imaginebook.util.VersionFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

public class Image implements BookDrawable {
    private final Identifier identifier;
    private final ImageSize size;

    @Override
    public ImageSize getSize() {
        return size;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Image(Identifier identifier, ImageSize size) {
        this.identifier = identifier;
        this.size = size;
    }
    public Image(Identifier identifier) {
        this.identifier = identifier;
        //? if >= 1.21.4 {
        /*var texture = MinecraftClient.getInstance().getTextureManager().getTexture(identifier);
        *///?} else {
        var texture = MinecraftClient.getInstance().getTextureManager().getOrDefault(identifier,MissingSprite.getMissingSpriteTexture());
        //?}
        if (texture instanceof NativeImageBackedTexture imageBackedTexture){
            size = new ImageSize(imageBackedTexture.getImage().getWidth(), imageBackedTexture.getImage().getHeight());
        }
        else {
            size = new ImageSize(64,64);
        }
    }

    @Override
    public void render(DrawContext context) {
        VersionFunctions.drawTexture(context, this.getIdentifier(),
                0,
                0,
                (float) 0,
                (float) 0,
                getSize().getWidth(),
                getSize().getHeight(),
                getSize().getWidth(),
                getSize().getHeight()
        );
    }

    public static class ImageSize {
        private final int width;
        private final int height;

        public ImageSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        private int min() {
            return Math.min(width, height);
        }

        public int getWidth() {
            return width*100/min();
        }

        public int getHeight() {
            return height*100/min();
        }
    }
}
