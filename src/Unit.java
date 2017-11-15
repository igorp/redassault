/* Red Assault
 * Unit.java by Igor P.
 * Contains a unit object
 */

import java.awt.Point;
import java.util.ArrayList;

enum UnitType {

    t72, bmp, conscript, bradley, apache, mi24, battleship, paladin, katyusha, marine, abrams,
    rpg
};

enum UnitCategory {

    armour, inf, air, water
}

public class Unit extends MapEntity {

    static int tileSize = RedAssault.m * RedAssault.tileSizeSource;
    static int attackAnimTimer = 8;
    int movementAnimTimer = 2;

    // Unit properties
    private UnitType type;
    private int movSpeed;
    private int movementRange;
    private UnitCategory category;
    private int maxHP;
    private int maxAmmo;
    private int maxFuel = 100;
    private int attack;

    // Unit variables
    public int HP;
    private int kills = 0;
    public int ammo;
    public int fuel = 100;
    // Relevant only to inf and trans units.
    public boolean loadedTrans = false;

    private Direction dir;
    private boolean displayUnit = true;
    private boolean moving = false;
    private boolean attacking = false;
    // hasMoved is set to true once unit starts moving, so even if it hasnt finished value is True.
    private boolean hasMoved = false;
    private boolean hasAttacked = false;
    //which direction the unit turns when attacking
    public Direction attackDir;
    private Point target;

    // movement path
    private ArrayList<Point> path = new ArrayList<>();

    //protected TileImageEntity image;
    // protected Image image;
    protected TileImageEntity tracks;

    protected TileImageEntity rightTracks[] = new TileImageEntity[4];
    protected TileImageEntity leftTracks[] = new TileImageEntity[4];

    protected TileImageEntity rightBody;
    protected TileImageEntity leftBody;
    protected TileImageEntity rightBodyGray;
    protected TileImageEntity leftBodyGray;

    protected TileImageEntity upBody;
    protected TileImageEntity downBody;
    protected TileImageEntity upBodyGray;
    protected TileImageEntity downBodyGray;

    protected TileImageEntity rightMove[] = new TileImageEntity[10];
    protected TileImageEntity leftMove[] = new TileImageEntity[10];
    protected TileImageEntity downMove[] = new TileImageEntity[8];
    protected TileImageEntity upMove[] = new TileImageEntity[8];

    protected TileImageEntity rightAttack[] = new TileImageEntity[4];
    protected TileImageEntity leftAttack[] = new TileImageEntity[4];
    protected TileImageEntity upAttack[] = new TileImageEntity[4];
    protected TileImageEntity downAttack[] = new TileImageEntity[4];

    public Unit() {
    }

