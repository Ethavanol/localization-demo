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

Similarly, the [epistemic-agent](https://github.com/Ethavanol/epistemic-agent) repository also needs to be installed as a local Gradle dependency.

Clone the epistemic-agent repository.

Refer to the instructions in the README for how to add it as a Gradle dependency.

### Step 4: Build and Run the Localization Project

Once the dependencies are set up:

- Clone this repo if not done.
- Navigate to the localization project folder.
- Run the following command to build the project:

    ./gradlew build

- After the build process is complete, run the projectrunner file.

## Simulation Files:

Various .mas2j files are available to run different simulations. At the moment, only the navigation simulation has been fully tested and is nearly functional. 

The simulation for the "as" and "8 a" (named aces) has also been tested but does not work due to a missing API call.

## Testing Map:

A 4x3 map has been created for faster testing. This map contains 12 - 2 = 10 possible cells, meaning it generates 2^10 = 1024 possible worlds. In contrast, the 5x5 map would generate 2^23 = 8,388,608 possible worlds, which is considerably slower to generate.

If you wish to use the 5x5 map in the future and add agents, remember to uncomment lines 68 to 73 in the mapLocalizationMapModel file. These lines were commented out to make the 4x3 map functional. Specifically, the lines were commented because the agents added with those lines were placed outside the bounds of the 4x3 map.

## Current Issue:

The current problem we are facing is related to the fact that, in Jason, there are no error plans for the navigate(dispenser(red)...) plan. Specifically, this is due to our BeliefBase not receiving or updating with possible(...) facts.

This is potentially the last issue to resolve. For instance, by adding the following command between .print("I Moved."); and !updateGUIPossible; on line 188:

+possible(closest(dispenser(red), left));

The agent will move left. The missing information is the possible(...) fact, which would validate the plan's condition.


