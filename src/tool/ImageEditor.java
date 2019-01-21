package tool;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageEditor {
	public static BufferedImage loadImage(String path) {
		try {
			return ImageIO.read(ImageEditor.class.getResource(path));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static BufferedImage[] getSprite(BufferedImage image,int col1, int row1, int col2, int row2,  int width,int height ) {
		BufferedImage[] sprites=new BufferedImage[(row2-row1)*(col2-col1)];
		int index=0;
		for(int i=row1;i<row2;++i) {
			for(int j=col1; j<col2;++j) {
				sprites[index]=image.getSubimage(j*width, i*height, width, height);
				index++;
			}
		}
		return sprites;
	}
	
}
