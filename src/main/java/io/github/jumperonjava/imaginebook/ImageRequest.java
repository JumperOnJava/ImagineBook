package io.github.jumperonjava.imaginebook;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;


/**
 * To future coders
 * If you see any weird stuff like TextureMap field please
 * beware that I've made most of this class logic around 2
 * years ago. I'm too lazy to rewrite this at this moment,
 * and it works good enough
 */
public class ImageRequest {
    public static Identifier EMPTY_TEXTURE = Identifier.of("imaginebook","textures/not_loaded.png");
    public static Image EMPTY_IMAGE = new Image(EMPTY_TEXTURE,new Image.ImageSize(128,128),"");

    public static int maxDownloadAttepts = 5;

    public static int currentDownloads = 0;

    public static int maxParralelDownloads=4;

    private static HashMap<Integer, TextureRequestInfo> TextureMap = new HashMap<>();

    private final String link;

    public ImageRequest(String link) {
        this.link = link;
    }

    private Path getFile() {
        return FabricLoader.getInstance().getGameDir().resolve("imaginebook").resolve(getIdentifierString());
    }

    private String getIdentifierString() {
        return String.valueOf(link.hashCode());
    }

    private Identifier getIdentifier() {
        return Identifier.of(Imaginebook.MOD_ID, getIdentifierString());
    }

    private String getDownloadLink() {
        return this.link;
    }

    public Image getTexture() {

        var info = TextureMap.containsKey(hashCode()) ? TextureMap.get(hashCode()) : new TextureRequestInfo();
        switch (info.status)
        {
            case INIT -> {
                info.status =
                    Files.exists(getFile()) ?
                            TextureRequestInfo.DownloadStatus.DOWNLOADED_NOT_REGISTERED :
                            TextureRequestInfo.DownloadStatus.NOT_DOWNLOADED;
                }
            case NOT_DOWNLOADED -> {
                if(currentDownloads>maxParralelDownloads)
                    break;
                new Thread(() -> {
                    currentDownloads++;
                    info.status= TextureRequestInfo.DownloadStatus.DOWNLOADING;
                    try {
                        InputStream in = new URL(getDownloadLink()).openStream();
                        Files.createDirectories(getFile().getParent());
                        Files.copy(in, getFile(), StandardCopyOption.REPLACE_EXISTING);
                        info.status = TextureRequestInfo.DownloadStatus.DOWNLOADED_NOT_REGISTERED;
                    } catch (Exception e) {
                        info.status = TextureRequestInfo.DownloadStatus.DOWNLOAD_ERROR;
                    }
                    currentDownloads--;
                }).start();
            }
            case DOWNLOADING -> {
                LoggerFactory.getLogger("abc").info(String.format("Downloading %s",link));
            }
            case DOWNLOAD_ERROR ->
            {
                if(info.attempt<maxDownloadAttepts){
                    info.status= TextureRequestInfo.DownloadStatus.NOT_DOWNLOADED;
                    info.attempt++;
                }
            }
            case DOWNLOADED_NOT_REGISTERED -> {
                if(!Files.exists(getFile()))
                {
                    info.status = TextureRequestInfo.DownloadStatus.NOT_DOWNLOADED;
                    break;
                }
                info.identifier = getIdentifier();
                try {
                    info.registered = registerTexture(this.getFile().toFile(), info.identifier,link);
                }
                catch(Exception e)
                {
                    break;
                }
                info.status = TextureRequestInfo.DownloadStatus.READY;
            }
            case READY -> {
                return TextureMap.get(hashCode()).registered;
            }
        }

        TextureMap.put(hashCode(), info);
        return new Image(EMPTY_TEXTURE,new Image.ImageSize(100,100), link);

    }

    @Override
    public int hashCode() {
        return getIdentifierString().hashCode();
    }
    private class TextureRequestInfo
    {
        public int attempt;
        public  Image registered;

        public TextureRequestInfo()
        {
            attempt=0;
            status = DownloadStatus.INIT;
        }
        public DownloadStatus status;
        public Identifier identifier;
        public enum DownloadStatus
        {
            INIT,
            NOT_DOWNLOADED,
            DOWNLOADING,
            DOWNLOAD_ERROR,
            DOWNLOADED_NOT_REGISTERED,
            READY,
        }
    }

    public static Image registerTexture(File texture, Identifier identifier, String link)
    {
        NativeImage nativeImage = toNativeImage(texture);
        var backedTestTexture = new NativeImageBackedTexture(nativeImage);
        MinecraftClient.getInstance().getTextureManager().registerTexture(identifier,backedTestTexture);
        return new Image(identifier,new Image.ImageSize(nativeImage.getWidth(),nativeImage.getHeight()),link);
    }
    public static NativeImage toNativeImage(File file){
        try {
            InputStream inputStream = new FileInputStream(file);
            NativeImage nativeImage = NativeImage.read(inputStream);
            inputStream.close();
            return nativeImage;
        }
        catch(Exception e) {
            throw new RuntimeException(String.format("problem registring %s",file.toPath().toString()));
        }
    }

}
