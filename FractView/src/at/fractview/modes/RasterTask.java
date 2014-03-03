/*
 * This file is part of FractView.
 *
 * FractView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FractView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FractView.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.fractview.modes;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import android.graphics.Paint;
import android.os.Process;
import android.util.Log;

public class RasterTask implements AbstractImgCache.Task {
	
	private static final String TAG = "RasterTask"; 
	
	public static int INIT_STEP_SIZE = 64; // must be multiple of next value
	public static int STEP_SIZE_DIVISOR = 4; 

	private static final int THREAD_COUNT = 4;
	
	// Thread priority (low priority keeps device responsive.)
	private static final int THREAD_PRIORITY = Process.THREAD_PRIORITY_BACKGROUND;
	
	private Rasterable rasterable;
	
	private volatile boolean cancelled;
	private volatile boolean running;
	
	private long startTime;

	private Environment[] envs;
	
	private ExecutorService executorService;
	
	private CyclicBarrier nextStepSizeBarrier;
	
	private Runnable updateStatsRunnable = new Runnable() {
		@Override
		public void run() {
			updateStats();
		}
	};

	
	public RasterTask(Rasterable rasterable) {
		this.rasterable = rasterable;
		
		this.envs = new Environment[THREAD_COUNT];
		
		executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		nextStepSizeBarrier = new CyclicBarrier(THREAD_COUNT, updateStatsRunnable);
	}

	public void start(AbstractImgCache cache) {
		Log.d(TAG, "starting task...");
		
		rasterable.initStatistics();
		
		startTime = System.currentTimeMillis();
		
		running = true;
		
		// Create threads
		for(int i = 0; i < THREAD_COUNT; i++) {
			executorService.submit(new Worker(i, cache));
		}
	}
	
	@Override
	public double getRunTime() {
		return ((double) (System.currentTimeMillis() - startTime)) / 1000.;
	}

	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	private void updateStats() {
		for(int i = 0; i < THREAD_COUNT; i++) {
			RasterTask.this.rasterable.updateStatisticsFromEnv(envs[i]);
		}
	}
	
	public void cancel() {
		Log.d(TAG, "canceling tasks");
		this.cancelled = true;
		
		executorService.shutdownNow();
	}

	@Override
	public void join() throws InterruptedException {
		Log.d(TAG, "Awaiting termination...");
		executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		Log.d(TAG, "ExecutorService terminated...");
	}
	
	/**
	 * This class contains the working thread(s) that generates the fractal
	 *
	 */
	private class Worker implements Runnable {		
		private static final String TAG = "RasterTask.Worker";
		
		private int index;
		private AbstractImgCache cache;
		private Environment env;
		
		Paint paint = new Paint(); // Create one paint per worker.

		Worker(int index, AbstractImgCache cache) {
			this.cache = cache;
			this.index = index;

			// Create new environment
			this.env = rasterable.createEnvironment();
			envs[index] = env; // Set it in parent
		}

		int pointCount = 0;
		
		private void pix(int x, int y) throws CancelException {
			if(isCancelled()) throw new CancelException();
			paint.setColor(env.color(x, y));
			cache.canvas().drawPoint(x, y, paint);
			pointCount ++;
		}
		
		
		int paintCirc(int rad, int x0, int y0, int x1, int y1, int stepSize, int offset) throws CancelException {
			// draw top
			if(-rad >= y0) {
				int rb = Math.min(x1, rad); // right bound (x1 is excl)

				int x;
				
				for(x = Math.max(x0, -rad) + offset; x < rb; x += THREAD_COUNT) {
					pix(x * stepSize + cache.centerX, -rad * stepSize + cache.centerY);
				}
				
				offset = x - rb;
			}
			
			// draw right
			if(rad < x1) {
				int bb = Math.min(y1, rad); // bottom bound (y1 is excl)

				int y;
				
				for(y = Math.max(y0, -rad) + offset; y < bb; y += THREAD_COUNT) {
					pix(rad * stepSize + cache.centerX, y * stepSize + cache.centerY);
				}
				
				offset = y - bb;
			}
			
			// draw bottom
			if(rad < y1) {
				int lb = Math.max(x0 - 1, -rad); // x0 is incl
				
				int x;
				
				for(x = Math.min(x1 - 1, rad) - offset; x > lb; x -= THREAD_COUNT) {
					pix(x * stepSize + cache.centerX, rad * stepSize + cache.centerY);
				}
				
				offset = lb - x;
			}
			
			// draw left		
			if(-rad >= x0) {
				int tb = Math.max(y0 - 1, -rad); // y0 is incl bound
				
				int y;
				
				for(y = Math.min(y1 - 1, rad) - offset; y > tb; y -= THREAD_COUNT) {
					pix(-rad * stepSize + cache.centerX, y * stepSize + cache.centerY);
				}
				
				offset = tb - y;
			}
			
			return offset;
		}
		
		private void paintFull() throws CancelException, InterruptedException, BrokenBarrierException {
			
			int offset = index;
			
			for(int stepSize = INIT_STEP_SIZE; stepSize > 0; stepSize /= STEP_SIZE_DIVISOR) {
				Log.d(TAG, "Thread " + index + ", Step = " + stepSize + ": Calculating...");

				paint.setStrokeWidth(stepSize);

				if(index == THREAD_COUNT - 1) {
					// We need to repaint the whole image for each stepsize
					pix(cache.centerX, cache.centerY);
				}
				
				int x0 = -cache.centerX; // incl
				int y0 = -cache.centerY; // incl
				int x1 = cache.width - cache.centerX + stepSize - 1; // excl
				int y1 = cache.height - cache.centerY + stepSize - 1; // excl
				
				x0 /= stepSize;
				x1 /= stepSize;
				y0 /= stepSize;
				y1 /= stepSize;
				
				int maxR = Math.max(Math.max(-x0 + 1, x1), Math.max(-y0 + 1, y1));
		
				// We update all pixels because some values might depend on statistical values
				for(int rad = 1; rad <= maxR; rad ++) {
					offset = paintCirc(rad, x0, y0, x1, y1, stepSize, offset);
				}
				
				// Lock until all threads have passed here, then update statistics
				Log.d(TAG, "Thread " + index + ", Step = " + stepSize + ": Waiting at barrier...");
				nextStepSizeBarrier.await();
			}
			
			Log.d(TAG, "Thread " + index + ", Count = " + pointCount + ", " + "Offset = " + offset);
		}
		
		public void run() {
			// Use lower thread priority to have less impact on our runtime
			Process.setThreadPriority(THREAD_PRIORITY);

			try {
				paintFull();
			} catch(CancelException e) {
				// Someone told us to stop
				Log.d(TAG, "Cancelled " + index);
			} catch(InterruptedException e) {
				// Someone told us to wake up. No problem, we are done anyways...
				Log.d(TAG, "Thread " + index + " was interrupted");
			} catch(BrokenBarrierException e) {
				Log.d(TAG, "Barrier is broken for Thread " + index);
			} finally {
				if(index == 0) {
					running = false;
					Log.d(TAG, "Runtime of task was " + (System.currentTimeMillis() - startTime) + " ms");
				}
			}
			
			Log.d(TAG, "Thread " + index + " is done.");
		}
	}
	
	public static interface Environment {
		/** Calculates the point
		 * 
		 * @param x
		 * @param y
		 * @return
		 */
		int color(int x, int y);
	}
	
	public static interface Rasterable {
		Environment createEnvironment();
		
		void initStatistics();
		void updateStatisticsFromEnv(Environment env);
		
		/** If it uses statistics, we should not skip the first pixel
		 * @return
		 */
		boolean usesStats();
	}
	
	/** This exception is thrown if during calculation cancel is called.
	 *
	 */
	private class CancelException extends Exception {
		private static final long serialVersionUID = 6296523206940181359L;
	}
}
