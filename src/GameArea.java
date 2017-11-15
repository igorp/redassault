/* Red Assault
 * GameArea.java by Igor P.
 * Handles the playing screen area logic in the game.
 */

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

import static org.lwjgl.glfw.GLFW.*;

enum Direction {

    up, right, down, left, none
};

public class GameArea {

    Map map;
    Renderer gameScreen;
    static final int m = RedAssault.m;
    static int tileSize = RedAssault.tileSizeSource * m;
    static int screenWidth = RedAssault.screenWidth;
    static int screenHeight = RedAssault.screenHeight;
    int bottomOfGameArea = screenHeight - 75 * m;

    static int turnCounter = 1;
    static int playerResourceBread = 0;
    static int playerResourceMetal = 0;
    static boolean playersTurn;
    //focused means if a unit has been clicked and possible moves are displayed
    static boolean focused;
    //focused tile is the tile that has a unit and is selected
    static Point focusedTile;
    //oldHover is to check if we need to redraw path again
    static Point oldHover;
    //tile over which the mouse is hovering
    static Tile tileHovering;
    static Point hover;
    static boolean transportUnitSelected;

    //global user input variables. 
    static int mouseButton;
    static int mouseX, mouseY;

    static boolean developerView = false;
    static boolean drawPath = false;
    boolean canPressDeveloperViewKey = true;

    //focused unit's tiles that are in range, first element is the unit's own location
    static ArrayList<Point> tilesInRange;
    //focused unit's enemy locations that are in range
    static ArrayList<Point> targetsInRange;
    //focused unit's path to where the mouse shows
    static ArrayList<Point> unitPathDrawn;

    public GameArea() {
        map = new Map();
        gameScreen = new Renderer(map);
        focusedTile = new Point();
        oldHover = new Point();
        tilesInRange = new ArrayList<>();
        targetsInRange = new ArrayList<>();
        unitPathDrawn = new ArrayList<>();
        playersTurn = true;
        focused = false;
    }

    public void renderScreen() {
        gameScreen.render();
    }

    int computerUnitIterator;
    public static boolean renderYourTurnLabel;
    int yourTurnTimer = 0;

