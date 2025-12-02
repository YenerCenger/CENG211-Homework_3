package interfaces;

import enums.Direction;
import core.IcyTerrain;

public interface ISlidable {
    // kayabiliyor mu
    void slide(Direction direction, IcyTerrain terrain);
}
