/*
 * Copyright (C) 2014 Accelerated I/O, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acceleratedio.pac_n_zoom;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationSet; 
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation; 
import android.view.animation.TranslateAnimation; 
import android.view.animation.Animation.AnimationListener; 
import android.view.animation.RotateAnimation; 
import android.view.Display;
import android.view.View;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.acceleratedio.pac_n_zoom.DrawSVG;
import com.acceleratedio.pac_n_zoom.PickAnmActivity;
import com.acceleratedio.pac_n_zoom.R;
import com.acceleratedio.pac_n_zoom.SaveAnmActivity;
import com.acceleratedio.pac_n_zoom.SelectImageActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Runnable;
import java.lang.Thread;
import java.util.ArrayList;

/**
 * Created by clive on 03-Jun-14.
 * www.101apps.co.za
 * Modified by Accelerated I/O, Inc.	
 */
public class AnimActivity extends Activity {
  public static LoadSVG.SVGData svg_data;
	ArrayList<ImageView> anmViews;
	public static Bitmap thumbnail;
	public static Bitmap orgnlImageBmp;
	public static String orgnlImageWdth;
	public static String orgnlImageHght;
	private int frm_mbr;
	private Thread anmThread;
	ImageView anim_view = null;
	float[] initScl;
  private PathMeasure pth_len; // Path length
  private float stp_len;  // Length of animation step
	final Path path = new Path();
  private static Bitmap bmBackground;
	AnimationDrawable animatn = new AnimationDrawable();
	public static ProgressDialog progress;
	private Context crt_ctx;
	private DrawSVG draw_view = null;
	Handler	DrwHandler;
	ImageView orgnlImageView;
	public static String animFileName;
	private static int orgnl_iv_wdth;			
	private static int orgnl_iv_hght;			
	private static int bgn_hrz;			
	private static int bgn_vrt;			
	private static int bgn_top;			
	private static int bgn_lft;
	private static int msr_wdth;
	private static int msr_hght;
	private static RelativeLayout.LayoutParams orlp = null;
	private ScaleGestureDetector scaleGestureDetector;
	private static boolean flgInScale = false;
	public static float scaleFactor = 1;
	private Button sav_anm_btn;	// The button that saves the animation

