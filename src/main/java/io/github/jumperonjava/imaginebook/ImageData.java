package io.github.jumperonjava.imaginebook;

import net.minecraft.client.texture.NativeImage;

import java.util.Objects;

public final class ImageData {
    public String url;
    public float x;
    public float y;
    public float width=1;
    public float height=1;
    public float rotation;

    public ImageData(String url, short x, short y, float width, float height) {
        this.url = url;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public ImageData() {}

    public String url() {
        return url;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width(Image.ImageSize nativeImage) {
        return width*nativeImage.getWidth();
    }

    public float height(Image.ImageSize nativeImage) {
        return height*nativeImage.getHeight();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ImageData) obj;
        return Objects.equals(this.url, that.url) &&
                this.x == that.x &&
                this.y == that.y &&
                this.width == that.width &&
                this.height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, x, y, width, height);
    }

    @Override
    public String toString() {
        return "ImageDefinition[" +
                "url=" + url + ", " +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "width=" + width + ", " +
                "height=" + height + ']';
    }

    public void rotate(float degrees) {
        this.rotation += degrees;
        this.rotation = Math.floorMod((int) rotation,360);
    }
}
