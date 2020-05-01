## Ant Art

Import this project as a maven project in your prefered IDE
* Intellij - https://www.lagomframework.com/documentation/1.6.x/java/IntellijMaven.html
* Eclipse - https://vaadin.com/learn/tutorials/import-maven-project-eclipse

### Important directories
* THESE DIRECTORIES ARE ACCEPTED TO BE AT THE ROOT DIRECTORY OF THE PROJECT.
* `inputs` - The default directory from where the code will pick input images
* `outputs` - The default directory where the code will dump the outputs
* `pallets` - The default directory where code looks for colour pallets to paint the image
* `processed` - The default directory where code transfers an image after processing.

### Main configurations

All the configurations are present in the class `config.Configuration`. Most of the configurations are set over a course 
of period through experimentation. Some configurations which shouldn't be changed without experimentation are clearly 
marked. The ones which can be changed to play with the outputs are mentioned below.

Below configurations can be used by the grader to experiment with the system.
* `Configuration.GUI.FPS` - Change this setting to increase/decrease the simulation speed but be mindful that if we 
increase the simulation speed we can't able to process large input files. Adjust `Configuarion.MAXIMUM_IMAGE_SIZE` accordingly.
* `Configuration.GUI.DURATION` - Increase/decrease the length of simulation
* `Configuration.MAX_ANTS` - The number of ants to spawn in the system.
* `Configuration.PHEROMNONE_DECAY_RATE` - The speed at which home and food pheromone levels are decayed.
* `Configuration.MAXIMUM_IMAGE_SIZE` - The maximum size the system can handle. Larger images are scaled down. This will
in sync with `Configuration.GUI.FPS`. Larger images require less FPS because of processing overhead.
* `Configuration.DEFAULT_TARGET_COOLOR_COUNT` - The no of colors ants will look for. This setting **highly affects the output**.
 
### How to run the project


### Acknowledgements
* Thanks [Lopsec](https://lospec.com/palette-list) for there free awesome palettes.
* Thanks Amy Dyer for excellent [tutorial](http://amydyer.art/wp/index.php/2020/01/01/drawing-with-ants-generative-art-with-ant-colony-optimization-algorithms/) on ant colony optimization.