package ant_art;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created By: Prashant Chaubey
 * Created On: 18-04-2020 13:46
 * Purpose: TODO:
 **/
public class ImageUtils {
    public static class GIFBuilder {
        private List<BufferedImage> images = new ArrayList<>();
        private ImageWriter writer;
        private ImageWriteParam params;
        private IIOMetadata metadata;

        public void addImage(BufferedImage image) {
            images.add(image);
        }

        private void configureRootMetadata(int delay, boolean loop) throws IIOInvalidTreeException {
            String metaFormatName = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

            IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10));
            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

            IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
            commentsNode.setAttribute("CommentExtension", "Created by: https://memorynotfound.com");

            IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
            IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");

            int loopContinuously = loop ? 0 : 1;
            child.setUserObject(new byte[]{0x1, (byte) (loopContinuously & 0xFF), (byte) ((loopContinuously >> 8) & 0xFF)});
            appExtensionsNode.appendChild(child);
            metadata.setFromTree(metaFormatName, root);
        }

        private IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
            int nNodes = rootNode.getLength();
            for (int i = 0; i < nNodes; i++) {
                if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                    return (IIOMetadataNode) rootNode.item(i);
                }
            }
            IIOMetadataNode node = new IIOMetadataNode(nodeName);
            rootNode.appendChild(node);
            return (node);
        }

        public void writeToSequence(RenderedImage img) throws IOException {
            writer.writeToSequence(new IIOImage(img, null, metadata), params);
        }

        public void close() throws IOException {
            writer.endWriteSequence();
        }

        public void create(File outputFile, int delay, boolean loop) throws IOException {
            if (images.isEmpty()) {
                return;
            }
            ImageOutputStream output = new FileImageOutputStream(outputFile);
            writer = ImageIO.getImageWritersBySuffix("gif").next();
            params = writer.getDefaultWriteParam();
            ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(images.get(0).getType());
            metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, params);
            configureRootMetadata(delay, loop);
            writer.setOutput(output);
            writer.prepareWriteSequence(null);

            for (BufferedImage image : images) {
                writeToSequence(image);
            }

            close();
            output.close();
        }
    }

    public static class OilPainter {
        int radius = 5;
        int intensityLevels = 24;

        public BufferedImage paint(BufferedImage src) {
            BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
            int averageR[] = new int[intensityLevels];
            int averageG[] = new int[intensityLevels];
            int averageB[] = new int[intensityLevels];
            int intensityCount[] = new int[intensityLevels];

            for (int x = 0; x < src.getWidth(); ++x) {
                int left = Math.max(0, x - radius);
                int right = Math.min(x + radius, dest.getWidth() - 1);
                for (int y = 0; y < src.getHeight(); ++y) {
                    int top = Math.max(0, y - radius);
                    int bottom = Math.min(y + radius, dest.getHeight() - 1);
                    Arrays.fill(averageR, 0);
                    Arrays.fill(averageG, 0);
                    Arrays.fill(averageB, 0);
                    Arrays.fill(intensityCount, 0);
                    int maxIndex = -1;
                    for (int j = top; j <= bottom; ++j) {
                        for (int i = left; i <= right; ++i) {
                            if (!inRange(x, y, i, j)) continue;
                            int rgb = src.getRGB(i, j);
                            int red = (rgb >> 16) & 0xFF;
                            int green = (rgb >> 8) & 0xFF;
                            int blue = (rgb) & 0xFF;
                            int intensityIndex = (int) ((((red + green + blue) / 3.0) / 256.0) * intensityLevels);
                            intensityCount[intensityIndex]++;
                            averageR[intensityIndex] += red;
                            averageG[intensityIndex] += green;
                            averageB[intensityIndex] += blue;
                            if (maxIndex == -1 ||
                                    intensityCount[maxIndex] < intensityCount[intensityIndex]
                                    ) {
                                maxIndex = intensityIndex;
                            }
                        }
                    }
                    int curMax = intensityCount[maxIndex];
                    int r = averageR[maxIndex] / curMax;
                    int g = averageG[maxIndex] / curMax;
                    int b = averageB[maxIndex] / curMax;
                    int rgb = ((r << 16) | ((g << 8) | b));
                    dest.setRGB(x, y, rgb);
                }
            }
            return dest;
        }

        private boolean inRange(int cx, int cy, int i, int j) {
            double d;
            d = Point2D.distance(i, j, cx, cy);
            return d < radius;
        }
    }

    public static BufferedImage deepCopy(BufferedImage input) {
        ColorModel cm = input.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = input.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static boolean isSimilar(Color src, Color target) {
        int limit = 50;
        int srcRed = src.getRed();
        int srcGreen = src.getGreen();
        int srcBlue = src.getBlue();
        int destRed = target.getRed();
        int destGreen = target.getGreen();
        int destBlue = target.getBlue();

        return destRed >= srcRed - limit && destRed <= srcRed + limit &&
                destGreen >= srcGreen - limit && destGreen <= srcGreen + limit
                && destBlue >= srcBlue - limit && destBlue <= srcBlue + limit;

    }

    public static Map<Color, Float> colorProfile(BufferedImage src) {
        Map<Color, Integer> colorCounts = new HashMap<>();
        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {
                Color pixelColor = new Color(src.getRGB(i, j));
                boolean found = false;
                for (Color color : colorCounts.keySet()) {
                    if (isSimilar(color, pixelColor)) {
                        colorCounts.put(color, colorCounts.get(color) + 1);
                        found = true;
                    }
                }
                if (!found) {
                    colorCounts.put(pixelColor, 1);
                }
            }
        }
        int size = src.getWidth() * src.getHeight();
        Map<Color, Float> colorProfile = new HashMap<>();
        for (Color color : colorCounts.keySet()) {
            colorProfile.put(color, colorCounts.get(color) / (float) size);
        }
        return colorProfile;
    }
}
