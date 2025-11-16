package io.github.jumperonjava.imaginebook.mixin.accessor;

import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.util.math.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if < 1.21.6 {
@Mixin(BookEditScreen.PageContent.class)
public interface PageContentAccessor {
    @Accessor("lines")
    BookEditScreen.Line[] getLines();

    @Accessor("selectionRectangles")
    Rect2i[] getSelectionRectangles();

    @Accessor("position")
    BookEditScreen.Position getPosition();

    @Accessor("atEnd")
    boolean isAtEnd();
}
//?} else {
/*@Mixin(BookEditScreen.class)
public interface PageContentAccessor {}
*///?}