    public Unit(String typeStr) {
        super();
        if (typeStr.equals("t72")) {
            type = UnitType.t72;
            typeString = "T-72 Tank";
            category = UnitCategory.armour;
            dir = Direction.right;
            HP = maxHP = 30;
            attack = 8;
            movementRange = 5;
            movSpeed = 5;
            ammo = maxAmmo = 40;
            fuel = maxFuel = 30;
        }
        if (typeStr.equals("conscript")) {
            type = UnitType.conscript;
            typeString = "Conscript";
            category = UnitCategory.inf;
            dir = Direction.right;
            HP = maxHP = 10;
            attack = 2;
            movementRange = 4;
            // speed 3 and timer 2, OR speed 2 timer 3 (latter looks better?)
            movSpeed = 2;
            movementAnimTimer = 3;
            ammo = maxAmmo = 20;
        }
        if (typeStr.equals("rpg")) {
            type = UnitType.rpg;
            typeString = "RPG-soldier";
            category = UnitCategory.inf;
            dir = Direction.right;
            HP = maxHP = 10;
            attack = 10;
            movementRange = 4;
            // speed 3 and timer 2, OR speed 2 timer 3 (latter looks better?)
            movSpeed = 2;
            movementAnimTimer = 3;
            ammo = maxAmmo = 8;
        }
        if (typeStr.equals("marine")) {
            type = UnitType.marine;
            typeString = "US Marine";
            category = UnitCategory.inf;
            dir = Direction.left;
            HP = maxHP = 10;
            attack = 2;
            movementRange = 4;
            // speed 3 and timer 2, OR speed 2 timer 3 (latter looks better?)
            movSpeed = 2;
            movementAnimTimer = 3;
            ammo = maxAmmo = 20;
        }
        if (typeStr.equals("bradley")) {
            type = UnitType.bradley;
            typeString = "M2 Bradley";
            category = UnitCategory.armour;
            dir = Direction.left;
            HP = maxHP = 20;
            attack = 4;
            movementRange = 5;
            movSpeed = 5;
            ammo = maxAmmo = 35;
            fuel = maxFuel = 60;
        }
        if (typeStr.equals("bmp")) {
            type = UnitType.bmp;
            typeString = "BMP-2";
            category = UnitCategory.armour;
            dir = Direction.right;
            HP = maxHP = 20;
            attack = 4;
            movementRange = 5;
            movSpeed = 5;
            ammo = maxAmmo = 30;
            fuel = maxFuel = 60;
        }
        if (typeStr.equals("abrams")) {
            type = UnitType.abrams;
            typeString = "M1 Abrams";
            category = UnitCategory.armour;
            dir = Direction.left;
            HP = maxHP = 30;
            attack = 8;
            movementRange = 5;
            movSpeed = 5;
            ammo = maxAmmo = 35;
            fuel = maxFuel = 50;
        }
        for (int i = 0; i < 4; i++) {
            rightTracks[i] = new TileImageEntity(type, i);
        }

        for (int i = 0; i < 4; i++) {
            leftTracks[i] = new TileImageEntity(type, i + 4);
        }

        rightBody = new TileImageEntity(type, 8);
        rightBodyGray = new TileImageEntity(type, 9);
        leftBody = new TileImageEntity(type, 10);
        leftBodyGray = new TileImageEntity(type, 11);

        upBody = new TileImageEntity(type, 12);
        downBody = new TileImageEntity(type, 13);
        upBodyGray = new TileImageEntity(type, 14);
        downBodyGray = new TileImageEntity(type, 15);

        for (int i = 0; i < 4; i++) {
            rightAttack[i] = new TileImageEntity(type, i + 16);
        }
        for (int i = 0; i < 4; i++) {
            leftAttack[i] = new TileImageEntity(type, i + 20);
        }
        for (int i = 0; i < 4; i++) {
            downAttack[i] = new TileImageEntity(type, i + 24);
        }
        for (int i = 0; i < 4; i++) {
            upAttack[i] = new TileImageEntity(type, i + 28);
        }

        for (int i = 0; i < 10; i++) {
            rightMove[i] = new TileImageEntity(type, i + 32);
        }
        for (int i = 0; i < 10; i++) {
            leftMove[i] = new TileImageEntity(type, i + 58);
        }
        for (int i = 0; i < 8; i++) {
            downMove[i] = new TileImageEntity(type, i + 42);
        }
        for (int i = 0; i < 8; i++) {
            upMove[i] = new TileImageEntity(type, i + 50);
        }
        attackDir = Direction.none;

        switch (dir) {
            case right:
                imgEntity = rightBody;
                if (category != UnitCategory.inf) {
                    tracks = rightTracks[0];
                }
                break;
            case left:
                imgEntity = leftBody;
                if (category != UnitCategory.inf) {
                    tracks = leftTracks[0];
                }
                break;
            case up:
                imgEntity = upBody;
                break;
            case down:
                imgEntity = downBody;
                break;
        }
    }

    public void renderUnit() {
        if (displayUnit) {
            imgEntity.render();
        }
        if (category != UnitCategory.inf) {
            if (dir == Direction.left || dir == Direction.right) {
                tracks.render();
            }
        }
    }

    // Used to check in case we have to turn the unit
    Direction oldDirection;

    boolean startAttack = false;

    public void updateUnit(int fuelCost) {
        if (startAttack) {
            if (dir != attackDir) {
                turnUnit(attackDir);
            }
            startAttacking();
            startAttack = false;
        }
        if (moving) {
            moveUnit(fuelCost);
        }
        if (attacking) {
            setAttackingImage();
        }
        oldDirection = dir;
    }

    public void setScreenLocation(int x, int y) {
        super.setScreenLocation(x, y);
        imgEntity.setPosition(x, y);
        if (category != UnitCategory.inf) {
            tracks.setPosition(x, y);
        }
    }

    public boolean isMoving() {
        return moving;
    }

    private void moveUnit(int fuelCost) {

        // If unit has reached a tile, remove it from path to give way to the next one
        if (screenLocation.equals(path.get(0))) {
            if (category != UnitCategory.inf) {
                fuel -= fuelCost;
            }
            path.remove(0);
            // Check if unit has reached its destination
            if (path.isEmpty()) {
                moving = false;
                //attackDir also is a flag. If the value isn't 'none' it means it will attack soon.
                if (attackDir != Direction.none) {
                    if (attackDir != dir) {
                        turnUnit(attackDir);
                    }
                    startAttacking();
                    return;
                }
                if (category == UnitCategory.inf && loadedTrans) {
                    displayUnit = false;
                }
                setBodyImage();
                return;
            }
        }
        // move to right tile
        if (screenLocation.x < path.get(0).x) {
            screenLocation.x = screenLocation.x + movSpeed;
            dir = Direction.right;
        }
        // move to left tile
        if (screenLocation.x > path.get(0).x) {
            screenLocation.x = screenLocation.x - movSpeed;
            dir = Direction.left;
        }
        // move to down tile
        if (screenLocation.y < path.get(0).y) {
            screenLocation.y = screenLocation.y + movSpeed;
            dir = Direction.down;
        }
        // move to up tile
        if (screenLocation.y > path.get(0).y) {
            screenLocation.y = screenLocation.y - movSpeed;
            dir = Direction.up;
        }

        setMovingImage();

        if (oldDirection != dir) {
            turnUnit(dir);
        }
    }

