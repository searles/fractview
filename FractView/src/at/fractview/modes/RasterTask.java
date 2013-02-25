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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Process;
import android.util.Log;

public class RasterTask implements AbstractImgCache.Task {
	
	private static final String TAG = "RasterTask"; 
	
	private static final int THREAD_COUNT = 4;
	
	// Thread priority (low priority keeps device responsive.)
	private static final int THREAD_PRIORITY = Process.THREAD_PRIORITY_BACKGROUND;
	
	private static final int TILE_SIZE = 4;
	
	private Rasterable rasterable;
	
	private LinkedList<TileIterator> tileIterators;

	private volatile boolean cancelled;
	private volatile boolean running;
	
	private long startTime;

	private volatile int checkPointCount = 0;
	private Lock lock;
	private Condition wc;
	
	private Thread[] threads;
	
	public RasterTask(Rasterable rasterable) {
		this.rasterable = rasterable;
		this.threads = new Thread[THREAD_COUNT];
		this.lock = new ReentrantLock();
		this.wc = this.lock.newCondition();
	}

	public void start(AbstractImgCache cache) {
		Log.d(TAG, "starting task...");
		
		startTime = System.currentTimeMillis();
		
		// initialize iterator to get tiles
		tileIterators = new LinkedList<TileIterator>();
				
		// Width, height
		int w = cache.width();
		int h = cache.height();

		// Start pixel: This must not be out of bounds of image!
		int x0 = w / 2;
		int y0 = h / 2;

		int stepSize = 1;
		boolean skipFirstPix;

		// The start tile is the biggest one
		// Therefore we start with the smallest tile
		// (stepSize = 1), and factor up until
		// we found it.
		do {
			// Get first step size
			skipFirstPix = stepSize * TILE_SIZE < Math.min(w, h);
			tileIterators.addFirst(new TileIterator(x0, y0, w, h, stepSize, skipFirstPix));
			stepSize *= TILE_SIZE;
		} while(skipFirstPix);

		// Create threads
		running = true;
		
		for(int i = 0; i < THREAD_COUNT; i++) {
			// Create new worker and run it [recycling old workers is not a 
			// good idea because they might be still running]
			
			threads[i] = new Thread(new Worker(cache));
			
			// Set priority (low priority keeps device responsive.)
			threads[i].start();
		}
		
		Log.d(TAG, "background-threads have been started");
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void cancel() {
		Log.d(TAG, "cancel " + hashCode());
		this.cancelled = true;
		for(int i = 0; i < THREAD_COUNT; i++) {
			threads[i].interrupt(); // maybe we are in a waiting position...
		}
	}

	@Override
	public void join() throws InterruptedException {
		Log.d(TAG, "Joining calculating threads - "  + hashCode());
		for(int i = 0; i < THREAD_COUNT; i++) {
			threads[i].join();
		}
	}
	
	private class Tile {
		
		int x0;
		int y0; 
		int stepSize;
		boolean skipFirstPix;
		
		void calc(Environment env, Canvas canvas, Paint paint) throws CancelException {
			// TODO: The distribution of labor is not perfect here... canvas is inside cache, paint can be inside Environment.
			int w = canvas.getWidth();
			int h = canvas.getHeight();
			
			int x1 = Math.min(w, x0 + stepSize * TILE_SIZE);
			int y1 = Math.min(h, y0 + stepSize * TILE_SIZE);
			
			paint.setStrokeWidth(stepSize);
			
			for(int y = y0; y < y1; y += stepSize) {
				for(int x = x0; x < x1; x += stepSize) {
					if(isCancelled()) {
						throw new CancelException();
					}

					if(!skipFirstPix) {
						// Calculate pixel
						int color = env.color(x, y);

						// and paint it
						paint.setColor(color);
						canvas.drawPoint(x, y, paint);
					} else {
						// first pixel was just skipped
						skipFirstPix = false;
					}
				}
			}
		}
	}
	
	private class TileIterator {
		// The following fields represent the tile that is currently iterated
		final int x0;
		final int y0;
		final int w;
		final int h;
		
		final int stepSize;
		
		// The following fields help with the maths.
		// They are used to iterate the tiles
		int rad; // Current "radius" (square style)
		int lineIndex;
		int x;
		int y;
		boolean hasNext;
		boolean skipFirstPix;
		
		TileIterator(int x0, int y0, int w, int h, int stepSize, boolean skipFirstPix) {
			this.x0 = x0 / (stepSize * TILE_SIZE);
			this.y0 = y0 / (stepSize * TILE_SIZE);
			this.w = (w + (stepSize * TILE_SIZE) - 1) / (stepSize * TILE_SIZE);
			this.h = (h + (stepSize * TILE_SIZE) - 1) / (stepSize * TILE_SIZE);
			
			this.stepSize = stepSize;
			this.skipFirstPix = skipFirstPix;
			
			// And now we start.
			
			this.rad = 0;
			this.hasNext = true;
		}
		
		private boolean nextRad() {
			rad++;
			
			x = x0 - rad;
			y = y0 - rad;
			
			lineIndex = 0;
			
			if(x < 0) {
				if(y < 0) {
					// Find a start
					lineIndex++;
					x = x0 + rad;
					y = 0;
					
					if(x >= w) {
						lineIndex++;
						y = y0 + rad;
						x = w - 1;
						if(y >= h) {
							// We are done.
							return false;
						} else {
							return true;
						}
					} else {
						return true;
					}
				} else {
					x = 0;
					return true;
				}
			} else if(y < 0) {
				lineIndex ++;
				x = x0 + rad;
				y = 0;
				
				if(x >= w) {
					lineIndex++;
					y = y0 + rad;
					x = w - 1;
					
					if(y >= h) {
						lineIndex ++;
						
						x = x0 - rad; // this must be 0 here
						y = h - 1;
						
						return true;
					} else {
						return true;
					}
				} else {
					return true;
				}
			} else {
				return true;
			}
		}
		
		private boolean nextPix() {
			if(lineIndex == 0) {
				x++;
				
				if(x >= w) {
					x--;
					lineIndex += 2;
					y = y0 + rad;
					
					if(y >= h) {
						y = h - 1;
						x = x0 - rad;
						lineIndex = 3;
						
						if(x < 0) {
							return nextRad();
						}
					}
				} else if(x == x0 + rad) {
					lineIndex++;
				}
			} else if(lineIndex == 1) {
				y++;

				if(y >= h) {
					lineIndex += 2;
					y--;
					x = x0 - rad;
					
					if(x < 0) {
						return nextRad();
					}
				} else if(y == y0 + rad) {
					lineIndex++;
				}
			} else if(lineIndex == 2) {
				x--;
				
				if(x < 0) {
					return nextRad();
				} else if(x == x0 - rad) {
					lineIndex++;
				}
			} else {
				y--;
				
				if(y < 0 || y == y0 - rad) {
					return nextRad();
				}
			}
						
			return true;
		}
		
		synchronized Tile next(Tile t) {
			int tileX;
			int tileY;
			
			if(this.hasNext) {
				if(rad == 0) {
					// When we start, there is no next pixel.
					tileX = x0;
					tileY = y0;
						
					this.hasNext = nextRad();
				} else {
					tileX = x;
					tileY = y;
					this.hasNext = nextPix();
				}
				
				// Paint
				t.x0 = tileX * stepSize * TILE_SIZE;
				t.y0 = tileY * stepSize * TILE_SIZE;
				t.stepSize = stepSize;
				t.skipFirstPix = skipFirstPix;
				
				return t;
			} else {
				return null;
			}
		}
	}

	/**
	 * This class contains the working thread(s) that generates the fractal
	 *
	 */
	private class Worker implements Runnable {		
		private static final String TAG = "RasterTask.Worker";
		
		private AbstractImgCache cache;
		
		Worker(AbstractImgCache cache) {
			Log.d(TAG, "Worker " + hashCode() + " created");
			this.cache = cache;
		}
		
		public void run() {
			// Use lower thread priority to have less impact on our runtime
			Process.setThreadPriority(THREAD_PRIORITY);
			
			Log.d(TAG, "Worker " + hashCode() + " started");
			
			Paint paint = new Paint(); // Create one paint per worker.
			
			// And create new environment per worker
			Environment env = rasterable.createEnvironment();
			
			// We create one tile and reuse it.
			Tile t = new Tile();
			
			try {
				// TODO: There is a kind of race-condition: Sometimes a worker is stuck in wc.await so that formally
				// the calculation never stops...
				Iterator<TileIterator> iter = tileIterators.iterator();
				
				// iter has definitely at least one element
				while(true) {
					TileIterator ti = iter.next();
					
					while(ti.next(t) != null) {
						t.calc(env, cache.canvas(), paint);
					}
					
					if(iter.hasNext()) {
						// Wait until all threads are done with this tileIterator
						try {
							lock.lock();
							int index = checkPointCount ++;
							
							if(checkPointCount == THREAD_COUNT) {
								// This is a good point to update statistical data or similar things in Cache
								rasterable.updateDataFromEnv(env);
								
								// All threads have passed the checkpoint
								Log.d(TAG, t.stepSize + ": Waking up others");
								
								checkPointCount = 0; // Reset checkPointCount
								wc.signalAll();
							} else {
								Log.d(TAG, t.stepSize + ": waiting " + index);
								wc.await();
								Log.d(TAG, t.stepSize + ": continuing " + index);
							}
						} finally {
							lock.unlock();
						}
					} else {
						// if iter is done, finish this loop.
						break;
					}
				}
			} catch(CancelException e) {
				Log.d(TAG, "Worker was cancelled");
			} catch(InterruptedException e) {
				Log.d(TAG, "Worker was interrupted");
			} finally {
				lock.lock();
				checkPointCount++;
				int index = checkPointCount;
				lock.unlock();

				Log.d(TAG, "Worker " + index + " is done");
			
				if(index == THREAD_COUNT) {
					// Now we are done.
					running = false;
					Log.d(TAG, "Runtime of task was " + (System.currentTimeMillis() - startTime) + " ms");
				}
			}
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
		void updateDataFromEnv(Environment env);
	}
	
	/** This exception is thrown if during calculation cancel is called.
	 *
	 */
	private class CancelException extends Exception {
		private static final long serialVersionUID = 6296523206940181359L;
	}

}
