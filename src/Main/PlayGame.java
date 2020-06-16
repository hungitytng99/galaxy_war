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
	private BufferedImage tenlua;
	ArrayList<BufferedImage> danArray = new ArrayList<BufferedImage>();

	private boolean circle = true;
	private boolean accepted = false;
	private int x_tenlua = 350;
	private int y_tenlua = 490;
	private int size_tenlua_x = 70;
	private int size_tenlua_y = 70;

	private int x_dan = x_tenlua;
	private int y_dan = y_tenlua;
	private int size_dan_x = 40;
	private int size_dan_y = 40;

	private boolean[][] listdan = new boolean[10][16];
	private boolean[][] listmb = new boolean[10][16];
	private Timer timer_dan;
	private Timer timer_mb;
	public int time = 50;
	public int time_mb = 100;
	
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
		};
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
		thread.start();
	}

	public void run() {
		while (true) {
			tick();
			painter.repaint();
			if (!accepted) {
				listenForServerRequest();
				painter.repaint();
			}

		}
	}
	
	private void tick() {
		try {
			int input = dis.readInt();
			if (input % 50 == 0) {
				x_tenlua = input;
			} else {
				listdan[9][input] = true;
			}
			painter.repaint();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
	
	private void render(Graphics g) {// KHI THUC HIEN REPAINT => VE LAI DO HOA

		//g.drawImage(galaxy, 0, 0, null);
		if (accepted) {
			g.drawImage(tenlua, x_tenlua, y_tenlua, size_tenlua_x, size_tenlua_y, null, null);
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 16; j++) {
					if (listdan[i][j] == true)
						g.drawImage(dan, j * 50 + 15, i * 50, size_dan_x, size_dan_y, null, null);
				}
			};
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 16; j++) {
					if (listmb[i][j] == true)
						g.drawImage(maybay, j * 50 + 20, i * 50, size_dan_x - 10, size_dan_y - 10, null, null);
				}
			};
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
	}

	private void loadImages() {
		try {
			galaxy = ImageIO.read(getClass().getResourceAsStream("/galaxy.png"));
			tenlua = ImageIO.read(getClass().getResourceAsStream("/tenlua.png"));
			maybay = ImageIO.read(getClass().getResourceAsStream("/maybay.png"));
			dan = ImageIO.read(getClass().getResourceAsStream("/dan.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		PlayGame galaxy_war = new PlayGame();
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
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render(g);
		}

		@Override
		public void keyTyped(KeyEvent e) {
			throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																			// choose Tools | Templates.
		}

		@Override
		public void keyPressed(KeyEvent e) {

			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (x_tenlua < 750) {
					x_tenlua += 50;
					try {
						dos.writeInt(x_tenlua);
						dos.flush();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					repaint();
				}
			}

			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				if (x_tenlua > 50) {
					x_tenlua -= 50;
					try {
						dos.writeInt(x_tenlua);
						dos.flush();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					repaint();
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				int dan = x_tenlua / 50;
				listdan[9][dan] = true;
				repaint();

				try {
					dos.writeInt(dan);
					dos.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				// System.out.println(dan);
			}
			Toolkit.getDefaultToolkit().sync();

		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			timer_dan.start();
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 16; j++) {
					if (listdan[i][j] == true) {
						listdan[i][j] = false;
						if (i > 0)
						{
							listdan[i - 1][j] = true;
						}
					}
				}
			}
			painter.repaint();

		}

	}

	private class Maybay extends Timer implements ActionListener {//XET XEM THANG NAO BANG TRUE THI CHO NO BAY XUONG
		public Maybay(int delay, ActionListener listener) {
			super(delay, listener);
			System.out.println("START MAY BAY");
			this.start();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			System.out.println("GOGOOGOGO");
			this.start();
			for(int i = 0; i < 3; i++) // them bien dieu chinh so luong may bay
			{
				int temp = generator.nextInt(16);
				listmb[0][temp] = true;
			}
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 16; j++) {
					if (listmb[i][j] == true) {
						listmb[i][j] = false;
						if (i < 10)
						{
							listmb[i + 1][j] = true;
						}
					}
				}
			}
			painter.repaint();
		}

	}
}
