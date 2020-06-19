package ant_art.utils;

import ant_art.config.Configuration;

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
 * Purpose: Utility classes for image manipulation
 **/
public class ImageUtils {
    /**
     * Class to create gif from a set of images
     * REFERENCE: http://elliot.kroo.net/software/java/GifSequenceWriter/
     * I have used the above mentioned URL to implement this according to my needs.
     */
    public static class GIFBuilder {
        //All the images which will be added to the image
        private List<BufferedImage> images = new ArrayList<>();

        //Used to create GIF
        private ImageWriter writer;
        private ImageWriteParam params;
        private IIOMetadata metadata;


        /**
         * Add an image for the GIF
         *
         * @param image input image
         */
        public void addImage(BufferedImage image) {
            images.add(image);
        }

        /**
         * Set up for building GIF
         *
         * @param delay delay between iamges
         * @param loop  whether gif will loop or not
         * @throws IIOInvalidTreeException if something goes wrong
         */
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

        /**
         * Helper method to get a node from a root node
         *
         * @param rootNode root node
         * @param nodeName name of the node to find
         * @return node inside the `rootNode` with the name `nodeName`
         */
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

        /**
         * Helper method
         *
         * @param img input image
         * @throws IOException if something goes wrong
         */
        void writeToSequence(RenderedImage img) throws IOException {
            writer.writeToSequence(new IIOImage(img, null, metadata), params);
        }

        /**
         * Clean up
         *
         * @throws IOException if something goes wrong
         */
        void close() throws IOException {
            writer.endWriteSequence();
        }

        /**
         * Create the gif from the set of images in the instance
         *
         * @param outputFile output file
         * @param delay      delay between images in the gif.
         * @param loop       whether gif is looping or not
         * @throws IOException if something goes wrong
         */
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
            //Add the images
            for (BufferedImage image : images) {
                writeToSequence(image);
            }
            //Cleanup
            close();
            output.close();
        }
    }

    /**
     * Class which can create a Oil painting from input image
     * REFERENCE: https://github.com/lindenb/jsandbox/blob/master/src/sandbox/OilPainting.java
     * I have used the above mentioned URL to implement this according to my needs.
     */
    public static class OilPainter {
        int radius = Configuration.OilPainting.RADIUS;
        int intensityLevels = Configuration.OilPainting.INTENSITY_LEVEL;

        /**
         * Oil paint the source image
         *
         * @param src source image
         * @return oil painted image
         */
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

        /**
         * Check a the distance between two points is within the configured radius or not.
         *
         * @param cx the X coordinate of the first specified point
         * @param cy the Y coordinate of the first specified point
         * @param i  the X coordinate of the second specified point
         * @param j  the Y coordinate of the second specified point
         * @return true if two coordinates are within the radius.
         */
        private boolean inRange(int cx, int cy, int i, int j) {
            double d;
            d = Point2D.distance(i, j, cx, cy);
            return d < radius;
        }
    }

    /**
     * Copy an image
     *
     * @param input input iamge
     * @return copied iamge
     */
    public static BufferedImage deepCopy(BufferedImage input) {
        ColorModel cm = input.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = input.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Check that two color are similar or not. We will check that individual channels of the destination color falls
     * into a fixed range or not of the source channel.
     *
     * @param src    source color
     * @param target destination color
     * @return if colors are similar
     */
    public static boolean isSimilar(Color src, Color target) {
        int limit = Configuration.COLOR_SIMILARITY_THRESHOLD;
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

    /**
     * Create a color profile from the input image. Color profile is a map containing colors in the image and their ratio.
     * This method will not extract each individual color. It will try to represent colors which are similar by a representative.
     * The representative color is just the first color which is found.
     *
     * @param src source image
     * @return map containing color profile of the image
     */
    public static Map<Color, Float> colorProfile(BufferedImage src) {
        Map<Color, Integer> colorCounts = new HashMap<>();

        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {
                Color pixelColor = new Color(src.getRGB(i, j));
                boolean found = false;
                //If a pixel color is similar to already found color then it will just increase the count of that.
                for (Color color : colorCounts.keySet()) {
                    if (!isSimilar(color, pixelColor)) {
                        continue;
                    }
                    colorCounts.put(color, colorCounts.get(color) + 1);
                    found = true;
                    break;
                }
                //If the pixel color can't be represented by any existing color then add it as a representative.
                if (!found) {
                    colorCounts.put(pixelColor, 1);
                }
            }
        }
        //Convert the count into ratios
        int size = src.getWidth() * src.getHeight();
        Map<Color, Float> colorProfile = new HashMap<>();
        for (Color color : colorCounts.keySet()) {
            colorProfile.put(color, colorCounts.get(color) / (float) size);
        }

        return colorProfile;
    }

    /**
     * Convert a `Image` object into `BufferedImage` object
     *
     * @param image input `Image` object
     * @return `BufferedImage` object
     */
    private static BufferedImage convertToBufferedImage(Image image) {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    /**
     * Rescale a image to fit in a square of side length given by `limit`.
     *
     * @param input input image
     * @param limit side of the square in which to fit the image
     * @param depth no of times to rescale
     * @return rescaled image or null if can't rescale in specified depth.
     */
    @SuppressWarnings("unused")
    private static BufferedImage rescaleToLimit(BufferedImage input, int limit, int depth) {
        if (depth == 0) {
            return null;
        }
        if (input.getWidth() <= limit && input.getHeight() <= limit) {
            return input;
        }
        System.out.println("Rescaling...");
        Image rescaled = input.getScaledInstance(input.getWidth() / 2, input.getHeight() / 2, Image.SCALE_DEFAULT);
        return rescaleToLimit(convertToBufferedImage(rescaled), limit, depth - 1);
    }

    public static BufferedImage rescaleToLimit(BufferedImage input, int limit) {
        if (input.getWidth() <= limit && input.getHeight() <= limit) {
            return input;
        }
        Image rescaled;

        boolean processWidth = false;
        if (input.getWidth() > limit && input.getHeight() > limit) {
            if (input.getWidth() > input.getHeight()) {
                processWidth = true;
            }
        } else if (input.getWidth() > limit) {
            processWidth = true;
        }

        if (processWidth) {
            int newHeight = (int) (((float) Configuration.MAXIMUM_IMAGE_SIZE / input.getWidth()) * input.getHeight());
            System.out.println(String.format("Resizing (%s, %s) to (%s,%s)", input.getWidth(), input.getHeight(),
                    Configuration.MAXIMUM_IMAGE_SIZE, newHeight));
            System.out.println();
            rescaled = input.getScaledInstance(Configuration.MAXIMUM_IMAGE_SIZE, newHeight, Image.SCALE_DEFAULT);
        } else {
            int newWidth = (int) (((float) input.getWidth() * Configuration.MAXIMUM_IMAGE_SIZE) / input.getHeight());
            System.out.println(String.format("Resizing (%s, %s) to (%s,%s)", input.getWidth(), input.getHeight(),
                    newWidth, Configuration.MAXIMUM_IMAGE_SIZE));
            System.out.println();
            rescaled = input.getScaledInstance(newWidth, Configuration.MAXIMUM_IMAGE_SIZE, Image.SCALE_DEFAULT);
        }
        return convertToBufferedImage(rescaled);
    }
}
