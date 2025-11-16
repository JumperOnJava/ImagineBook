package io.github.jumperonjava.imaginebook.resolvers;

import com.ibm.icu.impl.InvalidFormatException;
import io.github.jumperonjava.imaginebook.Imaginebook;
import io.github.jumperonjava.imaginebook.image.BookDrawable;
import io.github.jumperonjava.imaginebook.image.Image;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;


/**
 * yay i have rewritten it
 */
public class AsyncImageDownloader {
    public static BookDrawable ERROR_IMAGE = new Image(Identifier.of("imaginebook", "textures/not_loaded.png"), new Image.ImageSize(128, 128));
    public static BookDrawable SIZE_ERROR = new Image(Identifier.of("imaginebook", "textures/size_error.png"), new Image.ImageSize(128, 128));
    public static BookDrawable DOWNLOADING_IMAGE = new Image(Identifier.of("imaginebook", "textures/downloading.png"), new Image.ImageSize(128, 128));
    public static BookDrawable URI_INVALID_ERROR = new Image(Identifier.of("imaginebook", "textures/url_invalid.png"), new Image.ImageSize(128, 128));

    //here is why it is not URI but string
    //https://www.reddit.com/r/java/comments/qi8yu8/hint_to_myself_and_other_poor_souls_dont_use/
    public Map<String, BookDrawable> downloadedImages = new HashMap<>();


    public List<URI> inProgressImages = Collections.synchronizedList(new LinkedList<>());
    public List<URI> inProgressRetryImages = Collections.synchronizedList(new LinkedList<>());
    public List<URI> unregisteredImages = Collections.synchronizedList(new LinkedList<>());
    public List<URI> errorImages = Collections.synchronizedList(new LinkedList<>());
    public List<URI> overLimitImages = Collections.synchronizedList(new LinkedList<>());

    public AsyncImageDownloader() {
        new Thread(this::downloadThread).start();
    }

    public Path URIPath(URI URI) {
        return Imaginebook.getCachePath().resolve("%s/%d.cache".formatted(URI.getHost().toLowerCase(), URI.getPath().hashCode()));
    }

    public Identifier URIIdentifier(URI URI) {
        return Identifier.of(Imaginebook.MOD_ID, "%s/%d.cache".formatted(URI.getHost().toLowerCase(), URI.getPath().hashCode()));
    }

    public void downloadThread() {
        while (true) {
            URI downloadURI = null;
            if (!inProgressImages.isEmpty())
                downloadURI = inProgressImages.remove(0);

            if (!inProgressRetryImages.isEmpty())
                downloadURI = inProgressRetryImages.remove(0);

            if (downloadURI == null) {
                continue;
            }
            var URIFile = URIPath(downloadURI);
            try {
                Files.createDirectories(URIFile.getParent());
            } catch (IOException e) {
                Imaginebook.LOGGER.error("Failed to create folder for file: " + URIFile);
                continue;
            }
            try (InputStream URIIn = downloadURI.toURL().openStream();
                 OutputStream fileOut = Files.newOutputStream(URIFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                Imaginebook.createImagineBookFolder();

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;
                long maxSize = 8 * 1024 * 1024;

                var convertReader = new ByteArrayOutputStream();

                while ((bytesRead = URIIn.read(buffer)) != -1) {
                    totalBytesRead += bytesRead;
                    if (totalBytesRead > maxSize) {
                        overLimitImages.add(downloadURI);
                        throw new InvalidFormatException("File size exceeds 1MB limit.");
                    }
                    convertReader.write(buffer, 0, bytesRead);
                }
                var convertWriter = new ByteArrayInputStream(convertReader.toByteArray());

                var converted = convertToPng(convertWriter);

                IOUtils.copy(converted, fileOut);

                unregisteredImages.add(downloadURI);
            } catch (Exception e) {
                errorImages.add(downloadURI);
                try {
                    Files.deleteIfExists(URIFile);
                } catch (IOException deleteException) {
                    Imaginebook.LOGGER.error("Failed to clean up incomplete file: " + URIFile);
                    deleteException.printStackTrace();
                }
            }
        }
    }

    public BookDrawable requestTexture(String URIStr) {

        URI URI = null;
        try {
            URI = new URL(URIStr).toURI();
        } catch (MalformedURLException e) {
            return URI_INVALID_ERROR;
        } catch (URISyntaxException e) {
            return URI_INVALID_ERROR;
        }

        if (downloadedImages.containsKey(URI.toString())) {
            return downloadedImages.get(URI.toString());
        }

        if (inProgressImages.contains(URI)) {
            return DOWNLOADING_IMAGE;
        }

        if (inProgressRetryImages.contains(URI)) {
            return ERROR_IMAGE;
        }

        if (errorImages.contains(URI)) {
            inProgressRetryImages.add(URI);
            errorImages.remove(URI);
            return ERROR_IMAGE;
        }

        if (overLimitImages.contains(URI)) {
            return SIZE_ERROR;
        }

        if (Files.exists(URIPath(URI))) {
            unregisteredImages.add(URI);
        }

        if (unregisteredImages.contains(URI)) {
            BookDrawable image = null;
            try {
                image = registerTexture(new FileInputStream(URIPath(URI).toFile()), URIIdentifier(URI));
                downloadedImages.put(URI.toString(), image);
                unregisteredImages.remove(URI);
            } catch (FileNotFoundException e) {
                errorImages.add(URI);
                return ERROR_IMAGE;
            }


            return image;
        }

        inProgressImages.add(URI);

        return ERROR_IMAGE;
    }

    public static BookDrawable registerTexture(InputStream texture, Identifier identifier) {
        NativeImage nativeImage = toNativeImage(texture);
        //? if < 1.21.5 {
        var backedTestTexture = new NativeImageBackedTexture(nativeImage);
        //?} else {
        /*var backedTestTexture = new NativeImageBackedTexture(identifier::toString, nativeImage);
        *///?}
        MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, backedTestTexture);
        return new Image(identifier, new Image.ImageSize(nativeImage.getWidth(), nativeImage.getHeight()));
    }

    public static NativeImage toNativeImage(InputStream file) {
        try {
            NativeImage nativeImage = NativeImage.read((file));
            (file).close();
            return nativeImage;
        } catch (Exception e) {
            throw new RuntimeException(String.format("problem registring texture"));
        }
    }

    public static InputStream convertToPng(InputStream inputStream) throws IOException {
        BufferedImage inputImage = ImageIO.read(inputStream);
        if (inputImage == null) {
            throw new IOException("The input stream does not contain a valid image.");
        }

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();

        boolean success = ImageIO.write(inputImage, "png", pngOutputStream);
        if (!success) {
            throw new IOException("Failed to write the image in PNG format.");
        }

        return new ByteArrayInputStream(pngOutputStream.toByteArray());
    }

}