    // Topmost logic class where game logic is handled. 
    // Called once at beginning of every cycle.  
    public void handleLogic(int x, int y) {

        // 1. Handle scrolling
        handleScrolling();

        mouseX = x;
        mouseY = y;
        if (getTileCoordinate(mouseX, mouseY) != null) {
            if (mouseY < screenHeight - 60 * m) {
                tileHovering = map.getTile(getTileCoordinate(mouseX, mouseY));
                hover = getTileCoordinate(mouseX, mouseY);
            }
        }

        // 2. First of all check winning conditions
        if (map.computerUnits.isEmpty()) {
            //System.out.println("YOU HAVE WON");
        }
        if (map.playerUnits.isEmpty()) {
            //System.out.println("YOU HAVE LOST");
        }
        // Clear focused selection if escape is pressed
        if (Input.keys[GLFW_KEY_ESCAPE] && playersTurn && focused) {
            focused = false;
            tilesInRange.clear();
            targetsInRange.clear();
            oldHover.setLocation(-1, -1);
        }

        // Check that none of the units are moving when pressing the end turn button
        boolean canEndTurn = true;
        for (Unit u : map.playerUnits) {
            if (u.isMoving() || u.isAttacking()) {
                canEndTurn = false;
            }
        }
        for (Unit u : map.computerUnits) {
            if (u.isMoving() || u.isAttacking()) {
                canEndTurn = false;
            }
        }

        //if 'x' key is pressed, toggle developer view    
        if (Input.keys[GLFW_KEY_X]) {
            if (canPressDeveloperViewKey) {
                canPressDeveloperViewKey = false;
                if (developerView) {
                    developerView = false;
                } else {
                    developerView = true;
                }
            }
        } else {
            canPressDeveloperViewKey = true;
        }

        // 3. Check if player unit is starting its attack (attackDir has some value on the first frame of the attack)
        // If true, then edit HP.
        for (Unit au : map.playerUnits) {
            if (RedAssault.timer % Unit.attackAnimTimer == 0 && au.attackDir != Direction.none && au.isAttacking()) {
                Unit t = map.getUnit(au.getTarget());
                if (playersTurn) {
                    au.setHasAttacked(true);
                }
                t.HP = t.HP - au.getAttack() - map.getTile(au.getTileLocation()).getAttackBonus() + map.getTile(t.getTileLocation()).getDefense();
                // If out of health, then the unit is destroyed
                if (t.HP <= 0) {
                    if (t.isTransporting()) {
                        Unit transportedUnit = map.getTransportedUnit(t.getTileLocation());
                        map.computerUnits.remove(transportedUnit);
                    }
                    map.computerUnits.remove(t);
                    au.increaseKillCount();
                    if (!playersTurn && !map.computerUnits.isEmpty()) {
                        computerUnitIterator = map.computerUnits.size();
                        Unit uNext = map.computerUnits.get(map.computerUnits.size() - 1);
                        handleComputerUnitsTurn(uNext);
                    }
                    map.getTile(t.getTileLocation()).setOccupied(false);
                    map.getTile(t.getTileLocation()).startDrawingExplosion();
                } else {
                    if (!t.hasAttacked()) {
                        attackUnit(t, au.getTileLocation());
                    }
                }
            }
            // Check if transportable unit has arrived to board transport vehicle FIXX DISSS!!
//            if(playersTurn && au.isTransporting() && !au.hasFinishedItsTurn() && !map.getTransportedUnit(au.getTileLocation()).isMoving() 
//                    && !map.getTransportedUnit(au.getTileLocation()).isBeingDisplayed()) {
//                au.setHasAttacked(true);
//                au.setHasMoved(true);
//            }
        }
        // Handle displaying "Your Turn" label for set amount of time
        if (renderYourTurnLabel) {
            if (yourTurnTimer++ > 70) {
                yourTurnTimer = 0;
                renderYourTurnLabel = false;
            }
        }

        // Check if computer unit is starting attack and edit HP
        for (Unit au : map.computerUnits) {
            if (RedAssault.timer % Unit.attackAnimTimer == 0 && au.attackDir != Direction.none && au.isAttacking()) {
                Unit t = map.getUnit(au.getTarget());
                if (!playersTurn) {
                    au.setHasAttacked(true);
                }
                t.HP = t.HP - au.getAttack() - map.getTile(au.getTileLocation()).getAttackBonus() + map.getTile(t.getTileLocation()).getDefense();
                if (t.HP <= 0) {
                    if (t.isTransporting()) {
                        Unit transportedUnit = map.getTransportedUnit(t.getTileLocation());
                        map.playerUnits.remove(transportedUnit);
                    }
                    map.playerUnits.remove(t);
                    au.increaseKillCount();
                    map.getTile(t.getTileLocation()).setOccupied(false);
                    map.getTile(t.getTileLocation()).startDrawingExplosion();
                } else {
                    if (!t.hasAttacked() && t.ammo > 0) {
                        if (t.getUnitType() == UnitType.rpg) {
                            if (au.getUnitCategory() == UnitCategory.armour) {
                                attackUnit(t, au.getTileLocation());
                            }
                        } else {
                            attackUnit(t, au.getTileLocation());
                        }
                    }
                }
            }
        }
        if (playersTurn) {
            // If player presses end turn (space or button on screen), then perform needed actions
            if (canEndTurn && ((Input.keys[GLFW_KEY_SPACE]) || (RedAssault.pressedDownRMB
                    && mouseX > Renderer.endButtonCoordinateX + m
                    && mouseY > Renderer.endButtonCoordinateY + m
                    && mouseX < Renderer.endButtonCoordinateX + 62 * m
                    && mouseY < Renderer.endButtonCoordinateY + 21 * m))) {
                tilesInRange.clear();
                targetsInRange.clear();
                focused = false;
                playersTurn = false;

                for (int i = 0; i < map.playerUnits.size(); i++) {
                    map.playerUnits.get(i).setHasMoved(false);
                    map.playerUnits.get(i).setHasAttacked(false);
                }
                // Start moving enemy units
                if (!map.computerUnits.isEmpty()) {
                    computerUnitIterator = map.computerUnits.size();
                    Unit u = map.computerUnits.get(map.computerUnits.size() - 1);
                    handleComputerUnitsTurn(u);
                }
            }
            // If player presses unload troops button, then perform needed actions
            if (focused && (Input.keys[GLFW_KEY_U] || (mouseButton == 1
                    && mouseX >= gameScreen.unload[0].getWidth()
                    && mouseY >= gameScreen.unload[0].getPosition().y + m
                    && mouseX < gameScreen.unload[0].getPosition().x + gameScreen.unload[0].getWidth()
                    && mouseY < gameScreen.unload[0].getPosition().y + gameScreen.unload[0].getHeight() - m))) {
                if (map.getUnit(focusedTile).isTransporting() && !map.getTransportedUnit(GameArea.focusedTile).hasFinishedItsTurn()) {
                    Unit u = map.getTransportedUnit(focusedTile);
                    map.playerUnits.remove(u);
                    map.playerUnits.add(u);
                    transportUnitSelected = true;
                    tilesInRange.clear();
                    targetsInRange.clear();

                    tilesInRange.add(new Point(focusedTile.x + 1, focusedTile.y));
                    tilesInRange.add(new Point(focusedTile.x - 1, focusedTile.y));
                    tilesInRange.add(new Point(focusedTile.x, focusedTile.y + 1));
                    tilesInRange.add(new Point(focusedTile.x, focusedTile.y - 1));
                    int i = 0;
                    while (i < tilesInRange.size()) {
                        Point p = tilesInRange.get(i);
                        if (p.x < 0 || p.y < 0 || p.x >= map.getWidth() || p.y >= map.getHeight()
                                || map.getTile(p).isOccupied() || map.getTile(p).getType() == TileType.water || map.getTile(p).getType() == TileType.mountain) {
                            tilesInRange.remove(i);
                        } else {
                            i++;
                        }
                    }
                    tilesInRange.add(focusedTile);
                }

            }
        } // If it's not the players turn handle computers turn.
        else {
            // First make sure that there are computer units alive
            if (!map.computerUnits.isEmpty()) {
                Unit u = map.computerUnits.get(map.computerUnits.size() - 1);
                // Before the next unit can move/attack check that the current one's target has finished his attack
                boolean targetHasFinishedAttackOrNoTarget = true;
                // To do this first check that there is a target, then that it is on the map and finally that its not attacking
                if (u.getTarget() != null) {
                    if (map.getUnit(u.getTarget()) != null) {
                        if (map.getUnit(u.getTarget()).isAttacking()) {
                            targetHasFinishedAttackOrNoTarget = false;
                        }
                    }
                }
                // If the current unit has ended its turn, select the next one.
                if (u.hasAttacked() && !u.isAttacking() && !u.isMoving() && targetHasFinishedAttackOrNoTarget) {
                    computerUnitIterator--;
                    map.computerUnits.remove(u);
                    map.computerUnits.add(0, u);
                    if (computerUnitIterator > 0) {
                        Unit uNext = map.computerUnits.get(map.computerUnits.size() - 1);
                        handleComputerUnitsTurn(uNext);
                    } // If all computer's units have moved, its turn ends and players's begins.
                    else {
                        turnCounter++;
                        playersTurn = true;
                        renderYourTurnLabel = true;
                        for (int i = 0; i < map.computerUnits.size(); i++) {
                            map.computerUnits.get(i).setHasMoved(false);
                            map.computerUnits.get(i).setHasAttacked(false);
                        }
                    }
                }
            } // If computer unit list is empty, then end computer's turn (temporary! so that player can move)
            else {
                turnCounter++;
                playersTurn = true;
                computerUnitIterator = 0;
                for (int i = 0; i < map.computerUnits.size(); i++) {
                    map.computerUnits.get(i).setHasMoved(false);
                    map.computerUnits.get(i).setHasAttacked(false);
                }
            }
        }
    }

