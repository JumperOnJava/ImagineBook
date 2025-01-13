package io.github.jumperonjava.imaginebook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.jumperonjava.imaginebook.*;
import io.github.jumperonjava.imaginebook.util.VersionFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.apache.logging.log4j.util.Strings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {


    @Shadow
    private int currentPage;

    @Shadow
    @Final
    private List<String> pages;

    @Shadow
    private boolean dirty;
    @Shadow
    private boolean signing;
    @Shadow
    private PageTurnWidget nextPageButton;
    @Shadow
    private PageTurnWidget previousPageButton;
    @Shadow
    private ButtonWidget signButton;
    @Shadow
    private ButtonWidget doneButton;
    @Shadow private Text pageIndicatorText;

    @Shadow protected abstract BookEditScreen.PageContent getPageContent();

    @Shadow protected abstract void drawSelection(DrawContext context, Rect2i[] selectionRectangles);

    @Shadow protected abstract void drawCursor(DrawContext context, BookEditScreen.Position position, boolean atEnd);

    private TextFieldWidget urlField;
    private TextFieldWidget xPosField;
    private TextFieldWidget widthField;
    private TextFieldWidget yPosField;
    private TextFieldWidget heightField;
    private TextFieldWidget spinField;

    private ButtonWidget xPosButton;
    private ButtonWidget yPosButton;
    private ButtonWidget widthButton;
    private ButtonWidget heightButton;
    private ButtonWidget spinButton;

    private ButtonWidget removeButton;
    private ButtonWidget addButton;
    private Text error;

    protected BookEditScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "changePage", at = @At("HEAD"))
    void onChangePage(CallbackInfo ci) {
        currentEdited = null;
    }

    List<List<ImageData>> imaginebook_pages = new ArrayList<>();
    List<List<ImageData>> imaginebook_safe_pages = new ArrayList<>();


    @Inject(method = "<init>", at = @At("TAIL"))
    //? if >= 1.21.3 {
    void construct(PlayerEntity player, ItemStack stack, Hand hand, WritableBookContentComponent writableBookContent, CallbackInfo ci) {
    //?} else {
    /*void construct(PlayerEntity player, ItemStack itemStack, Hand hand, CallbackInfo ci) {
    *///?}
        for (int i = 0; i < 250; i++) {
            imaginebook_pages.add(new ArrayList<>());
            imaginebook_safe_pages.add(new ArrayList<>());
        }
        for (int i = 0; i < pages.size(); i++) {
            var page = pages.get(i);
            if (page.length() == Imaginebook.LENGTH) {
                var split = page.split("\n");
                var last = split[split.length - 1];
                page = page.replace(last, "").trim();
                pages.set(i, page);
                try {
                    var asbytes = Base64.getDecoder().decode(last);
                    var definitions = ImageSerializer.deserializeImageMetadata(asbytes);
                    imaginebook_pages.set(i, definitions);
                } catch (Exception e) {
                    this.error = Text.literal(e.getMessage() == null ? "Unknown error" : e.getMessage());
                    e.printStackTrace();
                }
            }

            imaginebook_safe_pages.set(currentPage, ImageSerializer.parseSafeModeImages(page));
        }
    }

    ImageData currentEdited;

    @Inject(method = "updateButtons", at = @At("HEAD"), cancellable = true)
    void updateButtons(CallbackInfo ci) {
        if (Imaginebook.cancelledFinalize) {
            this.signing = false;
        }
    }

    private void setCurrentEdited(ImageData image) {
        currentEdited = image;
        if (currentEdited == null) {
            setFocused(null);
        } else {
            updateFields();
        }
    }

    @Inject(method = "invalidatePageContent", at = @At("HEAD"))
    void updateSafeModeImages(CallbackInfo ci) {
        imaginebook_safe_pages.set(currentPage, ImageSerializer.parseSafeModeImages(pages.get(currentPage)));
    }


    @Inject(method = "init", at = @At("HEAD"))
    void init(CallbackInfo ci) {
        int elementHeight = 20;
        int gap = 4;
        int fieldWidth = 200;
        int heightOffset = 0;

        if (FabricLoader.getInstance().isModLoaded("stendhal")) {
            heightOffset = 140;
        }

        urlField = addDrawableChild(new TextFieldWidget(client.textRenderer, 0 + gap, heightOffset +(elementHeight + gap) + gap, fieldWidth, elementHeight, Text.translatable("imaginebook.gui.urlhere")));
        urlField.setMaxLength(256);//+1 to be able to notify user it is too long
        urlField.setChangedListener((url) -> {
            try {
                if (url.equals(currentEdited.url)) return;
                if (url.length() > 255) {
                    urlField.setText(I18n.translate("imaginebook.error.too_long"));
                    return;
                }
                new URL(url).toURI();
                currentEdited.url = Imaginebook.fixImgurLink(url);
            } catch (MalformedURLException | URISyntaxException e) {
            }
        });
        addButton = addDrawableChild(ButtonWidget.builder(Text.translatable("imaginebook.gui.add"), (b) -> {
            var newImage = new ImageData("", (short) 0, (short) 0, 1, 1);
            imaginebook_pages.get(currentPage).add(newImage);
            setCurrentEdited(newImage);
        }).position(0 + gap, heightOffset + 0 + gap).size(fieldWidth / 2 - gap, elementHeight).build());
        removeButton = addDrawableChild(ButtonWidget.builder(Text.translatable("imaginebook.gui.remove"), (b) -> {
            currentEdited.height = 0;
            currentEdited.width = 0;
            setCurrentEdited(null);
        }).position(fieldWidth / 2 + gap + gap, heightOffset + 0 + gap).size(fieldWidth / 2 - gap, elementHeight).build());

        // x position elements
        xPosButton = addDrawableChild(ButtonWidget.builder(Text.literal("⇄"), (b) -> {
        }).position(0 + gap, heightOffset + 2 * (elementHeight + gap) + gap).size(20, elementHeight).build());
        xPosField = addDrawableChild(new TextFieldWidget(client.textRenderer, 24 + gap, heightOffset + 2 * (elementHeight + gap) + gap, fieldWidth / 2 - 24 - gap / 2, elementHeight, Text.empty()));
        xPosField.setChangedListener(createSetter(x -> currentEdited.x = x, xPosField));

        yPosButton = addDrawableChild(ButtonWidget.builder(Text.literal("⇅"), (b) -> {
        }).position(0 + gap, heightOffset + 3 * (elementHeight + gap) + gap).size(20, elementHeight).build());
        yPosField = addDrawableChild(new TextFieldWidget(client.textRenderer, 24 + gap, heightOffset + 3 * (elementHeight + gap) + gap, fieldWidth / 2 - 24 - gap / 2, elementHeight, Text.empty()));
        yPosField.setChangedListener(createSetter(y -> currentEdited.y = y, yPosField));

        widthButton = addDrawableChild(ButtonWidget.builder(Text.literal("⇔"), (b) -> {
        }).position(fieldWidth / 2 + gap, heightOffset + 2 * (elementHeight + gap) + gap).size(20, elementHeight).build());
        widthField = addDrawableChild(new TextFieldWidget(client.textRenderer, fieldWidth / 2 + 24 - gap / 2 + gap, heightOffset + 2 * (elementHeight + gap) + gap, fieldWidth / 2 - 24 - gap / 2, elementHeight, Text.empty()));
        widthField.setChangedListener(createSetter(w -> currentEdited.width = (w / new ImageRequest(currentEdited.url).getTexture().getRight().getWidth()), widthField));

        heightButton = addDrawableChild(ButtonWidget.builder(Text.literal("⇕"), (b) -> {
        }).position(fieldWidth / 2 + gap, heightOffset + 3 * (elementHeight + gap) + gap).size(20, elementHeight).build());
        heightField = addDrawableChild(new TextFieldWidget(client.textRenderer, fieldWidth / 2 + 24 - gap / 2 + gap, heightOffset + 3 * (elementHeight + gap) + gap, fieldWidth / 2 - 24 - gap / 2, elementHeight, Text.empty()));
        heightField.setChangedListener(createSetter(h -> currentEdited.height = (h / new ImageRequest(currentEdited.url).getTexture().getRight().getHeight()), heightField));

        spinButton = addDrawableChild(ButtonWidget.builder(Text.literal("↻"), (b) -> {
        }).position(0 + gap, heightOffset + 4 * (elementHeight + gap) + gap).size(20, elementHeight).build());
        spinField = addDrawableChild(new TextFieldWidget(client.textRenderer, 24 + gap, heightOffset + 4 * (elementHeight + gap) + gap, fieldWidth - gap - 24, elementHeight, Text.empty()));
        spinField.setChangedListener(createSetter(h -> {
            currentEdited.rotation = 0;
            currentEdited.rotate(h);
        }, spinField));


        removeButton.visible = false;
        urlField.setVisible(false);
        xPosField.setVisible(false);
        xPosButton.visible = false;
        yPosField.setVisible(false);
        yPosButton.visible = false;
        widthField.setVisible(false);
        widthButton.visible = false;
        heightField.setVisible(false);
        heightButton.visible = false;
        spinField.setVisible(false);
        spinButton.visible = false;

    }

    private void updateFields() {
        List<Integer> pageErrors = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            var img_page = imaginebook_pages.get(i);
            if (img_page == null || img_page.size() == 0)
                continue;

            var used = getEncoded(imaginebook_pages.get(i)).length();
            var left = 1000 - pages.get(i).length();

            if (used > left) {
                pageErrors.add(i);
            }
        }
        if (pageErrors.size() > 0) {
            signing = false;
            Imaginebook.cancelledFinalize = true;
            error = Text.translatable("imaginebook.error.too_long_encoded", String.join(", ", pageErrors.stream().map(s -> String.valueOf(s + 1)).toArray(String[]::new)));
        } else {
            Imaginebook.cancelledFinalize = false;
            error = Text.empty();
        }


        if (currentEdited == null)
            return;

        var image = new ImageRequest(currentEdited.url);
        Image.ImageSize nativeImage = image.getTexture().getRight();
        urlField.setText(currentEdited.url);
        xPosField.setText((int) currentEdited.x() + "");
        yPosField.setText((int) currentEdited.y() + "");
        widthField.setText(currentEdited.width(nativeImage) + "");
        heightField.setText(currentEdited.height(nativeImage) + "");
        spinField.setText(currentEdited.rotation + "");

    }

    Consumer<String> createSetter(Consumer<Float> setter, TextFieldWidget fieldWidget) {
        return (value) -> {
            try {
                float number = Float.parseFloat(value);
                setter.accept(number);
                if ("0".equals(String.valueOf(Math.round(number)))) {
                    setter.accept(0f);
                } else if (!fieldWidget.getText().equals(String.valueOf(Math.round(number)))) {
                    fieldWidget.setText(String.valueOf(Math.round(number)));
                }

            } catch (NumberFormatException e) {

            }
        };
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    void mouseClicked(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        var images = imaginebook_pages.get(currentPage);
        if (signing)
            return;
        var buttons = new ButtonWidget[]{nextPageButton, previousPageButton, signButton, doneButton};
        for (var button : buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                button.onPress();
                cir.setReturnValue(true);
                return;
            }
        }
        for (var image : images) {
            if (isMouseOver(image, mouseX, mouseY)) {
                setCurrentEdited(image);
                draggedByMouse = image;
                cir.setReturnValue(true);
                return;
            }
        }

        if (isMouseOver(new ImageData("", (short) 0, (short) 0, (short) 192, (short) 192), mouseX, mouseY)) {
            setFocused(null);
        }
    }

    public ImageData draggedByMouse = null;
    public double bufferX, bufferY;

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0)
            return;
