/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.load_balancing.criteria;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Range;

import fr.inria.eventcloud.load_balancing.balancer.PeerAllocatorBalancer;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Disk usage load balancing criterion.
 * 
 * @author lpellegr
 */
public class DiskUsageCriterion extends Criterion {

    private static final long serialVersionUID = 160L;

    private final File directory;

    private final long virtualCapacity;

    public DiskUsageCriterion(String directory, long virtualCapacity) {
        super("diskUsage", new PeerAllocatorBalancer(),
                Range.closed(0.0, 100.0));

        this.directory = new File(directory);

        if (virtualCapacity <= 0) {
            this.virtualCapacity = this.directory.getTotalSpace();
        } else {
            this.virtualCapacity = virtualCapacity;
        }
    }

    public File getDirectory() {
        return this.directory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLoad(SemanticCanOverlay overlay) {
        return ((FileUtils.sizeOfDirectory(this.directory) * 100) / (double) this.virtualCapacity);
    }

    public long getVirtualCapacity() {
        return this.virtualCapacity;
    }

}
