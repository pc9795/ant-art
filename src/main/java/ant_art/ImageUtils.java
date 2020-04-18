package ant_art;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    public static BufferedImage deepCopy(BufferedImage input) {
        ColorModel cm = input.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = input.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
