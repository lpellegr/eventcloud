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
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ColorUIResource;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.StringCanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetSplitHistoryOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetSplitHistoryResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.can.FloodingBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.BroadcastConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to draw a canvas that shows a Content-Addressable Network
 * where it is possible to check the neighbors of a peer by clicking on it.
 * 
 * @author lpellegr
 */
public class Can2dVisualizer extends JFrame {

    private static final Logger log =
            LoggerFactory.getLogger(Can2dVisualizer.class);

    private static final long serialVersionUID = 160L;

    private static final int DEFAULT_CANVAS_HEIGHT = 800;

    private static final int DEFAULT_CANVAS_WIDTH = 1024;

    private JComponent area;

    private JButton joinButton;

    private JButton leaveButton;

    private JButton neighborsModeButton;

    private Checkbox displaySplitHistoryCheckbox;

    private PeersCache cache;

    private Map<OverlayId, Color> peerColors;

    private enum Mode {
        JOIN, LEAVE, SHOW_NEIGHBORS
    };

    private Mode mode;

    public Can2dVisualizer(List<Peer> peers) {
        this.cache = new PeersCache();
        this.peerColors = new HashMap<OverlayId, Color>();

        for (Peer peer : peers) {
            this.cache.addEntry(peer);
            this.peerColors.put(peer.getId(), getRandomColor());
        }

        this.mode = Mode.SHOW_NEIGHBORS;
        this.createAndShowGUI();
    }

