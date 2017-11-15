/* Red Assault
 * Renderer.java by Igor P.
 * Renders the game onto the screen. 
 * (No calculating game logic here! Only checks stuff and drawing functions!)
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_U;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    // Get the game map from the GameArea class
    Map map;
    TextRenderer textRender;

    static final int m = RedAssault.m;
    static int tileSize = RedAssault.tileSizeSource * m;
    static int screenWidth = RedAssault.screenWidth;
    static int screenHeight = RedAssault.screenHeight;
    int bottomOfGameArea = screenHeight - 75 * m;
    // Minimap magnification factor
    int minimap_m = 3;

    // Integer RGB to float color values converter, when multiplied
    float iToF = 1 / 256f;

    // Given a percentage, returns the respective coordinate
    private static int getMenuCoordinateX(float percentage) {
        float result = screenWidth * percentage / 100;
        return (int) result;
    }

    private static int getMenuCoordinateXOrig(float percentage) {
        float result = (screenWidth / m) * percentage / 100;
        return (int) result;
    }

    // Menu item coordinates.
    static int minimapCoordinateX = getMenuCoordinateX(7);
    static int tileInfoCoordinateX = getMenuCoordinateX(26);
    static int endButtonCoordinateX = getMenuCoordinateX(80);
    static int endButtonCoordinateY = screenHeight - 60 * m;
    // Percentages of screenWidth at which submenu starts (A) and ends (B)
    int subMenuA = 46;
    int subMenuB = 74;

    ImageEntity cursor;
    ImageEntity activeCursor;
    ImageEntity select1;
    ImageEntity select2;
    ImageEntity attack1;
    ImageEntity attack2;
    ImageEntity load1;
    ImageEntity load2;
    ImageEntity yellowGrille;

    BufferedImage cursors;
    BufferedImage menuSheet;
    BufferedImage submenuSheet;
    BufferedImage iconSheet;
    BufferedImage rankSheet;
    BufferedImage pathArrow;
    BufferedImage endTurnSheet;
    BufferedImage buttonSheet;
    BufferedImage unitCategorySheet;

    ImageEntity menu;
    ImageEntity menu_top = new ImageEntity("images/menu_top.png");
    ImageEntity minimap;
    ImageEntity minimapText = new ImageEntity("images/overview.png");
    ImageEntity resources = new ImageEntity("images/resources.png");
    ImageEntity[] endTurn;
    ImageEntity menuButtonCurrent;
    ImageEntity helpButtonCurrent;
    ImageEntity endTurnCurrent;
    ImageEntity yourTurnLabel = new ImageEntity("images/your_turn_label5.png");
    ImageEntity[] helpButton;
    ImageEntity[] menuButton;
    ImageEntity[] unload;

    ImageEntity[] iconsTiles;
    ImageEntity[] iconsUSSR;
    ImageEntity[] iconsNATO;
    ImageEntity[] menuItems;
    ImageEntity[][] arrow;
    ImageEntity[][] unitCategory;

    public Renderer(Map m) {
        map = m;

        ImageEntity.loadImages();
        textRender = new TextRenderer();
        loadImageEntities();
        //set minimap coordinate and magnification values after map has loaded        
//        if (map.getWidth() > 33 || map.getHeight() > 25) {
//            minimap_m = 1;
//        }
//        minimapCoordinateY = screenHeight / m - 30 - map.getHeight() * minimap_m / 2;
//        System.out.println(getMenuCoordinateX(1));
    }

    public void render() {
        renderMap();

        if (GameArea.focused) {
            renderPossibleMoves();
            renderPossibleTargets();
            if (GameArea.drawPath) {
                renderPath();
            }
        }
        renderUnits();
        if (!GameArea.focused && GameArea.mouseButton == 3) {
            renderPossibleTargets();
            GameArea.focusedTile.setLocation(-1, -1);
            GameArea.tilesInRange.clear();
            GameArea.targetsInRange.clear();
            GameArea.mouseButton = 0;
            if (map.getUnit(GameArea.hover) != null) {
                map.getUnit(GameArea.hover).renderUnit();
            }
        }
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                map.tiles[i][j].renderExplosion();
            }
        }
        if ((GameArea.focused || GameArea.mouseY < bottomOfGameArea)) {
            renderMouse();
        }

        if (GameArea.developerView) {
            renderTileCoordinates();
            textRender.print("mouse coord. ", Color.white, screenWidth - 50 * m, m);
            textRender.print("X " + GameArea.mouseX, Color.white, screenWidth - 50 * m, m * 9);
            textRender.print("Y " + GameArea.mouseY, Color.white, screenWidth - 25 * m, m * 9);
        }
        renderMenu();

        if (!GameArea.focused || ((GameArea.focused && GameArea.mouseY > bottomOfGameArea - 1))) {
            renderMouse();
        }

    }

    private void loadImageEntities() {
        ImageEntity.loadImages();
        textRender = new TextRenderer();
        cursor = new ImageEntity("images/cursor.png");

        try {
            menuSheet = ImageIO.read(new File("images/menu.png"));
            submenuSheet = ImageIO.read(new File("images/submenu.png"));
            pathArrow = ImageIO.read(new File("images/path3.png"));
            cursors = ImageIO.read(new File("images/cursors2.png"));
            iconSheet = ImageIO.read(new File("images/icons.png"));
            rankSheet = ImageIO.read(new File("images/ranks.png"));
            endTurnSheet = ImageIO.read(new File("images/endturnbutton.png"));
            buttonSheet = ImageIO.read(new File("images/buttons.png"));
            unitCategorySheet = ImageIO.read(new File("images/unit_types.png"));
        } catch (IOException ex) {
        }

        menuItems = new ImageEntity[8];
        for (int i = 0; i < 3; i++) {
            menuItems[i] = new ImageEntity(rankSheet, i * 8, 0, 8, 11);
            menuItems[i].setPosition(tileInfoCoordinateX + 46 * m, screenHeight - 64 * m);
        }
        // Fuel and ammo icons
        menuItems[3] = new ImageEntity(rankSheet, 29, 0, 7, 8);
        menuItems[3].setPosition(tileInfoCoordinateX + 38 * m, screenHeight - 18 * m);
        menuItems[4] = new ImageEntity(rankSheet, 24, 0, 5, 8);
        menuItems[4].setPosition(tileInfoCoordinateX, screenHeight - 18 * m);
        // Running out of fuel and ammo notifications
        menuItems[5] = new ImageEntity(rankSheet, 36, 0, 6, 7);
        menuItems[6] = new ImageEntity(rankSheet, 41, 0, 5, 7);
        menuItems[7] = new ImageEntity(rankSheet, 46, 0, 11, 7);
        menuItems[7].setPosition(getMenuCoordinateX(subMenuA) + (getMenuCoordinateX(subMenuB) - getMenuCoordinateX(subMenuA)) / 2 - 3 * m, screenHeight - 40 * m);

        // Icons for menu info.
        iconsTiles = new ImageEntity[11];
        iconsUSSR = new ImageEntity[4];
        iconsNATO = new ImageEntity[3];

        for (int i = 0; i < iconsTiles.length; i++) {
            iconsTiles[i] = new ImageEntity(iconSheet, i * 30, 0, 30, 30);
            iconsTiles[i].setPosition(tileInfoCoordinateX, screenHeight - 50 * m);
        }
        for (int i = 0; i < iconsUSSR.length; i++) {
            iconsUSSR[i] = new ImageEntity(iconSheet, i * 30, 30, 30, 30);
            iconsUSSR[i].setPosition(tileInfoCoordinateX, screenHeight - 50 * m);
        }
        for (int i = 0; i < iconsNATO.length; i++) {
            iconsNATO[i] = new ImageEntity(iconSheet, i * 30, 60, 30, 30);
            iconsNATO[i].setPosition(tileInfoCoordinateX, screenHeight - 50 * m);
        }

        // Menu buttons, menu and help
        menuButton = new ImageEntity[3];
        for (int i = 0; i < 3; i++) {
            menuButton[i] = new ImageEntity(buttonSheet, i * 31, 0, 31, 10);
            menuButton[i].setPosition(endButtonCoordinateX + 2 * m, screenHeight - 20 * m);
        }
        menuButtonCurrent = menuButton[0];

        helpButton = new ImageEntity[4];
        for (int i = 0; i < 4; i++) {
            helpButton[i] = new ImageEntity(buttonSheet, i * 31, 10, 31, 10);
            helpButton[i].setPosition(endButtonCoordinateX + 40 * m, screenHeight - 20 * m);
        }
        helpButtonCurrent = helpButton[0];

        unitCategory = new ImageEntity[2][8];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                unitCategory[i][j] = new ImageEntity(unitCategorySheet, j * 16, i * 10, 16, 10);
                if (i == 0) {
                    unitCategory[i][j].setPosition(getMenuCoordinateX(subMenuA) + (getMenuCoordinateX(subMenuB) - getMenuCoordinateX(subMenuA)) / 5, screenHeight - 42 * m);
                } else {
                    unitCategory[i][j].setPosition(getMenuCoordinateX(subMenuA) + (((getMenuCoordinateX(subMenuB) - getMenuCoordinateX(subMenuA)) / 4) * 3) - 5 * m, screenHeight - 42 * m
                    );
                }
            }
        }
        // Next block of code creates a menu image, depending on the user's screen size.            
        // If screen width is not divisible by 3, we add one to screen position of menu items (to center them)
        int extra = 0;
        if (screenWidth % 3 != 0) {
            extra = 1;
        }
        BufferedImage menuImg = new BufferedImage(screenWidth / m + extra, 75, BufferedImage.TYPE_INT_ARGB);
        Graphics gMenu = menuImg.createGraphics();

        // This is how many vertical menu lines we need to put in
        int lines = (screenWidth / m - 112) / 4 + 2;

        for (int i = 0; i < lines; i++) {
            gMenu.drawImage(menuSheet, 19 + 2 * i, 0, 21 + 2 * i, 75, 19, 0, 21, 75, null);
            gMenu.drawImage(menuSheet, screenWidth / m / 2 + 37 + 2 * i + extra, 0, screenWidth / m / 2 + 39 + 2 * i + extra, 75, 19, 0, 21, 75, null);
        }

        // Menu decorations: left, center and right              
        gMenu.drawImage(menuSheet, 0, 0, 19, 75, 0, 0, 19, 75, null);
        gMenu.drawImage(menuSheet, screenWidth / m / 2 - 37 + extra, 0, screenWidth / m / 2 + 37 + extra, 75, 21, 0, 95, 75, null);
        gMenu.drawImage(menuSheet, screenWidth / m + extra - 19, 0, screenWidth / m + extra, 75, 97, 0, 116, 75, null);

        // Draw submenu on the menu.       
        gMenu.drawImage(submenuSheet, getMenuCoordinateXOrig(subMenuA), 15, getMenuCoordinateXOrig(subMenuA) + 6, 68, 0, 0, 6, 53, null);
        gMenu.drawImage(submenuSheet, getMenuCoordinateXOrig(subMenuB), 15, getMenuCoordinateXOrig(subMenuB) + 6, 68, 7, 0, 13, 53, null);
        int lines2 = getMenuCoordinateXOrig(subMenuB) - getMenuCoordinateXOrig(subMenuA) - 6;
        for (int i = 0; i < lines2; i++) {
            gMenu.drawImage(submenuSheet, getMenuCoordinateXOrig(subMenuA) + 6 + i, 15, getMenuCoordinateXOrig(subMenuA) + 7 + i, 68, 6, 0, 7, 53, null);
        }

        menu = new ImageEntity(menuImg, 0, 0, screenWidth / m + extra, menuSheet.getHeight());
        menu.setPosition(0, bottomOfGameArea);
        menu_top.setPosition(screenWidth / 2 - 35 * m, bottomOfGameArea - 6 * m);
        minimapText.setPosition(minimapCoordinateX, screenHeight - 12 * m);

        // Center "Your Turn" label's position
        yourTurnLabel.setPosition((screenWidth - yourTurnLabel.getWidth()) / 2,
                (screenHeight - yourTurnLabel.getHeight() - menu.getHeight()) / 2);

        // Create minimap
        BufferedImage miniMapImg = new BufferedImage(map.getWidth() * minimap_m + 2, map.getHeight() * minimap_m + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics gMinimap = miniMapImg.createGraphics();

        gMinimap.setColor(new Color(255, 255, 255));
        gMinimap.fillRect(0, 0, (map.getWidth() * minimap_m + 2), (map.getHeight() * minimap_m + 2));
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                Tile t = map.tiles[i][j];
                switch (t.getType()) {
                    case water:
                        gMinimap.setColor(new Color(16, 166, 239));
                        break;
                    case forest:
                        gMinimap.setColor(new Color(0, 109, 0));
                        break;
                    case mountain:
                        gMinimap.setColor(new Color(107, 73, 33));
                        break;
                    case hill:
                        gMinimap.setColor(new Color(0, 176, 39));
                        break;
                    case road:
                        gMinimap.setColor(new Color(148, 109, 74));
                        break;
                    case rocks:
                        gMinimap.setColor(new Color(156, 156, 156));
                        break;
                    default:
                        gMinimap.setColor(new Color(33, 211, 57));
                        break;
                }
                gMinimap.fillRect((i * minimap_m + 1), (j * minimap_m + 1), minimap_m, minimap_m);
            }
        }
        minimap = new ImageEntity(miniMapImg, 0, 0, (map.getWidth() * minimap_m + 2), (map.getHeight() * minimap_m + 2));
        minimap.setPosition(minimapCoordinateX, screenHeight - 62 * m);

        arrow = new ImageEntity[4][4];
        for (int i = 0; i < arrow.length; i++) {
            for (int j = 0; j < arrow.length; j++) {
                arrow[i][j] = new ImageEntity(pathArrow, i * RedAssault.tileSizeSource, j * RedAssault.tileSizeSource, RedAssault.tileSizeSource, RedAssault.tileSizeSource);
            }
        }
        select1 = new ImageEntity(cursors, 0, 0, 30, 30);
        select2 = new ImageEntity(cursors, 30, 0, 30, 30);
        attack1 = new ImageEntity(cursors, 0, 30, 30, 30);
        attack2 = new ImageEntity(cursors, 30, 30, 30, 30);
        load1 = new ImageEntity(cursors, 0, 60, 30, 30);
        load2 = new ImageEntity(cursors, 30, 60, 30, 30);
        yellowGrille = new ImageEntity(cursors, 60, 0, 30, 30);
        activeCursor = select1;

        endTurn = new ImageEntity[3];
        for (int i = 0; i < endTurn.length; i++) {
            endTurn[i] = new ImageEntity(endTurnSheet, 0, 23 * i, 64, 23);
            endTurn[i].setPosition(endButtonCoordinateX, endButtonCoordinateY);
        }
        endTurnCurrent = endTurn[0];

        unload = new ImageEntity[2];
        unload[0] = new ImageEntity(buttonSheet, 0, 30, 30, 12);
        unload[1] = new ImageEntity(buttonSheet, 30, 30, 30, 12);
        unload[0].setPosition(getMenuCoordinateX(subMenuA) + 63 * m, screenHeight - 30 * m);
        unload[1].setPosition(getMenuCoordinateX(subMenuA) + 63 * m, screenHeight - 30 * m);
    }

    private void renderMap() {
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                Tile t = map.tiles[i][j];
                if (isVisibleOnScreen(t)) {
                    t.renderTile();
                }
            }
        }
    }

    private void renderTileCoordinates() {
        glColor3f(1.0f, 1.0f, 1.0f);
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                Tile t = map.tiles[i][j];
                if (isVisibleOnScreen(t)) {
                    if (GameArea.developerView) {
                        // Print tile location and draw a rectangle around it
                        textRender.print(t.getTileLocation().x + ", " + t.getTileLocation().y, Color.black, t.getScreenLocation().x + 2 * m, t.getScreenLocation().y + 2 * m);
                        fillRect(t.getScreenLocation().x, t.getScreenLocation().y, t.getScreenLocation().x + tileSize, t.getScreenLocation().y + 1);
                        fillRect(t.getScreenLocation().x, t.getScreenLocation().y, t.getScreenLocation().x + 1, t.getScreenLocation().y + tileSize);
                    }

                }
            }
        }
    }

    public void renderMouse() {
        if (!GameArea.focused || GameArea.mouseY > bottomOfGameArea - 1) {
            cursor.setPosition(GameArea.mouseX, GameArea.mouseY);
            cursor.render();
        } else {
            activeCursor.render();
        }
    }

    private void renderMenu() {
        menu.render();
        menu_top.render();
        renderMinimap();
        minimapText.render();
        renderTileInfo();
        renderButtons();

        boolean renderingBattleInfo = false;
        if (GameArea.focused && GameArea.tileHovering.isOccupied()) {
            for (Unit u : map.computerUnits) {
                if (GameArea.hover.x == u.getTileLocation().x
                        && GameArea.hover.y == u.getTileLocation().y) {
                    renderBattleInfo();
                    renderingBattleInfo = true;
                }
            }
        }
        if (GameArea.tileHovering.isOccupied() && map.getUnit(GameArea.hover).isTransport() && !renderingBattleInfo) {
            renderTransportInfo(GameArea.hover);
        }
        if (GameArea.focused && map.getUnit(GameArea.focusedTile).isTransport() && !renderingBattleInfo) {
            renderTransportInfo(GameArea.focusedTile);
        }
        renderResources();
        if (GameArea.renderYourTurnLabel) {
            yourTurnLabel.render();
        }
    }

    private void renderResources() {
        resources.render();
        textRender.print(Integer.toString(GameArea.playerResourceBread), Color.white, 15 * m, 4 * m);
        textRender.print(Integer.toString(GameArea.playerResourceMetal), Color.white, 47 * m, 4 * m);
    }

    private void renderTransportInfo(Point p) {
        Unit t = map.getTransportedUnit(p);
        String unitType = "EMPTY";
        String hp = "-/-";
        String atk = "-";

        if (t != null) {

            unitType = t.typeString;
            if (GameArea.transportUnitSelected) {
                if (activeCursor == select2 || activeCursor == attack2 || activeCursor == load2) {
                    renderUnitCategory(t);
                }
            } else {
                renderUnitCategory(t);
            }
            hp = t.HP + "/" + t.getMaxHP();
            atk = "" + t.getAttack();
        }
        textRender.print("TRANSPORT: " + unitType, Color.lightGray, getMenuCoordinateX(subMenuA) + 15 * m, screenHeight - 50 * m);
        textRender.print("HP:  " + hp, Color.lightGray, getMenuCoordinateX(subMenuA) + 15 * m, screenHeight - 30 * m);
        textRender.print("ATK: " + atk, Color.lightGray, getMenuCoordinateX(subMenuA) + 15 * m, screenHeight - 22 * m);

        if (GameArea.focused && map.getUnit(GameArea.focusedTile).isTransporting() && !map.getTransportedUnit(GameArea.focusedTile).hasFinishedItsTurn()) {
            if (Input.keys[GLFW_KEY_U] || (RedAssault.pressedDownRMB
                    && GameArea.mouseX >= unload[0].getPosition().x && GameArea.mouseY >= unload[0].getPosition().y + m
                    && GameArea.mouseX < unload[0].getPosition().x + unload[0].getWidth()
                    && GameArea.mouseY < unload[0].getPosition().y + unload[0].getHeight() - m)) {
                unload[1].render();
            } else {
                unload[0].render();
            }
        }
    }

    private void renderBattleInfo() {
        // Attacking unit
        Unit playerUnit = map.getUnit(GameArea.focusedTile);
        // Defending unit
        Unit cpuUnit = map.getUnit(GameArea.tileHovering.tileLocation);

        int atkBonus = 0;
        if (GameArea.unitPathDrawn != null) {
            atkBonus = map.getTile(GameArea.unitPathDrawn.get(GameArea.unitPathDrawn.size() - 1)).getAttackBonus();
        }
        textRender.print(playerUnit.typeString, Color.lightGray, getMenuCoordinateX(subMenuA) + 20 * m, screenHeight - 50 * m);

        renderUnitCategory(playerUnit);
        textRender.print("HP:  " + playerUnit.HP + "/" + playerUnit.getMaxHP(), Color.lightGray, getMenuCoordinateX(subMenuA) + 20 * m, screenHeight - 30 * m);
        textRender.print("ATK: " + (playerUnit.getAttack() + atkBonus), Color.lightGray, getMenuCoordinateX(subMenuA) + 20 * m, screenHeight - 22 * m);

        // Render health bar
        glColor3f(143 * iToF, 134 * iToF, 137 * iToF);
        fillRect(getMenuCoordinateX(subMenuA) + 9 * m, screenHeight - 51 * m, getMenuCoordinateX(subMenuA) + 14 * m, screenHeight - 16 * m);
        glColor3f(215 * iToF, 206 * iToF, 209 * iToF);
        fillRect(getMenuCoordinateX(subMenuA) + 10 * m, screenHeight - 50 * m, getMenuCoordinateX(subMenuA) + 13 * m, screenHeight - 17 * m);

        // Render how much health now (in red)
        if ((activeCursor == select2 || activeCursor == attack2)) {
            glColor3f(219 * iToF, 110 * iToF, 113 * iToF);
        } else {
            glColor3f(237 * iToF, 128 * iToF, 131 * iToF);
        }
        int healthBeforeAttack = (int) (((float) playerUnit.HP / (float) playerUnit.getMaxHP()) * (float) (33.0 * m));
        fillRect(getMenuCoordinateX(subMenuA) + 10 * m, screenHeight - 17 * m - healthBeforeAttack, getMenuCoordinateX(subMenuA) + 13 * m, screenHeight - 17 * m);

        // Render how much health will be left (in green)
        int healthAfterAttack = playerUnit.HP - cpuUnit.getAttack() - atkBonus + map.getTile(playerUnit.getTileLocation()).getDefense();
        if (playerUnit.getAttack() >= cpuUnit.HP) {
            healthAfterAttack = playerUnit.HP;
        }
        if (healthAfterAttack < 0) {
            healthAfterAttack = 0;
        }
        int heightOfGreenBar = (int) (((float) healthAfterAttack / (float) playerUnit.getMaxHP()) * (33.0 * m));
        glColor3f(113 * iToF, 226 * iToF, 123 * iToF);
        fillRect(getMenuCoordinateX(subMenuA) + 10 * m, screenHeight - 17 * m - heightOfGreenBar, getMenuCoordinateX(subMenuA) + 13 * m, screenHeight - 17 * m);

        // VS. text in the middle
        menuItems[7].render();

        atkBonus = 0;
        if (GameArea.unitPathDrawn != null) {
            atkBonus = map.getTile(GameArea.tileHovering.getTileLocation()).getAttackBonus();
        }

        textRender.print(cpuUnit.typeString, Color.lightGray, getMenuCoordinateX(subMenuB) - 53 * m, screenHeight - 50 * m);
        renderUnitCategory(cpuUnit);
        textRender.print("HP:  " + cpuUnit.HP + "/" + cpuUnit.getMaxHP(), Color.lightGray, getMenuCoordinateX(subMenuB) - 53 * m, screenHeight - 30 * m);
        textRender.print("ATK: " + (cpuUnit.getAttack() + atkBonus), Color.lightGray, getMenuCoordinateX(subMenuB) - 53 * m, screenHeight - 22 * m);

        // Render health bar
        glColor3f(143 * iToF, 134 * iToF, 137 * iToF);
        fillRect(getMenuCoordinateX(subMenuB) - 9 * m, screenHeight - 51 * m, getMenuCoordinateX(subMenuB) - 4 * m, screenHeight - 16 * m);
        glColor3f(215 * iToF, 206 * iToF, 209 * iToF);
        fillRect(getMenuCoordinateX(subMenuB) - 8 * m, screenHeight - 50 * m, getMenuCoordinateX(subMenuB) - 5 * m, screenHeight - 17 * m);

        // Render how much health now (in red)
        if ((activeCursor == select2 || activeCursor == attack2)) {
            glColor3f(219 * iToF, 110 * iToF, 113 * iToF);
        } else {
            glColor3f(237 * iToF, 128 * iToF, 131 * iToF);
        }
        healthBeforeAttack = (int) (((float) cpuUnit.HP / (float) cpuUnit.getMaxHP()) * (float) (33.0 * m));
        fillRect(getMenuCoordinateX(subMenuB) - 8 * m, screenHeight - 17 * m - healthBeforeAttack, getMenuCoordinateX(subMenuB) - 5 * m, screenHeight - 17 * m);

        // Render how much health will be left (in green)        
        healthAfterAttack = cpuUnit.HP - playerUnit.getAttack() - atkBonus + map.getTile(cpuUnit.getTileLocation()).getDefense();
        if (healthAfterAttack < 0) {
            healthAfterAttack = 0;
        }

        heightOfGreenBar = (int) (((float) healthAfterAttack / (float) cpuUnit.getMaxHP()) * (33.0 * m));
        glColor3f(113 * iToF, 226 * iToF, 123 * iToF);
        fillRect(getMenuCoordinateX(subMenuB) - 8 * m, screenHeight - 17 * m - heightOfGreenBar, getMenuCoordinateX(subMenuB) - 5 * m, screenHeight - 17 * m);
    }

    private void renderUnitCategory(Unit u) {
        int sideIndex = map.computerUnits.contains(u) ? 1 : 0;

        if (u.getUnitCategory() == UnitCategory.armour) {
            unitCategory[sideIndex][2].render();
        } else {
            unitCategory[sideIndex][0].render();
        }
    }

    private void renderTileInfo() {
        if (getTileCoordinate(GameArea.mouseX, GameArea.mouseY) != null) {
            if (GameArea.mouseY < screenHeight - 75 * m) { //TODO hmm... why set tilehoverin here?
                GameArea.tileHovering = map.getTile(getTileCoordinate(GameArea.mouseX, GameArea.mouseY));
            }
        }
        if (GameArea.tileHovering != null) {
            if (!GameArea.tileHovering.isOccupied()) {
                //draw terrain icons and info
                switch (GameArea.tileHovering.getType()) {
                    case grass:
                        iconsTiles[0].render();
                        break;
                    case water:
                        iconsTiles[1].render();
                        break;
                    case hill:
                        iconsTiles[2].render();
                        break;
                    case forest:
                        iconsTiles[3].render();
                        break;
                    case mountain:
                        iconsTiles[4].render();
                        break;
                    case road:
                        iconsTiles[5].render();
                        break;
                    case rocks:
                        iconsTiles[6].render();
                        break;
                    case church:
                        iconsTiles[7].render();
                        break;
                    case village:
                        iconsTiles[8].render();
                        break;
                }
                textRender.print(GameArea.tileHovering.typeString, Color.white, tileInfoCoordinateX, screenHeight - 60 * m);
                textRender.print("DEFENSE", Color.white, tileInfoCoordinateX + 34 * m, screenHeight - 50 * m);
                textRender.print(Integer.toString(GameArea.tileHovering.getDefense()), Color.white, tileInfoCoordinateX + 75 * m, screenHeight - 50 * m);
                textRender.print("ATK BONUS", Color.white, tileInfoCoordinateX + 34 * m, screenHeight - 40 * m);
                textRender.print(Integer.toString(GameArea.tileHovering.getAttackBonus()), Color.white, tileInfoCoordinateX + 75 * m, screenHeight - 40 * m);
                String moveCost = "-";
                if (GameArea.tileHovering.getTerrainCost() < 10) {
                    moveCost = Integer.toString(GameArea.tileHovering.getTerrainCost());
                }
                textRender.print("MOVEMENT", Color.white, tileInfoCoordinateX + 34 * m, screenHeight - 30 * m);
                textRender.print(moveCost, Color.white, tileInfoCoordinateX + 75 * m, screenHeight - 30 * m);
            } else {
                Unit u = map.getUnit(GameArea.tileHovering.getTileLocation());
                UnitType type = u.getUnitType();
                textRender.print(u.typeString, Color.white, tileInfoCoordinateX, screenHeight - 60 * m);
                switch (type) {
                    case t72:
                        iconsUSSR[0].render();
                        break;
                    case conscript:
                        iconsUSSR[1].render();
                        break;
                    case bmp:
                        iconsUSSR[2].render();
                        break;
                    case rpg:
                        iconsUSSR[3].render();
                        break;
                    case abrams:
                        iconsNATO[0].render();
                        break;
                    case marine:
                        iconsNATO[1].render();
                        break;
                    case bradley:
                        iconsNATO[2].render();
                        break;
                }
                int count = u.getKills();
                if (count < 2) {
                    textRender.print("private", Color.gray, tileInfoCoordinateX + 55 * m, screenHeight - 59 * m);
                }
                if (count == 2 || count == 3) {
                    menuItems[0].render();
                    textRender.print("corporal", Color.gray, tileInfoCoordinateX + 55 * m, screenHeight - 59 * m);
                }
                if (count == 4 || count == 5) {
                    menuItems[1].render();
                    textRender.print("sergeant", Color.gray, tileInfoCoordinateX + 55 * m, screenHeight - 59 * m);
                }
                if (count > 5) {
                    menuItems[2].render();
                    textRender.print("lieutent.", Color.gray, tileInfoCoordinateX + 55 * m, screenHeight - 59 * m);
                }
                String defBonus = "";
                if (map.getTile(u.getTileLocation()).getDefense() != 0) {
                    defBonus = "+" + map.getTile(u.getTileLocation()).getDefense();
                }
                textRender.print("HEALTH  " + u.HP + defBonus + "/" + u.getMaxHP(), Color.white, tileInfoCoordinateX + 34 * m, screenHeight - 50 * m);
                String atkBonus = "";
                if (map.getTile(u.getTileLocation()).getAttackBonus() != 0) {
                    atkBonus = "+" + map.getTile(u.getTileLocation()).getAttackBonus();
                }
                textRender.print("ATTACK  " + u.getAttack() + atkBonus, Color.white, tileInfoCoordinateX + 34 * m, screenHeight - 40 * m);
                textRender.print("RANGE   " + (u.getMovementRange() - 1), Color.white, tileInfoCoordinateX + 34 * m, screenHeight - 30 * m);

                if (u.getUnitCategory() != UnitCategory.inf) {
                    menuItems[3].render();
                    textRender.print(u.fuel + "/" + u.getMaxFuel(), Color.gray, tileInfoCoordinateX + 46 * m, screenHeight - 16 * m);
                }
                menuItems[4].render();
                textRender.print(u.ammo + "/" + u.getMaxAmmo(), Color.gray, tileInfoCoordinateX + 6 * m, screenHeight - 16 * m);
            }
        }
    }

    private void renderMinimap() {
        minimap.render();
        // Render the units onto the minimap
        for (int i = 0; i < map.playerUnits.size(); i++) {
            Point p = screenToTileLocation(map.playerUnits.get(i).getScreenLocation());
            glColor3f(218 * iToF, 2 * iToF, 5 * iToF);
            fillRect(minimapCoordinateX + (p.x * minimap_m + 1) * m, (screenHeight / m - 62 + p.y * minimap_m + 1) * m, minimapCoordinateX + m * (minimap_m + p.x * minimap_m + 1), m * (minimap_m + screenHeight / m - 62 + p.y * minimap_m + 1));
        }
        for (int i = 0; i < map.computerUnits.size(); i++) {
            Point p = screenToTileLocation(map.computerUnits.get(i).getScreenLocation());
            glColor3f(33 * iToF, 109 * iToF, 222 * iToF);
            fillRect(minimapCoordinateX + (p.x * minimap_m + 1) * m, (screenHeight / m - 62 + p.y * minimap_m + 1) * m, minimapCoordinateX + m * (minimap_m + p.x * minimap_m + 1), m * (minimap_m + screenHeight / m - 62 + p.y * minimap_m + 1));
        }
        if (GameArea.focused && (activeCursor == select2 || activeCursor == attack2)) {
            glColor3f(1.0f, 1.0f, 1.0f);
            fillRect(minimapCoordinateX + (GameArea.focusedTile.x * minimap_m + 1) * m, (screenHeight / m - 62 + GameArea.focusedTile.y * minimap_m + 1) * m, minimapCoordinateX + m * (minimap_m + GameArea.focusedTile.x * minimap_m + 1), m * (minimap_m + screenHeight / m - 62 + GameArea.focusedTile.y * minimap_m + 1));
        }
    }

    private boolean isVisibleOnScreen(MapEntity me) {
        if (me.getScreenLocation().y + tileSize > 0 && me.getScreenLocation().y < bottomOfGameArea
                && me.getScreenLocation().x + tileSize > 0 && me.getScreenLocation().x < screenWidth) {
            return true;
        }
        return false;
    }

    private Point screenToTileLocation(Point p) {
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                // If the p is within bounds of the tile, save it
                if (p.x >= map.tiles[i][j].getScreenLocation().x && p.x < map.tiles[i][j].getScreenLocation().x + tileSize) {
                    if (p.y >= map.tiles[i][j].getScreenLocation().y && p.y < map.tiles[i][j].getScreenLocation().y + tileSize) {
                        return new Point(i, j);
                    }
                }
            }
        }
        return null;
    }

    private void renderPossibleMoves() {
        glColor3f(1.0f, 1.0f, 1.0f);
        Point tile;
        if (GameArea.mouseButton != 3) {
            for (int i = 0; i < GameArea.tilesInRange.size(); i++) {
                tile = new Point(tileToScreenLocation(GameArea.tilesInRange.get(i)));
                fillRect(tile.x + m, tile.y + m, tile.x + 2 * m, tile.y + tileSize - m);
                fillRect(tile.x + m, tile.y + m, tile.x + tileSize - m, tile.y + 2 * m);
                fillRect(tile.x + m, tile.y + tileSize - m * 2, tile.x + tileSize - m, tile.y + tileSize - m);
                fillRect(tile.x + tileSize - m * 2, tile.y + m, tile.x + tileSize - m, tile.y + tileSize - m);
            }
        }
    }

    private void renderPossibleTargets() {
        glColor3f(1.0f, 1.0f, 0.0f);
        Point tile;
        for (int i = 0; i < GameArea.targetsInRange.size(); i++) {
            if (map.isWithinBounds(GameArea.targetsInRange.get(i))) {
                tile = new Point(tileToScreenLocation(GameArea.targetsInRange.get(i)));
                if (GameArea.mouseButton != 3) {
                    fillRect(tile.x + m, tile.y + m, tile.x + 2 * m, tile.y + tileSize - m);
                    fillRect(tile.x + m, tile.y + m, tile.x + tileSize - m, tile.y + 2 * m);
                    fillRect(tile.x + m, tile.y + tileSize - m * 2, tile.x + tileSize - m, tile.y + tileSize - m);
                    fillRect(tile.x + tileSize - m * 2, tile.y + m, tile.x + tileSize - m, tile.y + tileSize - m);
                } else {
                    yellowGrille.setPosition(tile.x, tile.y);
                    yellowGrille.render();
                }
            }
        }
    }

    public void fillRect(int x1, int y1, int x2, int y2) {
        glBegin(GL_QUADS);
        glVertex3f((float) x1, (float) y1, 0.7f);
        glVertex3f((float) x2, (float) y1, 0.7f);
        glVertex3f((float) x2, (float) y2, 0.7f);
        glVertex3f((float) x1, (float) y2, 0.7f);
        glEnd();
    }

    private Point tileToScreenLocation(Point p) {
        return new Point(map.getTile(p).getScreenLocation().x, map.getTile(p).getScreenLocation().y);
    }

    // moves attack text up
    int attackOffsetY = 0;

    private void renderUnits() {
        for (Unit u : map.playerUnits) {
            if (isVisibleOnScreen(u)) {
                u.renderUnit();
                if (u.isBeingDisplayed()) {
                    renderHealthAndNotifications(u);
                }
            }
        }
        for (Unit u : map.computerUnits) {
            if (isVisibleOnScreen(u)) {
                u.renderUnit();
                if (u.isBeingDisplayed()) {
                    renderHealthAndNotifications(u);
                }
            }
        }
        // Draw attack numbers above units
        for (Unit u : map.playerUnits) {
            if (u.isAttacking()) {
                textRender.print("-" + Integer.toString(u.getAttack() + map.getTile(u.getTileLocation()).getAttackBonus() - map.getTile(u.getTarget()).getDefense()),
                        Color.yellow, map.getTile(u.getTarget()).getScreenLocation().x + 11 * m, map.getTile(u.getTarget()).getScreenLocation().y + 4 * m + attackOffsetY);
                if (RedAssault.timer % 4 == 0) {
                    attackOffsetY -= 2;
                }
            }
        }
        for (Unit u : map.computerUnits) {
            if (u.isAttacking()) {
                textRender.print("-" + Integer.toString(u.getAttack() + map.getTile(u.getTileLocation()).getAttackBonus() - map.getTile(u.getTarget()).getDefense()),
                        Color.yellow, map.getTile(u.getTarget()).getScreenLocation().x + 11 * m, map.getTile(u.getTarget()).getScreenLocation().y + 4 * m + attackOffsetY);
                if (RedAssault.timer % 4 == 0) {
                    attackOffsetY -= 2;
                }
            }
        }
    }

    private void renderHealthAndNotifications(Unit u) {
        int x = u.getScreenLocation().x;
        int y = u.getScreenLocation().y;
        if (u.HP != u.getMaxHP()) {
            if (!u.isMoving()) {
                textRender.print(Integer.toString(u.HP), Color.white,
                        x + 22 * m, y + 23 * m);
            }
        }
        // Low ammo and low fuel indicators start flashing when their levels drops to 20% of maximum
        if (RedAssault.timer % 100 < 33) {
            if (u.fuel < u.getMaxFuel() / 5) {
                menuItems[5].setPosition(x, y + 24 * m);
                menuItems[5].render();
            }
            if (u.ammo < u.getMaxAmmo() / 5) {
                menuItems[6].setPosition(x, y + 24 * m);
                menuItems[6].render();
            }
            if (u.fuel < u.getMaxFuel() / 5 && u.ammo < u.getMaxAmmo() / 5) {
                menuItems[5].setPosition(x, y + 24 * m);
                menuItems[5].render();
                menuItems[6].setPosition(x + 5 * m, y + 24 * m);
                menuItems[6].render();
            }
        }
    }

    // Function to draw the yellow path arrow
    private void renderPath() {
        // Get the path from upper upper class
        ArrayList<Point> path = GameArea.unitPathDrawn;
        // Index of what part of the arrow currently at.
        int index = 0;
        // If tile is in range or it is a target then draw it
        if (GameArea.mouseY < bottomOfGameArea) {
            // Draw starting path image
            if (path != null) {
                if (path.size() > 1) {
                    if (path.get(0).x + 1 == path.get(1).x && path.get(0).y == path.get(1).y) {
                        renderArrowSegment(path, 0, 2, 3);
                    }
                    if (path.get(0).x - 1 == path.get(1).x && path.get(0).y == path.get(1).y) {
                        renderArrowSegment(path, 0, 1, 3);
                    }
                    if (path.get(0).x == path.get(1).x && path.get(0).y + 1 == path.get(1).y) {
                        renderArrowSegment(path, 0, 0, 3);
                    }
                    if (path.get(0).x == path.get(1).x && path.get(0).y - 1 == path.get(1).y) {
                        renderArrowSegment(path, 0, 3, 3);
                    }
                }
                if (path.size() > 2) {
                    while (index + 2 < path.size()) {
                        //draw straight path lines
                        if (path.get(index).x + 1 == path.get(index + 1).x && path.get(index).y == path.get(index + 1).y
                                && path.get(index + 1).x + 1 == path.get(index + 2).x && path.get(index + 1).y == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 0, 1);
                        }
                        if (path.get(index).x - 1 == path.get(index + 1).x && path.get(index).y == path.get(index + 1).y
                                && path.get(index + 1).x - 1 == path.get(index + 2).x && path.get(index + 1).y == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 0, 1);

                        }
                        if (path.get(index).x == path.get(index + 1).x && path.get(index).y + 1 == path.get(index + 1).y
                                && path.get(index + 1).x == path.get(index + 2).x && path.get(index + 1).y + 1 == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 1, 1);

                        }
                        if (path.get(index).x == path.get(index + 1).x && path.get(index).y - 1 == path.get(index + 1).y
                                && path.get(index + 1).x == path.get(index + 2).x && path.get(index + 1).y - 1 == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 1, 1);
                        }
                        //draw curved path lines
                        if (path.get(index).x + 1 == path.get(index + 1).x && path.get(index).y == path.get(index + 1).y
                                && path.get(index + 1).x == path.get(index + 2).x && path.get(index + 1).y + 1 == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 2, 2);
                        }
                        if (path.get(index).x + 1 == path.get(index + 1).x && path.get(index).y == path.get(index + 1).y
                                && path.get(index + 1).x == path.get(index + 2).x && path.get(index + 1).y - 1 == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 3, 2);
                        }
                        if (path.get(index).x == path.get(index + 1).x && path.get(index).y - 1 == path.get(index + 1).y
                                && path.get(index + 1).x + 1 == path.get(index + 2).x && path.get(index + 1).y == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 1, 2);
                        }
                        if (path.get(index).x == path.get(index + 1).x && path.get(index).y + 1 == path.get(index + 1).y
                                && path.get(index + 1).x + 1 == path.get(index + 2).x && path.get(index + 1).y == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 0, 2);
                        }
                        if (path.get(index).x - 1 == path.get(index + 1).x && path.get(index).y == path.get(index + 1).y
                                && path.get(index + 1).x == path.get(index + 2).x && path.get(index + 1).y - 1 == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 0, 2);
                        }
                        if (path.get(index).x - 1 == path.get(index + 1).x && path.get(index).y == path.get(index + 1).y
                                && path.get(index + 1).x == path.get(index + 2).x && path.get(index + 1).y + 1 == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 1, 2);
                        }
                        if (path.get(index).x == path.get(index + 1).x && path.get(index).y - 1 == path.get(index + 1).y
                                && path.get(index + 1).x - 1 == path.get(index + 2).x && path.get(index + 1).y == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 2, 2);
                        }
                        if (path.get(index).x == path.get(index + 1).x && path.get(index).y + 1 == path.get(index + 1).y
                                && path.get(index + 1).x - 1 == path.get(index + 2).x && path.get(index + 1).y == path.get(index + 2).y) {
                            renderArrowSegment(path, index + 1, 3, 2);
                        }
                        index++;
                    }
                }
                // Draw ending arrow image
                if (path.size() > 1) {
                    if (path.get(index).x + 1 == path.get(index + 1).x && path.get(index).y == path.get(index + 1).y) {
                        arrow[1][0].setPosition(tileToScreenLocation(path.get(index + 1)).x, tileToScreenLocation(path.get(index + 1)).y);
                        arrow[1][0].render();
                    }
                    if (path.get(index).x - 1 == path.get(index + 1).x && path.get(index).y == path.get(index + 1).y) {
                        arrow[2][0].setPosition(tileToScreenLocation(path.get(index + 1)).x, tileToScreenLocation(path.get(index + 1)).y);
                        arrow[2][0].render();
                    }
                    if (path.get(index).x == path.get(index + 1).x && path.get(index).y + 1 == path.get(index + 1).y) {
                        arrow[3][0].setPosition(tileToScreenLocation(path.get(index + 1)).x, tileToScreenLocation(path.get(index + 1)).y);
                        arrow[3][0].render();
                    }
                    if (path.get(index).x == path.get(index + 1).x && path.get(index).y - 1 == path.get(index + 1).y) {
                        arrow[0][0].setPosition(tileToScreenLocation(path.get(index + 1)).x, tileToScreenLocation(path.get(index + 1)).y);
                        arrow[0][0].render();
                    }
                }
            }
        }
    }

    private void renderArrowSegment(ArrayList<Point> path, int index, int x, int y) {
        arrow[x][y].setPosition(tileToScreenLocation(path.get(index)).x, tileToScreenLocation(path.get(index)).y);
        arrow[x][y].render();
    }

    private Point getTileCoordinate(int mouseX, int mouseY) {
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                // If the clicked coordinate is within bounds of the tile, save it
                if (mouseX >= map.tiles[i][j].getScreenLocation().x && mouseX < map.tiles[i][j].getScreenLocation().x + tileSize) {
                    if (mouseY >= map.tiles[i][j].getScreenLocation().y && mouseY < map.tiles[i][j].getScreenLocation().y + tileSize) {
                        return new Point(i, j);
                    }
                }
            }
        }
        return null;
    }

    int mouseDelay = 27;

    public void updateMouse(Point hover, boolean hoverIsEnemy) {
        if (GameArea.mouseX > 0 && GameArea.mouseY > 0 && GameArea.mouseX < screenWidth && GameArea.mouseY < bottomOfGameArea - 1) {
            if (map.getTile(hover).isOccupied() && hoverIsEnemy) {
                if (!activeCursor.equals(attack1) && !activeCursor.equals(attack2)) {
                    activeCursor = attack1;
                }
                if (RedAssault.timer % mouseDelay == 0) {
                    if (activeCursor.equals(attack1)) {
                        activeCursor = attack2;
                    } else {
                        activeCursor = attack1;
                    }
                }
            } else if ((GameArea.transportUnitSelected && GameArea.tilesInRange.contains(hover))
                    || (map.getTile(hover).isOccupied() && !hoverIsEnemy && !hover.equals(GameArea.focusedTile) && map.getUnit(hover).isTransport()
                    && GameArea.focused && map.getUnit(GameArea.focusedTile).getUnitCategory() == UnitCategory.inf)) {
                if (!activeCursor.equals(load1) && !activeCursor.equals(load2)) {
                    activeCursor = load1;
                }
                if (RedAssault.timer % mouseDelay == 0) {
                    if (activeCursor.equals(load1)) {
                        activeCursor = load2;
                    } else {
                        activeCursor = load1;
                    }
                }
            } else {
                if (!activeCursor.equals(select1) && !activeCursor.equals(select2)) {
                    activeCursor = select1;
                }
                if (RedAssault.timer % mouseDelay == 0) {
                    if (activeCursor.equals(select1)) {
                        activeCursor = select2;
                    } else if (activeCursor.equals(select2)) {
                        activeCursor = select1;
                    }
                }
            }
            activeCursor.setPosition(tileToScreenLocation(hover).x, tileToScreenLocation(hover).y);
        }
    }

    // Used to animate periods in "computer's turn.." text
    String s = ".";

    private void renderButtons() {
        if (Input.keys[GLFW_KEY_SPACE] || (RedAssault.pressedDownRMB
                && GameArea.mouseX > endButtonCoordinateX + m
                && GameArea.mouseY > endButtonCoordinateY + m
                && GameArea.mouseX < endButtonCoordinateX + 62 * m
                && GameArea.mouseY < endButtonCoordinateY + 21 * m)) {
            endTurn[1].render();
        } else {
            boolean flashing = true;
            //end turn button flashes when all of the player's units have moved
            for (int i = 0; i < map.playerUnits.size(); i++) {
                if (!map.playerUnits.get(i).hasAttacked()) {
                    flashing = false;
                }
            }
            if (flashing && RedAssault.timer % 25 == 0) {
                if (endTurnCurrent == endTurn[0]) {
                    endTurnCurrent = endTurn[2];
                } else {
                    endTurnCurrent = endTurn[0];
                }
            }
            if (!GameArea.playersTurn) {
                endTurnCurrent = endTurn[0];
            }
            endTurnCurrent.render();
        }
        if (GameArea.playersTurn) {
            textRender.print("Turn: " + GameArea.turnCounter, Color.gray, endButtonCoordinateX, endButtonCoordinateY + 28 * m);
        } else {
            if (RedAssault.timer % 25 == 0) {
                if (s.equals(".")) {
                    s = "..";
                } else if (s.equals("..")) {
                    s = "...";
                } else {
                    s = ".";
                }
            }
            textRender.print("Computer's turn" + s, Color.gray, endButtonCoordinateX, endButtonCoordinateY + 28 * m);
        }
        // Render menu and help buttons
        if (GameArea.mouseX > endButtonCoordinateX + 2 * m
                && GameArea.mouseY > screenHeight - 18 * m
                && GameArea.mouseX < endButtonCoordinateX + 22 * m
                && GameArea.mouseY < screenHeight - 11 * m) {
            if (RedAssault.timer % 14 == 0) {
                if (menuButtonCurrent == menuButton[0]) {
                    menuButtonCurrent = menuButton[1];
                } else if (menuButtonCurrent == menuButton[1]) {
                    menuButtonCurrent = menuButton[2];
                } else if (menuButtonCurrent == menuButton[2]) {
                    menuButtonCurrent = menuButton[0];
                }
            }
        } else {
            menuButtonCurrent = menuButton[0];
        }
        menuButtonCurrent.render();

        if (GameArea.mouseX > endButtonCoordinateX + 40 * m
                && GameArea.mouseY > screenHeight - 18 * m
                && GameArea.mouseX < endButtonCoordinateX + 58 * m
                && GameArea.mouseY < screenHeight - 11 * m) {
            if (RedAssault.timer % 12 == 0) {
                if (helpButtonCurrent == helpButton[0]) {
                    helpButtonCurrent = helpButton[1];
                } else if (helpButtonCurrent == helpButton[1]) {
                    helpButtonCurrent = helpButton[2];
                } else if (helpButtonCurrent == helpButton[2]) {
                    helpButtonCurrent = helpButton[3];
                } else {
                    helpButtonCurrent = helpButton[0];
                }
            }
        } else {
            helpButtonCurrent = helpButton[0];
        }
        helpButtonCurrent.render();
    }

    private void print(Object s) {
        System.out.print(s);
    }

    private void println(Object s) {
        System.out.println(s);
    }
}
