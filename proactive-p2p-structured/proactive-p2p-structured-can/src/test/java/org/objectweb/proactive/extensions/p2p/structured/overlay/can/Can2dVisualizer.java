/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
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

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.StringCanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;

/**
 * This class is used to draw a canvas that shows a Content-Addressable Network
 * where it is possible to check the neighbors of a peer by clicking on it.
 * 
 * @author lpellegr
 */
public class Can2dVisualizer extends JFrame {

    private static final long serialVersionUID = 140L;

    private static final int CANVAS_HEIGHT = 800;

    private static final int CANVAS_WIDTH = 800;

    private JComponent area;

    private JButton joinButton;

    private JButton leaveButton;

    private JButton neighborsModeButton;

    private PeersCache cache;

    private Map<UUID, Color> peerColors;

    private enum Mode {
        JOIN, LEAVE, SHOW_NEIGHBORS
    };

    private Mode mode;

    public Can2dVisualizer(List<Peer> peers) {
        this.cache = new PeersCache();
        this.peerColors = new HashMap<UUID, Color>();

        for (Peer peer : peers) {
            this.cache.addEntry(peer);
            this.peerColors.put(peer.getId(), getRandomColor());
        }

        this.mode = Mode.SHOW_NEIGHBORS;
        this.createAndShowGUI();
    }

    public Color getPeerColor(UUID peerId) {
        Color color = this.peerColors.get(peerId);

        if (color == null) {
            color = getRandomColor();
            this.peerColors.put(peerId, color);
        }

        return color;
    }

