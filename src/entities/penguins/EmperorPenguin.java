package entities.penguins;

import core.IcyTerrain;
import entities.Penguin;

public class EmperorPenguin extends Penguin {

    public EmperorPenguin(String name) {
        super(name);
    }

    @Override
    public void performSpecialAction(IcyTerrain terrain) {
        // Yeteneği aktif et: 3 adım sonra dur.
        this.stopAtStep = 3;
        System.out.println(name + " (Emperor) activated ability: Will stop at 3rd step.");
    }
}