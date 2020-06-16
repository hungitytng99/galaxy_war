package Main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Dan extends JPanel {
	int x;
	int y;
	private BufferedImage dan;

	public Dan(int x, int y) {
		try {
			dan = ImageIO.read(getClass().getResourceAsStream("/dan.png"));
		} catch (IOException e) {
		}
		this.x = x;
		this.y = y;
		this.repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(dan,x, y,this);
	}
}
