package io.github.jumperonjava.imaginebook;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageSerializer {


    public static byte[] serializeImageMetadata(List<ImageData> imagesDefinition) {
        var buf = ByteBuffer.allocate(1000);
        buf.putShort((short) (2 + 2 + 4 + 4 + 2)); //Format (size)
        buf.put((byte) imagesDefinition.size());
        for (var image : imagesDefinition) {
            var url = image.url.getBytes(StandardCharsets.UTF_8);
            buf.put((byte) url.length);
            buf.put(url, 0, url.length);
            //count from here
            buf.putShort((short) image.x);
            buf.putShort((short) image.y);
            buf.putFloat(image.width);
            buf.putFloat(image.height);
            buf.putShort((short) ((image.rotation - 180) / 180 * Short.MAX_VALUE));
        }
        var len = buf.position();
        var shortBuf = new byte[len + 1];

        for (int i = 0; i < len; i++) {
            shortBuf[i] = buf.get(i);
        }

        return shortBuf;
    }

    public static List<ImageData> deserializeImageMetadata(byte[] data) {
        var buf = ByteBuffer.wrap(data);
        var format = buf.getShort();

        var images = new ArrayList<ImageData>();
        var amount = buf.get();
        for (int i = 0; i < amount; i++) {
            var image = new ImageData();
            images.add(image);
            var urlsize = buf.get() & 0xFF;
            var urlbytes = new byte[urlsize];
            buf.get(urlbytes);
            image.url = new String(urlbytes, StandardCharsets.UTF_8);
            var usedBytes = 0;
            //count from here
            image.x = buf.getShort();
            usedBytes += 2;
            image.y = buf.getShort();
            usedBytes += 2;
            image.width = buf.getFloat();
            usedBytes += 4;
            image.height = buf.getFloat();
            usedBytes += 4;
            image.rotation = ((float) buf.getShort()) / Short.MAX_VALUE * 180 + 180;
            usedBytes += 2;

            while (format > usedBytes) {
                buf.get();
                usedBytes++;
            }
        }
        return images;
    }

    public static List<ImageData> parseSafeModeImages(String page) {
        Pattern pattern = Pattern.compile("\\[.*?\\]");

        Matcher matcher = pattern.matcher(page);

        List<ImageData> images = new ArrayList<>();
        while (matcher.find()) {
            try {
                var str = matcher.group();
                if (str == null) {
                    continue;
                }
                var elements = Arrays.stream(str
                        .replace("[", "")
                        .replace("]", "")
                        .split("[,]")).toArray(String[]::new);

                var data = new ImageData();
                data.url = elements[0];


                var numbers = Arrays.stream(elements)
                        .skip(1)
                        .flatMap(element -> Arrays.stream(element.split(",")))
                        .flatMap(s -> Arrays.stream(s.split("(?=[+-])"))).toList().toArray(String[]::new);

                if (numbers.length >= 2) {
                    data.x = Float.parseFloat(numbers[0]);
                    data.y = Float.parseFloat(numbers[1]);
                }
                if (numbers.length == 3) {
                    data.width = Float.parseFloat(numbers[2]);
                    data.height = Float.parseFloat(numbers[2]);
                }
                if (numbers.length == 4) {
                    data.width = Float.parseFloat(numbers[2]) / 100;
                    data.height = Float.parseFloat(numbers[3]) / 100;
                }
                if (numbers.length == 5) {
                    data.width = Float.parseFloat(numbers[2]) / 100;
                    data.height = Float.parseFloat(numbers[3]) / 100;
                    data.rotation = Float.parseFloat(numbers[4]);
                }

                images.add(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return images;
    }
}
