package fr.inria.eventcloud.messages.can;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.DoubleCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.DoubleElement;

/**
 * 
 * @author lpellegr
 */
public class Network2DVisualizer extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int CANVAS_HEIGHT = 800;
	
	private static final int CANVAS_WIDTH = 800;
	
	private JComponent area;

	private Map<Zone, ZoneEntry> peers;
	
	public Network2DVisualizer(List<Peer> peers) {
		this.peers = new HashMap<Zone, ZoneEntry>();
		NeighborTable table = null;
		for (Peer peer : peers) {
			List<Zone> neighbors = new ArrayList<Zone>();
			for (int dim = 0; dim < 2; dim++) {
				for (int dir = 0; dir < 2; dir++) {
					table = CanOperations.getNeighborTable(peer);
					for (NeighborEntry entry : table.get(dim, dir).values()) {
						neighbors.add((Zone) entry.getZone());
					}
				}
			}
			
			this.peers.put(
					(Zone) CanOperations.getIdAndZoneResponseOperation(peer).getPeerZone(),
					new ZoneEntry(getRandomColor(), neighbors));
		}

		this.createAndShowGUI();
	}

	private static Color getRandomColor() {
        int r = ProActiveRandom.nextInt(256);
        int v = ProActiveRandom.nextInt(256);
        int b = ProActiveRandom.nextInt(256);

        if (r + v + b < 420) {
            return getRandomColor();
        }

        return new Color(r, v, b);
    }
	
	public void createAndShowGUI() {
		this.area = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

		Container contentPane = super.getContentPane();
		contentPane.add(this.area, BorderLayout.CENTER);

		super.pack();
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		super.setResizable(false);
		super.setTitle("CAN 2D View");
		super.setLocationRelativeTo(null);
	}

	public class Canvas extends JComponent {
		
		private static final long serialVersionUID = 1L;

		public Zone zoneClicked = null;

		public Canvas(int width, int height) {
			super();
			super.setSize(width, height);
			super.setPreferredSize(new Dimension(width, height));

			this.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					Zone clickedZone = Canvas.this.getClicked(e.getX(), CANVAS_HEIGHT - e.getY());
					if (e.getButton() == MouseEvent.BUTTON1) {
						Canvas.this.zoneClicked = clickedZone;
						System.out.println("Clicked in (" + e.getX() + "," + e.getY() + ") wich is contained by zone " + clickedZone.getNumericView() + " <-> " + clickedZone);
						Canvas.this.repaint();
					} else if (e.getButton() == MouseEvent.BUTTON3) {
						for (ZoneEntry entry : peers.values()) {
							entry.setColor(getRandomColor()); 
						}
						Canvas.this.repaint();
					}
				}
			});
		}

		public int getXmin(Zone z) {
			return this.getWidth(z.getNumericView().getLowerBound(0).getValue());
		}
		
		public int getXmax(Zone z) {
			return this.getWidth(z.getNumericView().getUpperBound(0).getValue());
		}
		
		public int getYmin(Zone z) { 
			return this.getWidth(z.getNumericView().getLowerBound(1).getValue());
		}
		
		public int getYmax(Zone z) {
			return this.getWidth(z.getNumericView().getUpperBound(1).getValue());
		}
		
		public int getHeight(double v) {
			return (int) Math.round(CANVAS_HEIGHT * v);
		}

		public int getWidth(double v) {
			return (int) Math.round(CANVAS_WIDTH * v); 
		}
		
		public void paintComponent(Graphics g) {
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int height, xMin, xMax, yMin, yMax;
			for (Zone zone : peers.keySet()) {
				xMin = this.getXmin(zone);
				xMax = this.getXmax(zone);
				yMin = this.getYmin(zone);
				yMax = this.getYmax(zone);
				
				g2d.setColor(Network2DVisualizer.this.peers.get(zone).getZoneColor());
				height = yMax - yMin;
				g2d.fillRect(xMin, CANVAS_HEIGHT - yMin - height, xMax - xMin, height);
				g2d.setColor(Color.black);
			}

			if (this.zoneClicked != null) {
				xMin = this.getXmin(this.zoneClicked);
				xMax = this.getXmax(this.zoneClicked);
				yMin = this.getYmin(this.zoneClicked);
				yMax = this.getYmax(this.zoneClicked);
				
				g2d.setColor(Color.white);
				g2d.fillOval(xMin + ((xMax - xMin) / 2) - 8, CANVAS_HEIGHT - (yMin + ((yMax - yMin) / 2)) - 8, 16, 16);
				g2d.drawLine(xMin, CANVAS_HEIGHT - yMin, xMax, CANVAS_HEIGHT - yMax);
				g2d.drawLine(xMax, CANVAS_HEIGHT - yMin, xMin, CANVAS_HEIGHT - yMax);
				g2d.setColor(Color.black);
				g2d.drawOval(xMin + ((xMax - xMin) / 2) - 8, CANVAS_HEIGHT - (yMin + ((yMax - yMin) / 2)) - 8, 16, 16);
				
				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < 2; j++) {
						for (Zone zone : peers.get(this.zoneClicked).getNeighbors()) {
							xMin = this.getXmin(zone);
							xMax = this.getXmax(zone);
							yMin = this.getYmin(zone);
							yMax = this.getYmax(zone);

							g2d.drawLine(xMin, CANVAS_HEIGHT - yMin, xMax, CANVAS_HEIGHT - yMax);
							g2d.drawLine(xMax, CANVAS_HEIGHT - yMin, xMin, CANVAS_HEIGHT - yMax);
						}
					}
				}
			}
		}
		
		public Zone getClicked(int x, int y) {
			for (Zone zone : peers.keySet()) {
				if (zone.getNumericView().contains(
				        new DoubleCoordinate(
				                new DoubleElement(x / (double) CANVAS_WIDTH), 
				                new DoubleElement(y / (double) CANVAS_HEIGHT)))) {
					return zone;
				}
			}
			return null;
		}
		
		public boolean contains(double[][] intervals, double[] coordinates) {
	        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
	            if ((coordinates[i] < intervals[i][0])
	                    || (coordinates[i] >= intervals[i][1])) {
	                return false;
	            }
	        }

	        return true;
	    }
	}
	
	private class ZoneEntry {
		
		private Color zoneColor;
		
		private final List<Zone> neighbors;

		public ZoneEntry(final Color zoneColor, final List<Zone> neighbors) {
			super();
			this.zoneColor = zoneColor;
			this.neighbors = neighbors;
		}

		public void setColor(Color randomColor) {
			this.zoneColor = randomColor;
		}

		public Color getZoneColor() {
			return this.zoneColor;
		}

		public List<Zone> getNeighbors() {
			return this.neighbors;
		}
		
	}
	
}
