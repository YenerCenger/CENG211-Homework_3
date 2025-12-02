package entities.penguins;

import core.IcyTerrain;
import entities.Penguin;

public class KingPenguin extends Penguin {

    public KingPenguin(String name) {
        super(name);
    }

    @Override
    public void performSpecialAction(IcyTerrain terrain) {
        // Yeteneği aktif et: 5 adım sonra dur.
        // Base class'taki slide metodu bu değişkeni kontrol edecek.
        this.stopAtStep = 5;
        System.out.println(name + " (King) activated ability: Will stop at 5th step.");
    }
}