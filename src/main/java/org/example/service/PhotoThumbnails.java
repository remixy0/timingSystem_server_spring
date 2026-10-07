package org.example.service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns stored profile photos (often several MB straight from a phone camera)
 * into small square JPEG thumbnails before they are sent to the frontend.
 * The frontend only shows them as ~52px avatars, so 160px covers retina screens.
 *
 * Results are cached in memory by photo content, so each photo is resized once.
 * If a photo can't be read (unsupported format, corrupt data), the original is returned.
 */
public final class PhotoThumbnails {

    private static final int SIZE = 160;
    private static final float JPEG_QUALITY = 0.82f;
    private static final int MAX_CACHE_ENTRIES = 1000;
    /**
     * Images with more pixels than this aren't decoded. A small file can claim to be a huge
     * image ("decompression bomb") and decoding it would need gigabytes of memory.
     * 60 MP is above any phone camera.
     */
    private static final long MAX_PIXELS = 60_000_000L;

    private static final Map<String, byte[]> CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                    return size() > MAX_CACHE_ENTRIES;
                }
            });

    private PhotoThumbnails() {}

    public static byte[] thumbnail(byte[] original) {
        if (original == null || original.length == 0) return original;

        String key = hash(original);
        byte[] cached = CACHE.get(key);
        if (cached != null) return cached;

        byte[] result = original;
        try {
            byte[] resized = resize(original);
            // Only use the thumbnail if it is actually smaller
            if (resized != null && resized.length < original.length) {
                result = resized;
            }
        } catch (Exception | OutOfMemoryError e) {
            // keep the original photo
        }
        CACHE.put(key, result);
        return result;
    }

    private static byte[] resize(byte[] original) throws Exception {
        BufferedImage source = readWithinPixelLimit(original);
        if (source == null) return null;

        int orientation = ExifOrientation.read(original);
        boolean swapsSides = orientation >= 5 && orientation <= 8;
        int srcW = source.getWidth();
        int srcH = source.getHeight();
        int shownW = swapsSides ? srcH : srcW;
        int shownH = swapsSides ? srcW : srcH;

        // Scale so the short side is SIZE, then centre-crop to a square (like CSS object-fit: cover)
        double scale = Math.min(1.0, (double) SIZE / Math.min(shownW, shownH));
        int outSize = (int) Math.round(Math.min(shownW, shownH) * scale);
        if (outSize < 1) return null;

        // Downscale in halving steps first for better quality on very large photos
        BufferedImage working = toRgb(source);
        int targetW = (int) Math.round(srcW * scale);
        int targetH = (int) Math.round(srcH * scale);
        while (working.getWidth() / 2 >= targetW && working.getHeight() / 2 >= targetH) {
            working = scaled(working, working.getWidth() / 2, working.getHeight() / 2);
        }
        working = scaled(working, Math.max(1, targetW), Math.max(1, targetH));

        BufferedImage out = new BufferedImage(outSize, outSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, outSize, outSize);

        int w = working.getWidth();
        int h = working.getHeight();
        int shownWorkW = swapsSides ? h : w;
        int shownWorkH = swapsSides ? w : h;
        AffineTransform t = new AffineTransform();
        t.translate((outSize - shownWorkW) / 2.0, (outSize - shownWorkH) / 2.0);
        t.concatenate(ExifOrientation.transform(orientation, w, h));
        g.drawImage(working, t, null);
        g.dispose();

        return encodeJpeg(out);
    }

    /** Like ImageIO.read, but checks the image size from its header before decoding it. */
    private static BufferedImage readWithinPixelLimit(byte[] data) throws Exception {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            if (in == null) return null;
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) return null;
            ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels <= 0 || pixels > MAX_PIXELS) return null;
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        }
    }

    private static BufferedImage toRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) return src;
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE); // transparent PNG areas become white
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private static BufferedImage scaled(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }

    private static byte[] encodeJpeg(BufferedImage img) throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ImageOutputStream out = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(out);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPEG_QUALITY);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }

    private static String hash(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (Exception e) {
            return data.length + ":" + java.util.Arrays.hashCode(data);
        }
    }

    /**
     * Phone photos are often stored sideways with an EXIF "orientation" flag that tells
     * viewers to rotate them. Re-encoding drops that flag, so the rotation is applied here.
     */
    static final class ExifOrientation {

        static int read(byte[] jpeg) {
            try {
                if (jpeg.length < 4 || (jpeg[0] & 0xFF) != 0xFF || (jpeg[1] & 0xFF) != 0xD8) return 1;
                int pos = 2;
                while (pos + 4 < jpeg.length) {
                    if ((jpeg[pos] & 0xFF) != 0xFF) return 1;
                    int marker = jpeg[pos + 1] & 0xFF;
                    int len = ((jpeg[pos + 2] & 0xFF) << 8) | (jpeg[pos + 3] & 0xFF);
                    if (marker == 0xDA) return 1; // image data starts, no EXIF found
                    if (marker == 0xE1 && pos + 10 < jpeg.length
                            && jpeg[pos + 4] == 'E' && jpeg[pos + 5] == 'x' && jpeg[pos + 6] == 'i' && jpeg[pos + 7] == 'f') {
                        return parseTiff(jpeg, pos + 10, Math.min(jpeg.length, pos + 2 + len));
                    }
                    pos += 2 + len;
                }
            } catch (RuntimeException ignored) {
                // malformed EXIF – treat as not rotated
            }
            return 1;
        }

        private static int parseTiff(byte[] b, int start, int end) {
            boolean little = b[start] == 'I';
            int ifd = start + readInt(b, start + 4, little);
            int entries = readShort(b, ifd, little);
            for (int i = 0; i < entries; i++) {
                int entry = ifd + 2 + i * 12;
                if (entry + 12 > end) break;
                if (readShort(b, entry, little) == 0x0112) {
                    int value = readShort(b, entry + 8, little);
                    return (value >= 1 && value <= 8) ? value : 1;
                }
            }
            return 1;
        }

        private static int readShort(byte[] b, int p, boolean little) {
            return little ? (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8)
                          : ((b[p] & 0xFF) << 8) | (b[p + 1] & 0xFF);
        }

        private static int readInt(byte[] b, int p, boolean little) {
            return little
                    ? (b[p] & 0xFF) | ((b[p + 1] & 0xFF) << 8) | ((b[p + 2] & 0xFF) << 16) | ((b[p + 3] & 0xFF) << 24)
                    : ((b[p] & 0xFF) << 24) | ((b[p + 1] & 0xFF) << 16) | ((b[p + 2] & 0xFF) << 8) | (b[p + 3] & 0xFF);
        }

        /** Transform that draws a w×h stored image the right way up, placed at (0,0). */
        static AffineTransform transform(int orientation, int w, int h) {
            AffineTransform t = new AffineTransform();
            switch (orientation) {
                case 2 -> { t.translate(w, 0); t.scale(-1, 1); }
                case 3 -> { t.translate(w, h); t.rotate(Math.PI); }
                case 4 -> { t.translate(0, h); t.scale(1, -1); }
                case 5 -> { t.rotate(-Math.PI / 2); t.scale(-1, 1); }
                case 6 -> { t.translate(h, 0); t.rotate(Math.PI / 2); }
                case 7 -> { t.scale(-1, 1); t.translate(-h, 0); t.translate(0, w); t.rotate(3 * Math.PI / 2); }
                case 8 -> { t.translate(0, w); t.rotate(3 * Math.PI / 2); }
                default -> { }
            }
            return t;
        }
    }
}
