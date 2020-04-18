package ant_art;

/**
 * Created By: Prashant Chaubey
 * Created On: 17-04-2020 15:07
 * Purpose: TODO:
 **/
public class Main {
    public static void main(String[] args) {
        AntArea antArea = new AntArea(512, 512);
        Renderer renderer = new Renderer(antArea, "Ant Simulator", 600, 600, 30, 120, 10);
        renderer.execute();
    }
}
