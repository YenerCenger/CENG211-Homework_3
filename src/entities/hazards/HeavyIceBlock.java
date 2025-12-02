package entities.hazards;

import core.IcyTerrain;
import entities.Hazard;
import entities.Penguin;
import enums.HazardType;

public class HeavyIceBlock extends Hazard {
    public HeavyIceBlock() {
        super(HazardType.HEAVY_ICE_BLOCK);
    }

    @Override
    public String getSymbol() {
        return "HB"; // Heavy Block
    }

    @Override
    public boolean onCollision(Penguin penguin, IcyTerrain terrain) {
        System.out.println(penguin.getSymbol() + " hit a HeavyIceBlock!");

        // Ceza: En hafif yemeği kaybet
        penguin.loseLightestFood();
        return false; // Hareket durdu (öldü)
    }
}