    private void setBodyImage() {
        switch (dir) {
            case right:
                if (hasAttacked) {
                    imgEntity = rightBodyGray;
                } else {
                    imgEntity = rightBody;
                }
                break;
            case left:
                if (hasAttacked) {
                    imgEntity = leftBodyGray;
                } else {
                    imgEntity = leftBody;
                }
                break;
            case down:
                if (hasAttacked) {
                    imgEntity = downBodyGray;
                } else {
                    imgEntity = downBody;
                }
                break;
            case up:
                if (hasAttacked) {
                    imgEntity = upBodyGray;
                } else {
                    imgEntity = upBody;
                }
                break;
        }
        imgEntity.setPosition(screenLocation.x, screenLocation.y);
        if (category != UnitCategory.inf) {
            tracks.setPosition(screenLocation.x, screenLocation.y);
        }
    }

    int animationIndex = 0;

    private void setMovingImage() {
        // Tank movement
        if (category != UnitCategory.inf) {
            if (RedAssault.timer % movementAnimTimer == 0) {
                switch (dir) {
                    case right:
                        tracks = rightTracks[animationIndex++];
                        if (animationIndex >= 4) {
                            animationIndex = 0;
                        }
                        imgEntity = rightBody;
                        break;
                    case left:
                        tracks = leftTracks[animationIndex++];
                        if (animationIndex >= 4) {
                            animationIndex = 0;
                        }
                        imgEntity = leftBody;
                        break;
                    case down:
                        imgEntity = downBody;
                        break;
                    case up:
                        imgEntity = upBody;
                        break;
                }
            }
            tracks.setPosition(screenLocation.x, screenLocation.y);
            imgEntity.setPosition(screenLocation.x, screenLocation.y);
        }
        // CONSCRIPT MOVEMENT
        if (category == UnitCategory.inf) {
            if (dir == Direction.right) {
                if (RedAssault.timer % movementAnimTimer == 0) {
                    if (animationIndex >= 10) {
                        animationIndex = 0;
                    }
                    imgEntity = rightMove[animationIndex++];
                    imgEntity.setPosition(screenLocation.x, screenLocation.y);
                }
            }
            if (dir == Direction.left) {
                if (RedAssault.timer % movementAnimTimer == 0) {
                    if (animationIndex >= 10) {
                        animationIndex = 0;
                    }
                    imgEntity = leftMove[animationIndex++];
                    imgEntity.setPosition(screenLocation.x, screenLocation.y);

                }
            }
            if (dir == Direction.down) {
                if (RedAssault.timer % movementAnimTimer == 0) {
                    if (animationIndex >= 8) {
                        animationIndex = 0;
                    }
                    imgEntity = downMove[animationIndex++];
                    imgEntity.setPosition(screenLocation.x, screenLocation.y);

                }
            }
            if (dir == Direction.up) {
                if (RedAssault.timer % movementAnimTimer == 0) {
                    if (animationIndex >= 8) {
                        animationIndex = 0;
                    }
                    imgEntity = upMove[animationIndex++];
                    imgEntity.setPosition(screenLocation.x, screenLocation.y);

                }
            }
        }
    }

    private int attackAnimatIterator = 0;

    private void setAttackingImage() {
        if (RedAssault.timer % attackAnimTimer == 0) {
            switch (dir) {
                case right:
                    imgEntity = rightAttack[animationIndex++];
                    if (type == UnitType.rpg && attackAnimatIterator == 1) {
                        imgEntity = rightBody;
                    }
                    break;
                case left:
                    imgEntity = leftAttack[animationIndex++];
                    if (type == UnitType.rpg && attackAnimatIterator == 1) {
                        imgEntity = leftBody;
                    }
                    break;
                case down:
                    imgEntity = downAttack[animationIndex++];
                    if (type == UnitType.rpg && attackAnimatIterator == 1) {
                        imgEntity = downBody;
                    }
                    break;
                case up:
                    imgEntity = upAttack[animationIndex++];
                    if (type == UnitType.rpg && attackAnimatIterator == 1) {
                        imgEntity = upBody;
                    }
                    break;
            }
            if (animationIndex > 3) {
                animationIndex = 0;
                attackAnimatIterator++;
            }
            // Perform needed actions when the attack has finished.
            if (attackAnimatIterator > 1) {
                attackAnimatIterator = 0;
                attackDir = Direction.none;
                attacking = false;
                setBodyImage();
            }
            // This block sets the attackDir to some other value than 'none' to signal upper classes for one frame
            if (animationIndex == 3) {
                if (attackAnimatIterator == 1) {
                    attackDir = dir;
                }
            }
        }
        imgEntity.setPosition(screenLocation.x, screenLocation.y);
    }

