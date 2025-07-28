# Localization Example
This is a fork of [Michael Vezina Localization Demo](https://github.com/MikeVezina/localization-demo).

This example introduces Jason agents using epistemic extension that localize within a partially observable environment.

## Limitations Imposed on Agent:

- No access to map data. The agent must localize using data it has already perceived.
- Agents do not know their location on the map (only their immediate north/south/east/west cell perceptions).

## Setup and Execution Guide

### Step 1: Set Up the Required Services

You need to have the following services running for the demo to work:

- [epistemic-reasoner](https://github.com/Ethavanol/epistemic-reasoner)

- [touist-service](https://github.com/Ethavanol/touist-service)

Please refer to the README files and instructions in the respective repositories to see how to set them up and get them running.

### Step 2: Install the epistemic-jason Dependency

The [epistemic-jason](https://github.com/Ethavanol/epistemic-jason) repository needs to be installed as a local Gradle dependency. Follow these steps:

Please refer to the README files and instructions in the respective repositories to see how to set them up and get them running.

Follow the instructions there to publish it to your local Maven repository.

### Step 3: Build and Run the Localization Project

Once the dependencies are set up:

- Clone this repo if not done.
- Navigate to the localization project folder.
- Run the following command to build the project:

        ./gradlew build

## Step 4 : Configuration

- Rename the ***reasoner-config.json.example*** in ***reasoner-config.json***.
- Rename the ***reasonertype-config.json.example*** in ***reasonertype-config.json***.
- You can manually change the reasonner type in the ***reasonnertype-config.json***. It can be either one of these two options :
    - PAL
    - DEL
- You can manually change the separateAgentWorlds value in the ***reasonnertype-config.json***. It can be either one of these two options :
    - true
    - false

If you want  a MultiAgent System and each agent to be treated as autonomous with his own model containing his possibles worlds, then you will go for true. Anyway, even if you have a mono-agent system, it will still works with the value to true. Recommandations is to keep it true no matter what. Basically, with a true value, this will just associates a model to the agent name in the reasonner. Setting it to false can allow you to see the old version of how it was done.
Just keep it always true for the separateAgentWorlds variable, it will works for either MultiAgent or MonoAgent Systems.

For example, in the case of a multi agent navigation (mapc example in the code) where each agent doesn't know where he spawned, we will use "DEL" and true.
Because we want each agent to have HIS model with HIS possible worlds and modifiy those based on what he perceived.
If we put the value to false, the perceptions of every agent will all update the same model.

- Finally, in the MapType files (or everywhere you need to give a path), you will use :
    - on Windows "\\\\"
    - on Linux "/"

If you are on Windows, the path will be "maps\\\localization.."

On Linux it will be "maps/localization.."

## Step 5 : Run a Example

Run the projectrunner file.

## Simulation Files:

Various .mas2j files are available to run different simulations.

You will be able to see the logs of what the agent does in the logs/ folder in the reasonner and to see the semnatic model declarations constraints in the cache_touist/ folder in the TouistServer.

A paper explaining the examples limitations and contexts and how the code works for those simulations is available here. Reading it will really help you.

- The weather simulation works properly and have many comments to explain how it's working. Recommended to start by this one for a new epistemic-jason user/developer.
    - Using PAL/DEL and true.
- The navigation simulation. It treats the same problem as the navigation-changing-model but in a different way. Recommended to run this one before the changing model one.
    - Using DEL and true.
- The navigation-changing-model. This demonstration will shows you how to manipulate the semantic-model from the reasonner.
    - Using DEL and true.


## Recent addings (after the fork):

- Versions updates and gradle build fixed & updated.
- Modification of code.
- Add of Multi-Agent handling by the reasonner.
- Add of Touit-server.
- Add of a reasonertype-config.json so we can directly change the reasoner type & the separateAgentWorlds value (for MultiAgent system) manually from the project.
- Adding consideration for rules with constraints and avoiding evaluation of formulas when model firstly created.
- Refactorisation of the epistemic-jason extension
- Handling of nested epistemic formulas

## Versions used
- For localization demo :
    - JDK 23
    - Language Level 21

- python 3.12.3
- node 18.19.1

For the dependencies see all versions in build.gradle and graddle.wrapper

Btw the version of gradle in the gradle wrapper isn't related to your version of gradle. You don't even need to have gradle install locally to run a ./gradlew command or to use the gradle.wrapper files.
You will need it if you want to generate a gradle.wrapper file.