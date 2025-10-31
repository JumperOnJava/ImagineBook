package io.github.jumperonjava.imaginebook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.jumperonjava.imaginebook.*;
import io.github.jumperonjava.imaginebook.image.Image;
import io.github.jumperonjava.imaginebook.image.ImageData;
import io.github.jumperonjava.imaginebook.mixin.accessor.LineAccessor;
import io.github.jumperonjava.imaginebook.mixin.accessor.PageContentAccessor;
import io.github.jumperonjava.imaginebook.mixin.accessor.ScreenAccessor;
import io.github.jumperonjava.imaginebook.util.DeletedImageData;
import io.github.jumperonjava.imaginebook.util.VersionFunctions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >= 1.21.3
import net.minecraft.component.type.WritableBookContentComponent;

//? if >= 1.21.6 {
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.screen.ingame.BookSigningScreen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
//?}

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.jumperonjava.imaginebook.image.ImageData.isMouseOverImage;

@Mixin(BookEditScreen.class)
public abstract class NewBookEditScreenMixin extends Screen {

    @Shadow
    private int currentPage;

    @Shadow
    @Final
    private List<String> pages;

    @Shadow
    private PageTurnWidget nextPageButton;
    @Shadow
    private PageTurnWidget previousPageButton;
    @Shadow
    private Text pageIndicatorText;

    //? if >= 1.21.6 {
    @Shadow
    private EditBoxWidget editBox;
    //?}

    //? if < 1.21.6 {
    /*@Shadow
    private boolean dirty;
    @Shadow
    private boolean signing;
    @Shadow
    private ButtonWidget signButton;
    @Shadow
    private ButtonWidget doneButton;
    @Shadow
    @Final
    private SelectionManager currentPageSelectionManager;

    @Shadow
    protected abstract BookEditScreen.PageContent getPageContent();

    @Shadow
    protected abstract void drawSelection(DrawContext context, Rect2i[] selectionRectangles);

    @Shadow
    protected abstract void drawCursor(DrawContext context, BookEditScreen.Position position, boolean atEnd);

    @Shadow
    protected abstract void invalidatePageContent();
    *///?}

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

    protected NewBookEditScreenMixin(Text title) {
        super(title);
    }

    List<List<ImageData>> display_pages = new ArrayList<>();


    @Inject(method = "<init>", at = @At("TAIL"))
        //? if >= 1.21.3 {
    void construct(PlayerEntity player, ItemStack stack, Hand hand, WritableBookContentComponent writableBookContent, CallbackInfo ci) {
        //?} else {
        /*void construct(PlayerEntity player, ItemStack itemStack, Hand hand, CallbackInfo ci) {
         *///?}
        for (int i = 0; i < 250; i++) {
            display_pages.add(new ArrayList<>());
        }
        updateDisplayImages();
    }

    void updateDisplayImages() {
        for (int i = 0; i < pages.size(); i++) {
            var page = pages.get(i);
            display_pages.set(i, ImageSerializer.parseSafeModeImages(page));
        }
        currentEdited = Math.min(currentEdited, display_pages.get(currentPage).size() - 1);
    }

    private String getCurrentPageContent() {
        if (currentPage >= pages.size()) {
            return "";
        }
        return pages.get(currentPage);
    }

    private void setCurrentPageContent(String content) {
        pages.set(currentPage, content);
        //? if >= 1.21.7 {
        this.editBox.setText(content, true);
        //?} else if >= 1.21.6 {
        /*this.editBox.setText(content);
        *///?} else {


        /*this.dirty = true;
        var start = currentPageSelectionManager.getSelectionStart();
        var end = currentPageSelectionManager.getSelectionEnd();
        start = MathHelper.clamp(start, 0, pages.size() - 1);
        end = MathHelper.clamp(end, 0, pages.size() - 1);
        currentPageSelectionManager.setSelection(start, end);

        invalidatePageContent();
        *///?}
    }

