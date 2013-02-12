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

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Process;
import android.util.Log;

public class RasterTask implements Preferences.Task {
	
	private static final String TAG = "RasterTask"; 
	
	private static final int THREAD_COUNT = 4;
	
	// Thread priority (low priority keeps device responsive.)
	private static final int THREAD_PRIORITY = Process.THREAD_PRIORITY_BACKGROUND;
	
	private static final int TILE_SIZE = 4;
	
	private Rasterable preferences;
	
	private LinkedList<TileIterator> tileIterators;
	private volatile boolean cancelled;
	
	private AtomicInteger runningThreadCount;
	
	private long startTime;

	private int checkPointCount = 0;
	private Lock lock;
	private Condition wc;
	
	public RasterTask(Rasterable preferences) {
		this.preferences = preferences;
		this.runningThreadCount = new AtomicInteger(THREAD_COUNT);
		
		this.lock = new ReentrantLock();
		this.wc = this.lock.newCondition();
	}

	public void start(Bitmap bitmap) {
		Log.v(TAG, "starting task...");
		
		startTime = System.currentTimeMillis();
		
		Canvas canvas = new Canvas(bitmap);

		// initialize iterator to get tiles
		tileIterators = new LinkedList<TileIterator>();
				
		// Width, height
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

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
		for(int i = 0; i < runningThreadCount.get(); i++) {
			// Create new worker and run it [recycling old workers is not a 
			// good idea because they might be still running]
			
			// TODO: Consider using an executor service here...
			Thread thread = new Thread(new Worker(canvas));
			
			// Set priority (low priority keeps device responsive.)
			thread.start();
		}
		
		Log.v(TAG, "background-threads have been started");
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public boolean isRunning() {
		return runningThreadCount.get() > 0;
	}
	
	public void cancel() {
		this.cancelled = true;
	}
	
	private class Tile {
		
		int x0;
		int y0; 
		int stepSize;
		boolean skipFirstPix;
		
		void calc(Environment env, Canvas canvas, Paint paint) throws CancelException, InterruptedException {
			int w = canvas.getWidth();
			int h = canvas.getHeight();
			
			int x1 = Math.min(w, x0 + stepSize * TILE_SIZE);
			int y1 = Math.min(h, y0 + stepSize * TILE_SIZE);
			
			paint.setStrokeWidth(stepSize);
			
			for(int y = y0; y < y1; y += stepSize) {
				for(int x = x0; x < x1; x += stepSize) {
					if(isCancelled()) {
						throw new CancelException();
					} else if(Thread.currentThread().isInterrupted()) {
						throw new InterruptedException();
					}

					if(!skipFirstPix) {
						// Calculate pixel
						int color = env.color(x, y, w, h);

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
		
		Canvas canvas;
		
		Worker(Canvas canvas) {
			this.canvas = canvas;
		}
		
		public void run() {
			// Use lower thread priority to have less impact on our runtime
			Process.setThreadPriority(THREAD_PRIORITY);
			
			Paint paint = new Paint();
			Environment env = preferences.createEnvironment();
			
			// We create one tile and reuse it.
			Tile t = new Tile();
			
			Log.v(TAG, "Worker " + this + " started");
			try {
				for(TileIterator ti : tileIterators) {
					while(ti.next(t) != null) {
						t.calc(env, canvas, paint);
					}
				
					// Wait until all threads are done with this tileIterator
					// TODO: Can this be done in a nicer way?
					try {
						lock.lock();
						checkPointCount ++;
						
						if(checkPointCount == THREAD_COUNT) {
							// All threads have passed the checkpoint
							wc.signalAll();
						} else {
							wc.await();
						}
						
						checkPointCount --;
					} finally {
						lock.unlock();
					}
				}
			} catch(CancelException e) {
				Log.v(TAG, "Worker was cancelled");
			} catch(InterruptedException e) {
				Log.v(TAG, "Worker was interrupted");
			} finally {
				int index = runningThreadCount.decrementAndGet();
				Log.v(TAG, "Worker " + this + "(" + index + ") is done");
			
				if(index == 0) {
					Log.v(TAG, "Runtime of task was " + (System.currentTimeMillis() - startTime) + " ms");
					Log.v(TAG, "This is the last running worker");
				}
			}
		}
	}
	
	public static interface Environment {
		/** Returns the color of the pixel at the given coordinates (as screen coordinates).
		 * Call ScaleablePrefs.map to map the point into the current coordinates.
		 * @param x Coordinates of current pixel
		 * @param y Coordinates of current pixel
		 * @return
		 */
		int color(float x, float y, int w, int h);
	}
	
	public static interface Rasterable {
		Environment createEnvironment();
	}


	/** This exception is thrown if during calculation cancel is called.
	 *
	 */
	private class CancelException extends Exception {
		private static final long serialVersionUID = 6296523206940181359L;
	}	
}
