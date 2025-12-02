package entities.penguins;

import core.IcyTerrain;
import entities.Penguin;

public class RockhopperPenguin extends Penguin {

    public RockhopperPenguin(String name) {
        super(name);
    }

    @Override
    public void performSpecialAction(IcyTerrain terrain) {
        // Rockhopper tehlikelerin üzerinden zıplayabilir.
        System.out.println(name + " (Rockhopper) is preparing to jump over a hazard.");
    }
}