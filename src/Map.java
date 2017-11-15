/* Red Assault
 * Map.java by Igor P.
 * This class stores a game map object. Doesn't perform any logic inside, simply a data structure.
 */

import java.awt.*;
import java.io.*;
import java.util.*;

public class Map {

    //the maps is stored in this 2d array of tile objects
    Tile[][] tiles;
    ArrayList<Unit> playerUnits = new ArrayList<>();
    ArrayList<Unit> computerUnits = new ArrayList<>();

    static private int width;
    static private int height;
    //on screen size
    static int tileSize = RedAssault.m * RedAssault.tileSizeSource;

    public Map() {
        loadMap("maps/test.map");
    }

    private void loadMap(String fileName) {

        TileImageEntity.initialize();

        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            StringTokenizer st = new StringTokenizer(in.readLine());
            width = Integer.parseInt(st.nextToken());
            height = Integer.parseInt(st.nextToken());
            tiles = new Tile[width][height];
            String line;
            int x;
            int y;

            while ((line = in.readLine()) != null) {
                if (line.charAt(0) == '@') {
                    st = new StringTokenizer(line);
                    st.nextToken();
                    x = Integer.parseInt(st.nextToken());
                    y = Integer.parseInt(st.nextToken());
                    String type = st.nextToken();
                    if (type.equals("t72") || type.equals("conscript") || type.equals("bmp") || type.equals("rpg")) {
                        playerUnits.add(new Unit(type));
                        playerUnits.get(playerUnits.size() - 1).setTileLocation(x, y);
                        playerUnits.get(playerUnits.size() - 1).setScreenLocation(x * tileSize, y * tileSize);
                    }
                    if (type.equals("bradley") || type.equals("abrams") || type.equals("marine")) {
                        computerUnits.add(new Unit(type));
                        computerUnits.get(computerUnits.size() - 1).setTileLocation(x, y);
                        computerUnits.get(computerUnits.size() - 1).setScreenLocation(x * tileSize, y * tileSize);
                    }
                } else if (line.charAt(0) != '/') {
                    st = new StringTokenizer(line);
                    x = Integer.parseInt(st.nextToken());
                    y = Integer.parseInt(st.nextToken());
                    String type = st.nextToken();
                    tiles[x][y] = new Tile(type);
                    tiles[x][y].setTileLocation(x, y);
                    tiles[x][y].setScreenLocation(x * tileSize, y * tileSize);

                }
            }
        } catch (IOException e) {
        }
        // set occupied status for each tile
        for (int i = 0; i < playerUnits.size(); i++) {
            Unit u = playerUnits.get(i);
            tiles[u.getTileLocation().x][u.getTileLocation().y].setOccupied(true);
        }
        for (int i = 0; i < computerUnits.size(); i++) {
            Unit u = computerUnits.get(i);
            tiles[u.getTileLocation().x][u.getTileLocation().y].setOccupied(true);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Tile getTile(Point p) {
        if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height) {
            return tiles[p.x][p.y];
        }
        return null;
    }

    public Tile getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return null;
    }

    public Unit getUnit(Point p) {
        Unit u = null;
        for (int i = 0; i < playerUnits.size(); i++) {
            if (playerUnits.get(i).getTileLocation().x == p.x && playerUnits.get(i).getTileLocation().y == p.y) {
                // Does not return unit which are hidden, ie. being transported
                if (!playerUnits.get(i).isBeingTransported()) {
                    return playerUnits.get(i);
                }
            }
        }
        for (int i = 0; i < computerUnits.size(); i++) {
            if (computerUnits.get(i).getTileLocation().x == p.x && computerUnits.get(i).getTileLocation().y == p.y) {
                if (!computerUnits.get(i).isBeingTransported()) {
                    return computerUnits.get(i);
                }
            }
        }
        return null;
    }

    public Unit getTransportedUnit(Point p) {
        for (int i = 0; i < playerUnits.size(); i++) {
            Unit u = playerUnits.get(i);
            if (u.getTileLocation().x == p.x && u.getTileLocation().y == p.y
                    && u.getUnitCategory() == UnitCategory.inf && u.loadedTrans) {
                return u;
            }
        }
        for (int i = 0; i < computerUnits.size(); i++) {
            Unit u = computerUnits.get(i);
            if (u.getTileLocation().x == p.x && u.getTileLocation().y == p.y
                    && u.getUnitCategory() == UnitCategory.inf && u.loadedTrans) {
                return u;
            }
        }
        return null;
    }

    public static boolean isWithinBounds(Point p) {
        if (p.x >= 0 && p.y >= 0 && p.x < width && p.y < height) {
            return true;
        }
        return false;
    }
}
