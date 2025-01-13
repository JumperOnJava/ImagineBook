package io.github.jumperonjava.imaginebook;

import net.minecraft.util.Identifier;

public class Image {
    private final String url;
    private final Identifier left;
    private final ImageSize right;

    public ImageSize getRight() {
        return right;
    }

    public Identifier getLeft() {
        return left;
    }

    public Image(Identifier left, ImageSize right, String url) {
        this.left = left;
        this.right = right;
        this.url = url;
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
