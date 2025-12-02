package entities.penguins;

import core.IcyTerrain;
import entities.Penguin;

public class EmperorPenguin extends Penguin {

    public EmperorPenguin(String name) {
        super(name);
    }

    @Override
    public void performSpecialAction(IcyTerrain terrain) {
        // Emperor Penguin kayarken 3. karede durabilir.
        System.out.println(name + " (Emperor) is preparing to stop at the 3rd square if possible.");
    }
}