    // AI function
    // Handles a single computer unit's turn by finding closest enemy and attacking it
    private void handleComputerUnitsTurn(Unit u) {
        map.getTile(u.getTileLocation()).setOccupied(false);
        tilesInRange.add(u.getTileLocation());
        findTilesInRange(u);
        findTargetsInRange(u);

        ArrayList<Point> pathToEnemy = null;
        Unit closestEnemy = null;
        int distanceToClosestEnemy = Integer.MAX_VALUE;
        // First find the closest player unit (check that path to enemy is reachable ie. not null)
        for (Unit enemy : map.playerUnits) {
            pathToEnemy = findPath(u.getTileLocation(), enemy.getTileLocation());
            if (pathToEnemy != null && pathToEnemy.size() < distanceToClosestEnemy) {
                distanceToClosestEnemy = pathToEnemy.size();
                closestEnemy = enemy;
            }
        }
        // Check that there are player units left. If not... well then you've lost!
        if (closestEnemy != null) {
            pathToEnemy = findPath(u.getTileLocation(), closestEnemy.getTileLocation());
            // If enemy is in target range, then attack it. OLD: && pathToEnemy.size() <= u.getMovementRange() &&
            if (targetsInRange.contains(closestEnemy.getTileLocation()) && tilesInRange.contains(pathToEnemy.get(pathToEnemy.size() - 1))) {
                attackUnit(u, closestEnemy.getTileLocation());
            } // Otherwise move the tile that is closest to it ( the first one that is in range and 
            //   not occupied when coming back the path from the enemy to the computer unit)
            else {
                if (pathToEnemy != null) {
                    for (int i = pathToEnemy.size() - 1; i >= 0; i--) {
                        if (tilesInRange.contains(pathToEnemy.get(i)) && !map.getTile(pathToEnemy.get(i)).isOccupied()) {
                            moveUnit(u, pathToEnemy.get(i));
                            //the enemy unit hasn't 'really' attacked anyone, but this signals that its turn is over
                            u.setHasAttacked(true);
                            break;
                        }
                    }
                }
            }
        }
        tilesInRange.clear();
        targetsInRange.clear();
    }

    private void print(Object s) {
        System.out.print(s);
    }

    private String pointToString(Point p) {
        return "(" + p.x + "," + p.y + ")";
    }

    private void println(Object s) {
        System.out.println(s);
    }

    private void handleScrolling() {
        if (mouseX == 0 || Input.keys[GLFW_KEY_LEFT] || Input.keys[GLFW_KEY_A]) {
            scrollMap(Direction.left);
        }
        if (mouseX == screenWidth - 1 || Input.keys[GLFW_KEY_RIGHT] || Input.keys[GLFW_KEY_D]) {
            scrollMap(Direction.right);
        }
        if (mouseY == 0 || Input.keys[GLFW_KEY_UP] || Input.keys[GLFW_KEY_W]) {
            scrollMap(Direction.up);
        }
        if (mouseY == screenHeight - 1 || Input.keys[GLFW_KEY_DOWN] || Input.keys[GLFW_KEY_S]) {
            scrollMap(Direction.down);
        }
    }

    private void scrollMap(Direction d) {
        int changeX = 0;
        int changeY = 0;
        // By default we scroll the screen this much during each update, but if we are on the edge
        // of the map and only parrt less than this value is visible, we change mov to this correct amount
        int mov = tileSize / 5;
        Point leftTopCorner = map.tiles[0][0].getScreenLocation();
        Point rightBottomCorner = map.tiles[map.getWidth() - 1][map.getHeight() - 1].getScreenLocation();

        switch (d) {
            case left:
                if (-leftTopCorner.x < mov) {
                    mov = -leftTopCorner.x;
                }
                changeX = mov;
                break;
            case right:
                if (rightBottomCorner.x + tileSize - screenWidth < mov) {
                    mov = rightBottomCorner.x + tileSize - screenWidth;
                }
                changeX = -mov;
                break;
            case up:
                if (-leftTopCorner.y < mov) {
                    mov = -leftTopCorner.y;
                }
                changeY = mov;
                break;
            case down:
                if (rightBottomCorner.y + tileSize - bottomOfGameArea < mov) {
                    mov = rightBottomCorner.y + tileSize - bottomOfGameArea;
                }
                changeY = -mov;
                break;
            default:
                break;
        }
        // If added change doesnt make map show background then scroll the map
        if (!outOfScreen(changeX, changeY)) {
            //scroll map tiles
            for (int i = 0; i < map.getWidth(); i++) {
                for (int j = 0; j < map.getHeight(); j++) {
                    Tile t = map.tiles[i][j];
                    t.setScreenLocation(t.getScreenLocation().x + changeX, t.getScreenLocation().y + changeY);
                }
            }
            //scroll unit's and path coordinates locations
            for (int i = 0; i < map.playerUnits.size(); i++) {
                Unit u = map.playerUnits.get(i);
                u.setScreenLocation(u.getScreenLocation().x + changeX, u.getScreenLocation().y + changeY);
                u.scrollPathCoordinates(changeX, changeY);
            }
            for (int i = 0; i < map.computerUnits.size(); i++) {
                Unit u = map.computerUnits.get(i);
                u.setScreenLocation(u.getScreenLocation().x + changeX, u.getScreenLocation().y + changeY);
                u.scrollPathCoordinates(changeX, changeY);
            }
        }
    }

