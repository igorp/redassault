
import graphics.Shader;
import graphics.Texture;
import graphics.VertexArray;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import maths.Matrix4f;
import maths.Vector3f;

public class ImageEntity {

    private float SIZE_X;
    private float SIZE_Y;
    private VertexArray mesh;
    public Texture texture;

    final static int charCount = 58;
    private static Texture ASCIIcharactersWhite[] = new Texture[charCount];
    private static Texture ASCIIcharactersYellow[] = new Texture[charCount];
    private static Texture ASCIIcharactersBlack[] = new Texture[charCount];
    private static Texture ASCIIcharactersGray[] = new Texture[charCount];
    private static Texture ASCIIcharactersLightGray[] = new Texture[charCount];

    public static void loadImages() {
        BufferedImage textSheet = null;
        try {
            textSheet = ImageIO.read(new FileInputStream("images/text2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int pixelPosX = 0;

        // Load ASCII characer textures
        for (int i = 0; i < charCount; i++) {
            int pixelPosY = 1;
            int pixelWidth = 5;
            int pixelHeight = 7;

            switch (i) {
                case 0:
                    pixelWidth = 3;
                    break;
                case 1:
                    pixelHeight = 4;
                    break;
                case 2:
                    pixelWidth = 7;
                    break;
                case 3:
                    pixelHeight = 9;
                    pixelPosY = 0;
                    break;
                case 4:
                    pixelWidth = 7;
                    break;
                case 5:
                    pixelWidth = 6;
                    break;
                case 6:
                    pixelWidth = 3;
                    pixelHeight = 4;
                    break;
                case 7:
                    pixelWidth = 4;
                    break;
                case 8:
                    pixelWidth = 4;
                    break;
                case 9:
                    pixelHeight = 5;
                    break;
                case 10:
                    pixelHeight = 6;
                    pixelPosY = 2;
                    break;
                case 11:
                    pixelWidth = 4;
                    pixelHeight = 8;
                    break;
                case 12:
                    pixelWidth = 4;
                    break;
                case 13:
                    pixelWidth = 3;
                    break;
                case 16:
                    pixelWidth = 3;
                    break;
                case 25:
                    pixelWidth = 3;
                    break;
                case 26:
                    pixelWidth = 4;
                    pixelHeight = 8;
                    break;
                case 27:
                    pixelWidth = 4;
                    break;
                case 28:
                    pixelWidth = 4;
                    break;
                case 29:
                    pixelWidth = 4;
                    break;
                case 30:
                    pixelHeight = 8;
                    pixelPosY = 0;
                    break;
                case 31:
                    pixelWidth = 6;
                    break;
                case 40:
                    pixelWidth = 3;
                    break;
                case 44:
                    pixelWidth = 7;
                    break;
                case 45:
                    pixelWidth = 6;
                    break;
                case 48:
                    pixelWidth = 6;
                    break;
                case 54:
                    pixelWidth = 7;
                    break;

            }

//       ! " # $ % & ' ( ) * +  ,  -  .  /  0  1  2  3  4  5  6  7  8  9  :  ;  <  =  >  ?  @  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S  T  U  V  W  X  Y  Z
//       0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57
            //  System.out.println(i);
            ASCIIcharactersWhite[i] = new Texture(textSheet, pixelPosX, pixelPosY, pixelWidth, pixelHeight);
            ASCIIcharactersYellow[i] = new Texture(textSheet, pixelPosX, pixelPosY + 9, pixelWidth, pixelHeight);
            ASCIIcharactersBlack[i] = new Texture(textSheet, pixelPosX, pixelPosY + 19, pixelWidth, pixelHeight);
            ASCIIcharactersGray[i] = new Texture(textSheet, pixelPosX, pixelPosY + 29, pixelWidth, pixelHeight);
            ASCIIcharactersLightGray[i] = new Texture(textSheet, pixelPosX, pixelPosY + 39, pixelWidth, pixelHeight);
            pixelPosX += pixelWidth;
        }
    }

    public ImageEntity(int charIndex, Color c) {

        if (c == Color.white) {
            texture = ASCIIcharactersWhite[charIndex];
        } else if (c == Color.yellow) {
            texture = ASCIIcharactersYellow[charIndex];
        } else if (c == Color.gray) {
            texture = ASCIIcharactersGray[charIndex];
        } else if (c == Color.lightGray) {
            texture = ASCIIcharactersLightGray[charIndex];
        } else {
            texture = ASCIIcharactersBlack[charIndex];
        }
        SIZE_X = RedAssault.m * texture.width;
        SIZE_Y = RedAssault.m * texture.height;

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
        position.z = 0.3f;
    }

    // Where nullnullimage is on screen. Sets to (0,0,0) by default
    private Vector3f position = new Vector3f();

    public ImageEntity(BufferedImage img, int x, int y, int w, int h) {
        texture = new Texture(img, x, y, w, h);
        SIZE_X = RedAssault.m * texture.width;
        SIZE_Y = RedAssault.m * texture.height;

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
    }

    public ImageEntity(String path) {
        texture = new Texture(path);
        SIZE_X = RedAssault.m * texture.width;
        SIZE_Y = RedAssault.m * texture.height;

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
        position.z = 0.3f;
    }

    public ImageEntity(ImageEntity original) {
        this.SIZE_X = original.SIZE_X;
        this.SIZE_Y = original.SIZE_Y;
        this.mesh = original.mesh;
        this.texture = original.texture;
        this.position = new Vector3f(original.position);
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

    public int getWidth() {
        return (int) SIZE_X;
    }

    public int getHeight() {
        return (int) SIZE_Y;
    }

    public void setPosition(float x, float y) {
        position.x = x;
        position.y = y;
    }
}
