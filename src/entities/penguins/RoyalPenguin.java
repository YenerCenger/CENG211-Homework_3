package entities.penguins;

import core.IcyTerrain;
import entities.Penguin;

public class RoyalPenguin extends Penguin {

    public RoyalPenguin(String name) {
        super(name);
    }

    @Override
    public void performSpecialAction(IcyTerrain terrain) {
        // Royal Penguin kaymadan önce 1 kare güvenli adım atabilir.
        System.out.println(name + " (Royal) can move 1 tile safely before sliding.");
    }
}