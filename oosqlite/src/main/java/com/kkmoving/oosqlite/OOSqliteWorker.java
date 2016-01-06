package com.kkmoving.oosqlite;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

class OOSqliteWorker {
	
	private static final int MSG_RUN = 1;

	private HandlerThread sWorkerThread = new HandlerThread("sqlite_worker") {
		
		protected void onLooperPrepared() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			
			sWorkerHandler = new Handler(Looper.myLooper()) {
				
				public void handleMessage(Message msg) {
					switch (msg.what) {
						case MSG_RUN:
							Object o = msg.obj;
							if (o != null && o instanceof Runnable) {
								((Runnable) o).run();
							}
							break;

						default:
							break;
					}
				};
			};
		};
	};
	private Handler sWorkerHandler;
	
	public OOSqliteWorker() {
		sWorkerThread.start();
	}
	
	void run(final Runnable runnable) {
		Message msg = sWorkerHandler.obtainMessage();
		msg.what = MSG_RUN;
		msg.obj = runnable;
		
		msg.sendToTarget();
	}
	
}
