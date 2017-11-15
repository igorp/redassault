
import graphics.Shader;
import graphics.Texture;
import graphics.VertexArray;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import maths.Matrix4f;
import maths.Vector3f;

public class TileImageEntity {

    private static float SIZE_X;
    private static float SIZE_Y;
    private static VertexArray mesh;
    public Texture texture;
    private static int tileSize = RedAssault.tileSizeSource;

    private static Texture tiles[][] = new Texture[3][11];
    private static Texture unit[][] = new Texture[7][68];
    private static Texture explosion[] = new Texture[7];

    // Where image is on screen. Sets to (0,0,0) by default
    private Vector3f position = new Vector3f();
//    private float delta = 0.0f;

    public static void initialize() {

        SIZE_X = RedAssault.m * RedAssault.tileSizeSource;
        SIZE_Y = RedAssault.m * RedAssault.tileSizeSource;

        // Sets the size of the tile
        // Order or vertices: 1. bottom left 2. top left 3.top right 4. bottom right
        float[] vertices = new float[]{
            0, SIZE_Y, 0.5f,
            0, 0, 0.5f,
            SIZE_X, 0, 0.5f,
            SIZE_X, SIZE_Y, 0.5f
        };

        byte[] indices = new byte[]{
            0, 1, 2,
            2, 3, 0
        };

        float[] tcs = new float[]{
            0, 1,
            0, 0,
            1, 0,
            1, 1
        };

        mesh = new VertexArray(vertices, indices, tcs);

        BufferedImage tileSheet = null;
        try {

            tileSheet = ImageIO.read(new File("images/tiles.png"));

        } catch (IOException e) {
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 11; j++) {
                tiles[i][j] = new Texture(tileSheet, j * tileSize, i * tileSize, tileSize, tileSize);
            }
        }

        for (int i = 0; i < unit.length; i++) {
            // Load unit spritesheet
            BufferedImage spirteSheet = null;
            try {
                if (i == 0) {
                    spirteSheet = ImageIO.read(new File("images/t72.png"));
                }
                if (i == 1) {
                    spirteSheet = ImageIO.read(new File("images/bradley.png"));
                }
                if (i == 2) {
                    spirteSheet = ImageIO.read(new File("images/abrams.png"));
                }
                if (i == 3) {
                    spirteSheet = ImageIO.read(new File("images/conscript.png"));
                }
                if (i == 4) {
                    spirteSheet = ImageIO.read(new File("images/marine.png"));
                }
                if (i == 5) {
                    spirteSheet = ImageIO.read(new File("images/bmp.png"));
                }
                if (i == 6) {
                    spirteSheet = ImageIO.read(new File("images/rpg.png"));
                }
            } catch (IOException e) {
            }
            // Track images right
            unit[i][0] = new Texture(spirteSheet, 6 * tileSize, 0, tileSize, tileSize);
            unit[i][1] = new Texture(spirteSheet, 7 * tileSize, 0, tileSize, tileSize);
            unit[i][2] = new Texture(spirteSheet, 8 * tileSize, 0, tileSize, tileSize);
            unit[i][3] = new Texture(spirteSheet, 9 * tileSize, 0, tileSize, tileSize);

            // Track images left
            unit[i][4] = new Texture(spirteSheet, 6 * tileSize, tileSize, tileSize, tileSize);
            unit[i][5] = new Texture(spirteSheet, 7 * tileSize, tileSize, tileSize, tileSize);
            unit[i][6] = new Texture(spirteSheet, 8 * tileSize, tileSize, tileSize, tileSize);
            unit[i][7] = new Texture(spirteSheet, 9 * tileSize, tileSize, tileSize, tileSize);

            // Left and right static body images
            unit[i][8] = new Texture(spirteSheet, 0, 0, tileSize, tileSize);
            unit[i][9] = new Texture(spirteSheet, 2 * tileSize, 0, tileSize, tileSize);
            unit[i][10] = new Texture(spirteSheet, 0, tileSize, tileSize, tileSize);
            unit[i][11] = new Texture(spirteSheet, 2 * tileSize, tileSize, tileSize, tileSize);

            // Up and down static body images
            unit[i][12] = new Texture(spirteSheet, 0, 3 * tileSize, tileSize, tileSize);
            unit[i][13] = new Texture(spirteSheet, 0 * tileSize, 2 * tileSize, tileSize, tileSize);
            unit[i][14] = new Texture(spirteSheet, 2 * tileSize, 3 * tileSize, tileSize, tileSize);
            unit[i][15] = new Texture(spirteSheet, 2 * tileSize, 2 * tileSize, tileSize, tileSize);

            // Right attack
            unit[i][16] = new Texture(spirteSheet, 0, 8 * tileSize, tileSize, tileSize);
            unit[i][17] = new Texture(spirteSheet, 1 * tileSize, 8 * tileSize, tileSize, tileSize);
            unit[i][18] = new Texture(spirteSheet, 2 * tileSize, 8 * tileSize, tileSize, tileSize);
            unit[i][19] = new Texture(spirteSheet, 3 * tileSize, 8 * tileSize, tileSize, tileSize);

            // Left attack
            unit[i][20] = new Texture(spirteSheet, 0, 9 * tileSize, tileSize, tileSize);
            unit[i][21] = new Texture(spirteSheet, 1 * tileSize, 9 * tileSize, tileSize, tileSize);
            unit[i][22] = new Texture(spirteSheet, 2 * tileSize, 9 * tileSize, tileSize, tileSize);
            unit[i][23] = new Texture(spirteSheet, 3 * tileSize, 9 * tileSize, tileSize, tileSize);

            // Down attack
            unit[i][24] = new Texture(spirteSheet, 0, 10 * tileSize, tileSize, tileSize);
            unit[i][25] = new Texture(spirteSheet, 1 * tileSize, 10 * tileSize, tileSize, tileSize);
            unit[i][26] = new Texture(spirteSheet, 2 * tileSize, 10 * tileSize, tileSize, tileSize);
            unit[i][27] = new Texture(spirteSheet, 3 * tileSize, 10 * tileSize, tileSize, tileSize);

            // Up attack
            unit[i][28] = new Texture(spirteSheet, 0, 11 * tileSize, tileSize, tileSize);
            unit[i][29] = new Texture(spirteSheet, 1 * tileSize, 11 * tileSize, tileSize, tileSize);
            unit[i][30] = new Texture(spirteSheet, 2 * tileSize, 11 * tileSize, tileSize, tileSize);
            unit[i][31] = new Texture(spirteSheet, 3 * tileSize, 11 * tileSize, tileSize, tileSize);

            // Movement images, first right
            for (int j = 0; j < 10; j++) {
                unit[i][j + 32] = new Texture(spirteSheet, j * tileSize, 4 * tileSize, tileSize, tileSize);
            }

            // Then down
            for (int j = 0; j < 8; j++) {
                unit[i][j + 42] = new Texture(spirteSheet, j * tileSize, 6 * tileSize, tileSize, tileSize);
            }
            // Up movement
            for (int j = 0; j < 8; j++) {
                unit[i][j + 50] = new Texture(spirteSheet, j * tileSize, 7 * tileSize, tileSize, tileSize);
            }
            // And finally left movement
            for (int j = 0; j < 10; j++) {
                unit[i][j + 58] = new Texture(spirteSheet, j * tileSize, 5 * tileSize, tileSize, tileSize);
            }
        }
        BufferedImage explosionSheet = null;
        try {
            explosionSheet = ImageIO.read(new File("images/explosion.png"));
        } catch (IOException e) {
        }

