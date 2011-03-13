package org.objectweb.proactive.extensions.p2p.structured.util;


/**
 * A set of utility operations that provide necessary information about the
 * architecture of the machine that the system is running on. The values
 * provided are automatically determined at JVM startup.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @author lpellegr
 */
public final class SystemUtil {

	private static final int numProcessors;

	private static final String architecture;

	private static final String osName;

	private static final String osVersion;

	private static final boolean windows;

	private static final boolean linux;

	static {
		architecture = System.getProperty("os.arch");
		osName = System.getProperty("os.name");
		osVersion = System.getProperty("os.version");
		numProcessors = Runtime.getRuntime().availableProcessors();
		windows = SystemUtil.operatingSystem().startsWith("Windows");
		linux = SystemUtil.operatingSystem().startsWith("Linux");
	}

	/**
	 * Keep utility from being instantiated.
	 */
	private SystemUtil() {

	}

	public static final String architecture() {
		return architecture;
	}
	
	/**
	 * Return the number of processors available on this machine. This is useful
	 * in classes like Thread/Processor thread pool models.
	 */
	public static final int numProcessors() {
		return numProcessors;
	}

	/**
	 * Return the Operating System name.
	 */
	public static final String operatingSystem() {
		return osName;
	}

	/**
	 * Return the Operating System version.
	 */
	public static final String osVersion() {
		return osVersion;
	}

	/**
	 * Return <code>true</code> if running on Microsoft Windows.
	 */
	public static final boolean isWindows() {
		return windows;
	}

	/**
	 * Return <code>true</code> if running on Linux.
	 */
	public static final boolean isLinux() {
		return linux;
	}

	/**
	 * Returns the optimal number of threads to create depending of the desired
	 * system load and the estimated ratio between the waiting time and the
	 * computation time to parallelize.
	 * <p>
	 * For a task which needs to use all system resources and which is pure CPU
	 * (i.e. no I/O waiting time), this method must be called with arguments
	 * <code>systemLoad=1</code> and <code>ratio=0</code>.
	 * 
	 * @param systemLoad
	 *            indicates what will be the system load. Must be a double
	 *            between <code>]0; 1]</code>.
	 * 
	 * @param ratio
	 *            the ratio between the waiting time and the computation time.
	 *            Must be between <code>]0;1]</code>.
	 * 
	 * @return the optimal number of threads to create depending of the desired
	 *         system load and the estimated ratio between the waiting time and
	 *         the computation time to parallelize.
	 */
	public static int getOptimalNumberOfThreads(double systemLoad, double ratio) {
		return (int) Math.round(
						Runtime.getRuntime().availableProcessors() 
							* systemLoad * (1 + ratio));
	}

	/**
	 * Returns {@link #getOptimalNumberOfThreads(double, double)} with
	 * respectively parameters {@code systemLoad} equals to {@code 1} and
	 * {@code ratio} equals to {@code 0}.
	 * 
	 * @return {@link #getOptimalNumberOfThreads(double, double)} with
	 *         respectively parameters {@code systemLoad} equals to {@code 1}
	 *         and {@code ratio} equals to {@code 0}.
	 */
	public static int getOptimalNumberOfThreads() {
		return getOptimalNumberOfThreads(1, 0);
	}
	
}
