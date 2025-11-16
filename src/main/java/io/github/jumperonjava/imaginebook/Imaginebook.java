package io.github.jumperonjava.imaginebook;

import io.github.jumperonjava.imaginebook.resolvers.AsyncImageDownloader;
import io.github.jumperonjava.imaginebook.resolvers.Resolver;
import io.github.jumperonjava.imaginebook.resolvers.TextureResolver;
import io.github.jumperonjava.imaginebook.resolvers.UrlResolver;



/*? if fabric {*/
import net.fabricmc.loader.api.FabricLoader;
/*?} elif neoforge {*/
/*import net.neoforged.fml.loading.FMLPaths;
*//*?} elif forge {*/
/*import net.minecraftforge.fml.loading.FMLPaths;
*//*?}*/

//? if >= 1.21.5 {
/*import net.minecraft.client.gl.RenderPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import static net.minecraft.client.gl.RenderPipelines.POSITION_TEX_COLOR_SNIPPET;
*///?}

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public final class Imaginebook {
    public static final String MOD_ID = "imaginebook";
    public static Logger LOGGER = LoggerFactory.getLogger("ImagineBook");
    public static final String TEST_BALLER = "https://i.kym-cdn.com/photos/images/original/002/461/188/20d.png";

    //? if >=1.21.6 {
    /*public static RenderPipeline GUI_TEXTURED_NOCULL;
    *///?}
    public static void init() {
        createImagineBookFolder();
        //? if >=1.21.6 {
            /*Imaginebook.GUI_TEXTURED_NOCULL = RenderPipelines.register(RenderPipeline.builder(POSITION_TEX_COLOR_SNIPPET).withLocation("pipeline/imaginebook_gui_textured_nocull").withCull(false).build());
        *///?}
    }


    public static int LENGTH = 1023;
    public static boolean cancelledFinalize = false;


    public static String fixImgurLink(String link) {
        if (link.startsWith("https://imgur.com/")) {
            String imageId = link.substring("https://imgur.com/".length());
            return "https://i.imgur.com/" + imageId + ".png";
        }
        return link;
    }

    public static Path getCachePath(){
        //? if fabric {
        return FabricLoader.getInstance().getGameDir().resolve("imaginebook");
        //?} elif neoforge || forge {
        /*return FMLPaths.GAMEDIR.get().resolve("imaginebook");
         *///?}
    }
    public static Path getConfigFile(){
        //? if fabric {
        return FabricLoader.getInstance().getConfigDir().resolve("imaginebook.json");
        //?} elif neoforge || forge {
        /*return FMLPaths.CONFIGDIR.get().resolve("imaginebook.json");
         *///?}
    }

    private static Map<String,Resolver> resolvers = new HashMap<>(Map.of(
            "http",UrlResolver.INSTANCE,
            "https",UrlResolver.INSTANCE_S,
            "res", TextureResolver.INSTANCE
    ));
    public static void addResolver(String id, Resolver resolver) {
        resolvers.put(id, resolver);
    }
    public static Resolver getResolver(String type) {
        if(resolvers.containsKey(type))
            return resolvers.get(type);
        return path -> AsyncImageDownloader.ERROR_IMAGE;
    }

    public static boolean resolverExists(String resolver) {
        return resolvers.containsKey(resolver);
    }

    public void cleanFiles(){

    }

    public static void createImagineBookFolder() {
        Path imagineBookPath = getCachePath();

        try {
            if (!Files.exists(imagineBookPath)) {
                Files.createDirectories(imagineBookPath);
                LOGGER.info("imaginebook folder created at: " + imagineBookPath.toString());
            } else {
                //LOGGER.info("imaginebook folder already exists at: " + imagineBookPath.toString());
            }
        } catch (IOException e) {
            LOGGER.info("Failed to create imaginebook folder: " + e.getMessage());
        }
    }


}