    // Turns the unit in the given direction 'd'
    public void turnUnit(Direction d) {
        animationIndex = 0;
        if (d == Direction.right) {
            if (category != UnitCategory.inf) {
                tracks = rightTracks[0];
            }
            imgEntity = rightBody;
        } else if (d == Direction.left) {
            if (category != UnitCategory.inf) {
                tracks = leftTracks[0];
            }
            imgEntity = leftBody;
        }
        imgEntity.setPosition(screenLocation.x, screenLocation.y);
        if (category != UnitCategory.inf) {
            tracks.setPosition(screenLocation.x, screenLocation.y);
        }
    }

    public void startMoving(ArrayList<Point> movementPath) {
        animationIndex = 0;
        moving = true;
        path = movementPath;
        // Need to add one fuel, b/c when unit moves it decrements at every path tile, including
        // the first tile the unit is already on
        fuel++;
    }

    public void scrollPathCoordinates(int changeX, int changeY) {
        for (int i = 0; i < path.size(); i++) {
            path.get(i).setLocation(path.get(i).x + changeX, path.get(i).y + changeY);
        }
    }

    public int getMovementRange() {
        return movementRange;
    }

    public UnitCategory getUnitCategory() {
        return category;
    }

    public UnitType getUnitType() {
        return type;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    public int getAttack() {
        return attack;
    }

    public int getKills() {
        return kills;
    }

    public void setHasMoved(Boolean hm) {
        hasMoved = hm;
        setBodyImage();
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasAttacked(Boolean ha) {
        hasAttacked = ha;
        if (hasAttacked == false) {
            setBodyImage();
        }
    }

    public boolean hasAttacked() {
        return hasAttacked;
    }

    public boolean hasFinishedItsTurn() {
        return hasAttacked && hasMoved;
    }

    public void setAttackFlag() {
        startAttack = true;
    }

    private void startAttacking() {
        ammo--;

        dir = attackDir;
        attacking = true;
        attackDir = Direction.none;

        //  Turn to the direction 
        switch (dir) {
            case right:
                imgEntity = rightAttack[0];
                attackAnimatIterator = 0;
                animationIndex = 0;
                break;
            case left:
                imgEntity = leftAttack[0];
                attackAnimatIterator = 0;
                animationIndex = 0;
                break;
            case up:
                imgEntity = upAttack[0];
                attackAnimatIterator = 0;
                animationIndex = 0;
                break;
            case down:
                imgEntity = downAttack[0];
                attackAnimatIterator = 0;
                animationIndex = 0;
                break;
        }
        if (category != UnitCategory.inf) {
            tracks.setPosition(screenLocation.x, screenLocation.y);
        }
        imgEntity.setPosition(screenLocation.x, screenLocation.y);
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void setAttackDir(Direction d) {
        attackDir = d;
    }

    public void setTarget(Point p) {
        target = new Point(p);
    }

    public Point getTarget() {
        return target;
    }

    // Increases kill count and gives bonuses to unit. 
    void increaseKillCount() {
        kills++;
        // Corpral: increases maximum ammo and fuel capacities.
        if (kills == 2) {
            maxAmmo += maxAmmo / 10;
            maxFuel += maxFuel / 10;
        }
        // Sergeant: increases health.
        if (kills == 4) {
            maxHP++;
        }
        // Lieutenant: increases attack.
        if (kills == 6) {
            attack++;
        }
    }

    public boolean isLandUnit() {
        if (category == UnitCategory.armour || category == UnitCategory.inf) {
            return true;
        }
        return false;
    }

    public boolean isBeingTransported() {
        if (category == UnitCategory.inf && loadedTrans) {
            return true;
        }
        return false;
    }

    public boolean isTransporting() {
        if (isTransport() && loadedTrans) {
            return true;
        }
        return false;
    }

    public boolean isTransport() {
        if (type == UnitType.bmp || type == UnitType.bradley) {
            return true;
        }
        return false;
    }

    public boolean isBeingDisplayed() {
        return displayUnit;
    }

    public void displayUnit() {
        dir = Direction.down;
        setBodyImage();
        displayUnit = true;
    }

    public void hideUnit() {
        displayUnit = false;
    }
}