    /*? if <1.21.6 {*/
    /*@Inject(method = "changePage", at = @At("HEAD"))
    void onChangePage(CallbackInfo ci) {
        currentEdited = -1;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/BookEditScreen;setFocused(Lnet/minecraft/client/gui/Element;)V"))
    void ignoreSetFocused(BookEditScreen instance, Element element, Operation<Void> original) {}

    @Inject(method = "updateButtons", at = @At("HEAD"), cancellable = true)
    void updateButtons(CallbackInfo ci) {
        if (Imaginebook.cancelledFinalize) {
            this.signing = false;
        }
    }

    @Inject(method = "invalidatePageContent", at = @At("HEAD"))
    void updateSafeModeImages(CallbackInfo ci) {
        updateDisplayImages();
    }
    *//*?}*/

    int currentEdited = -1;

    private void setCurrentEdited(int imageId) {
        currentEdited = imageId;
        if (currentEdited == -1) {
            setFocused(null);
        } else {
            updateFields();
        }
    }

    //current page get
    @Nullable
    ImageData getCurrentPageImage(int id) {
        if (id == -1)
            return null;
        if (display_pages.get(currentPage).isEmpty())
            return null;
        return display_pages.get(currentPage).get(id);
    }

    void addCurrentPageImage(ImageData image) {
        setCurrentPageContent(getCurrentPageContent() + image.bookString());
        updateDisplayImages();
    }

    @Override
    protected void switchFocus(GuiNavigationPath path) {
        //forge eats ass and doesn't apply access widener
        //? if forge {
        /*try {
            java.lang.reflect.Method method = this.getClass().getDeclaredMethod("blur");
            method.setAccessible(true);
            method.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        *///?} else {
        ((ScreenAccessor) this).invokeBlur();
        //?}
    }

