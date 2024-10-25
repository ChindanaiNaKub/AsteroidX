package se233.asterioddemo;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import java.util.HashMap;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javafx.geometry.Rectangle2D;

public class SpriteLoader {
    private HashMap<String, Image> spriteMap = new HashMap<>();

    public SpriteLoader(String spriteSheetPath, String xmlPath) {
        try {
            // Load image with transparency enabled
            Image spriteSheet = new Image(
                    getClass().getResourceAsStream(spriteSheetPath),
                    -1, -1, true, true  // preserveRatio=true, smooth=true
            );

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(getClass().getResourceAsStream(xmlPath));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("SubTexture");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String name = element.getAttribute("name");
                int x = Integer.parseInt(element.getAttribute("x"));
                int y = Integer.parseInt(element.getAttribute("y"));
                int width = Integer.parseInt(element.getAttribute("width"));
                int height = Integer.parseInt(element.getAttribute("height"));

                // Create a new transparent image for each sprite
                WritableImage sprite = new WritableImage(width, height);
                PixelWriter pixelWriter = sprite.getPixelWriter();
                PixelReader pixelReader = spriteSheet.getPixelReader();

                // Copy pixels with transparency
                for (int py = 0; py < height; py++) {
                    for (int px = 0; px < width; px++) {
                        int argb = pixelReader.getArgb(x + px, y + py);
                        // Check if pixel is white (you might need to adjust these values)
                        int red = (argb >> 16) & 0xff;
                        int green = (argb >> 8) & 0xff;
                        int blue = argb & 0xff;
                        if (red >= 250 && green >= 250 && blue >= 250) {
                            // Make white pixels transparent
                            argb = 0x00000000;
                        }
                        pixelWriter.setArgb(px, py, argb);
                    }
                }

                spriteMap.put(name, sprite);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Image getSprite(String name) {
        return spriteMap.get(name);
    }
}