    // Returns true if tiles (with added change) are out of bound
    private boolean outOfScreen(int changeX, int changeY) {
        Tile t = map.tiles[0][0];
        //if top-most left tile is more right or down than should be (showing black screen) map is out of screen
        if (t.getScreenLocation().x + changeX > 0 || t.getScreenLocation().y + changeY > 0) {
            return true;
        }
        t = map.tiles[map.getWidth() - 1][map.getHeight() - 1];
        //if bottom-most right tile is more left or up than should be(showing black screen) map is out of screen
        if (t.getScreenLocation().x + tileSize + changeX < screenWidth
                || t.getScreenLocation().y + tileSize + changeY < bottomOfGameArea) {
            return true;
        }
        return false;
    }

    // When the mouse is clicked, this function is called. Value of int button: 1=LMB 2=MMB 3=RMB
    public void mouseLogic(int button) {
        mouseButton = button;
        boolean noPlayerUnitsMoving = true;

        for (Unit u : map.playerUnits) {
            if (u.isMoving() || u.isAttacking()) {
                noPlayerUnitsMoving = false;
            }
        }
        // First make sure it is legal to click mouse
        if (mouseY >= 0 && mouseY < bottomOfGameArea && playersTurn && noPlayerUnitsMoving) {
            Point clickedTile = getTileCoordinate(mouseX, mouseY);
            // Select unit if not focused and clicked tile with LMB is occupied by one
            if (button == 1 && !focused && map.getTile(clickedTile).isOccupied() && !isEnemy(clickedTile)) {
                Unit u = map.getUnit(clickedTile);
                // If unit hasn't done finished it's turn and...
                if (!u.isMoving() && !u.isAttacking() && !u.hasAttacked()) {
                    // ...hasn't moved, then make it focused and display movement range and targets
                    if (!u.hasMoved()) {
                        focused = true;
                        focusedTile.setLocation(clickedTile);
                        tilesInRange.add(focusedTile);
                        map.getTile(focusedTile).movementScore = Integer.MAX_VALUE;
                        findTilesInRange(u);
                        findTargetsInRange(u);
                    } // ...has moved, then select it and display possible targets if there are any 
                    else {
                        tilesInRange.add(clickedTile);
                        targetsInRange = getTargetsInCloseRange(clickedTile);
                        if (targetsInRange.isEmpty()) {
                            tilesInRange.clear();
                            targetsInRange.clear();
                        } else {
                            focused = true;
                            focusedTile.setLocation(clickedTile);
                        }
                    }
                    // Make transport transport unit focused, so it can unload its cargo after moving.
                    if (u.hasMoved() && u.isTransporting() && !map.getTransportedUnit(u.getTileLocation()).hasFinishedItsTurn()) {
                        focused = true;
                        focusedTile.setLocation(clickedTile);
                        Unit t = map.getTransportedUnit(focusedTile);
                        map.playerUnits.remove(t);
                        map.playerUnits.add(t);
                        transportUnitSelected = true;
                        tilesInRange.clear();

                        tilesInRange.add(new Point(focusedTile.x + 1, focusedTile.y));
                        tilesInRange.add(new Point(focusedTile.x - 1, focusedTile.y));
                        tilesInRange.add(new Point(focusedTile.x, focusedTile.y + 1));
                        tilesInRange.add(new Point(focusedTile.x, focusedTile.y - 1));
                        int i = 0;
                        while (i < tilesInRange.size()) {
                            Point p = tilesInRange.get(i);
                            if (p.x < 0 || p.y < 0 || p.x >= map.getWidth() || p.y >= map.getHeight()
                                    || map.getTile(p).isOccupied() || map.getTile(p).getType() == TileType.water || map.getTile(p).getType() == TileType.mountain) {
                                tilesInRange.remove(i);
                            } else {
                                i++;
                            }
                        }
                        tilesInRange.add(focusedTile);
                    }
                }
            }
            // If LMB clicked on a tile in a unit's movement range, then move it there
            if (button == 1 && focused && !map.getTile(clickedTile).isOccupied() && tilesInRange.contains(clickedTile)) {
                Unit u;
                if (transportUnitSelected) {
                    transportUnitSelected = false;
                    u = map.getTransportedUnit(focusedTile);
                    map.getUnit(focusedTile).setHasAttacked(true);
                    map.getUnit(focusedTile).setHasMoved(true);
                    map.getUnit(focusedTile).loadedTrans = false;
                    u.loadedTrans = false;
                    println("moving unit");
                    u.displayUnit();
                    map.playerUnits.remove(u);
                    map.playerUnits.add(u);
                    moveUnit(u, clickedTile);
                    map.getTile(focusedTile).setOccupied(true);
                    focused = false;
                    tilesInRange.clear();
                    targetsInRange.clear();
                    oldHover.setLocation(-1, -1);
                } else {
                    u = map.getUnit(focusedTile);
                    if (!u.hasMoved()) {
                        map.playerUnits.remove(u);
                        map.playerUnits.add(u);
                        moveUnit(u, clickedTile);
                        focused = false;
                        tilesInRange.clear();
                        targetsInRange.clear();
                        oldHover.setLocation(-1, -1);
                    }
                }
            }
            // If LMB clicked on a target, then attack it
            if (button == 1 && focused && targetsInRange.contains(clickedTile)) {
                ArrayList<Point> unitPath = findPath(focusedTile, clickedTile);
                // Make sure target is in movement range by checking that hover is in range
                if (tilesInRange.contains(unitPath.get(unitPath.size() - 1))) {
                    Unit u = map.getUnit(focusedTile);
                    Unit t = map.getUnit(clickedTile);
                    if (!t.isAttacking() && !t.isMoving()) {
                        map.playerUnits.remove(u);
                        map.playerUnits.add(u);
                        attackUnit(u, clickedTile);
                        focused = false;
                        tilesInRange.clear();
                        targetsInRange.clear();
                        oldHover.setLocation(-1, -1);
                    }
                }
            }
            // If LMB is clicked on a boardable friendly transport unit by a inf unit, then board it
            if (button == 1 && focused && tilesInRange.contains(clickedTile)
                    && map.getTile(clickedTile).isOccupied() && !clickedTile.equals(focusedTile)
                    && map.getUnit(focusedTile).getUnitCategory() == UnitCategory.inf
                    && map.getUnit(clickedTile).isTransport()
                    && !map.getUnit(clickedTile).loadedTrans) {
                Unit u = map.getUnit(focusedTile);
                Unit b = map.getUnit(clickedTile);
                map.playerUnits.remove(u);
                map.playerUnits.add(u);
                moveUnit(u, clickedTile);
                focused = false;
                tilesInRange.clear();
                targetsInRange.clear();
                oldHover.setLocation(-1, -1);

                u.loadedTrans = true;
                b.loadedTrans = true;
            }
            // If RMB is clicked, then clear selection and remove focus.
            if (button == 3 && focused) {
                if (transportUnitSelected) {
                    transportUnitSelected = false;
                }
                focused = false;
                tilesInRange.clear();
                targetsInRange.clear();
                oldHover.setLocation(-1, -1);
            }
        }
    }