    @Inject(method = "init", at = @At("HEAD"))
    void init(CallbackInfo ci) {
        int elementHeight = 20;
        int gap = 4;
        int heightOffset = height - 50;
        int columnSize = 100 - gap;
        int smallFieldWidth = columnSize - 24;


        int[] row = new int[10];
        int[] column = new int[10];
        int[] fieldColumn = new int[10];
        for (int i = 0; i < 10; ++i) {
            row[i] = i * (elementHeight + gap) + gap;
        }
        for (int i = 0; i < 10; ++i) {
            column[i] = i * (columnSize + gap) + gap;
        }
        for (int i = 0; i < 10; ++i) {
            fieldColumn[i] = column[i] + 24;
        }

        int urlFieldX = column[0];
        int urlFieldY = row[0];
        int urlFieldWidth = (columnSize) * 2 + gap;

        int addButtonX = column[0];
        int addButtonY = row[1];
        int addButtonWidth = columnSize;

        int removeButtonX = column[1];
        int removeButtonY = row[1];
        int removeButtonWidth = columnSize;

        int xPosButtonX = column[2];
        int xPosButtonY = row[0];
        int xPosFieldX = fieldColumn[2];
        int xPosFieldY = row[0];

        int yPosButtonX = column[2];
        int yPosButtonY = row[1];
        int yPosFieldX = fieldColumn[2];
        int yPosFieldY = row[1];

        int widthButtonX = column[3];
        int widthButtonY = row[0];
        int widthFieldX = fieldColumn[3];
        int widthFieldY = row[0];

        int heightButtonX = column[3];
        int heightButtonY = row[1];
        int heightFieldX = fieldColumn[3];
        int heightFieldY = row[1];

        int spinButtonX = column[4];
        int spinButtonY = row[1];
        int spinFieldX = fieldColumn[4];
        int spinFieldY = row[1];

        urlFieldY += heightOffset;
        addButtonY += heightOffset;
        removeButtonY += heightOffset;
        xPosButtonY += heightOffset;
        xPosFieldY += heightOffset;
        yPosButtonY += heightOffset;
        yPosFieldY += heightOffset;
        widthButtonY += heightOffset;
        widthFieldY += heightOffset;
        heightButtonY += heightOffset;
        heightFieldY += heightOffset;
        spinButtonY += heightOffset;
        spinFieldY += heightOffset;


        urlField = addDrawableChild(new TextFieldWidget(client.textRenderer, urlFieldX, urlFieldY, urlFieldWidth, elementHeight, Text.translatable("imaginebook.gui.urlhere")));
        urlField.setMaxLength(256);
        urlField.setChangedListener((url) -> {
            var getCurrentEdited = getCurrentEdited();
            if(getCurrentEdited == null){
                return;
            }
            if (url.equals(getCurrentEdited.getUrl())) return;
            if (url.length() > 255) {
                urlField.setText(I18n.translate("imaginebook.error.too_long"));
                return;
            }
            mutateImage(currentEdited, (imageData -> {
                imageData.heightFraction = 1;
                imageData.widthFraction = 1;
                imageData.setUrl(Imaginebook.fixImgurLink(url));
                return imageData;
            }));
        });

        addButton = addDrawableChild(ButtonWidget.builder(Text.translatable("imaginebook.gui.add"), (b) -> {
            var newImage = new ImageData("", (short) 0, (short) 0, 1, 1);
            addCurrentPageImage(newImage);
            setCurrentEdited(display_pages.get(currentPage).size() - 1);
        }).position(addButtonX, addButtonY).size(addButtonWidth, elementHeight).build());

        removeButton = addDrawableChild(ButtonWidget.builder(Text.translatable("imaginebook.gui.remove"), (b) -> {
            mutateImage(currentEdited, (imageData -> {
                setCurrentEdited(-1);
                return new DeletedImageData();
            }));
        }).position(removeButtonX, removeButtonY).size(removeButtonWidth, elementHeight).build());

        xPosButton = addDrawableChild(ButtonWidget.builder(Text.literal("⇄"), (b) -> {
        }).position(xPosButtonX, xPosButtonY).size(20, elementHeight).build());
        xPosField = addDrawableChild(new TextFieldWidget(client.textRenderer, xPosFieldX, xPosFieldY, smallFieldWidth, elementHeight, Text.empty()));
        xPosField.setChangedListener(createSetter(x -> {
            mutateImage(currentEdited, (imageData -> {
                imageData.x = x;
                return imageData;
            }));
        }, xPosField));

        yPosButton = addDrawableChild(ButtonWidget.builder(Text.literal("⇅"), (b) -> {
        }).position(yPosButtonX, yPosButtonY).size(20, elementHeight).build());
        yPosField = addDrawableChild(new TextFieldWidget(client.textRenderer, yPosFieldX, yPosFieldY, smallFieldWidth, elementHeight, Text.empty()));
        yPosField.setChangedListener(createSetter(y -> {
            mutateImage(currentEdited, (imageData -> {
                imageData.y = y;
                return imageData;
            }));
        }, yPosField));

        widthButton = addDrawableChild(ButtonWidget.builder(Text.literal("⇔"), (b) -> {
        }).position(widthButtonX, widthButtonY).size(20, elementHeight).build());
        widthField = addDrawableChild(new TextFieldWidget(client.textRenderer, widthFieldX, widthFieldY, smallFieldWidth, elementHeight, Text.empty()));
        widthField.setChangedListener(createSetter(w -> {
            mutateImage(currentEdited, (imageData -> {
                imageData.widthFraction = (float) Math.round(w) / 100;
                return imageData;
            }));
        }, widthField));

        heightButton = addDrawableChild(ButtonWidget.builder(Text.literal("⇕"), (b) -> {
        }).position(heightButtonX, heightButtonY).size(20, elementHeight).build());
        heightField = addDrawableChild(new TextFieldWidget(client.textRenderer, heightFieldX, heightFieldY, smallFieldWidth, elementHeight, Text.empty()));
        heightField.setChangedListener(createSetter(h -> {
            mutateImage(currentEdited, (imageData -> {
                imageData.heightFraction = (float) Math.round(h) / 100;
                return imageData;
            }));
        }, heightField));

        spinButton = addDrawableChild(ButtonWidget.builder(Text.literal("↻"), (b) -> {
        }).position(spinButtonX, spinButtonY).size(20, elementHeight).build());
        spinField = addDrawableChild(new TextFieldWidget(client.textRenderer, spinFieldX, spinFieldY, smallFieldWidth, elementHeight, Text.empty()));
        spinField.setChangedListener(createSetter(h -> {
            mutateImage(currentEdited, (imageData -> {
                imageData.rotation = 0;
                imageData.rotate(h);
                return imageData;
            }));
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

        updateFields();
    }

    private ImageData getCurrentEdited() {
        return getCurrentPageImage(currentEdited);
    }

    private void updateFields() {
        List<Integer> pageErrors = new ArrayList<>();
        if (pageErrors.size() > 0) {
            //? if < 1.21.6
            /*signing = false;*/
            Imaginebook.cancelledFinalize = true;
            error = Text.translatable("imaginebook.error.too_long_encoded", String.join(", ", pageErrors.stream().map(s -> String.valueOf(s + 1)).toArray(String[]::new)));
        } else {
            Imaginebook.cancelledFinalize = false;
            error = Text.empty();
        }

        if (currentEdited == -1)
            return;

        var currentPageImage = getCurrentEdited();

        if (currentPageImage != null) {
            urlField.setText(currentPageImage.getUrl());

            String xpos = String.valueOf((int) currentPageImage.x());
            xPosField.setText(xpos);

            String ypos = String.valueOf((int) currentPageImage.y());
            yPosField.setText(ypos);

            String width = String.valueOf(currentPageImage.widthFraction() * 100);
            widthField.setText(width);

            String height = String.valueOf(currentPageImage.heightFraction() * 100);
            heightField.setText(height);

            spinField.setText(currentPageImage.rotation + "");
        }
    }

    Consumer<String> createSetter(Consumer<Float> setter, TextFieldWidget fieldWidget) {
        return (value) -> {
            try {
                float number = Float.parseFloat(value);
                if (String.valueOf(Math.round(number)).equals("0")) {
                    setter.accept(0f);
                } else if (!fieldWidget.getText().equals(String.valueOf(Math.round(number)))) {
                    fieldWidget.setText(String.valueOf(Math.round(number)));
                } else {
                    setter.accept(number);
                }
            } catch (NumberFormatException e) {

            }
        };
    }

    //? if >= 1.21.9 {
    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
    //?} else if >= 1.21.6 {
    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    *///?} else {
    /*@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    void mouseClicked(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        *///?}
        var images = display_pages.get(currentPage);
        //? if >= 1.21.6 {
        boolean inSigningScreen = client.currentScreen instanceof BookSigningScreen;
        if (inSigningScreen)
            return false;
        var buttons = new ButtonWidget[]{nextPageButton, previousPageButton};
        //?} else {
        /*if (signing)
            return;
        var buttons = new ButtonWidget[]{nextPageButton, previousPageButton, signButton, doneButton};
        *///?}
        for (var button : buttons) {
            //? if >= 1.21.9 {
            if (button.isMouseOver(click.x(), click.y())) {
                button.onPress(click);
            //?} else {
            /*if (button.isMouseOver(mouseX, mouseY)) {
                button.onPress();
            *///?}
                //? if >= 1.21.6 {
                return true;
                //?} else {
                /*cir.setReturnValue(true);
                return;
                *///?}
            }
        }
        int i = 0;
        for (var image : images) {
            //? if >= 1.21.9 {
            if (isMouseOver(image, click.x(), click.y())) {
            //?} else {
            /*if (isMouseOver(image, mouseX, mouseY)) {
            *///?}
                setCurrentEdited(i);
                draggedByMouse = i;
                //? if >= 1.21.6 {
                return true;
                //?} else {
                /*cir.setReturnValue(true);
                return;
                *///?}
            }
            i++;
        }

        //Workaround so if you click on book area it unfocuses elements like text fields
        //? if >= 1.21.9 {
        if (isMouseOver(new ImageData("", (short) 0, (short) 0, (short) 192, (short) 192), click.x(), click.y())) {
        //?} else {
        /*if (isMouseOver(new ImageData("", (short) 0, (short) 0, (short) 192, (short) 192), mouseX, mouseY)) {
        *///?}
            setFocused(null);
        }

        //? if >= 1.21.9 {
        return super.mouseClicked(click, doubled);
        //?} else if >= 1.21.6 {
        /*return super.mouseClicked(mouseX, mouseY, mouseButton);
        *///?}
    }

    public int draggedByMouse = -1;
    public double bufferX, bufferY;

    //? if >= 1.21.9 {
    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
    //?} else if >= 1.21.6 {
    /*@Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    *///?} else {
    /*@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
    *///?}
        //? if >= 1.21.9 {
        if (click.button() != 0)
        //?} else {
        /*if (button != 0)
        *///?}
            //? if >= 1.21.6 {
            return false;
        //?} else {
        /*return;
         *///?}
        if (draggedByMouse != -1) {
            bufferX += deltaX;
            bufferY += deltaY;
            mutateImage(draggedByMouse, (imageData -> {
                imageData.x += (int) bufferX;
                imageData.y += (int) bufferY;
                return imageData;
            }));
            bufferX -= (int) bufferX;
            bufferY -= (int) bufferY;
            //? if >= 1.21.6 {
            updateFields();
            /*return true;
             *///?} else {
            /*cir.setReturnValue(true);
            updateFields();
            *///?}
        }
        //? if >= 1.21.9 {
        return super.mouseDragged(click, deltaX, deltaY);
        //?} else if >= 1.21.6 {
        /*return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        *///?}
    }

    //? if >= 1.21.9 {
    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
    //?} else {
    /*@Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        *///?}
        if (draggedByMouse != -1) {
            updateFields();
            draggedByMouse = -1;
        }
        //? if >= 1.21.9 {
        return super.mouseReleased(click);
        //?} else {
        /*return super.mouseReleased(mouseX, mouseY, button);
        *///?}
    }

