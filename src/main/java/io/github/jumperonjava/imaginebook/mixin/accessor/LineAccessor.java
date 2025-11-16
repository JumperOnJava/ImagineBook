package io.github.jumperonjava.imaginebook.mixin.accessor;

import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if < 1.21.6 {
@Mixin(BookEditScreen.Line.class)
public interface LineAccessor {
    @Accessor("text")
    Text getText();

    @Accessor("x")
    int getX();

    @Accessor("y")
    int getY();
}
//?} else {
/*@Mixin(BookEditScreen.class)
public interface LineAccessor {}
*///?}