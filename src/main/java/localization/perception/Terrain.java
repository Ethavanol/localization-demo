package localization.perception;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;

public enum Terrain {
    RED_DISPENSER("obstacle"),
    OBSTACLE("obstacle"),
    NONE("none");

    private final Atom terrainAtom;
    private final String atomName;
    Terrain(String name) {
        this.atomName = name;
        this.terrainAtom = ASSyntax.createAtom(name);
    }

    public String getAtomName() {
        return atomName;
    }

    public Atom getTerrainAtom() {
        return terrainAtom;
    }
}