    private static Color getRandomColor() {
        int r = RandomUtils.nextInt(256);
        int v = RandomUtils.nextInt(256);
        int b = RandomUtils.nextInt(256);

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
                Can2dVisualizer.this.mode = Mode.JOIN;
                Can2dVisualizer.this.joinButton.setBackground(new ColorUIResource(
                        127, 238, 38));
            }
        });
        this.leaveButton = new JButton("Leave");
        this.leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Can2dVisualizer.this.mode = Mode.LEAVE;
            }
        });
        this.neighborsModeButton = new JButton("Neighbors Mode");
        this.neighborsModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Can2dVisualizer.this.mode = Mode.SHOW_NEIGHBORS;
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

        private static final long serialVersionUID = 140L;

        public Zone<StringElement> zoneClicked = null;

        public Canvas(int width, int height) {
            super();
            super.setSize(width, height);
            super.setPreferredSize(new Dimension(width, height));

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    PeerEntry entry =
                            Can2dVisualizer.this.cache.findBy(
                                    e.getX(), CANVAS_HEIGHT - e.getY());

                    if (entry == null) {
                        return;
                    }

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (Can2dVisualizer.this.mode == Mode.JOIN) {
                            Canvas.this.zoneClicked = null;

                            Peer newPeer =
                                    PeerFactory.newPeer(SerializableProvider.create(StringCanOverlay.class));
                            try {
                                newPeer.join(entry.getStub());
                            } catch (NetworkAlreadyJoinedException ex) {
                                throw new IllegalStateException(ex);
                            } catch (PeerNotActivatedException ex) {
                                throw new IllegalStateException(ex);
                            }

                            Can2dVisualizer.this.cache.addEntry(newPeer);
                            Can2dVisualizer.this.cache.invalidate();

                            // System.out.println("--> JOIN");
                            // entry.getStub().sendv(
                            // new PrintSplitHistoryRequest());
                        } else if (Can2dVisualizer.this.mode == Mode.LEAVE) {
                            Canvas.this.zoneClicked = null;

                            try {
                                entry.getStub().leave();
                            } catch (NetworkNotJoinedException nnje) {
                                nnje.printStackTrace();
                            }

                            // System.out.println("--> LEAVE");
                            // entry.getStub().sendv(
                            // new PrintSplitHistoryRequest());

                            Can2dVisualizer.this.cache.removeEntry(entry.getId());
                            Can2dVisualizer.this.cache.invalidate();
                            Can2dVisualizer.this.peerColors.remove(entry.getId());
                        } else if (Can2dVisualizer.this.mode == Mode.SHOW_NEIGHBORS) {
                            Canvas.this.zoneClicked = entry.getZone();
                        }

                        Canvas.this.repaint();

                        System.out.println("Clicked in (" + e.getX() + ","
                                + e.getY() + ") wich is contained by zone "
                                + entry.getZone());
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        Can2dVisualizer.this.peerColors.clear();
                        Canvas.this.repaint();
                    }
                }
            });
        }

        public int getXmin(Zone<StringElement> z) {
            return (int) z.getLowerBound((byte) 0).normalize(0, CANVAS_WIDTH);
        }

        public int getXmax(Zone<StringElement> z) {
            return (int) z.getUpperBound((byte) 0).normalize(0, CANVAS_WIDTH);
        }

        public int getYmin(Zone<StringElement> z) {
            return (int) z.getLowerBound((byte) 1).normalize(0, CANVAS_HEIGHT);
        }

        public int getYmax(Zone<StringElement> z) {
            return (int) z.getUpperBound((byte) 1).normalize(0, CANVAS_HEIGHT);
        }

        public int getHeight(double v) {
            return (int) Math.round(CANVAS_HEIGHT * v);
        }

        public int getWidth(double v) {
            return (int) Math.round(CANVAS_WIDTH * v);
        }

        @Override
        public void paintComponent(Graphics g) {
            final Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            int height, xMin, xMax, yMin, yMax;
            for (PeerEntry entry : Can2dVisualizer.this.cache) {
                Zone<StringElement> zone = entry.getZone();
                xMin = this.getXmin(zone);
                xMax = this.getXmax(zone);
                yMin = this.getYmin(zone);
                yMax = this.getYmax(zone);

                g2d.setColor(Can2dVisualizer.this.getPeerColor(entry.getId()));
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

                // g2d.drawLine(xMin, CANVAS_HEIGHT - yMin, xMax, CANVAS_HEIGHT
                // - yMax);
                // g2d.drawLine(xMax, CANVAS_HEIGHT - yMin, xMin, CANVAS_HEIGHT
                // - yMax);

                g2d.drawOval(xMin + ((xMax - xMin) / 2) - 8, CANVAS_HEIGHT
                        - (yMin + ((yMax - yMin) / 2)) - 8, 15, 15);
                g2d.fillOval(xMin + ((xMax - xMin) / 2) - 5, CANVAS_HEIGHT
                        - (yMin + ((yMax - yMin) / 2)) - 5, 10, 10);

                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        PeerEntry peerClicked =
                                Can2dVisualizer.this.cache.findBy(this.zoneClicked);

                        if (peerClicked != null) {
                            for (Zone<StringElement> zone : peerClicked.getNeighbors()) {
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

            g.dispose();
        }

        public boolean contains(double[][] intervals, double[] coordinates) {
            for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
                if (coordinates[i] < intervals[i][0]
                        || coordinates[i] >= intervals[i][1]) {
                    return false;
                }
            }

            return true;
        }
    }

    private static class PeerEntry {

        private final Zone<StringElement> zone;

        private final UUID id;

        private final Peer stub;

        private final List<Zone<StringElement>> neighbors;

        public PeerEntry(final UUID id, final Peer stub,
                final Zone<StringElement> zone,
                final List<Zone<StringElement>> neighbors) {
            this.id = id;
            this.stub = stub;
            this.zone = zone;
            this.neighbors = neighbors;
        }

        public UUID getId() {
            return this.id;
        }

        public Zone<StringElement> getZone() {
            return this.zone;
        }

        public Peer getStub() {
            return this.stub;
        }

        public List<Zone<StringElement>> getNeighbors() {
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

        public PeerEntry findBy(Zone<StringElement> zone) {
            this.fixCacheCoherence();

            for (PeerEntry entry : this.cacheEntries.values()) {
                if (entry.getZone().equals(zone)) {
                    return entry;
                }
            }

            return null;
        }

        private static final double SCALE_HEIGHT =
                P2PStructuredProperties.CAN_UPPER_BOUND.getValue()
                        / (double) CANVAS_HEIGHT;
        private static final double SCALE_WIDTH =
                P2PStructuredProperties.CAN_UPPER_BOUND.getValue()
                        / (double) CANVAS_WIDTH;

        public PeerEntry findBy(int x, int y) {
            this.fixCacheCoherence();

            for (PeerEntry entry : this.cacheEntries.values()) {
                if (entry.getZone()
                        .contains(
                                new Coordinate<StringElement>(
                                        new StringElement(
                                                new String(
                                                        Character.toChars((int) (x * SCALE_WIDTH)))),
                                        new StringElement(
                                                new String(
                                                        Character.toChars((int) (y * SCALE_HEIGHT))))))) {
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
            NeighborTable<StringElement> table = null;
            List<Zone<StringElement>> neighbors =
                    new ArrayList<Zone<StringElement>>();
            Peer peerStub = this.stubEntries.get(id);

            for (byte dim = 0; dim < 2; dim++) {
                for (byte dir = 0; dir < 2; dir++) {
                    table = CanOperations.getNeighborTable(peerStub);
                    for (NeighborEntry<StringElement> entry : table.get(
                            dim, dir).values()) {
                        neighbors.add(entry.getZone());
                    }
                }
            }

            this.cacheEntries.put(
                    id,
                    new PeerEntry(
                            id,
                            peerStub,
                            CanOperations.<StringElement> getIdAndZoneResponseOperation(
                                    peerStub)
                                    .getPeerZone(), neighbors));
        }

    }

    @SuppressWarnings("unused")
    private static final class PrintSplitHistoryRequest extends
            AnycastRequest<StringElement> {

        private static final long serialVersionUID = 140L;

        public PrintSplitHistoryRequest() {
            super(new DefaultAnycastConstraintsValidator<StringElement>(
                    new Coordinate<StringElement>(null, null)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AnycastRequestRouter<AnycastRequest<StringElement>, StringElement> getRouter() {
            return new AnycastRequestRouter<AnycastRequest<StringElement>, StringElement>() {
                @Override
                public void onPeerValidatingKeyConstraints(CanOverlay<StringElement> overlay,
                                                           org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest<StringElement> request) {
                    System.err.println("Peer " + overlay.getZone());
                    for (SplitEntry entry : overlay.getSplitHistory()) {
                        System.err.println("  " + entry);
                    }
                }
            };
        }
    }

    public static void main(String[] args) {
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 2);
        P2PStructuredProperties.CAN_REFRESH_TASK_INTERVAL.setValue(1000);

        InjectionConstraintsProvider injectionConstraintsProvider = null;
        if (args.length == 1 && args[0].equals("-fractal")) {
            injectionConstraintsProvider =
                    InjectionConstraintsProvider.newFractalInjectionConstraintsProvider();
        }

        CanNetworkDeployer deployer =
                new CanNetworkDeployer(
                        new StringCanDeploymentDescriptor().setInjectionConstraintsProvider(injectionConstraintsProvider));
        deployer.deploy(100);

        final List<Peer> peers = deployer.getRandomTracker().getPeers();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Can2dVisualizer(peers).setVisible(true);
            }
        });

        deployer.undeploy();
    }

}
