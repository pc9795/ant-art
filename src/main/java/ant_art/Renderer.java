package ant_art;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 15:06
 * Purpose: TODO:
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
    private boolean showGui;
    // In seconds
    private int duration;
    private int sampleInterval;
    private ImageUtils.GIFBuilder gifBuilder;

    public Renderer(AntArea antArea, String title, int width, int height, int fps, int duration, int sampleInterval) {
        super(title);
        this.antArea = antArea;
        this.fps = fps;
        this.duration = duration;
        this.showGui = true;
        this.sampleInterval = sampleInterval;
        this.height = height;
        this.width = width;
        this.gifBuilder = new ImageUtils.GIFBuilder();
    }

    private void createUI() {
        this.view = new Canvas();
        view.setPreferredSize(new Dimension(width, height));
        view.setBackground(Color.WHITE);
        //We are handling repainting on our own.
        view.setIgnoreRepaint(true);

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

    private void shutDown() {
        try {
            gifBuilder.create(new File("output.gif"), 250, true);
            ImageIO.write(antArea.getMapImage(), "jpg", new File("output.jpg"));

        } catch (IOException e) {
            System.out.println("Not able to create output gif.");
            e.printStackTrace();
        }
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

    private void start() {
        if (showGui) {
            createUI();
        }
        running = true;
        renderThread = new Thread(this);
        renderThread.start();
    }

    @Override
    public void run() {
        if (showGui) {
            view.requestFocus();
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

        while (running) {
            long now = System.currentTimeMillis();
            timeDelta += (now - lastTime) / timeBtwFrames;
            lastTime = now;
            while (timeDelta >= 1) {
                timeDelta--;
                antArea.update();
                if (now - samplingTimer > sampleInterval * 1000) {
                    samplingTimer = now;
                    System.out.println("Sample taken");
                    gifBuilder.addImage(ImageUtils.deepCopy(antArea.getMapImage()));
                }
                if (showGui) {
                    updateView(timer);
                }
                updates++;
            }
            if (now - fpsTimer > 1000) {
                fpsTimer += 1000;
                if (fps - updates > 10) {
                    System.out.println(String.format("FPS MISS: %s", updates));
                }
                updates = 0;
            }
            if (now - timer > duration * 1000) {
                System.out.println("Simulation completed.");
                shutDown();
            }
        }
    }

    private void updateView(long startTimeInMillis) {
        Graphics g = viewBs.getDrawGraphics();
        g.clearRect(0, 0, view.getWidth(), view.getHeight());
        long timeLeft = duration - (System.currentTimeMillis() - startTimeInMillis) / 1000;
        g.drawString("Time left: " + timeLeft + " seconds", 20, 20);
        int xOffset = (view.getWidth() - antArea.getWidth()) / 2;
        int yOffset = (view.getHeight() - antArea.getHeight()) / 2;
        g.drawImage(antArea.getMapImage(), xOffset, yOffset, antArea.getWidth(), antArea.getHeight(), null);
        g.dispose();
        viewBs.show();
    }

    public void execute() {
        SwingUtilities.invokeLater(this::start);
    }
}
