package ant_art;

/**
 * Created By: Prashant Chaubey
 * Created On: 29-04-2020 00:17
 * Purpose: Configuration of the project
 **/
public final class Configuration {
    private Configuration() {
    }

    //This parameter decides the probability of ant actions. A value of 5 means that out of 5 chances ant will do a
    //thing. A probability of 1/5 = 0.2.
    public static int ANT_SELECTION_SEED = 5;
    //The maximum food pheromone allowed on a cell
    public static float MAX_FOOD_PHEROMONE = 50;
    //The maximum home pheromone allowed on a cell
    public static float MAX_HOME_PHEROMONE = 50;
    //The minimum ratio of the cells which needed to mark it as a type.
    public static float TYPE_IDENTIFICATION_THRESHOLD = 0.5f;
    //How much food that can be picked from a cell at a time.
    public static int FOOD_PICKUP_QUANTITY = 1;
    // The color of the cell depends upon the food pheromone present on it. This multiplier will give push for low
    //pheromone levels
    public static float INTENSITY_AMPLIFIER = 25;
    public static int NEST_AREA_SIZE = 5;
    public static int FOOD_AREA_SIZE = 1;
    public static int CELL_SIZE = 4;
    public static int MAX_ANTS = 100;
    public static int FOOD_CELLS_TO_SPAWN = 20;
    public static int NEST_CELLS_TO_SPAWN = 1;
    public static int DEFAULT_FOOD_IN_CELL = 5;
    public static float PHEROMONE_DECAY_RATE = 0.01f;
    public static float MINIMUM_PHEROMONE_THRESHOLD = 0.01f;
    public static float PHEROMONE_GAIN = 1;
    public static int ANT_FOOD_CAPACITY = 100;

}