//        for (var image : imaginebook_pages.get(currentPage)) {
//            if (isMouseOver(image, mouseX, mouseY)) {
//                draggedByMouse = image;
//                break;
//            }
//        }
        if (draggedByMouse != null) {
            bufferX += deltaX;
            bufferY += deltaY;
            draggedByMouse.x += (int) bufferX;
            draggedByMouse.y += (int) bufferY;
            bufferX -= (int) bufferX;
            bufferY -= (int) bufferY;
            cir.setReturnValue(true);
            updateFields();
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggedByMouse != null) {
            updateFields();
            draggedByMouse = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!signing) {
            verticalAmount = Math.signum(verticalAmount);
            if (hasControlDown()) {
                verticalAmount *= 5;
            }
            if (hasShiftDown()) {
                verticalAmount *= 5;
            }
            if (hasAltDown()) {
                verticalAmount *= 5;
            }
            boolean pressed = false;
            if (currentEdited != null) {
                Image.ImageSize currentNativeImage = new ImageRequest(currentEdited.url).getTexture().getRight();

                if (xPosButton.isMouseOver(mouseX, mouseY) || xPosField.isMouseOver(mouseX, mouseY)) {
                    currentEdited.x += verticalAmount;
                    pressed = true;
                }
                if (yPosButton.isMouseOver(mouseX, mouseY) || yPosField.isMouseOver(mouseX, mouseY)) {
                    currentEdited.y += verticalAmount;
                    pressed = true;
                }
                if (widthButton.isMouseOver(mouseX, mouseY) || widthField.isMouseOver(mouseX, mouseY)) {
                    currentEdited.width += (float) (verticalAmount / currentNativeImage.getWidth());
                    pressed = true;
                }
                if (heightButton.isMouseOver(mouseX, mouseY) || heightField.isMouseOver(mouseX, mouseY)) {
                    currentEdited.height += (float) (verticalAmount / currentNativeImage.getHeight());
                    pressed = true;
                }
                if (spinButton.isMouseOver(mouseX, mouseY) || spinField.isMouseOver(mouseX, mouseY)) {
                    if (verticalAmount == 25)
                        verticalAmount = 15;
                    if (verticalAmount == 125)
                        verticalAmount = 90;
                    if (verticalAmount == -25)
                        verticalAmount = -15;
                    if (verticalAmount == -125)
                        verticalAmount = -90;

                    currentEdited.rotate((float) verticalAmount);
                    pressed = true;
                }
            }

            if (!pressed) {
                ImageData targetImage = null;
                for (ImageData image : imaginebook_pages.get(currentPage)) {
                    if (isMouseOver(image, mouseX, mouseY)) {
                        if (targetImage == null) {
                            targetImage = image;
                            break;
                        }
                    }
                }
                if (draggedByMouse != null)
                    targetImage = draggedByMouse;
                if (targetImage != null) {
                    Image.ImageSize targetNativeImage = new ImageRequest(targetImage.url).getTexture().getRight();
                    if (hasShiftDown() || hasAltDown()) {
                        verticalAmount /= 5;
                    }
                    double pow = Math.pow(1.00 + Math.abs(verticalAmount * 0.01), Math.signum(verticalAmount));

                    var min = Math.min(targetImage.width(targetNativeImage), targetImage.height(targetNativeImage));
                    if (Math.abs(min - min * pow) < 1) {
                        pow = (min + verticalAmount) / min;
                    }

                    if (!hasAltDown()) {
                        targetImage.width *= pow;
                    }
                    if (!hasShiftDown()) {
                        targetImage.height *= pow;
                    }

                }

            }
            updateFields();
            if (pressed) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                updateFields();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    Text lengthLeft;

    @Inject(method = "render", at = @At("TAIL"))
    void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var left = 1000 - pages.get(currentPage).length() - getEncoded(imaginebook_pages.get(currentPage)).length();
        lengthLeft = Text.translatable("imaginebook.gui.used", left);

        if (error != null) {
            context.drawCenteredTextWithShadow(client.textRenderer, error, width / 2, height - 20, VersionFunctions.ColorHelper.getArgb(255, 100, 100));
        }
        boolean editing = currentEdited != null;
        if (signing) {
            editing = false;
        }
        addButton.visible = !signing;
        removeButton.visible = editing;
        urlField.setVisible(editing);
        xPosField.setVisible(editing);
        xPosButton.visible = editing;
        yPosField.setVisible(editing);
        yPosButton.visible = editing;
        widthField.setVisible(editing);
        widthButton.visible = editing;
        heightField.setVisible(editing);
        heightButton.visible = editing;

        spinField.setVisible(editing);
        spinButton.visible = editing;

        if (signing) {
            return;
        }

        int bookX = this.width / 2 - 96;
        int bookY = 2;

        var combined = new ArrayList<ImageData>();
        combined.addAll(imaginebook_pages.get(currentPage));
        combined.addAll(imaginebook_safe_pages.get(currentPage));
        for (ImageData imageData : combined.reversed()) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();

            var image = new ImageRequest(imageData.url);
            Image.ImageSize nativeImage = image.getTexture().getRight();

            Imaginebook.renderImage(context, bookX, bookY, imageData, image, nativeImage);

        }

        var images = (imaginebook_pages.get(currentPage));
        if (images != null) {
            for (ImageData imageData : images) {
                if (isMouseOver(imageData, mouseX, mouseY)) {
                    var image = new ImageRequest(imageData.url);
                    Image.ImageSize nativeImage = image.getTexture().getRight();
                    context.fill((int) (bookX + imageData.x()), (int) (bookY + imageData.y()), (int) (bookX + imageData.x() + imageData.width(nativeImage)), (int) (bookY + imageData.y() + imageData.height(nativeImage)), 0x0fFFFFFF);
                    break;
                }
            }
        }

        nextPageButton.render(context, mouseX, mouseY, delta);
        previousPageButton.render(context, mouseX, mouseY, delta);
        signButton.render(context, mouseX, mouseY, delta);
        doneButton.render(context, mouseX, mouseY, delta);
        int i = (this.width - 192) / 2;
        int j = 2;
        int n = this.textRenderer.getWidth(this.pageIndicatorText);
        context.drawText(this.textRenderer, this.pageIndicatorText, i - n + 192 - 44, 18, 0x3F000000, false);
        var pageContent = this.getPageContent();

        for(var line : pageContent.lines) {
            context.drawText(this.textRenderer, line.text, line.x, line.y, 0x3F000000, false);
        }

        this.drawSelection(context, pageContent.selectionRectangles);
        this.drawCursor(context, pageContent.position, pageContent.atEnd);
        if (!signing) {
            context.drawTextWithShadow(client.textRenderer, lengthLeft, width - 4 - client.textRenderer.getWidth(lengthLeft), 4, 0x3F000000);
        }
    }


    public boolean isMouseOver(ImageData imageData, double mouseX, double mouseY) {
        int bookX = this.width / 2 - 96;
        int bookY = 2;

        var image = new ImageRequest(imageData.url);
        Image.ImageSize nativeImage = image.getTexture().getRight();

        double imageX1 = imageData.x() + bookX;
        double imageY1 = imageData.y() + bookY;
        double imageX2 = imageX1 + imageData.width(nativeImage);
        double imageY2 = imageY1 + imageData.height(nativeImage);

        double minX = Math.min(imageX1, imageX2);
        double maxX = Math.max(imageX1, imageX2);
        double minY = Math.min(imageY1, imageY2);
        double maxY = Math.max(imageY1, imageY2);

        return mouseX >= minX && mouseX < maxX && mouseY >= minY && mouseY < maxY;
    }


    @Inject(method = "finalizeBook", at = @At("HEAD"), cancellable = true)
    void finalizeBook(boolean sign, CallbackInfo ci) {
        if (!this.dirty) {
            this.dirty = true;
        }
        if (Imaginebook.cancelledFinalize) {
            ci.cancel();
            return;
        }
        for (int i = 0; i < pages.size(); i++) {
            var page = pages.get(i);
            var img_page = imaginebook_pages.get(i);
            if (img_page == null || img_page.size() == 0)
                continue;

            var bytes = getEncoded(img_page);
            var data = "\n" + bytes;
            page = page + Strings.repeat("\n", Imaginebook.LENGTH - page.length() - data.length()) + data;
            pages.set(i, page);
        }
    }

    private String getEncoded(List<ImageData> img_page) {
        return Base64.getEncoder().encodeToString(ImageSerializer.serializeImageMetadata(img_page.stream().filter(s -> s.width != 0 && s.height != 0).toList()));
    }
}
