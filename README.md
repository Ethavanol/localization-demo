# Localization Example
This example introduces Jason agents that localize within a partially observable environment.

## Limitations Imposed on Agent:

- No access to map data. The agent must localize using data it has already perceived.
- Agents do not know their location on the map (only their immediate north/south/east/west cell perceptions).

## FAQ: Could not find epistemic-agents artifact when building
Since the epistemic agent framework is not being published to maven central, you must install it on your local repository. 
- [How to install locally](https://github.com/MikeVezina/epistemic-agents/blob/master/README.md#install-gradle-local-dependency)
