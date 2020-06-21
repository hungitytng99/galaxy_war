package Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import javafx.scene.shape.Circle;

public class PlayGame implements Runnable {

	private String ip = "localhost";
	private int port = 22222;
	private Scanner scanner = new Scanner(System.in);
	private JFrame frame;
	private final int WIDTH = 800;
	private final int HEIGHT = 600;
	private Thread thread;

	private Painter painter;
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;

	private ServerSocket serverSocket;

	private BufferedImage galaxy;
	private BufferedImage dan;
	private BufferedImage maybay;
	private BufferedImage tenlua2;
	private BufferedImage tenlua1;
	ArrayList<BufferedImage> danArray = new ArrayList<BufferedImage>();

	private boolean client_join = true;
	private boolean accepted = false;

	private int x_tenlua_1 = 350;
	private int y_tenlua_1 = 490;
	private int size_tenlua_x_1 = 70;
	private int size_tenlua_y_1 = 70;

	private int x_tenlua_2 = 350;
	private int y_tenlua_2 = 500;
	private int size_tenlua_x_2 = 70;
	private int size_tenlua_y_2 = 70;

	private int x_dan = x_tenlua_1;
	private int y_dan = y_tenlua_1;
	private int size_dan_x = 40;
	private int size_dan_y = 40;

	private boolean[][] listdan = new boolean[10][16];
	private boolean[][] listmb = new boolean[11][16];
	private boolean status_tl_1 = true;// TRANG THAI TEN LUA 1( SONG / CHET)
	private boolean status_tl_2 = true;// TRANG THAI TEN LUA 2( SONG / CHET)

	private Timer timer_dan;
	private Timer timer_mb;
	private Timer timer_dis;
	public int time = 50;
	public int time_appear = 400;
	public int time_flight = 400;

	Random generator = new Random();

	/**
	 * <pre>
	 * 0, 1, 2 
	 * 3, 4, 5 
	 * 6, 7, 8
	 * </pre>
	 */

