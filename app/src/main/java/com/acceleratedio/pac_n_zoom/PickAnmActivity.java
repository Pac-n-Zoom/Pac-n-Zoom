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
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;

import java.lang.String;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class PickAnmActivity extends Activity {

 	private String LOG_TAG = "PickAnmActivity";
	private String url = "https://meme.svgvortec.com/Droid/snd_req_tns.php";
	public static String req_str;
	EditText searchEditText;
	public static ProgressDialog progress;
	String[] top_dim;
	public static String[] fil_nams;
	public static String orgnl_tags;
	DisplayImageOptions options;
	private ImageLoader imageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_anm);
		progress = ProgressDialog.show(this, "Loading the matching animations", "dialog message", true);
		EventBus.getDefault().register(this);
		req_str = getIntent().getExtras().getString("requestString").trim();
		MakePostRequest get_tags = new MakePostRequest();
	  get_tags.execute(req_str);	
		searchEditText = (EditText) findViewById(R.id.ed_tags);

		searchEditText.setOnClickListener(new View.OnClickListener() {

			public void onClick(View vw) {

				Intent intent = 
					new Intent(PickAnmActivity.this, com.acceleratedio.pac_n_zoom.FindTagsActivity.class);

				intent.putExtra("requestString", req_str);
				intent.putExtra("tagString", top_dim[1]);
				startActivity(intent);
			}
		});
	}	

	private void dsply_thumbnails(String tags) {
  	// tags is a string that holds a 3 dimension array with a different delimiter
		// for each dimension. The most significant dimension is delimited with '|'.
		// The second dimension is delimited with a comma. The least dimension 
		// significant is '/' for [0][] and ' ' for [1][].
		top_dim = tags.split("\\|");
		fil_nams = top_dim[0].split("\\,");

		if (req_str.equals("")) orgnl_tags = top_dim[1];	// Used when saving animation
  
		imageLoader = ImageLoader.getInstance();
   
		// set options for image display
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.place_holder)
			.showImageForEmptyUri(R.drawable.hand)
			.showImageOnFail(R.drawable.big_problem)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

    final GridView gridview = (GridView) findViewById(R.id.tnview);
		gridview.setAdapter(new ImageAdapter());

		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				gridview.getAdapter().getItem(position);
				Intent intent = new Intent(PickAnmActivity.this, com.acceleratedio.pac_n_zoom.AnimActivity.class);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});

		searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				boolean handled = false;
				req_str = searchEditText.getText().toString();

				if (actionId == EditorInfo.IME_ACTION_SEND) {

					MakePostRequest get_tags = new MakePostRequest();
					get_tags.execute(req_str);
					handled = true;
				}

				return handled;
			}
		});
 	}

	public class MakePostRequest extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {
      String response = "";
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("https://meme.svgvortec.com/Droid/snd_req_tns.php");
			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
			nameValuePair.add(new BasicNameValuePair("username", "VectorVortec@gmail.com"));
			nameValuePair.add(new BasicNameValuePair("password", "ListenToMe"));
			nameValuePair.add(new BasicNameValuePair("tags", req_str));

			// Encoding data
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
			} catch (UnsupportedEncodingException e) {
				// log exception
				e.printStackTrace();
			}

			// making request
			try {
				HttpResponse httpResponse = httpClient.execute(httpPost);
				response = EntityUtils.toString(httpResponse.getEntity());
				// write response to log
				Log.d("Http Post Response:", response.toString());
				progress.dismiss();
				EventBus.getDefault().post(new AlphaListUpdateEvent());
			} catch (ClientProtocolException e) {
				// Log exception
				e.printStackTrace();
				progress.dismiss();
			} catch (IOException e) {
				// Log exception
				e.printStackTrace();
				progress.dismiss();
			}
			return response;
		}

		protected void onPostExecute(String response) {
			super.onPostExecute(response);
     	dsply_thumbnails(response);
		}
	}
	
	public void onEvent(AlphaListUpdateEvent event){ 
		Log.d("event", "list has been updated");
	}

	public class AlphaListUpdateEvent{ 
	}

	static class ViewHolder {
		ImageView imageView;
	}

	// our custom adapter
	private class ImageAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return fil_nams.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder gridViewImageHolder;

			// check to see if we have a view
			if (convertView == null) { // no view - so create a new one
				
				view = getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
				gridViewImageHolder = new ViewHolder();
				gridViewImageHolder.imageView = (ImageView) view.findViewById(R.id.image);
				gridViewImageHolder.imageView.setMaxHeight(80);
				gridViewImageHolder.imageView.setMaxWidth(80);
				view.setTag(gridViewImageHolder);
			} else { // we've got a view
				gridViewImageHolder = (ViewHolder) view.getTag();
			}

			gridViewImageHolder.imageView.setAdjustViewBounds(true);

			imageLoader.displayImage("https://meme.svgvortec.com/Droid/db_rd.php?"
				+ fil_nams[position].replace('/', '?') + ".jpg"
				,gridViewImageHolder.imageView
				,options);

			return view;
		}
	}
}