        for (int i = 0; i < 7; i++) {
            explosion[i] = new Texture(explosionSheet, 30 * i, 0, 30, 30);
        }

    }

    public TileImageEntity(String path) {
//        System.out.println(path);

        if (path.equals("forest")) {
            texture = tiles[2][2];
        } else if (path.equals("grass")) {
            texture = tiles[0][0];
        } else if (path.equals("grass_e")) {
            texture = tiles[0][1];
        } else if (path.equals("grass_n")) {
            texture = tiles[0][4];
        } else if (path.equals("grass_ne")) {
            texture = tiles[0][5];
        } else if (path.equals("grass_nw")) {
            texture = tiles[0][8];
        } else if (path.equals("grass_s")) {
            texture = tiles[0][2];
        } else if (path.equals("grass_se")) {
            texture = tiles[0][6];
        } else if (path.equals("grass_sw")) {
            texture = tiles[0][7];
        } else if (path.equals("grass_w")) {
            texture = tiles[0][3];
        } else if (path.equals("hill")) {
            texture = tiles[2][1];
        } else if (path.equals("mountain")) {
            texture = tiles[2][0];
        } else if (path.equals("water1")) {
            texture = tiles[0][9];
        } else if (path.equals("water2")) {
            texture = tiles[0][10];
        } else if (path.equals("road_h")) {
            texture = tiles[1][0];
        } else if (path.equals("road_v")) {
            texture = tiles[1][1];
        } else if (path.equals("road_se")) {
            texture = tiles[1][2];
        } else if (path.equals("road_sw")) {
            texture = tiles[1][3];
        } else if (path.equals("road_ne")) {
            texture = tiles[1][4];
        } else if (path.equals("road_nw")) {
            texture = tiles[1][5];
        } else if (path.equals("road_vw")) {
            texture = tiles[1][6];
        } else if (path.equals("road_ve")) {
            texture = tiles[1][7];
        } else if (path.equals("road_hn")) {
            texture = tiles[1][8];
        } else if (path.equals("road_hs")) {
            texture = tiles[1][9];
        } else if (path.equals("road_hv")) {
            texture = tiles[1][9];
        } else if (path.equals("rocks")) {
            texture = tiles[2][3];
        } else if (path.equals("village")) {
            texture = tiles[2][6];
        } else if (path.equals("church")) {
            texture = tiles[2][5];
        } else {
            System.out.println("Warning: Default Texture");
            System.out.println(path);
            texture = new Texture(path);
        }
    }

    // Used for explosion animation
    public TileImageEntity(int i) {
        texture = explosion[i];
    }

    public TileImageEntity(UnitType u, int i) {
        switch (u) {
            case t72:
                texture = unit[0][i];
                break;
            case bradley:
                texture = unit[1][i];
                break;
            case abrams:
                texture = unit[2][i];
                break;
            case conscript:
                texture = unit[3][i];
                break;
            case marine:
                texture = unit[4][i];
                break;
            case bmp:
                texture = unit[5][i];
                break;
            case rpg:
                texture = unit[6][i];
                break;
        }
    }

    public TileImageEntity(BufferedImage img, int x, int y, int w, int h) {
        texture = new Texture(img, x, y, w, h);
    }

    public void render() {
        Shader.DEFAULT.enable();
        Shader.DEFAULT.setUniformMat4f("ml_matrix", Matrix4f.translate(position));
        texture.bind();
        mesh.render();
        Shader.DEFAULT.disable();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
    }

    public void setPositionX(float x) {
        position.x = x;
    }

    public void setPositionY(float y) {
        position.y = y;
    }
}
