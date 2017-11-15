/* Red Assault
 * MapEntity.java by Igor P.
 * Version 1.0
 */

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

public class MapEntity {
    
    public int size;
    protected TileImageEntity imgEntity;
    protected Point tileLocation;
    protected Point screenLocation;
    protected String url;
    protected String typeString;
    
    public MapEntity(String type) {

//        this.url = "images/" + type + ".png";
        loadImage(type);
    }
    
    public MapEntity() {
    }
    
    public MapEntity(int x, int y) {
        tileLocation = new Point(x, y);
    }
    
    public String displayInfo() {
        return new String("URL: " + url + " location: " + tileLocation);
    }
    
    public void setImage(String type) {
//        this.url = "images/" + type + ".png";
        loadImage(url);
    }
    
    public void loadImage(String url) {
        if (!"water".equals(url)) {
            imgEntity = new TileImageEntity(url);
        }
    }
    
    public String getUrl() {
        return url;
    }
    
    public Point getTileLocation() {
        return tileLocation;
    }
    
    public void setTileLocation(int x, int y) {
        tileLocation = new Point(x, y);
    }
    
    public void setTileLocation(Point p) {
        tileLocation = new Point(p.x, p.y);
    }
    
    public Point getScreenLocation() {
        return screenLocation;
    }
    
    public void setScreenLocation(int x, int y) {
        screenLocation = new Point(x, y);
        imgEntity.setPosition(x, y);
    }
    
    public void setScreenLocation(Point p) {
        setScreenLocation(p.x, p.y);
    }

    //Given an image returns subimage and scales it by multiplier RedAssault.m
    protected Image getSubImageAndScale(Image img, int x, int y, int w, int h) {
        // Create a buffered image with transparency
        BufferedImage buf_img = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics bG = buf_img.createGraphics();
        bG.drawImage(img, 0, 0, null);
        bG.dispose();
        
        BufferedImage buf_sub_img = buf_img.getSubimage(x, y, w, h);
        Image unscaled_img = Toolkit.getDefaultToolkit().createImage(buf_sub_img.getSource());
        
        return unscaled_img.getScaledInstance(RedAssault.tileSizeSource * RedAssault.m, RedAssault.tileSizeSource * RedAssault.m, Image.SCALE_DEFAULT);
    }
}
