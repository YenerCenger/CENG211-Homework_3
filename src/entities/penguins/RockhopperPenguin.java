package entities.penguins;

import core.IcyTerrain;
import entities.Penguin;

public class RockhopperPenguin extends Penguin {

    public RockhopperPenguin(String name) {
        super(name);
    }

    @Override
    public void performSpecialAction(IcyTerrain terrain) {
        // Zıplama bayrağını kaldır.
        // Base class'taki slide metodu engele çarpınca bu bayrağa bakıp zıplayacak.
        this.canJump = true;
        System.out.println(name + " (Rockhopper) activated ability: Ready to JUMP over next obstacle.");
    }
}