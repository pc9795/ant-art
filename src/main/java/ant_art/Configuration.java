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
    public static int antSelectionSeed = 5;
}
