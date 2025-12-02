package core;

import java.util.ArrayList;
import java.util.List;

import interfaces.ITerrainObject;

public class Cell {
    // Bir karede hem Penguen hem Yemek olabili o yüzden Liste kullanıyoruz.
    private List<ITerrainObject> objects;

    public Cell() {
        this.objects = new ArrayList<>();
    }

    public Cell(List<ITerrainObject> objects) {
        this.objects = new ArrayList<>(objects);
    }

    public void addObject(ITerrainObject object) {
        objects.add(object);
    }

    public void removeObject(ITerrainObject object) {
        objects.remove(object);
    }

    public List<ITerrainObject> getObjects() {
        return new ArrayList<>(objects);
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public <T> T getFirstObject(Class<T> clazz) {
        for (ITerrainObject obj : objects) {
            if (clazz.isInstance(obj)) {
                return clazz.cast(obj);
            }
        }
        return null; // O tipte bir nesne yoksa null döner
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "   ";
        }

        // GÖRÜNÜRLÜK ÖNCELİĞİ (Priority):
        // 1. PENGUEN varsa kesinlikle onu göster (P1, P2, P3)
        for (ITerrainObject obj : objects) {
            if (obj instanceof entities.Penguin) {
                return obj.getSymbol();
            }
        }

        // 2. TEHLİKE varsa (Penguen yoksa) onu göster (LB, SL, HI)
        for (ITerrainObject obj : objects) {
            if (obj instanceof entities.Hazard) {
                return obj.getSymbol();
            }
        }

        // 3. Hiçbiri yoksa (muhtemelen sadece Yemek vardır) ilkini göster
        return objects.get(0).getSymbol();
    }
}