    // Unit u performs an attack on the target which is located at point p.
    private void attackUnit(Unit u, Point target) {
        gameScreen.attackOffsetY = 0;
        ArrayList<Point> unitPath = findPath(u.getTileLocation(), target);
        u.setTarget(target);
        // If unit is already next to enemy target, then start attacking straight away
        if (unitPath.get(unitPath.size() - 1).equals(target)) {
            // Set attack direction
            if (u.getTileLocation().x > target.x) {
                u.setAttackDir(Direction.left);
            } else if (u.getTileLocation().x < target.x) {
                u.setAttackDir(Direction.right);
            } else if (u.getTileLocation().y > target.y) {
                u.setAttackDir(Direction.up);
            } else {
                u.setAttackDir(Direction.down);
            }
            u.setAttackFlag();
        } // Otherwise move there first
        else {
            //set attack direction, when reaches clicked tile (second last index of path list)
            if (unitPath.get(unitPath.size() - 1).x > target.x) {
                u.setAttackDir(Direction.left);
            } else if (unitPath.get(unitPath.size() - 1).x < target.x) {
                u.setAttackDir(Direction.right);
            } else if (unitPath.get(unitPath.size() - 1).y > target.y) {
                u.setAttackDir(Direction.up);
            } else {
                u.setAttackDir(Direction.down);
            }
            map.getTile(u.getTileLocation()).setOccupied(false);
            Point goal = unitPath.get(unitPath.size() - 1);
            map.tiles[goal.x][goal.y].setOccupied(true);

            // If the unit is transporting troops, move them there as well
            if (u.isTransporting()) {
                Unit t = map.getTransportedUnit(u.getTileLocation());
                t.setTileLocation(goal.x, goal.y);
                t.setScreenLocation(tileToScreenLocation(goal));
            }
            u.setTileLocation(unitPath.get(unitPath.size() - 1));
            u.startMoving(convertPathToScreenLocation(unitPath));
        }
    }

    // This function moves given unit u to the goal tile coordinate.
    private void moveUnit(Unit u, Point goal) {
        ArrayList<Point> cpuPath = findPath(u.getTileLocation(), goal);
        map.getTile(u.getTileLocation()).setOccupied(false);
        map.getTile(goal).setOccupied(true);

        // If the unit is transporting troops, move them there as well
        if (u.isTransporting()) {
            Unit t = map.getTransportedUnit(u.getTileLocation());
            t.setTileLocation(goal.x, goal.y);
            t.setScreenLocation(tileToScreenLocation(goal));
        }

        u.startMoving(convertPathToScreenLocation(cpuPath));
        u.setTileLocation(goal.x, goal.y);
        u.setHasMoved(true);

        targetsInRange = getTargetsInCloseRange(goal);
        // TODO: move this out maybe?
        // If player's unit has no targets in close range, then make it inactive.
        if (isSoviet(u.getTileLocation()) && targetsInRange.isEmpty()) {
            if (u.isTransport()) {
                if (!u.isTransporting()) {
                    u.setHasAttacked(true);
                } else {
                    if (map.getTransportedUnit(u.getTileLocation()).hasFinishedItsTurn()) {
                        u.setHasAttacked(true);
                    }
                }
            } else {
                u.setHasAttacked(true);
            }
        }
        targetsInRange.clear();
    }

