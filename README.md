# Localization Example
This is a fork of [Michael Vezina Localization Demo](https://github.com/MikeVezina/localization-demo).  

This example introduces Jason agents that localize within a partially observable environment.

## Limitations Imposed on Agent:

- No access to map data. The agent must localize using data it has already perceived.
- Agents do not know their location on the map (only their immediate north/south/east/west cell perceptions).

## Setup and Execution Guide

### Step 1: Set Up the Required Services

You need to have the following services running for the demo to work:

- [epistemic-reasoner](https://github.com/Ethavanol/epistemic-reasoner)

- [touist-service](https://github.com/Ethavanol/touist-service)

Please refer to the README files and instructions in the respective repositories for how to set them up and get them running.

### Step 2: Install the epistemic-jason Dependency

The [epistemic-jason](https://github.com/Ethavanol/epistemic-jason) repository needs to be installed as a local Gradle dependency. Follow these steps:

Clone the epistemic-jason repository.

In the README of the epistemic-jason repository, go to the "Publish epistemic-jason in MavenLocal" section.

Follow the instructions there to publish it to your local Maven repository.

### Step 3: Install the epistemic-agent Dependency

Similarly, the [epistemic-agent](https://github.com/Ethavanol/epistemic-agents) repository also needs to be installed as a local Gradle dependency.

Clone the epistemic-agent repository.

Refer to the instructions in the README for how to add it as a Gradle dependency.

### Step 4: Build and Run the Localization Project

Once the dependencies are set up:

- Clone this repo if not done.
- Navigate to the localization project folder.
- Run the following command to build the project:
  
        ./gradlew build
  
- After the build process is complete, run the projectrunner file.

## Configuration:

- Rename the ***reasoner-config.json.example*** in ***reasoner-config.json***.
- Rename the ***reasonertype-config.json.example*** in ***reasonertype-config.json***.
- You can manually change the reasonner type in the ***reasonnertype-config.json***. It can be either one of these two options :
  - PAL
  - DEL
- You can manually change the separateAgentWorlds value in the ***reasonnertype-config.json***. If you want  a MultiAgent System and each agent to be treated as autonomous with his own model containing his possibles worlds, then you will go for true. It can be either one of these two options :
  - true
  - false

For example, in the case of a multi agent navigation (mapc example in the code) where each agent doesn't know where he spawned, we will use "DEL" and true.
Because we want each agent to have HIS model with HIS possible worlds and modifiy those based on what he perceived.
If we put the value to false, the perceptions of every agent will all update the same model.

- Finally, in the MapType files (or everywhere you need to give a path), you will use :
  - on Windows "\\\\"
  - on Linux "/"

If you are on Windows, the path will be "maps\\\localization.."

On Linux it will be "maps/localization.."

## Simulation Files:

Various .mas2j files are available to run different simulations. At the moment, only two files have been tested and are lowkey running :

- The weather simulation works properly and have many comments to explain how it's working. 
  - Using PAL/DEL and false.
- The basic-navigation-demo simulation is working. 
  - Using DEL and false. 
- The mapc demo is working. 
  - Using DEL and true.

The simulation for the "as" and "8" (named aces) has also been tested but does not work due to a missing API call.

## Recent addings (after the fork):

- Versions updates and gradle build fixed & updated.
- Modification of code.
- Add of Multi-Agent handling by the reasonner.
- Add of Touit-server.
- Add of a reasonertype-config.json so we can directly change the reasoner type & the separateAgentWorlds value (for MultiAgent system) manually from the project.
- Adding consideration for rules with constraints and avoiding evaluation of formulas when model firstly created.