	public PlayGame() {
		System.out.println("Please input the IP: ");
		// ip = scanner.nextLine();
		ip = "127.0.0.1";
		System.out.println("Please input the port: ");
		port = 1234;
		// port = scanner.nextInt();
		while (port < 1 || port > 65535) {
			System.out.println("The port you entered was invalid, please input another port: ");
			port = scanner.nextInt();
		}

		for (int i = 0; i < 10; i++) {
			for (int j = 1; j < 16; j++) {
				listmb[i][j] = false;
			}
		}
		;
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 16; j++) {
				listdan[i][j] = false;
			}
		}
		;
		loadImages();

		painter = new Painter();
		painter.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		if (!connect()) {
			initializeServer();
			System.out.println("Khoi tao server.");
		} else {
			accepted = true;
			System.out.println("Khoi tao client.");
		}

		frame = new JFrame();
		frame.setTitle("Galaxy war");
		frame.setContentPane(painter);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);

		thread = new Thread(this, "PlayGame");
		thread.setDaemon(true);
		thread.start();
	}

	public void run() {
		while (true) {
			tick();
			if (!accepted) {
				listenForServerRequest();
				painter.repaint();
			}

		}
	}

	private void tick() {
		if (status_tl_1 || status_tl_2) {
			try {
				int input = dis.readInt();
				if (input % 50 == 0) {
					if (!client_join)
						x_tenlua_1 = input;
					else
						x_tenlua_2 = input;

				}
				if (input % 17 == 0) {
					listmb[0][input / 17] = true;
				} else {
					listdan[9][input] = true;
				}

				painter.repaint();
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else{
			accepted = false;
			thread.stop();
		}

	}

	private void render(Graphics g) {// KHI THUC HIEN REPAINT => VE LAI DO HOA

		g.drawImage(galaxy, 0, 0, null);
		if (accepted) {
			if (status_tl_1)
				g.drawImage(tenlua1, x_tenlua_1, y_tenlua_1, size_tenlua_x_1, size_tenlua_y_1, null, null);
			if (status_tl_2)
				g.drawImage(tenlua2, x_tenlua_2, y_tenlua_2, size_tenlua_x_2, size_tenlua_y_2, null, null);
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 16; j++) {
					if (listdan[i][j] == true)
						g.drawImage(dan, j * 50 + 15, i * 50, size_dan_x, size_dan_y, null, null);
				}
			}
			;
			for (int i = 0; i < 11; i++) {
				for (int j = 0; j < 16; j++) {
					if (listmb[i][j] == true) {
						g.drawImage(maybay, j * 50 + 10, i * 50, size_dan_x + 15, size_dan_y + 15, null, null);
					}
				}
			}
			;
		}
	}

	private void listenForServerRequest() {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
			System.out.println("CLIENT HAS REQUESTED TO JOIN, AND WE HAVE ACCEPTED");
		} catch (IOException e) {
			System.out.println("listenForServerRequest ERROR");
		}
	}

	private boolean connect() {
		try {
			socket = new Socket(ip, port);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
		} catch (IOException e) {
			System.out.println("Unable to connect to the address: " + ip + ":" + port + " | Starting a server");
			return false;
		}
		System.out.println("Successfully connected to the server.");
		return true;
	}

	private void initializeServer() {
		try {
			serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
		} catch (Exception e) {
			System.out.println("initializeServer");
		}
		client_join = false;
	}

	private void loadImages() {
		try {
			galaxy = ImageIO.read(getClass().getResourceAsStream("/galaxy.png"));
			tenlua1 = ImageIO.read(getClass().getResourceAsStream("/tenlua1.png"));
			maybay = ImageIO.read(getClass().getResourceAsStream("/maybay.png"));
			tenlua2 = ImageIO.read(getClass().getResourceAsStream("/tenlua2.gif"));
			dan = ImageIO.read(getClass().getResourceAsStream("/dan.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new PlayGame();
	}

	private class Painter extends JPanel implements KeyListener, ActionListener {
		private static final long serialVersionUID = 1L;

		public Painter() {
			setFocusable(true);
			requestFocus();
			addKeyListener(this);
			this.setBackground(Color.white);
			timer_dan = new Timer(time, this);
			timer_dan.start();

			Maybay appear = new Maybay();
			timer_mb = new Timer(time_appear, appear);
			timer_mb.start();

			Distance dis = new Distance();
			timer_dis = new Timer(time_flight, dis);
			timer_dis.start();
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render(g);
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {

			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (client_join) {
					if (x_tenlua_1 < 750) {
						x_tenlua_1 += 50;
						try {
							dos.writeInt(x_tenlua_1);
							dos.flush();
						} catch (Exception e2) {
							// TODO: handle exception
						}
					}
				} else {
					if (x_tenlua_2 < 750) {
						x_tenlua_2 += 50;
						try {
							dos.writeInt(x_tenlua_2);
							dos.flush();
						} catch (Exception e2) {
						}
					}
				}
			}

			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				if (client_join) {
					if (x_tenlua_1 > 50) {
						x_tenlua_1 -= 50;
						try {
							dos.writeInt(x_tenlua_1);
							dos.flush();
						} catch (Exception e2) {
							// TODO: handle exception
						}
					}
				} else {
					if (x_tenlua_2 > 50) {
						x_tenlua_2 -= 50;
						try {
							dos.writeInt(x_tenlua_2);
							dos.flush();
						} catch (Exception e2) {
							// TODO: handle exception
						}
					}
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				if (client_join && status_tl_1) {
					int dan = x_tenlua_1 / 50;
					listdan[9][dan] = true;
					repaint();

					try {
						dos.writeInt(dan);
						dos.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else if (status_tl_2) {
					int dan = x_tenlua_2 / 50;
					listdan[9][dan] = true;

					try {
						dos.writeInt(dan);
						dos.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			}
			Toolkit.getDefaultToolkit().sync();
			repaint();

		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// System.out.println("painter go");
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 16; j++) {
					if (listdan[i][j] == true) {
						listdan[i][j] = false;
						if (listmb[i][j] == true) {
							listmb[i][j] = false; // NEU DAN GAP PHAI MAY BAY BAY XUONG THI BIEN MAT CA 2
						} else if (i > 0)
							listdan[i - 1][j] = true;
					}
				}
			}
			painter.repaint();
		}
	}

	private class Maybay implements ActionListener {// khoang cach de may bay bay xuong
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (accepted) {
				timer_mb.start();
				for (int i = 10; i >= 0; i--) {
					for (int j = 15; j >= 0; j--) {
						if (listmb[i][j] == true) {
							listmb[i][j] = false;
							if (i >= 0 && i < 10) {
								listmb[i + 1][j] = true;
								painter.repaint();
								if ((x_tenlua_1 == j * 50) && ((i + 1) == 10))
									status_tl_1 = false;
								if ((x_tenlua_2 == j * 50) && ((i + 1) == 10))
									status_tl_2 = false;
							}
//						if(i == 10) {						
//							listmb[0][j] = true;
//						}
						}
					}
				}
			}
			painter.repaint();
		}

	}

	private class Distance implements ActionListener { // Khoang thoi gian tao ra 2 may bay

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (accepted) {
//				/System.out.println("START RAN DOM");
				int temp = generator.nextInt(15) + 1;
				listmb[0][temp] = true;
				try {
					dos.writeInt(temp * 17);
					dos.flush();
				} catch (Exception e2) {
				}
				painter.repaint();
			}
		}

	}
}
