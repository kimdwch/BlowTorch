package com.happygoatstudios.bt.window;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.view.View;
import android.widget.RelativeLayout;

import com.happygoatstudios.bt.service.IConnectionBinder;
import com.happygoatstudios.bt.service.WindowToken;

public class LayerManager {

	List<WindowToken> mWindows = null;
	Context mContext = null;
	IConnectionBinder mService = null;
	
	RelativeLayout mRootLayout = null;
	
	Handler rootHandler = null;
	
	public LayerManager(IConnectionBinder service, Context context, RelativeLayout root,Handler rootHandler) {
		this.rootHandler = rootHandler;
		mContext = context;
		mService = service;
		mRootLayout = root;
	}
	
	public void initiailize() {
		//ask the service for all the current windows for the connection.
		//List<WindowToken> windows =  null;
		//make windows in the order they are given, attach the callback and the view to the layout root.
		mRootLayout.removeAllViews();
		
		try {
			mWindows = mService.getWindowTokens();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(mWindows == null) {
			//Exception e = new Exception("No windows to show.");
			//throw new RuntimeException(e);
			synchronized(this) {
				while(mWindows == null) {
					try {
						this.wait(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						mWindows = mService.getWindowTokens();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else {
			//initialize windows.
			for(Object x : mWindows) {
				WindowToken w = null;
				if(x instanceof WindowToken) {
					w = (WindowToken)x;
				} else {
					//err.
				}
				
				switch(w.getType()) {
				case NORMAL:
					initByteView(w);
					break;
				case SCRIPT:
					initLuaWindow(w);
					
					break;
				default:
					break;
				}
				
				
			}
		}
	}
	
	private void initLuaWindow(WindowToken w) {
		View v = mRootLayout.findViewWithTag(w.getName());
		if(v == null) {
			
			LuaWindow tmp = new LuaWindow(mContext,this,w.getName(),w.getPluginName(),w.getX(),w.getY(),w.getWidth(),w.getHeight(),rootHandler);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			
			tmp.setLayoutParams(p);
			tmp.setTag(w.getName());
			
			try {
				String body = mService.getScript(w.getPluginName(),w.getScriptName());
				//TODO: this needs to be much harderly error checked.
				tmp.loadScript(body);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				mService.registerWindowCallback(w.getName(),tmp.getCallback());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mRootLayout.addView(tmp);
		}
	}

	private void initByteView(WindowToken w) {
		View v = mRootLayout.findViewWithTag(w.getName());
		if(v == null) {
			//need to initialize.
			ByteView tmp = new ByteView(mContext,this);
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
			p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			
			tmp.setTag(w.getName());
			tmp.setLayoutParams(p);
			
			
			tmp.setDisplayDimensions(w.getX(),w.getY(),w.getWidth(),w.getHeight());
			//register callback.
			tmp.setBufferText(w.isBufferText());
			try {
				mService.registerWindowCallback(w.getName(),tmp.getCallback());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tmp.addBytesImpl(w.getBuffer().dumpToBytes(false), true);
			tmp.setTag(w.getName());
			tmp.setName(w.getName());
			mRootLayout.addView(tmp);
		} else {
			//already exists.
		}
	}

	public void cleanup() {
		for(Object x : mWindows) {
			if(x instanceof WindowToken) {
				WindowToken w = (WindowToken)x;
				View tmp = mRootLayout.findViewWithTag(w.getName());
				if(tmp instanceof ByteView) {
					try {
						mService.unregisterWindowCallback(w.getName(),((ByteView)tmp).getCallback());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if(tmp instanceof LuaWindow) {
					try {
						mService.unregisterWindowCallback(w.getName(), ((LuaWindow)tmp).getCallback());
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void callScript(String window, String callback) {
		LuaWindow lview = (LuaWindow)mRootLayout.findViewWithTag(window);
		if(lview != null) {
			lview.callFunction(callback);
		}
	}

	public void shutdown(ByteView byteView) {
		try {
			mService.unregisterWindowCallback(byteView.getName(), byteView.getCallback());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void shutdown(LuaWindow luaWindow) {
		try {
			mService.unregisterWindowCallback(luaWindow.getName(), luaWindow.getCallback());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}