/* Red Assault Version 1.2
 * TextRenderer.java by Igor P.
 * Prints text and characaters on screen.
 * 
 * The 'character' array contains the following characters in the following order:
 * !"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ
 */

import java.awt.Color;

class TextRenderer {

    ImageEntity[] charactersWhite = new ImageEntity[ImageEntity.charCount];
    ImageEntity[] charactersYellow = new ImageEntity[ImageEntity.charCount];
    ImageEntity[] charactersBlack = new ImageEntity[ImageEntity.charCount];
    ImageEntity[] charactersGray = new ImageEntity[ImageEntity.charCount];
    ImageEntity[] charactersLightGray = new ImageEntity[ImageEntity.charCount];

    public TextRenderer() {
        for (int i = 0; i < ImageEntity.charCount; i++) {
            ImageEntity img = new ImageEntity(i, Color.white);
            ImageEntity img2 = new ImageEntity(i, Color.yellow);
            ImageEntity img3 = new ImageEntity(i, Color.black);
            ImageEntity img4 = new ImageEntity(i, Color.gray);
            ImageEntity img5 = new ImageEntity(i, Color.lightGray);

            charactersWhite[i] = new ImageEntity(img);
            charactersYellow[i] = new ImageEntity(img2);
            charactersBlack[i] = new ImageEntity(img3);
            charactersGray[i] = new ImageEntity(img4);
            charactersLightGray[i] = new ImageEntity(img5);
        }
    }

    // Draws string 's' on screen at location (x, y)
    public int print(String s, Color c, int x, int y) {
        char[] chars = s.toUpperCase().toCharArray();
        int pixelPositionX = x;
        ImageEntity[] characters;
        if (c == Color.white) {
            characters = charactersWhite;
        } else if (c == Color.yellow) {
            characters = charactersYellow;
        } else if (c == Color.gray) {
            characters = charactersGray;
        } else if (c == Color.lightGray) {
            characters = charactersLightGray;
        } else {
            characters = charactersBlack;
        }

        for (int i = 0; i < chars.length; i++) {
            int index = (int) chars[i] - 33;

            // In case the character is a space
            if (index == -1) {
                if (c == Color.black) {
                    pixelPositionX += 2 * RedAssault.m;
                } else {
                    pixelPositionX += 3 * RedAssault.m;
                }
            } else {
                int pixelPositionY = y;
                switch (index) {
                    default:
                        pixelPositionY = y + 1;
                        break;
                }
                characters[index].setPosition(pixelPositionX, pixelPositionY);
                characters[index].render();
                pixelPositionX += (characters[index].texture.width - 1) * RedAssault.m;
            }
        }
        return pixelPositionX;
    }
}
