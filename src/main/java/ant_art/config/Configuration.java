package ant_art.config;

import java.awt.*;

/**
 * Created By: Prashant Chaubey
 * Created On: 29-04-2020 00:17
 * Purpose: Configuration of the project
 **/
public final class Configuration {
    private Configuration() {
    }

    /**
     * Constants used to create OIL painting.
     */
    public final class OilPainting {
        private OilPainting() {
        }

        //**************
        //DO NOT CHANGE
        //**************
        //Change the oil painting effect applied to the output images.
        public static final int RADIUS = 5;
        public static final int INTENSITY_LEVEL = 24;
    }

    /**
     * Constants for default directory
     */
    public final class Directories {
        private Directories() {
        }

        //DON'T USE SLASHES AT THE END OF THESE DIRECTORIES. THE CODE ASSUMES THAT THESE WILL NOT HAVE SLASHES AT THE END.
        //Directory to look for color pallets
        public static final String PALLETS = "pallets";
        //Directory to look for inputs
        public static final String INPUT = "inputs";
        //Directory to look for outputs
        public static final String OUTPUT = "outputs";
        //Directory to move the images after processing
        public static final String PROCESSED = "processed";

        //**************
        //DO NOT CHANGE
        //**************
        //THESE SET OF DIRECTORIES ARE ASSUMED TO BE RELATIVE TO OUTPUT DIRECTORY
        //Directory where gifs are moved inside output directory
        public static final String GIF_RELATIVE = "gifs";
        //Directory where final image is generated inside output directory
        public static final String RAW_RELATIVE = "raw";
        //Directory where oil painted image is generated inside output directory
        public static final String OIL_PAINTED_RELATIVE = "oil_painted";
    }

    /**
     * Configuration for GUI
     */
    public final class GUI {
        private GUI() {
        }

        //GUI update rate per second
        public static final int FPS = 45;
        //Time period of the simulation in seconds
        public static final int DURATION = 30;

        //**************
        //DO NOT CHANGE
        //**************
        //Interval at which have to take a image snapshot
        public static final int SAMPLE_INTERVAL = 5;
        //This parameter keeps check the cases when simulation is not able to run properly.
        public static final int UPDATE_ACCUMULATION_THRESHOLD = 5;
    }

    /**
     * Configuration for outputs generated by the system
     */
    public final class Outputs {
        private Outputs() {
        }

        //**************
        //DO NOT CHANGE
        //**************
        //Image format used in the project
        public static final String IMG_FORMAT = "jpg";
    }

    /**
     * Configuration for colors in the ant area.
     */
    public static final class Colors {
        private Colors() {
        }

        //**************
        //DO NOT CHANGE
        //**************
        //Names are self-explanotry
        public static final Color DEFAULT = Color.BLACK;
        public static final Color FOOD = Color.RED;
        public static final Color NEST = Color.GREEN;
    }

    //No of ants to generate
    public static final int MAX_ANTS = 100;
    //Decay rate of pheromone.
    public static final float PHEROMONE_DECAY_RATE = 0.005f;
    //520 works best with an FPS of 60 so change accordingly. If increasing the FPS reduce the size and vice-versa.
    public static final int MAXIMUM_IMAGE_SIZE = 520;
    //This is the main setting which will greatly affect the output images. The number of colors to look for in images.
    public static final int DEFAULT_TARGET_COLOR_COUNT = 2;

    //**************
    //DO NOT CHANGE
    //**************
    //This parameter decides the probability of ant actions. A value of 5 means that out of 5 chances ant will do a
    //thing. A probability of 1/5 = 0.2.
    public static final int ANT_SELECTION_SEED = 5;
    //The maximum food pheromone allowed on a cell
    public static final float MAX_FOOD_PHEROMONE = 50;
    //The maximum home pheromone allowed on a cell
    public static final float MAX_HOME_PHEROMONE = 50;
    //The minimum ratio of the cells which needed to mark it as a type.
    public static final float TYPE_IDENTIFICATION_THRESHOLD = 0.5f;
    //How much food that can be picked from a cell at a time.
    public static final int FOOD_PICKUP_QUANTITY = 1;
    // The color of the cell depends upon the food pheromone present on it. This multiplier will give push for low
    //pheromone levels
    public static final float INTENSITY_AMPLIFIER = 25f;
    //The number of cells the nest occupy
    public static final int NEST_AREA_SIZE = 5;
    //The number of cells food occupy - NOT USED RIGHT NOW
    public static final int FOOD_AREA_SIZE = 1;
    //The side of a cell in pixel length
    public static final int CELL_SIZE = 4;
    //Food source to spawn in the image - NOT USED RIGHT NOW
    public static final int FOOD_CELLS_TO_SPAWN = 20;
    //Nests to spawn in the image
    public static final int NEST_CELLS_TO_SPAWN = 1;
    //Default amount of food in the cell
    public static final int DEFAULT_FOOD_IN_CELL = 5;
    //Minimum pheromone level to be in a cell. If below then the colour of the cell is set to default
    public static final float MINIMUM_PHEROMONE_THRESHOLD = 0.01f;
    //Gain of pheromone when an ant in a cell
    public static final float PHEROMONE_GAIN = 1;
    //Food capacity of the ants.
    public static final int ANT_FOOD_CAPACITY = 100;
    //The range to look for when checking two colors for similarity
    public static final int COLOR_SIMILARITY_THRESHOLD = 50;
    //Number of times rescaling is applied. - NOT USED
    @SuppressWarnings("unused")
    public static final int MAXIMUM_RESCALING_DEPTH = 3;
    //The percentage above which colors are removed.
    public static final float COLOR_FILTERING_HIGHER_LIMIT = 0.5f;
    //The percentage below which colors are removed
    public static final float COLOR_FILTERING_LOWER_LIMIT = 0.05f;
    //Delay between images in the output GIF
    public static final int OUTPUT_GIF_DELAY = 250;
    //Whether the GIF will be looping or not.
    public static final boolean OUTPUT_GIF_LOOPING = true;
    //The neighborhood to consider when evaluating the output for mosaic.
    public static final int MOSAIC_NEIGHBORHOOD = 3;
    //Number of different neighbors to look for in the neighborhood
    public static final float MOSAIC_THRESHOLD = 0.5f;
}