	//animation step
	private static int iMaxAnimationStep = 120;
	private int iCurStep = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anm);
		orgnlImageView = (ImageView) findViewById(R.id.imageView);
		orgnlImageView.setMaxHeight(800);
		orgnlImageView.setMaxWidth(600);
		crt_ctx = this;

		BitmapFactory.Options bmp_opt = new BitmapFactory.Options();
		bmp_opt.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
		Bitmap bmp = SelectImageActivity.dcodRszdBmpFil(SelectImageActivity.orgFil, bmp_opt); 

		// Now we need to set the GUI ImageView data with the orginal file selection.
		orgnlImageView.setImageBitmap(bmp);
		orgnl_iv_wdth = bmp.getWidth();
		orgnl_iv_hght = bmp.getHeight();

		final RelativeLayout rel_anm_lo = (RelativeLayout) findViewById(R.id.activity_anm_lo);
		scaleGestureDetector = new ScaleGestureDetector(this, new simpleOnScaleGestureListener());

		orgnlImageView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if (event.getPointerCount() > 1 || flgInScale) {

        	scaleGestureDetector.onTouchEvent(event);
					return true;
				}

				int end_hrz;
				int end_vrt;

				switch (event.getAction()) {
				
				case MotionEvent.ACTION_DOWN:

					bgn_hrz = (int) event.getX();
					bgn_vrt = (int) event.getY();

					String log_str = "Beginning coordinates: Horz = " +
						String.valueOf(bgn_hrz) + "; Vert = " + String.valueOf(bgn_vrt);

					Log.d("OnTouchListener", log_str);
					orlp = (RelativeLayout.LayoutParams) orgnlImageView.getLayoutParams();
					bgn_top = (int) orlp.topMargin;
					bgn_lft = (int) orlp.leftMargin;
					break;

				case MotionEvent.ACTION_MOVE:
				
					orlp.topMargin = bgn_top + (int) event.getY() - bgn_vrt; 
					orlp.bottomMargin = -450;
					orlp.leftMargin = bgn_lft + (int) event.getX() - bgn_hrz;
					orlp.rightMargin = -450;
					orgnlImageView.setLayoutParams(orlp);
					break;

				case MotionEvent.ACTION_UP:

					end_hrz = (int) event.getX();
					end_vrt = (int) event.getY();

					log_str = "Amount moved: Horz = " +
						String.valueOf(end_hrz - bgn_hrz) + "; Vert = " + String.valueOf(end_vrt - bgn_vrt);

					Log.d("OnTouchListener", log_str);
				}

				rel_anm_lo.invalidate();
				return true;
			}
		});

		sav_anm_btn = (Button) findViewById(R.id.sav_btn);

		sav_anm_btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View vw) {

				RelativeLayout rel_anm_lo = (RelativeLayout) findViewById(R.id.activity_anm_lo);
				rel_anm_lo.removeView(vw); 
				thumbnail = getViewBitmap(rel_anm_lo);
				int vw_nmbr = anmViews.size();

				for (int vw_mbr = 1; vw_mbr < vw_nmbr; vw_mbr += 1) {
					
					anim_view = anmViews.get(vw_mbr);
					anim_view.getAnimation().cancel();
					anim_view.setAnimation(null);
					rel_anm_lo.removeView(anim_view); 
				}

				orgnlImageBmp = getViewBitmap(rel_anm_lo);
				orgnlImageWdth = Integer.toString(orgnlImageBmp.getWidth());
      	orgnlImageHght = Integer.toString(orgnlImageBmp.getHeight());

				Intent intent =
					new Intent(AnimActivity.this, com.acceleratedio.pac_n_zoom.SaveAnmActivity.class);

				startActivity(intent);
			}
		});

		progress = ProgressDialog.show(crt_ctx, "Loading the animation", "dialog message", true);
		GetRequest get_svg_img = new GetRequest();
	  get_svg_img.execute("");	
	}

	public Bitmap getViewBitmap(View view) {

		int bmp_wdth = view.getWidth();
		int bmp_hght = view.getHeight();
		Bitmap bmpCaptured = Bitmap.createBitmap(bmp_wdth, bmp_hght, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bmpCaptured);
    view.draw(canvas);
		return bmpCaptured;
	}

	public class simpleOnScaleGestureListener extends SimpleOnScaleGestureListener {
	
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			Log.d("ScaleListener", "onScale");
			scaleFactor *= detector.getScaleFactor();
			scaleFactor = (scaleFactor < 1 ? 1 : scaleFactor); // prevent our view from becoming too small //
			
			// Change precision to help with jitter when user just rests their fingers //
			scaleFactor = ((float)((int)(scaleFactor * 100))) / 100; 
			
			orgnlImageView.setScaleX(scaleFactor);
			orgnlImageView.setScaleY(scaleFactor);
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			Log.d("MytextView", "onScaleBegin");
			flgInScale = true;
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			Log.d("MytextView", "onScaleEnd");
			flgInScale = false;        
		}
	}
 
	public class GetRequest extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {
			String result = "fail";
			int position = getIntent().getIntExtra("position", -1);
			
			String httpAddrs = "https://meme.svgvortec.com/Droid/db_rd.php?";
			animFileName = PickAnmActivity.fil_nams[position].replace('/', '?') + ".svg";
			httpAddrs += animFileName; 
			animFileName = animFileName.substring(animFileName.indexOf('?') + 1);
			BufferedReader inStream = null;

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(httpAddrs);
				HttpResponse response = httpClient.execute(httpRequest);

				inStream = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent()));

				StringBuffer buffer = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");

				while ((line = inStream.readLine()) != null) {
					buffer.append(line + NL);
				}

				inStream.close();
				result = buffer.toString();			
				progress.dismiss();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				progress.dismiss();
			} finally {
				progress.dismiss();
				if (inStream != null) {
					try {
						inStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return result;
		}

		protected void onPostExecute(String response) {
			super.onPostExecute(response);
     	anim_svg(response);
		}
	}

	private void anim_svg(String svg_fil) {

		Toast.makeText(this, "Parsing and Drawing Animation", Toast.LENGTH_SHORT).show();	
		LoadSVG loadSVG = new LoadSVG();
		svg_data = loadSVG.LoadSVG(svg_fil);
		Log.d("anim_svg", "Return from LoadSVG");
		DrawSVG drw_svg = new DrawSVG();
		anmViews = drw_svg.DrawSVG(crt_ctx, orgnlImageView);
		initScl = svg_data.svg.initScl;

		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		
		Log.d("dsply_svg", "rlp is set");

		// - Loop through the views
		int vw_nmbr = anmViews.size();
		RelativeLayout rel_anm_lo = (RelativeLayout) findViewById(R.id.activity_anm_lo);

		for (int vw_mbr = 1; vw_mbr < vw_nmbr; vw_mbr += 1) {
			
			anim_view = anmViews.get(vw_mbr);
			anim_view.setLayoutParams(rlp);
			anim_view.setBackgroundColor(Color.TRANSPARENT);
			rel_anm_lo.addView(anim_view);
		}

		setContentView(rel_anm_lo);
		frm_mbr = -1;
		mainAnmLoop();
	}

  public static void ld_scl_pth_pnts(ArrayList<Integer[]> pnts, Path path) {

		Integer[] crt_pnt = pnts.get(0);
		int pnt_nmbr = pnts.size();
		int min_x = crt_pnt[0];
		int max_x = crt_pnt[0];
		int min_y = crt_pnt[1];
		int max_y = crt_pnt[1];

		// Loop through the points of a path
		for (int pnt_mbr = 1; pnt_mbr < pnt_nmbr; pnt_mbr += 1) {

			crt_pnt = pnts.get(pnt_mbr);
			if (crt_pnt[0] < min_x) min_x = crt_pnt[0];
			if (crt_pnt[0] > max_x) max_x = crt_pnt[0];
			if (crt_pnt[1] < min_y) min_y = crt_pnt[1];
			if (crt_pnt[1] > max_y) max_y = crt_pnt[1];
		}

		float x_dif = (float) max_x / orgnl_iv_wdth;
		float y_dif = (float) max_y / orgnl_iv_hght;
		float crt_scl;
		float ivWdth;
		float ivHght;

		if (orgnl_iv_wdth > 480) ivWdth = 480;
		else ivWdth = orgnl_iv_wdth;

		if (orgnl_iv_hght > 680) ivHght = 680;
		else ivHght = orgnl_iv_hght; 

		if (x_dif >= y_dif) 
			crt_scl = (float) ivWdth / svg_data.svg.width * svg_data.g_scl;
		else
			crt_scl = (float) ivHght / svg_data.svg.height * svg_data.g_scl;

		crt_pnt = pnts.get(0);
		float x_scl = crt_pnt[0] * crt_scl;
		float y_scl = crt_pnt[1] * crt_scl;
		path.moveTo(x_scl, y_scl);

		for (int pnt_mbr = 1; pnt_mbr < pnt_nmbr; pnt_mbr += 1) {

			crt_pnt = pnts.get(pnt_mbr);
			x_scl = crt_pnt[0] * crt_scl;
			y_scl = crt_pnt[1] * crt_scl;
			path.lineTo(x_scl, y_scl);
		}
  }

	private void mainAnmLoop() {

		// --- Loop through the frames
		int frm_nmbr = svg_data.frm.size();

		if (++frm_mbr >= frm_nmbr) frm_mbr = 0;

		// -- You need to turn the sprites on and off for the current frame
		LoadSVG.frame crt_frm = svg_data.frm.get(frm_mbr);
		String crt_frm_ordr = crt_frm.frm_ordr;
		ArrayList<String> sprt_ordr = svg_data.svg.ordr;
		int crt_dur = crt_frm.end - crt_frm.bgn;

		// - Loop through the sprites 
		int sprt_nmbr = sprt_ordr.size();
		int frm_sprt_mbr = 0;

		for (int sprt_mbr = 0; sprt_mbr < sprt_nmbr; sprt_mbr += 1) {

			String sprt_id = sprt_ordr.get(sprt_mbr);  
			anim_view = anmViews.get(sprt_mbr);
			
			if (crt_frm_ordr.indexOf(sprt_id) >= 0) {

				anim_view.setAlpha(1f);
				int xfm_idx = crt_frm.xfm_idx[frm_sprt_mbr++];
				
				if (xfm_idx >= 0) { // An animation tag is present

					AnimationSet anmSet = new AnimationSet(false); 
					LoadSVG.xfrm crt_xfm = svg_data.xfm.get(xfm_idx);
					ArrayList<Integer[]> pnts = crt_xfm.mov_path;
					int init_scl = (int) (initScl[sprt_mbr] * 100);
					
					if (pnts.size() > 0) {

						final Path path = new Path();
						ld_scl_pth_pnts(pnts, path);
						PathAnimation pthAnm = new PathAnimation(path);
						pthAnm.setDuration(crt_dur);
						pthAnm.setInterpolator(new LinearInterpolator());
						pthAnm.setFillAfter(true); // Needed to keep the result of the animation
						anmSet.addAnimation(pthAnm); 
					}

					if (crt_xfm.scl_bgn != init_scl) {

						float crt_scl = crt_xfm.scl_bgn / init_scl;
						float end_scl = crt_scl; 

						if (crt_xfm.scl_end != crt_xfm.scl_bgn) end_scl = crt_xfm.scl_end / init_scl;

						ScaleAnimation sclAnm = new ScaleAnimation(crt_scl, end_scl, crt_scl, end_scl, 
							Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

						sclAnm.setDuration(crt_dur);
						anmSet.addAnimation(sclAnm); 
					}

					if (crt_xfm.rot_bgn != 0) {
					
						float crt_rot = crt_xfm.rot_bgn;
						float end_rot = crt_rot; 

						if (crt_xfm.rot_end != crt_xfm.rot_bgn) end_rot = crt_xfm.rot_end;

						RotateAnimation rotAnm = new RotateAnimation(crt_rot, end_rot, 
							Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 

						rotAnm.setDuration(crt_dur);
						anmSet.addAnimation(rotAnm);
					}

					anim_view.startAnimation(anmSet); //start animation
				}
			} else anim_view.setAlpha(0f);
		}
		
		waitDur(crt_dur);
	}

	void waitDur(int dur) {

    class DurSleep implements Runnable {
			int dur;
			Thread thread;

			DurSleep(int crt_dur, Thread thread) { dur = crt_dur; thread = anmThread; }

			@Override
			public void run() {
				try	{
					thread.sleep(dur);

					runOnUiThread(new Runnable() // start actions in UI thread
					{
						@Override
						public void run()
						{
							mainAnmLoop(); // this action has to be in UI thread
						}
					});
				}
				catch (InterruptedException e) {
					Log.d("DurSleep", "Sleep Problem");				
				}
			}
    }
    anmThread = new Thread(new DurSleep(dur, anmThread));
    anmThread.start();
	}
}