    // Returns true if point (x, y) is occupied by an enemy. Boolean parameter 'selectedUnitIsSoviet'
    // clarifies for whom the point is an enemy. If it is true then the selected unit is soviet,
    // otherwise it is assumed the selected unit american.
    public boolean isEnemy(int x, int y, boolean selectedUnitIsSoviet) {
        if (selectedUnitIsSoviet) {
            for (int i = 0; i < map.computerUnits.size(); i++) {
                Unit u = map.computerUnits.get(i);
                if (u.getTileLocation().x == x && u.getTileLocation().y == y) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < map.playerUnits.size(); i++) {
                Unit u = map.playerUnits.get(i);
                if (u.getTileLocation().x == x && u.getTileLocation().y == y) {
                    return true;
                }
            }
        }
        return false;
    }

    // If the 'selectedUnitIsSoviet' boolean parameter is ommited then isEnemy() returns
    // an answer depending on whose turn it is. If it is players turn, then american units are
    // considered enemies and vice versa.
    public boolean isEnemy(int x, int y) {
        return isEnemy(x, y, playersTurn);
    }

    public boolean isEnemy(Point p, boolean selectedUnitIsSoviet) {
        return isEnemy(p.x, p.y, selectedUnitIsSoviet);
    }

    public boolean isEnemy(Point p) {
        return isEnemy(p.x, p.y);
    }

    public boolean isSoviet(Point p) {
        for (int i = 0; i < map.playerUnits.size(); i++) {
            Unit u = map.playerUnits.get(i);
            if (u.getTileLocation().x == p.x && u.getTileLocation().y == p.y) {
                return true;
            }
        }
        return false;
    }

