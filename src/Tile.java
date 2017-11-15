/* Red Assault
 * Tile.java by Igor P.
 * A class for tiles.
 */

import java.awt.Point;
import java.awt.Image;

enum TileType {

    grass, hill, mountain, forest, water, road, rocks, village, church
};

public class Tile extends MapEntity {

    static int tileSize = RedAssault.m * RedAssault.tileSizeSource;
    // these two images are for animated tiles, like water
    protected Image img1 = null;
    protected Image img2 = null;
    protected TileImageEntity imgEntity1;
    protected TileImageEntity imgEntity2;

    protected ImageEntity imgExplosion;
    protected TileImageEntity explosion[];

    // these variables these are constant and determined when map loads
    private TileType type;
    private int terrainCost;
    private int defense;
    private int attackBonus;

    // these variables can change during the game
    private boolean occupied = false;
    private boolean drawExplosion = false;
    // movementScore is used when calculating a unit's range
    int movementScore;
    // g cost, f cost and parent point are all used for A*
    int g, f;
    Point parent;

    public Tile(String typeStr) {
        super(typeStr);

        explosion = new TileImageEntity[7];
        for (int i = 0; i < explosion.length; i++) {
            explosion[i] = new TileImageEntity(i);
        }
        explosionCurrent = explosion[0];
        imgEntity1 = new TileImageEntity("water1");
        imgEntity2 = new TileImageEntity("water2");
        movementScore = 0;
        g = f = 0;
        parent = null;

        if (typeStr.length() > 5 && typeStr.substring(0, 5).equals("grass")) {
            this.type = TileType.grass;
        } else if (typeStr.length() > 4 && typeStr.substring(0, 4).equals("road")) {
            this.type = TileType.road;
        } else {
            this.type = TileType.valueOf(typeStr);
        }
        switch (type) {
            case grass:
                typeString = "grassland";
                terrainCost = 1;
                defense = 0;
                attackBonus = 0;
                break;
            case hill:
                typeString = "hill";
                terrainCost = 2;
                defense = 0;
                attackBonus = 1;
                break;
            case mountain:
                typeString = "mountain";
                terrainCost = 999;
                defense = 0;
                attackBonus = 0;
                break;
            case forest:
                typeString = "forest";
                terrainCost = 2;
                defense = 1;
                attackBonus = 0;
                break;
            case water:
                typeString = "water";
                imgEntity1 = new TileImageEntity("water1");
                imgEntity2 = new TileImageEntity("water2");
                imgEntity = imgEntity1;

                terrainCost = 1;
                defense = 0;
                attackBonus = 0;
                break;
            case road:
                typeString = "road";
                terrainCost = 1;
                defense = 0;
                attackBonus = 0;
                break;
            case rocks:
                typeString = "rocks";
                terrainCost = 2;
                defense = 0;
                attackBonus = 0;
                break;
            case village:
                typeString = "village";
                terrainCost = 2;
                defense = 1;
                attackBonus = 0;
                break;
            case church:
                typeString = "church";
                terrainCost = 2;
                defense = 1;
                attackBonus = 1;
                break;
        }
    }

    public TileType getType() {
        return type;
    }

    int explosionAnim = 0;
    TileImageEntity explosionCurrent;

    public void renderTile() {
        imgEntity.render();
    }

    public void renderExplosion() {
        if (drawExplosion) {
            int speed = 6;
            if (explosionAnim < 2) {
                speed = 3;
            }
            if (RedAssault.timer % speed == 0) {
                explosion[explosionAnim].setPosition(imgEntity.getPosition().x, imgEntity.getPosition().y);
                explosionCurrent = explosion[explosionAnim++];
            }
            explosionCurrent.setPosition(imgEntity.getPosition().x, imgEntity.getPosition().y);
            explosionCurrent.render();
            if (explosionAnim == 6) {
                drawExplosion = false;
                explosionAnim = 0;
                explosionCurrent = explosion[0];
            }
        }
    }

    public void setOccupied(Boolean o) {
        occupied = o;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public int getTerrainCost() {
        return terrainCost;
    }

    public int getDefense() {
        return defense;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public void startDrawingExplosion() {
        drawExplosion = true;
    }

    @Override
    public void setScreenLocation(int x, int y) {
        super.setScreenLocation(x, y);
        imgEntity.setPosition(x, y);
        imgEntity1.setPosition(x, y);
        imgEntity2.setPosition(x, y);
    }

    void updateTile() {
        if (type == TileType.water) {
            if (RedAssault.timer % 120 == 0) {
                if (imgEntity.equals(imgEntity2)) {
                    imgEntity = imgEntity1;
                } else if (imgEntity.equals(imgEntity1)) {
                    imgEntity = imgEntity2;
                }

            }
        }
    }
}
