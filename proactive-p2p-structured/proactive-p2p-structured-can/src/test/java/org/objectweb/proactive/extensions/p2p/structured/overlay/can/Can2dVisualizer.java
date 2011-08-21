/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ColorUIResource;

import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanActiveObjectsNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.DoubleCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.DoubleElement;

/**
 * This class is used to draw a canvas that shows a Content-Addressable Network
 * where it is possible to check the neighbors of a peer by clicking on it.
 * 
 * @author lpellegr
 */
public class Can2dVisualizer extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final int CANVAS_HEIGHT = 800;

    private static final int CANVAS_WIDTH = 800;

    private JComponent area;

    private JButton joinButton;

    private JButton leaveButton;

    private JButton neighborsModeButton;

    private PeersCache cache;

    private enum Mode {
        JOIN, LEAVE, SHOW_NEIGHBORS
    };

    private Mode mode;

    public Can2dVisualizer(List<Peer> peers) {
        this.cache = new PeersCache();
        for (Peer peer : peers) {
            this.cache.addEntry(peer);
        }

        this.mode = Mode.SHOW_NEIGHBORS;
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

        this.joinButton = new JButton("Join");
        this.joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mode = Mode.JOIN;
                joinButton.setBackground(new ColorUIResource(127, 238, 38));
            }
        });
        this.leaveButton = new JButton("Leave");
        this.leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mode = Mode.LEAVE;
            }
        });
        this.neighborsModeButton = new JButton("Neighbors Mode");
        this.neighborsModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mode = Mode.SHOW_NEIGHBORS;
            }
        });

        JPanel toolBar = new JPanel();
        toolBar.setLayout(new FlowLayout(FlowLayout.CENTER));
        toolBar.add(this.joinButton);
        toolBar.add(this.leaveButton);
        toolBar.add(this.neighborsModeButton);

        Container contentPane = super.getContentPane();

        contentPane.add(toolBar, BorderLayout.NORTH);
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
                    PeerEntry entry =
                            cache.findBy(e.getX(), CANVAS_HEIGHT - e.getY());

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (mode == Mode.JOIN) {
                            Peer newPeer =
                                    PeerFactory.newActivePeer(new CanOverlay());
                            try {
                                newPeer.join(entry.getStub());
                            } catch (NetworkAlreadyJoinedException ex) {
                                ex.printStackTrace();
                            }

                            cache.addEntry(newPeer);
                            cache.invalidate();
                        } else if (mode == Mode.LEAVE) {
                            try {
                                entry.getStub().leave();
                            } catch (NetworkNotJoinedException e1) {
                                e1.printStackTrace();
                            }

                            cache.removeEntry(entry.getId());
                            cache.invalidate();
                        } else if (mode == Mode.SHOW_NEIGHBORS) {
                            Canvas.this.zoneClicked = entry.getZone();
                        }

                        Canvas.this.repaint();

                        System.out.println("Clicked in (" + e.getX() + ","
                                + e.getY() + ") wich is contained by zone "
                                + entry.getZone().getNumericView() + " <-> "
                                + entry.getZone());
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        for (PeerEntry peerEntry : cache) {
                            peerEntry.setZoneColor(getRandomColor());
                        }
                        Canvas.this.repaint();
                    }
                }
            });
        }

        public int getXmin(Zone z) {
            return this.getWidth(z.getNumericView()
                    .getLowerBound((byte) 0)
                    .getValue());
        }

        public int getXmax(Zone z) {
            return this.getWidth(z.getNumericView()
                    .getUpperBound((byte) 0)
                    .getValue());
        }

        public int getYmin(Zone z) {
            return this.getWidth(z.getNumericView()
                    .getLowerBound((byte) 1)
                    .getValue());
        }

        public int getYmax(Zone z) {
            return this.getWidth(z.getNumericView()
                    .getUpperBound((byte) 1)
                    .getValue());
        }

        public int getHeight(double v) {
            return (int) Math.round(CANVAS_HEIGHT * v);
        }

        public int getWidth(double v) {
            return (int) Math.round(CANVAS_WIDTH * v);
        }

        public void paintComponent(Graphics g) {
            final Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int height, xMin, xMax, yMin, yMax;
            for (PeerEntry entry : cache) {
                Zone zone = entry.getZone();
                xMin = this.getXmin(zone);
                xMax = this.getXmax(zone);
                yMin = this.getYmin(zone);
                yMax = this.getYmax(zone);

                g2d.setColor(entry.getZoneColor());
                height = yMax - yMin;
                g2d.fillRect(
                        xMin, CANVAS_HEIGHT - yMin - height, xMax - xMin,
                        height);
                g2d.setColor(Color.black);
            }

            if (this.zoneClicked != null) {
                xMin = this.getXmin(this.zoneClicked);
                xMax = this.getXmax(this.zoneClicked);
                yMin = this.getYmin(this.zoneClicked);
                yMax = this.getYmax(this.zoneClicked);

                g2d.setColor(Color.white);
                g2d.fillOval(xMin + ((xMax - xMin) / 2) - 8, CANVAS_HEIGHT
                        - (yMin + ((yMax - yMin) / 2)) - 8, 16, 16);
                g2d.drawLine(xMin, CANVAS_HEIGHT - yMin, xMax, CANVAS_HEIGHT
                        - yMax);
                g2d.drawLine(xMax, CANVAS_HEIGHT - yMin, xMin, CANVAS_HEIGHT
                        - yMax);
                g2d.setColor(Color.black);
                g2d.drawOval(xMin + ((xMax - xMin) / 2) - 8, CANVAS_HEIGHT
                        - (yMin + ((yMax - yMin) / 2)) - 8, 16, 16);

                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        for (Zone zone : cache.findBy(this.zoneClicked)
                                .getNeighbors()) {
                            xMin = this.getXmin(zone);
                            xMax = this.getXmax(zone);
                            yMin = this.getYmin(zone);
                            yMax = this.getYmax(zone);

                            g2d.drawLine(
                                    xMin, CANVAS_HEIGHT - yMin, xMax,
                                    CANVAS_HEIGHT - yMax);
                            g2d.drawLine(
                                    xMax, CANVAS_HEIGHT - yMin, xMin,
                                    CANVAS_HEIGHT - yMax);
                        }
                    }
                }
            }
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

    private static class PeerEntry {

        private Color zoneColor;

        private final Zone zone;

        private final UUID id;

        private final Peer stub;

        private final List<Zone> neighbors;

        public PeerEntry(final UUID id, final Peer stub, final Zone zone,
                final Color zoneColor, final List<Zone> neighbors) {
            this.id = id;
            this.stub = stub;
            this.zone = zone;
            this.zoneColor = zoneColor;
            this.neighbors = neighbors;
        }

        public UUID getId() {
            return this.id;
        }

        public Color getZoneColor() {
            return this.zoneColor;
        }

        public void setZoneColor(Color zoneColor) {
            this.zoneColor = zoneColor;
        }

        public Zone getZone() {
            return this.zone;
        }

        public Peer getStub() {
            return this.stub;
        }

        public List<Zone> getNeighbors() {
            return this.neighbors;
        }

    }

    private static class PeersCache implements Iterable<PeerEntry> {

        private Map<UUID, Peer> stubEntries;

        private Map<UUID, PeerEntry> cacheEntries;

        public PeersCache() {
            this.stubEntries = new HashMap<UUID, Peer>();
            this.cacheEntries = new HashMap<UUID, PeerEntry>();
        }

        public void addEntry(Peer peer) {
            this.stubEntries.put(peer.getId(), peer);
        }

        public void removeEntry(UUID peerId) {
            this.stubEntries.remove(peerId);
        }

        public synchronized void invalidate() {
            this.cacheEntries.clear();
        }

        public PeerEntry findBy(Zone zone) {
            this.fixCacheCoherence();

            for (PeerEntry entry : this.cacheEntries.values()) {
                if (entry.getZone().equals(zone)) {
                    return entry;
                }
            }

            return null;
        }

        public PeerEntry findBy(int x, int y) {
            this.fixCacheCoherence();

            for (PeerEntry entry : this.cacheEntries.values()) {
                if (entry.getZone().getNumericView().contains(
                        new DoubleCoordinate(new DoubleElement(x
                                / (double) CANVAS_WIDTH), new DoubleElement(y
                                / (double) CANVAS_HEIGHT)))) {
                    return entry;
                }
            }

            return null;
        }

        @Override
        public Iterator<PeerEntry> iterator() {
            this.fixCacheCoherence();
            return this.cacheEntries.values().iterator();
        }

        private void fixCacheCoherence() {
            if (this.stubEntries.size() != this.cacheEntries.size()) {
                for (UUID id : this.stubEntries.keySet()) {
                    if (!this.cacheEntries.containsKey(id)) {
                        this.populate(id);
                    }
                }
            }
        }

        private void populate(UUID id) {
            NeighborTable table = null;
            List<Zone> neighbors = new ArrayList<Zone>();
            Peer peerStub = this.stubEntries.get(id);

            for (byte dim = 0; dim < 2; dim++) {
                for (byte dir = 0; dir < 2; dir++) {
                    table = CanOperations.getNeighborTable(peerStub);
                    for (NeighborEntry entry : table.get(dim, dir).values()) {
                        neighbors.add((Zone) entry.getZone());
                    }
                }
            }

            this.cacheEntries.put(id, new PeerEntry(
                    id, peerStub, CanOperations.getIdAndZoneResponseOperation(
                            peerStub).getPeerZone(), getRandomColor(),
                    neighbors));
        }

    }

    public static void main(String[] args) {
        P2PStructuredProperties.CAN_REFRESH_TASK_INTERVAL.setValue(1000);
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 2);

        CanActiveObjectsNetworkDeployer deployer =
                new CanActiveObjectsNetworkDeployer();

        deployer.deploy(20);

        final List<Peer> peers = new ArrayList<Peer>();
        for (Peer peer : deployer.getRandomTracker().getPeers()) {
            peers.add(peer);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Can2dVisualizer(peers).setVisible(true);
            }
        });

        deployer.undeploy();
    }

}
