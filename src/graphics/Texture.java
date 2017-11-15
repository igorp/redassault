package graphics;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import static org.lwjgl.opengl.GL11.*;
import utils.BufferUtils;

public class Texture {

    public int width, height;
    private int texture;

    public Texture(String path) {
        texture = load(path);
    }
    
    // Copy constructor
    // NOT USED
    public Texture(Texture original) {
        this.height = original.height;
        this.width = original.width;
        this.texture = original.texture;
    }

    //load subimage. x and y starting coordinates, w and h width and height
    public Texture(BufferedImage source, int x, int y, int w, int h) {
        int[] pixels = null;

        BufferedImage image = source.getSubimage(x, y, w, h);
        width = image.getWidth();
        height = image.getHeight();
        pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        int[] data = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            int a = (pixels[i] & 0xff000000) >> 24;
            int r = (pixels[i] & 0xff0000) >> 16;
            int g = (pixels[i] & 0xff00) >> 8;
            int b = (pixels[i] & 0xff);

            data[i] = a << 24 | b << 16 | g << 8 | r;
        }

        int result = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, result);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, BufferUtils.createIntBuffer(data));
        glBindTexture(GL_TEXTURE_2D, 0);
        texture = result;
    }

    private int load(String path) {
        int[] pixels = null;
        try {
            BufferedImage image = ImageIO.read(new FileInputStream(path));
            width = image.getWidth();
            height = image.getHeight();
            pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[] data = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            int a = (pixels[i] & 0xff000000) >> 24;
            int r = (pixels[i] & 0xff0000) >> 16;
            int g = (pixels[i] & 0xff00) >> 8;
            int b = (pixels[i] & 0xff);

            data[i] = a << 24 | b << 16 | g << 8 | r;
        }

        int result = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, result);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, BufferUtils.createIntBuffer(data));
        glBindTexture(GL_TEXTURE_2D, 0);
        return result;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    public int getTexture() {
        return texture;
    }
    
    public void setTexture(int t) {
        texture = t;
    }
}
