package ant_art;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 15:06
 * Purpose: TODO:
 **/
public class GUI extends JFrame implements Runnable {
    private final Canvas view;
    private Thread viewThread;
    private boolean running;
    private int fps;
    private BufferStrategy viewBs;
    private final AntArea antArea;

    public GUI(AntArea antArea, String title, int width, int height, int fps) {
        super(title);
        this.antArea = antArea;
        this.fps = fps;
        this.view = new Canvas();
        view.setPreferredSize(new Dimension(width, height));
    }

    private void start() {
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
                onWindowClosing();
            }
        });
        setVisible(true);

        running = true;
        viewThread = new Thread(this);
        viewThread.start();
    }

    public void onWindowClosing() {
        running = false;
        if (viewThread != null) {
            try {
                //Wait for view thread to finish.
                viewThread.join();

            } catch (InterruptedException e) {
                System.out.println("Error happened while closing view thread.");
            }
        }
        System.exit(0);
    }

    @Override
    public void run() {
        view.requestFocus();
        view.createBufferStrategy(3);
        viewBs = view.getBufferStrategy();

        long lastTime = System.currentTimeMillis();
        double timeBtwFrames = 1000 / this.fps;
        double timeDelta = 0;
        long timer = lastTime;
        int updates = 0;

        while (running) {
            long now = System.currentTimeMillis();
            timeDelta += (now - lastTime) / timeBtwFrames;
            lastTime = now;
            while (timeDelta >= 1) {
                timeDelta--;
                antArea.update();
                updateView();
                updates++;
            }
            if (now - timer > 1000) {
                timer += 1000;
                if (fps - updates > 10) {
                    System.out.println(String.format("FPS MISS: %s", updates));
                }
                updates = 0;
            }
        }
    }

    private void updateView() {
        Graphics g = viewBs.getDrawGraphics();
        g.clearRect(0, 0, view.getWidth(), view.getHeight());
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
