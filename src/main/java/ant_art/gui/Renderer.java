package ant_art.gui;

import ant_art.config.Configuration;
import ant_art.entities.AntArea;
import ant_art.utils.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 15:06
 * Purpose: GUI
 **/
public class Renderer extends JFrame implements Runnable {
    private Canvas view;
    private int width;
    private int height;
    private Thread renderThread;
    private boolean running;
    private int fps;
    private BufferStrategy viewBs;
    private final AntArea antArea;
    //Controls whether to show UI on screen or not.
    private boolean showGui;
    // In seconds
    private int duration;
    private int sampleInterval;
    private ImageUtils.GIFBuilder gifBuilder;
    private File inputFile;

    public Renderer(AntArea antArea, String title, int fps, int duration, int sampleInterval, File inputFile) {
        super(title);
        this.antArea = antArea;
        this.fps = fps;
        this.duration = duration;
        this.showGui = true;
        this.sampleInterval = sampleInterval;

        int offSet = 50;
        this.height = antArea.getHeight() + offSet;
        this.width = antArea.getWidth() + offSet;

        this.gifBuilder = new ImageUtils.GIFBuilder();
        this.inputFile = inputFile;
    }

    /**
     * Create the UI
     */
    private void createUI() {
        //Create the GUI area
        this.view = new Canvas();
        view.setPreferredSize(new Dimension(width, height));
        view.setBackground(Color.WHITE);
        //We are handling repainting on our own.
        view.setIgnoreRepaint(true);

        //Add this to screen
        getContentPane().add(view);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(false);
        pack();
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutDown();
            }
        });
        setVisible(true);
    }

    /**
     * Generate output files. It should be called at the end of the simulation
     */
    private void generateOutputs() {
        //Remove extension from the output file
        String outputFileName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
        String gifDir = Configuration.Directories.OUTPUT + "/" + Configuration.Directories.GIF_RELATIVE;
        String rawDir = Configuration.Directories.OUTPUT + "/" + Configuration.Directories.RAW_RELATIVE;
        String oilPaintedDir = Configuration.Directories.OUTPUT + "/" + Configuration.Directories.OIL_PAINTED_RELATIVE;

        try {
            gifBuilder.create(new File(gifDir + "/" + outputFileName + ".gif"),
                    Configuration.OUTPUT_GIF_DELAY, Configuration.OUTPUT_GIF_LOOPING);
            ImageIO.write(ImageUtils.deepCopy(antArea.getMapImage()), Configuration.Outputs.IMG_FORMAT,
                    new File(rawDir + "/" + outputFileName + "_raw.jpg"));

            BufferedImage oilPainting = new ImageUtils.OilPainter().paint(antArea.getMapImage());
            ImageIO.write(oilPainting, "jpg", new File(oilPaintedDir + "/" + outputFileName + "_oil_painted.jpg"));

        } catch (IOException e) {
            System.out.println("Not able to create output files.");
            e.printStackTrace();
        }
    }

    /**
     * Clean up
     */
    private void shutDown() {
        antArea.shutDown();
        generateOutputs();

        //Moving input file to processed
        // if (!inputFile.renameTo(new File(Configuration.Directories.PROCESSED + "/" + inputFile.getName()))) {
        //     System.out.println("Not able to move the input file to processed. Kindly move that manually or that file will" +
        //             "be picked up in next simulation");
        // }

        //Close the rendering thread
        running = false;
        if (renderThread != null) {
            try {
                //Wait for view thread to finish.
                renderThread.join();

            } catch (InterruptedException e) {
                System.out.println("Error happened while closing view thread.");
            }
        }
        System.exit(0);
    }

    /**
     * Method to start the GUI
     */
    private void start() {
        if (showGui) {
            createUI();
        }
        //Create rendering thread. The thread mechanism is implemented as rendering UI on separate thread is lot faster.
        running = true;
        renderThread = new Thread(this);
        renderThread.start();
    }

    @Override
    public void run() {
        if (showGui) {
            view.requestFocus();
            //For fast GUI processing
            view.createBufferStrategy(3);
            viewBs = view.getBufferStrategy();
        }

        long lastTime = System.currentTimeMillis();
        double timeBtwFrames = 1000 / this.fps;
        double timeDelta = 0;
        long fpsTimer = lastTime;
        int updates = 0;
        long timer = lastTime;
        long samplingTimer = lastTime;
        int updateAccumulationCount = 0;

        while (running) {
            long now = System.currentTimeMillis();
            timeDelta += (now - lastTime) / timeBtwFrames;
            lastTime = now;
            while (timeDelta >= 1) {
                timeDelta--;
                antArea.update();
                //Take a snapshot of image
                if (now - samplingTimer > sampleInterval * 1000) {
                    samplingTimer = now;
                    System.out.println("Sample taken...");
                    gifBuilder.addImage(ImageUtils.deepCopy(antArea.getMapImage()));
                }
                if (showGui) {
                    long timeLeft = duration - ((now - timer) / 1000);
                    updateView(timeLeft);
                }
                updates++;
            }

            //In ideal case timeDelta should be always around 1.
            if (timeDelta > 5) {
                System.out.println("Updates are accumulating, timeDelta:" + timeDelta);
                updateAccumulationCount++;
            }

            //Check FPS misses
            if (now - fpsTimer > 1000) {
                fpsTimer += 1000;
                if (fps - updates > 10) {
                    System.out.println(String.format("FPS MISS: %s", updates));
                }
                updates = 0;
            }
            //Check simulation is completed or not.
            if ((now - timer) > (duration * 1000)) {
                System.out.println("Simulation completed.");
                //todo shutdown is not called automatically. Have to check
                shutDown();
            }
            //If updates are constantly accumulating it means FPS is not set right.
            if (updateAccumulationCount >= Configuration.GUI.UPDATE_ACCUMULATION_THRESHOLD) {
                System.out.println("Updates are accumulating. Check the FPS. Shutting down the system");
                //todo shutdown is not called automatically. Have to check
                shutDown();
            }
        }
    }

    /**
     * Update the view
     *
     * @param timeLeft time left for simulation
     */
    private void updateView(long timeLeft) {
        //Clear the screen
        Graphics g = viewBs.getDrawGraphics();
        g.clearRect(0, 0, view.getWidth(), view.getHeight());

        g.drawString("Time left: " + timeLeft + " seconds", 20, 20);

        //Draw the image
        int xOffset = (view.getWidth() - antArea.getWidth()) / 2;
        int yOffset = (view.getHeight() - antArea.getHeight()) / 2;
        g.drawImage(antArea.getMapImage(), xOffset, yOffset, antArea.getWidth(), antArea.getHeight(), null);

        //show the UI
        g.dispose();
        viewBs.show();
    }

    /**
     * Entry point of the class
     */
    public void execute() {
        //Using `SwingUtilities` is fast for UI.
        SwingUtilities.invokeLater(this::start);
    }
}