    private void findTilesInRange(Unit u) {
        int movementRange = u.getMovementRange();
        // Check that there is enough fuel (if not change mov. range to how much is left)
        if (u.fuel + 1 < movementRange) {
            movementRange = u.fuel + 1;
        }
        recursiveRangeFind(u.getTileLocation(), movementRange, u);
        //set all tiles movementScore back to zero
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                map.tiles[i][j].movementScore = 0;
            }
        }
        // Find road paths for vehciles and add +1
        if (map.getTile(u.getTileLocation()).getType() == TileType.road
                && u.getUnitCategory() == UnitCategory.armour) {
            ArrayList<Point> visited = new ArrayList<>();
            visited.add(u.getTileLocation());
            recursiveRoadFind(u.getTileLocation(), movementRange + 1, visited, u);
        }
    }

    // Recursive function that find all the tiles in range
    private void recursiveRangeFind(Point currentTile, int movesLeft, Unit u) {
        if (currentTile.x >= 0 && currentTile.y >= 0 && currentTile.x < map.getWidth() && currentTile.y < map.getHeight()) {
            movesLeft = movesLeft - map.getTile(currentTile).getTerrainCost();
            UnitCategory category = u.getUnitCategory();
            Point neighbour = null;
            // Loop through all the neighbours
            for (int i = 0; i < 4; i++) {
                if (i == 0) {
                    neighbour = new Point(currentTile.x + 1, currentTile.y);
                }
                if (i == 1) {
                    neighbour = new Point(currentTile.x, currentTile.y - 1);
                }
                if (i == 2) {
                    neighbour = new Point(currentTile.x - 1, currentTile.y);
                }
                if (i == 3) {
                    neighbour = new Point(currentTile.x, currentTile.y + 1);
                }
                if (neighbour.x >= 0 && neighbour.y >= 0 && neighbour.x < map.getWidth() && neighbour.y < map.getHeight()) {
                    boolean waterConditionOK = false;
                    if (u.isLandUnit() && map.getTile(neighbour).getType() != TileType.water) {
                        waterConditionOK = true;
                    }
                    if (category == UnitCategory.water && map.getTile(neighbour).getType() == TileType.water) {
                        waterConditionOK = true;
                    }
                    // If neighbour is not already added, it is not a water tile or enemy and
                    // there are enough moves left, then add it
                    if (!isEnemy(neighbour, isSoviet(u.getTileLocation())) && waterConditionOK) {
                        if (movesLeft - map.getTile(neighbour).getTerrainCost() >= 0) {
                            if (movesLeft >= map.getTile(neighbour).movementScore) {
                                tilesInRange.add(neighbour);
                                map.getTile(neighbour).movementScore = movesLeft;
                                recursiveRangeFind(neighbour, movesLeft, u);
                            }
                        }
                    }
                }
            }
        }
    }

    private void recursiveRoadFind(Point currentTile, int movesLeft, ArrayList visited, Unit u) {
        if (currentTile.x >= 0 && currentTile.y >= 0 && currentTile.x < map.getWidth() && currentTile.y < map.getHeight()) {
            movesLeft--;
            Point neighbour = null;
            // Loop through all the neighbours
            for (int i = 0; i < 4; i++) {
                if (i == 0) {
                    neighbour = new Point(currentTile.x + 1, currentTile.y);
                }
                if (i == 1) {
                    neighbour = new Point(currentTile.x, currentTile.y - 1);
                }
                if (i == 2) {
                    neighbour = new Point(currentTile.x - 1, currentTile.y);
                }
                if (i == 3) {
                    neighbour = new Point(currentTile.x, currentTile.y + 1);
                }
                if (neighbour.x >= 0 && neighbour.y >= 0 && neighbour.x < map.getWidth() && neighbour.y < map.getHeight()) {
                    if (map.getTile(neighbour).getType() == TileType.road && movesLeft > 0) {
                        if (!isEnemy(neighbour, isSoviet(u.getTileLocation())) && !visited.contains(neighbour)) {
                            visited.add(neighbour);
                            if (!tilesInRange.contains(neighbour)) {
                                tilesInRange.add(neighbour);
                            }
                            recursiveRoadFind(neighbour, movesLeft, visited, u);
                        }
                    }
                }
            }
        }

    }

    private void findTargetsInRange(Unit u) {
        if (u.ammo > 0) {
            for (int i = 0; i < tilesInRange.size(); i++) {
                Point currentTile = tilesInRange.get(i);
                Point neighbour = null;
                // Loop through all the neighbours
                for (int j = 0; j < 4; j++) {
                    if (j == 0) {
                        neighbour = new Point(currentTile.x + 1, currentTile.y);
                    }
                    if (j == 1) {
                        neighbour = new Point(currentTile.x, currentTile.y - 1);
                    }
                    if (j == 2) {
                        neighbour = new Point(currentTile.x - 1, currentTile.y);
                    }
                    if (j == 3) {
                        neighbour = new Point(currentTile.x, currentTile.y + 1);
                    }
                    if (mouseButton != 3) {
                        if (isEnemy(neighbour, isSoviet(u.getTileLocation())) && !targetsInRange.contains(neighbour)) {
                            // Check if unit is capable of attacking
                            if (u.getUnitType() == UnitType.rpg) {
                                if (map.getUnit(neighbour).getUnitCategory() == UnitCategory.armour) {
                                    targetsInRange.add(neighbour);
                                }
                            } else {
                                targetsInRange.add(neighbour);
                            }
                        }
                    } // If RMB is held, then add everything for showing range
                    else {
                        targetsInRange.add(neighbour);
                    }
                }
            }
        }
    }

    private ArrayList<Point> getTargetsInCloseRange(Point p) {
        ArrayList<Point> targetNeighbours = new ArrayList<>();
        if (isEnemy(p.x + 1, p.y)) {
            targetNeighbours.add(new Point(p.x + 1, p.y));
        }
        if (isEnemy(p.x - 1, p.y)) {
            targetNeighbours.add(new Point(p.x - 1, p.y));
        }
        if (isEnemy(p.x, p.y + 1)) {
            targetNeighbours.add(new Point(p.x, p.y + 1));
        }
        if (isEnemy(p.x, p.y - 1)) {
            targetNeighbours.add(new Point(p.x, p.y - 1));
        }
        return targetNeighbours;
    }

    // Returns path using A* algorithm. If boolean ignoreEnemies is true, then path can go through enemies
    public ArrayList<Point> findPath(Point start, Point goal) {
        ArrayList<Point> open = new ArrayList<>();
        ArrayList<Point> closed = new ArrayList<>();
        ArrayList<Point> neighbours;
        open.add(start);

//        println("FINDING PATH TO " + getString(goal));
        while (!open.isEmpty()) {
//            print("   Open list: ");
//            for (int i = 0; i < open.size(); i++) {
//                print(getString(open.get(i)));
//                print(":" + map.getTile(open.get(i)).f + ", ");
//            }
//            println("");
            Point current = getLowestFCost(open);
//            println("   Lowest F cost: " + getString(current));
            //if current node is the goal, then path has been found
            if (current.equals(goal)) {
                ArrayList<Point> path = buildPath(current, new ArrayList<>());
                //if goal is an enemy, select the second to last tile as the destination
                if (isEnemy(goal)) {
                    path.remove(path.size() - 1);
                }
                return path;
            }
            open.remove(current);
            closed.add(current);

            // Set current tile's neigbours and check them
            neighbours = getNeighbours(start, current, goal);
            for (int i = 0; i < neighbours.size(); i++) {
                // Get current step cost and distance from current to neighbour
                int stepCost = map.getTile(current).g + map.getTile(neighbours.get(i)).getTerrainCost();
                // Check if neighbour is in closed set and compare its cost to stepCost. If less, then skip current neighbour
                if (closed.contains(neighbours.get(i)) && stepCost >= map.getTile(neighbours.get(i)).g) {
                    continue;
                }
                // If neighbour doesn't exist in open or new score for it is better then add it to open
                if (!open.contains(neighbours.get(i)) || stepCost < map.getTile(neighbours.get(i)).g) {
                    //  TODO || (stepCost == map.getTile(neighbours.get(i)).g && map.getTile(neighbours.get(i)).getType() == TileType.road)) {
                    map.getTile(neighbours.get(i)).parent = current;
                    map.getTile(neighbours.get(i)).g = stepCost;
                    map.getTile(neighbours.get(i)).f = stepCost + getHeuristicCost(neighbours.get(i), goal);
                    if (!open.contains(neighbours.get(i))) {
                        if (map.getTile(neighbours.get(i)).getType() == TileType.road) {
                            open.add(0, neighbours.get(i));
                        } else {
                            open.add(neighbours.get(i));
                        }

                    }
                }
            }
        }
        return null;
    }

    // Returns the h cost, in this case Manhattan distance between two points
    private int getHeuristicCost(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    // This function returns a list of adjacent neighbours of tile p. Used when finding A* path.
    // Parameters: 'start' is the starting location of the unit whose path were trying to find. 
    //             'p' is the tile location which neighbours we are trying to find.
    //             'goal' is the goal were trying to reach
    private ArrayList<Point> getNeighbours(Point start, Point p, Point goal) {
        boolean goalIsEnemy = isEnemy(goal);
        ArrayList<Point> neighbours = new ArrayList<>();
        Point n = null;
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    n = new Point(p.x, p.y - 1);
                    break;
                case 1:
                    n = new Point(p.x, p.y + 1);
                    break;
                case 2:
                    n = new Point(p.x + 1, p.y);
                    break;
                case 3:
                    n = new Point(p.x - 1, p.y);
                    break;
            }
            // This block of code decided if the adjacent tile should be added to neighbour list
            if (n.x >= 0 && n.y >= 0 && n.x < map.getWidth() && n.y < map.getHeight()) {
                // First check terrain condidtions
                if (map.tiles[n.x][n.y].getType() != TileType.water) {
                    // If the target goal is an enemy...
                    if (goalIsEnemy) {
                        // ..and the current tile is also the starting tile
                        if (p.equals(start)) {
                            // ...and the neighbour we're looking at is the goal, then add it
                            if (goal.equals(n)) {
                                neighbours.add(n);
                            } // ...or if the neighbour isn't the goal and not an enemy, then add it
                            else if (!isEnemy(n)) {
                                neighbours.add(n);
                            }
                        } // If neighbour n is goal, check that current tile p is not occupied
                        else if (goal.equals(n)) {
                            if (isEnemy(n) && !map.getTile(p).isOccupied()) {
                                neighbours.add(n);
                            }
                        } //otherwise avoid enemy occupied tiles 
                        else if (!isEnemy(n)) {
                            neighbours.add(n);
                        }
                    } //if the goal is not an enemy, then just avoid enemies 
                    else {
                        if (!isEnemy(n)) {
                            neighbours.add(n);
                        }
                    }
                }
            }
        }
        return neighbours;
    }

    private Point tileToScreenLocation(Point p) {
        return new Point(map.getTile(p).getScreenLocation().x, map.getTile(p).getScreenLocation().y);
    }

    private Point getLowestFCost(ArrayList<Point> list) {
        int lowestCostIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            if (map.getTile(list.get(i)).f < map.getTile(list.get(lowestCostIndex)).f) {
                lowestCostIndex = i;
            }
        }
        return list.get(lowestCostIndex);
    }

    // Builds path recursively (essentially reverses the path and returns it).
    private ArrayList<Point> buildPath(Point p, ArrayList<Point> stack) {
        stack.add(p);
        if (map.getTile(p).parent != null) {
            return buildPath(map.getTile(p).parent, stack);
        } else {
            Collections.reverse(stack);
            // Reset all the tiles' parents
            for (int i = 0; i < map.getWidth(); i++) {
                for (int j = 0; j < map.getHeight(); j++) {
                    map.tiles[i][j].parent = null;
                }
            }
            return stack;
        }
    }

    // Converts path's tile locations to screen locations.
    private ArrayList<Point> convertPathToScreenLocation(ArrayList<Point> path) {
        for (int i = 0; i < path.size(); i++) {
            path.set(i, tileToScreenLocation(path.get(i)));
        }
        return path;
    }

    // Updates the on screen graphics, called before rendering.
    void updateGraphics() {
        // Update tiles and units
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                Tile t = map.tiles[i][j];
                if (t.getScreenLocation().y < screenHeight - 60 * m) {
                    t.updateTile();
                }
            }
        }
        for (Unit u : map.playerUnits) {
            Point p = getTileCoordinate(u.getScreenLocation().x, u.getScreenLocation().y);
            u.updateUnit(map.getTile(p).getTerrainCost());
        }
        for (Unit u : map.computerUnits) {
            u.updateUnit(map.getTile(u.getTileLocation()).getTerrainCost());
        }
        // Update path arrow TODO: fix calculating for no reason path when not shown
        if (focused) {
            // Check previous mouse hover tile, if its different then calculate (so that we don't calculate the path needlessly)
            if (tilesInRange.contains(hover) || targetsInRange.contains(hover)) {
                drawPath = true;
                if (hover != null && !GameArea.oldHover.equals(hover)) {
                    unitPathDrawn = findPath(GameArea.focusedTile, hover);
                    oldHover = hover;
                    /* This if clause is to make sure that the path is in the units range. For example:
                     ------
                     ---UE-
                     -U--E-
                     ------
                     Here the left unit (mov. range 5) would be able to attack the top enemy if this check wasn't done.
                     The enemy is within attack range, but only if the friendly unit moves away.
                     */
                    if (unitPathDrawn != null) {
                        // Make sure goal is within range
                        if (!tilesInRange.contains(unitPathDrawn.get(unitPathDrawn.size() - 1))) {
                            unitPathDrawn = null;
                        }
                        // Also that the target tile is not occupied by friendly troops
                        if (isSoviet(hover) || (!tilesInRange.contains(hover) && !targetsInRange.contains(hover))) {
                            if (!map.getUnit(hover).isTransport() || map.getUnit(focusedTile).getUnitCategory() != UnitCategory.inf) {
                                unitPathDrawn = null;
                            } else {
//                                unitPathDrawn.remove(unitPathDrawn.size() - 1);
                            }
                        }
                    }
                }
            } else {
                drawPath = false;
            }
        }
        Point hover = getTileCoordinate(GameArea.mouseX, GameArea.mouseY);
        if (hover != null) {
            gameScreen.updateMouse(hover, isEnemy(hover));
        }
        // Find unit movement range if right mouse button is pressed
        if (!focused && mouseButton == 3) {
            if (tileHovering.isOccupied()) {
                Unit u = map.getUnit(tileHovering.getTileLocation());
                if (!u.isAttacking() && !u.isMoving()) {
                    focusedTile.setLocation(tileHovering.getTileLocation());
                    tilesInRange.add(focusedTile);
                    findTilesInRange(map.getUnit(tileHovering.getTileLocation()));
                    findTargetsInRange(map.getUnit(tileHovering.getTileLocation()));//                
                }
            }
        }
    }

    public Point getTileCoordinate(int mouseX, int mouseY) {
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
}
