package io.github.jumperonjava.imaginebook.util;

import io.github.jumperonjava.imaginebook.Imaginebook;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//? if >= 1.21.6
/*import net.minecraft.client.gl.RenderPipelines;*/

import java.util.*;

public class VersionFunctions {
    public static void drawTexture(DrawContext context, Identifier identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        //? if >= 1.21.6 {
        /*context.drawTexture(Imaginebook.GUI_TEXTURED_NOCULL, identifier, x, y, u, v, width, height, textureWidth, textureHeight);
        *//*?} elif >= 1.21.3 {*/
        /*context.drawTexture(RenderLayer::getGuiTextured, identifier, x, y, u, v, width, height, textureWidth, textureHeight);
        *///?} else {
        context.drawTexture(identifier, x, y, u, v, width, height, textureWidth, textureHeight);
        //?}
    }


    //? if < 1.21.3 {
    public static ColorHelper.Argb ColorHelper = new ColorHelper.Argb();
    //?} else {
    /*public static ColorHelper ColorHelper = new ColorHelper();
    *///?}


    public static void pushMatrix(DrawContext context) {
        //? if >= 1.21.6 {
        /*context.getMatrices().pushMatrix();
        *///?} else {
        context.getMatrices().push();
        //?}
    }


    public static void popMatrix(DrawContext context) {
        //? if >= 1.21.6 {
        /*context.getMatrices().popMatrix();
        *///?} else {
        context.getMatrices().pop();
        //?}
    }


    public static void translate2D(DrawContext context, float x, float y) {
        //? if >= 1.21.6 {
        /*context.getMatrices().translate(x, y);
        *///?} else {
        context.getMatrices().translate(x, y, 0);
        //?}
    }


    public static void scale2D(DrawContext context, float x, float y) {
        //? if >= 1.21.6 {
        /*context.getMatrices().scale(x, y);
        *///?} else {
        context.getMatrices().scale(x, y, 1);
        //?}
    }


    public static void rotateZ(DrawContext context, float degrees) {
        //? if >= 1.21.6 {
        /*context.getMatrices().rotate((float) Math.toRadians(degrees));
        *///?} else {
        context.getMatrices().multiply(new Quaternionf().rotateZ((float) Math.toRadians(degrees)));
        //?}
    }


    // List.reversed method was added only in java 21, and it crashes mod (softly, just logs errors) when running on lower java version
    // this method reimplements that functionality
    public static <T> List<T> reversed(List<T> collection) {
        var reversedList = new LinkedList<T>();
        collection.forEach((item) -> reversedList.add(0, item));
        return new ArrayList<>(reversedList);
    }
}