    void mutateImage(int id, Function<ImageData, ImageData> function) {
        if (id == -1)
            return;
        getCurrentPageImage(id);
        var original = new ImageData(getCurrentPageImage(id));
        var modifiedImage = function.apply(original);
        Pattern pattern = Pattern.compile("\\[.*?\\]");

        Matcher matcher = pattern.matcher(getCurrentPageContent());

        List<ImageData> images = new ArrayList<>();
        int index = 0;

        var newPage = getCurrentPageContent();
        while (matcher.find()) {
            var start = matcher.start();
            var end = matcher.end();

            if (index == id) {
                var buf = new StringBuffer(newPage).delete(start, end).insert(start, modifiedImage.bookString());
                newPage = buf.toString();
                break;
            }
            index++;
        }
        setCurrentPageContent(newPage);
        updateDisplayImages();
    }

    @Override
    //? if < 1.20.4 {
    /*public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
     *///?} else {
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        //?}
        //? if >= 1.21.6
        boolean inSigningScreen = client.currentScreen instanceof BookSigningScreen;
        if (/*? if <1.21.6 {*//*!signing*//*?} else {*/ !inSigningScreen /*?}*/) {
            verticalAmount = Math.signum(verticalAmount);
            //? if >= 1.21.9 {
            if (client.isCtrlPressed()) {
            //?} else {
            /*if (hasControlDown()) {
            *///?}
                verticalAmount *= 5;
            }
            //? if >= 1.21.9 {
            if (client.isShiftPressed()) {
            //?} else {
            /*if (hasShiftDown()) {
            *///?}
                verticalAmount *= 5;
            }
            //? if >= 1.21.9 {
            if (client.isAltPressed()) {
            //?} else {
            /*if (hasAltDown()) {
            *///?}
                verticalAmount *= 5;
            }
            boolean pressed = false;
            if (getCurrentEdited() != null) {
                final double finalVerticalAmount = verticalAmount;
                Image.ImageSize currentNativeImage = getCurrentEdited().getImage().getSize();
                if (xPosButton.isMouseOver(mouseX, mouseY) || xPosField.isMouseOver(mouseX, mouseY)) {
                    mutateImage(currentEdited, (imageData -> {
                        imageData.x += (float) finalVerticalAmount;
                        return imageData;
                    }));
                    pressed = true;
                }
                if (yPosButton.isMouseOver(mouseX, mouseY) || yPosField.isMouseOver(mouseX, mouseY)) {
                    mutateImage(currentEdited, (imageData -> {
                        imageData.y += (float) finalVerticalAmount;
                        return imageData;
                    }));
                    pressed = true;
                }
                if (widthButton.isMouseOver(mouseX, mouseY) || widthField.isMouseOver(mouseX, mouseY)) {
                    mutateImage(currentEdited, (imageData -> {
                        imageData.widthFraction += (float) (finalVerticalAmount / currentNativeImage.getWidth());
                        return imageData;
                    }));
                    pressed = true;
                }
                if (heightButton.isMouseOver(mouseX, mouseY) || heightField.isMouseOver(mouseX, mouseY)) {
                    mutateImage(currentEdited, (imageData -> {
                        imageData.heightFraction += (float) (finalVerticalAmount / currentNativeImage.getHeight());
                        return imageData;
                    }));
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

                    mutateImage(currentEdited, (imageData -> {
                        imageData.rotate((float) finalVerticalAmount);
                        return imageData;
                    }));
                    pressed = true;
                }
            }

            if (!pressed) {
                int targetImage1 = -1;
                int i = 0;
                for (ImageData image : display_pages.get(currentPage)) {
                    if (isMouseOver(image, mouseX, mouseY)) {
                        if (targetImage1 == -1) {
                            targetImage1 = i;
                            break;
                        }
                    }
                    i++;
                }
                if (draggedByMouse != -1)
                    targetImage1 = draggedByMouse;

                final double finalVerticalAmount = verticalAmount;
                mutateImage(targetImage1, targetImage -> {
                    var localVerticalAmount = finalVerticalAmount;
                    if (targetImage != null) {
                        //? if >= 1.21.9 {
                        if (client.isShiftPressed() || client.isAltPressed()) {
                        //?} else {
                        /*if (hasShiftDown() || hasAltDown()) {
                        *///?}
                            localVerticalAmount /= 5;
                        }
                        double pow = Math.pow(1.00 + Math.abs(localVerticalAmount * 0.01), Math.signum(localVerticalAmount));

                        var min = Math.min(targetImage.renderWidth(), targetImage.renderHeight());
                        if (Math.abs(min - min * pow) < 1) {
                            pow = (min + localVerticalAmount) / min;
                        }

                        //? if >= 1.21.9 {
                        if (!client.isAltPressed()) {
                        //?} else {
                        /*if (!hasAltDown()) {
                        *///?}
                            targetImage.widthFraction *= pow;
                        }
                        //? if >= 1.21.9 {
                        if (!client.isShiftPressed()) {
                        //?} else {
                        /*if (!hasShiftDown()) {
                        *///?}
                            targetImage.heightFraction *= pow;
                        }

                    }
                    return targetImage;
                });


            }
            updateFields();
            if (pressed) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                updateFields();
                return true;
            }
        }
        //? if < 1.20.4 {
        /*return super.mouseScrolled(mouseX, mouseY, verticalAmount);
         *///?} else {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        //?}
    }

    @Inject(method = "render", at = @At("TAIL"))
    void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        currentPage = MathHelper.clamp(currentPage, -1, display_pages.size() - 1);

        /*? if <1.21.6 {*/
        /*doneButton.active = ((PageContentAccessor) this.getPageContent()).getLines().length <= 14;
        signButton.active = ((PageContentAccessor) this.getPageContent()).getLines().length <= 14;
        *//*?}*/

        if (error != null) {
            context.drawCenteredTextWithShadow(client.textRenderer, error, width / 2, height - 20, VersionFunctions.ColorHelper.getArgb(255, 255, 100, 100));
        }
        boolean editing = currentEdited != -1;
        //? if >= 1.21.6
        boolean inSigningScreen = client.currentScreen instanceof BookSigningScreen;
        if (/*? if <1.21.6 {*//*signing*//*?} else {*/ inSigningScreen /*?}*/) {
            editing = false;
        }
        addButton.visible = /*? if <1.21.6 {*//*!signing*//*?} else {*/ !inSigningScreen /*?}*/;
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

        if (/*? if <1.21.6 {*//*signing*//*?} else {*/ inSigningScreen /*?}*/) {
            return;
        }
        int bookX = this.width / 2 - 96;
        int bookY = 2;

        var combined = new ArrayList<ImageData>();
        combined.addAll(display_pages.get(currentPage));
        for (ImageData imageData : VersionFunctions.reversed(combined)) {
            //? if < 1.21.5 {
            /*RenderSystem.disableCull();
            RenderSystem.enableBlend();
            *///?}

            imageData.renderImage(context, bookX, bookY);

            //? if < 1.21.5 {
            /*RenderSystem.disableBlend();
            RenderSystem.enableCull();
            *///?}
        }

        var images = (display_pages.get(currentPage));
        if (images != null) {
            for (ImageData imageData : images) {
                if (isMouseOver(imageData, mouseX, mouseY)) {
                    context.fill((int) (bookX + imageData.x()), (int) (bookY + imageData.y()), (int) (bookX + imageData.x() + imageData.renderWidth()), (int) (bookY + imageData.y() + imageData.renderHeight()), 0x0fFFFFFF);
                    break;
                }
            }
        }

        List.of(urlField, addButton, removeButton, widthButton, widthField, heightButton, heightField, xPosButton, xPosField, yPosButton, yPosField, nextPageButton, previousPageButton, /*? if <1.21.6 {*//*signButton, doneButton,*//*?}*/spinButton, spinField).forEach(e -> e.render(context, mouseX, mouseY, delta));

        int i = (this.width - 192) / 2;
        int j = 2;
        int n = this.textRenderer.getWidth(this.pageIndicatorText);
        context.drawText(this.textRenderer, this.pageIndicatorText, i - n + 192 - 44, 18, 0x3F000000, false);

        /*? if <1.21.6 {*/
        /*for (var line : ((PageContentAccessor) this.getPageContent()).getLines()) {
            context.drawText(this.textRenderer, ((LineAccessor) line).getText(), ((LineAccessor) line).getX(), ((LineAccessor) line).getY(), 0x3F000000, false);
        }

        this.drawSelection(context, ((PageContentAccessor) this.getPageContent()).getSelectionRectangles());
        this.drawCursor(context, ((PageContentAccessor) this.getPageContent()).getPosition(), ((PageContentAccessor) this.getPageContent()).isAtEnd());

        if (((PageContentAccessor) this.getPageContent()).getLines().length > 14) {
            context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("imaginebook.error.too_much_lines"), width / 2, 222, VersionFunctions.ColorHelper.getArgb(255, 255, 100, 100));
        }
        *///?} else {
        int contentAreaX = (this.width - BookEditScreen.MAX_TEXT_WIDTH) / 2 - 4;
        int contentAreaY = 32;

        List<OrderedText> wrappedLines = this.textRenderer.wrapLines(StringVisitable.plain(getCurrentPageContent()), BookEditScreen.MAX_TEXT_WIDTH);

        int currentLineY = contentAreaY;
        int lineHeight = this.textRenderer.fontHeight;

        for (OrderedText line : wrappedLines) {
            int lineDrawY = currentLineY;

            context.drawText(this.textRenderer, line, contentAreaX, lineDrawY, 0x3F000000, false);

            currentLineY += lineHeight;
        }
        //?}
    }


    public boolean isMouseOver(ImageData imageData, double mouseX, double mouseY) {
        return isMouseOverImage(imageData, mouseX, mouseY, this.width / 2);
    }


    @Inject(method = "finalizeBook", at = @At("HEAD"), cancellable = true)
    void finalizeBook( /*? if <1.21.6 {*//*boolean sign,*//*?}*/ CallbackInfo ci) {
        /*? if <1.21.6 {*/
        /*if (!this.dirty) {
            this.dirty = true;
        }
        *//*?}*/
        if (Imaginebook.cancelledFinalize) {
            ci.cancel();
            return;
        }
    }
}
