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
package fr.inria.eventcloud.delayers.actions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.MoreExecutors;

import fr.inria.eventcloud.delayers.buffers.Buffer;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Actions performed by the buffer.
 * 
 * @author lpellegr
 */
public abstract class Action<T> {

    protected final SemanticCanOverlay overlay;

    protected final ExecutorService threadPool;

    public Action(SemanticCanOverlay overlay, int threadPoolSize) {
        this.overlay = overlay;

        if (threadPoolSize == 0) {
            this.threadPool = MoreExecutors.sameThreadExecutor();
        } else {
            this.threadPool =
                    Executors.newFixedThreadPool(
                            threadPoolSize, new ThreadFactory() {
                                private int counter = 0;

                                @Override
                                public Thread newThread(Runnable r) {
                                    return new Thread(r, this.getClass()
                                            .getSimpleName()
                                            + " Thread("
                                            + (this.counter++)
                                            + ")");
                                }
                            });
        }
    }

    public abstract void perform(Buffer<T> buffer);

}
