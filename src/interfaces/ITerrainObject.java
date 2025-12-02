package interfaces;

public interface ITerrainObject {
    // Semboller
    String getSymbol();

    // Nesnenin bulunduğu satır
    int getRow();

    // Nesnenin bulunduğu sütun
    int getCol();

    // Nesnenin konumunu güncellemek için
    void setPosition(int row, int col);
}