    public Color getPeerColor(OverlayId peerId) {
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
        this.area = new Canvas(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);

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
        this.neighborsModeButton = new JButton("Neighbors");
        this.neighborsModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Can2dVisualizer.this.mode = Mode.SHOW_NEIGHBORS;
            }
        });

        this.displaySplitHistoryCheckbox =
                new Checkbox("Display Split History", false);
        this.displaySplitHistoryCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Can2dVisualizer.this.repaint();
            }
        });

        JPanel topBar = new JPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.CENTER));
        topBar.add(this.joinButton);
        topBar.add(this.leaveButton);
        topBar.add(this.neighborsModeButton);
        topBar.add(this.displaySplitHistoryCheckbox);

        Container contentPane = super.getContentPane();
        contentPane.add(topBar, BorderLayout.NORTH);
        contentPane.add(this.area, BorderLayout.CENTER);

        super.pack();
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setMinimumSize(new Dimension(
                DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT));
        super.setResizable(true);
        super.setTitle("CAN 2D View");
        super.setLocationRelativeTo(null);
    }

    public class Canvas extends JComponent {

        private static final long serialVersionUID = 160L;

        public Zone<StringCoordinate> zoneClicked = null;

        public Canvas(int width, final int height) {
            super();
            super.setSize(width, height);
            super.setPreferredSize(new Dimension(width, height));

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    PeerEntry entry =
                            Can2dVisualizer.this.cache.findBy(
                                    Canvas.this.getWidth(),
                                    Canvas.this.getHeight(), e.getX(), e.getY());

                    if (entry == null) {
                        return;
                    }

                    log.debug(entry.getStub().dump());

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
                        } else if (Can2dVisualizer.this.mode == Mode.LEAVE) {
                            Canvas.this.zoneClicked = null;

                            try {
                                entry.getStub().leave();
                            } catch (NetworkNotJoinedException nnje) {
                                nnje.printStackTrace();
                            }

                            Can2dVisualizer.this.cache.removeEntry(entry.getId());
                            Can2dVisualizer.this.cache.invalidate();
                            Can2dVisualizer.this.peerColors.remove(entry.getId());
                        } else if (Can2dVisualizer.this.mode == Mode.SHOW_NEIGHBORS) {
                            Canvas.this.zoneClicked = entry.getZone();
                        }

                        Canvas.this.repaint();

                        log.info("Clicked in (" + e.getX() + "," + e.getY()
                                + ") wich is contained by zone "
                                + entry.getZone());
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        Can2dVisualizer.this.peerColors.clear();
                        Canvas.this.repaint();
                    }
                }
            });
        }

        public int getXmin(Zone<StringCoordinate> z) {
            return (int) z.getLowerBound((byte) 0)
                    .normalize(0, this.getWidth());
        }

        public int getXmax(Zone<StringCoordinate> z) {
            return (int) z.getUpperBound((byte) 0)
                    .normalize(0, this.getWidth());
        }

        public int getYmin(Zone<StringCoordinate> z) {
            return (int) z.getLowerBound((byte) 1).normalize(
                    0, this.getHeight());
        }

        public int getYmax(Zone<StringCoordinate> z) {
            return (int) z.getUpperBound((byte) 1).normalize(
                    0, this.getHeight());
        }

        public int getHeight(double v) {
            return (int) Math.round(this.getHeight() * v);
        }

        public int getWidth(double v) {
            return (int) Math.round(this.getWidth() * v);
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

            int xMin, xMax, yMin, yMax;
            for (PeerEntry entry : Can2dVisualizer.this.cache) {
                Zone<StringCoordinate> zone = entry.getZone();
                xMin = this.getXmin(zone);
                xMax = this.getXmax(zone);
                yMin = this.getYmin(zone);
                yMax = this.getYmax(zone);

                g2d.setColor(Can2dVisualizer.this.getPeerColor(entry.getId()));
                g2d.fillRect(xMin, yMin, xMax - xMin, yMax - yMin);
                g2d.setColor(Color.DARK_GRAY);

                if (this.zoneClicked == zone) {
                    g2d.drawOval(xMin + ((xMax - xMin) / 2) - 8, yMin
                            + ((yMax - yMin) / 2) - 8, 15, 15);
                    g2d.fillOval(xMin + ((xMax - xMin) / 2) - 5, yMin
                            + ((yMax - yMin) / 2) - 5, 10, 10);
                }
            }

            PeerEntry peerClicked =
                    Can2dVisualizer.this.cache.findBy(this.zoneClicked);
            g2d.setColor(Color.BLACK);

            if (peerClicked != null) {
                for (Zone<StringCoordinate> z : peerClicked.getNeighbors()) {
                    xMin = this.getXmin(z);
                    xMax = this.getXmax(z);
                    yMin = this.getYmin(z);
                    yMax = this.getYmax(z);

                    g2d.drawLine(xMin, yMin, xMax, yMax);
                    g2d.drawLine(xMin, yMax, xMax, yMin);
                }
            }

            if (Can2dVisualizer.this.displaySplitHistoryCheckbox.getState()) {
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
                for (PeerEntry entry : Can2dVisualizer.this.cache) {
                    Zone<StringCoordinate> zone = entry.getZone();
                    xMin = this.getXmin(zone);
                    yMin = this.getYmin(zone);

                    g2d.drawString(
                            entry.getSplitHistoryAsString(), xMin + 5,
                            yMin + 15);
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

        private final Zone<StringCoordinate> zone;

        private final List<SplitEntry> splitHistory;

        private final OverlayId id;

        private final Peer stub;

        private final List<Zone<StringCoordinate>> neighbors;

        public PeerEntry(final OverlayId id, final Peer stub,
                final Zone<StringCoordinate> zone,
                final List<Zone<StringCoordinate>> neighbors,
                List<SplitEntry> splitHistory) {
            this.id = id;
            this.stub = stub;
            this.zone = zone;
            this.neighbors = neighbors;
            this.splitHistory = splitHistory;
        }

        public OverlayId getId() {
            return this.id;
        }

        public Zone<StringCoordinate> getZone() {
            return this.zone;
        }

        public Peer getStub() {
            return this.stub;
        }

        public List<Zone<StringCoordinate>> getNeighbors() {
            return this.neighbors;
        }

        public String getSplitHistoryAsString() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < this.splitHistory.size(); i++) {
                SplitEntry se = this.splitHistory.get(i);

                result.append('{');
                result.append(se.getDimension());
                result.append(',');
                result.append(se.getDirection());
                result.append('}');

                if (i < this.splitHistory.size() - 1) {
                    result.append(',');
                }
            }
            return result.toString();
        }

    }

    private static class PeersCache implements Iterable<PeerEntry> {

        private Map<OverlayId, Peer> stubEntries;

        private Map<OverlayId, PeerEntry> cacheEntries;

        public PeersCache() {
            this.stubEntries = new HashMap<OverlayId, Peer>();
            this.cacheEntries = new HashMap<OverlayId, PeerEntry>();
        }

        public void addEntry(Peer peer) {
            this.stubEntries.put(peer.getId(), peer);
        }

        public void removeEntry(OverlayId peerId) {
            this.stubEntries.remove(peerId);
        }

        public synchronized void invalidate() {
            this.cacheEntries.clear();
        }

        public PeerEntry findBy(Zone<StringCoordinate> zone) {
            this.fixCacheCoherence();

            for (PeerEntry entry : this.cacheEntries.values()) {
                if (entry.getZone().equals(zone)) {
                    return entry;
                }
            }

            return null;
        }

        public PeerEntry findBy(int canvasWidth, int canvasHeight, int x, int y) {
            this.fixCacheCoherence();

            double scaleHeight =
                    canvasHeight
                            / (double) (P2PStructuredProperties.CAN_UPPER_BOUND.getValue() - P2PStructuredProperties.CAN_LOWER_BOUND.getValue());

            double scaleWidth =
                    canvasWidth
                            / (double) (P2PStructuredProperties.CAN_UPPER_BOUND.getValue() - P2PStructuredProperties.CAN_LOWER_BOUND.getValue());

            for (PeerEntry entry : this.cacheEntries.values()) {
                if (entry.getZone()
                        .contains(
                                new Point<StringCoordinate>(
                                        new StringCoordinate(
                                                new String(
                                                        Character.toChars((int) (x
                                                                / scaleWidth + P2PStructuredProperties.CAN_LOWER_BOUND.getValue())))),
                                        new StringCoordinate(
                                                new String(
                                                        Character.toChars((int) (y
                                                                / scaleHeight + P2PStructuredProperties.CAN_LOWER_BOUND.getValue()))))))) {
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
                for (OverlayId id : this.stubEntries.keySet()) {
                    if (!this.cacheEntries.containsKey(id)) {
                        this.populate(id);
                    }
                }
            }
        }

        private void populate(OverlayId id) {
            NeighborTable<StringCoordinate> table = null;
            List<Zone<StringCoordinate>> neighbors =
                    new ArrayList<Zone<StringCoordinate>>();
            Peer peerStub = this.stubEntries.get(id);

            for (byte dim = 0; dim < 2; dim++) {
                for (byte dir = 0; dir < 2; dir++) {
                    table = CanOperations.getNeighborTable(peerStub);
                    for (NeighborEntry<StringCoordinate> entry : table.get(
                            dim, dir).values()) {
                        neighbors.add(entry.getZone());
                    }
                }
            }

            @SuppressWarnings("unchecked")
            GetSplitHistoryResponseOperation<StringCoordinate> splitHistory =
                    (GetSplitHistoryResponseOperation<StringCoordinate>) PAFuture.getFutureValue(peerStub.receive(new GetSplitHistoryOperation<StringCoordinate>()));

            this.cacheEntries.put(
                    id,
                    new PeerEntry(
                            id,
                            peerStub,
                            CanOperations.<StringCoordinate> getIdAndZoneResponseOperation(
                                    peerStub)
                                    .getPeerZone(), neighbors,
                            splitHistory.getSplitHistory()));
        }
    }

    @SuppressWarnings("unused")
    private static final class PrintSplitHistoryRequest extends
            MulticastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public PrintSplitHistoryRequest() {
            super(new BroadcastConstraintsValidator<StringCoordinate>(
                    new Point<StringCoordinate>(null, null)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FloodingBroadcastRequestRouter<MulticastRequest<StringCoordinate>, StringCoordinate> getRouter() {
            return new FloodingBroadcastRequestRouter<MulticastRequest<StringCoordinate>, StringCoordinate>() {
                @Override
                public void onPeerValidatingKeyConstraints(CanOverlay<StringCoordinate> overlay,
                                                           org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest<StringCoordinate> request) {
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
        if (args.length == 1) {
            if (args[0].equals("-fractal")) {
                injectionConstraintsProvider =
                        InjectionConstraintsProvider.newFractalInjectionConstraintsProvider();
            } else if (args[0].equals("-uniform")) {
                injectionConstraintsProvider =
                        InjectionConstraintsProvider.newUniformInjectionConstraintsProvider();
            }
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
    }

}
