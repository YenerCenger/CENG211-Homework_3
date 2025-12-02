package entities.penguins;

import core.IcyTerrain;
import entities.Penguin;

public class KingPenguin extends Penguin {

    public KingPenguin(String name) {
        super(name);
        // Can (Health) veya başka özel durumlar varsa burada atanabilir
    }

    @Override
    public void performSpecialAction(IcyTerrain terrain) {
        // King Penguin kayarken 5. karede durabilir.
        // Bu mantık slide() içinde kontrol edilecek.
        System.out.println(name + " (King) is preparing to stop at the 5th square if possible.");
    }
}