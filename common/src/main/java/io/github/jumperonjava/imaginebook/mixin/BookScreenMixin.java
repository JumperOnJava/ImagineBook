package io.github.jumperonjava.imaginebook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.jumperonjava.imaginebook.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Mixin(BookScreen.class)
public class BookScreenMixin extends Screen {
    @Shadow private int pageIndex;
    @Shadow private PageTurnWidget nextPageButton;
    @Shadow private PageTurnWidget previousPageButton;
    List<List<ImageData>> imaginebook_pages = new ArrayList<>();
    private Text error;
    private Element closeButton;

    protected BookScreenMixin(Text title) {
        super(title);
    }


    @Inject(method = "<init>(Lnet/minecraft/client/gui/screen/ingame/BookScreen$Contents;Z)V", at = @At("TAIL"))
    void construct(BookScreen.Contents contents, boolean playPageTurnSound, CallbackInfo ci) {
        parseImages(contents);
    }

    @Inject(method = "setPageProvider", at = @At("TAIL"))
    void construct(BookScreen.Contents pageProvider, CallbackInfo ci) {
        parseImages(pageProvider);
    }


    void parseImages(BookScreen.Contents contents){
        for(int i=0;i<255;i++){
            imaginebook_pages.add(new ArrayList<>());
        }
        var pages = contents.pages();
        for (int i = 0; i < pages.size(); i++) {
            var page = pages.get(i).getString();
            if(page.length()!= Imaginebook.LENGTH)
                continue;
            var split = page.split("\n");
            var last = split[split.length - 1];
//            page = page.replace(last,"").trim();
//            contents.pages().set(i,page);
            try {
                var asbytes = Base64.getDecoder().decode(last);
                var definitions = ImageSerializer.deserializeImageMetadata(asbytes);
                imaginebook_pages.set(i, definitions);
            }
            catch (Exception e) {
                this.error = Text.literal(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    @Inject(method = "render",at = @At("TAIL"))
    void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int bookX = this.width / 2 - 96;
        int bookY = 2;

        var images = imaginebook_pages.get(pageIndex);
        if (images == null) return;
        for (ImageData image : images.reversed()) {
            var req = new ImageRequest(image.url());
            Image.ImageSize nativeImage = req.getTexture().getRight();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            Imaginebook.renderImage(context, bookX, bookY, image, req, nativeImage);
        }
        previousPageButton.render(context, mouseX, mouseY, delta);
        nextPageButton.render(context, mouseX, mouseY, delta);
        if(closeButton != null){
            ((Drawable)closeButton).render(context, mouseX, mouseY, delta);
        }
    }
    @WrapOperation(method = "addCloseButton",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/BookScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    Element capture(BookScreen instance, Element element, Operation<Element> original){
        this.closeButton = element;
        return original.call(instance,element);
    }
}