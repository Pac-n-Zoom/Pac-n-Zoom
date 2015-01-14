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
import android.util.Base64;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import de.greenrobot.event.EventBus;

import java.io.ByteArrayOutputStream;
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

import static android.util.Base64.encodeToString;

public class SaveAnmActivity extends Activity implements View.OnClickListener {

 	private String LOG_TAG = "SaveAnmActivity";
	private String srch_str = "";
	private Context crt_ctx;
	public static String[] fil_tags;
	public static ProgressDialog progress;
	EditText tagText;
	String tags_str;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_anm);
		EventBus.getDefault().register(this);
		crt_ctx = this;
		tagText = (EditText) findViewById(R.id.sav_tags);
		
		tagText.addTextChangedListener(new TextWatcher() {	
		
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence key_sqnc, int start, int before, int count) {

				final StringBuilder strBldr = new StringBuilder(key_sqnc.length());
				strBldr.append(key_sqnc);
				srch_str = strBldr.toString();
				dsply_tags();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		tagText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				boolean handled = false;
				tags_str = tagText.getText().toString();

				if (actionId == EditorInfo.IME_ACTION_SEND) {

					progress = ProgressDialog.show(crt_ctx, "Saving the animation", "dialog message", true);
					MakePostRequest savAnimation = new MakePostRequest();
					savAnimation.execute();
					handled = true;
				}

				return handled;
			}
		});

		fil_tags = PickAnmActivity.orgnl_tags.split("(\\s*,\\s*)|(\\s* \\s*)");
		Arrays.sort(fil_tags, String.CASE_INSENSITIVE_ORDER);
		dsply_tags();
	}	
	
	public void onClick(View vw) {

		switch(vw.getId()) {

		case R.id.sav_tags:
			dsply_tags();
			break;

		default:
			
			Button btn_vw = (Button) vw;
			CharSequence btn_sqn = btn_vw.getText();
			final StringBuilder strBldr = new StringBuilder(btn_sqn.length());
			strBldr.append(btn_sqn);
			String tag_str = tagText.getText().toString();
			int chr_idx = tag_str.lastIndexOf(" ");
			tag_str = tag_str.substring(0, chr_idx + 1) + strBldr.toString() + ' ';
			tagText.setText(tag_str, TextView.BufferType.EDITABLE);
			tagText.setSelection(tag_str.length());
			srch_str = "";
			dsply_tags();
		}
	}
	
	public void onEvent(AlphaListUpdateEvent event){ 
		Log.d("event", "list has been updated");
	}

	public class AlphaListUpdateEvent{ 
	}

	private void dsply_tags() {

		TableLayout tbl_tag_lo = (TableLayout) findViewById(R.id.tl_tgs);
		int tag_mbr = 0;
		int tag_nmbr = fil_tags.length;
		String lst_str = "";
		int row_mbr;
		
		tbl_tag_lo.setAlpha(185);

		// - Find the search string
		srch_str = srch_str.trim();
		boolean flg_srch_tags = !srch_str.equals("");

		if (flg_srch_tags) {

			String[] srch_ary = srch_str.split("\\s* \\s*");
			srch_str = srch_ary[srch_ary.length - 1].toLowerCase();
		}

		// - Remove any current rows
    int row_nmbr = tbl_tag_lo.getChildCount();

		if (row_nmbr > 0) tbl_tag_lo.removeAllViews();

		for (row_mbr = 0; tag_mbr < tag_nmbr; row_mbr++) {

			TableRow tableRow = new TableRow(this);
      tableRow.setGravity(Gravity.CENTER);
				
			tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
				TableLayout.LayoutParams.MATCH_PARENT, 1.0f));

			int clm_mbr;

			for (clm_mbr = 0; clm_mbr < 3; clm_mbr++) {

				Button btnTag = new Button(this);

				while (tag_mbr < tag_nmbr && (fil_tags[tag_mbr].equals("") || 
					fil_tags[tag_mbr].equalsIgnoreCase(lst_str) || 
					flg_srch_tags && !fil_tags[tag_mbr].toLowerCase().startsWith(srch_str))) {
					
					lst_str = fil_tags[tag_mbr];
					tag_mbr++;
				}

				if (tag_mbr >= tag_nmbr) break;
				
				lst_str = fil_tags[tag_mbr];
				btnTag.setText(fil_tags[tag_mbr++]);
				btnTag.setOnClickListener(this);

				btnTag.setLayoutParams(new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

				tableRow.addView(btnTag);
			}

      if (clm_mbr > 0) tbl_tag_lo.addView(tableRow);
    }
	}

	public class MakePostRequest extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {
      String response = "";
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("https://meme.svgvortec.com/Droid/sav_anm.php");
			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(9);
			nameValuePair.add(new BasicNameValuePair("username", "vgc"));
			nameValuePair.add(new BasicNameValuePair("appname", "com.acceleratedio.pac_n_zoom"));
			nameValuePair.add(new BasicNameValuePair("tags", tags_str));
			nameValuePair.add(new BasicNameValuePair("animname", AnimActivity.animFileName));
			nameValuePair.add(new BasicNameValuePair("thumbnail", encodeJPG(AnimActivity.thumbnail, 40)));
			nameValuePair.add(new BasicNameValuePair("bgimg", encodeJPG(AnimActivity.orgnlImageBmp, 90)));
			nameValuePair.add(new BasicNameValuePair("bgwdth", AnimActivity.orgnlImageWdth));
			nameValuePair.add(new BasicNameValuePair("bghght", AnimActivity.orgnlImageHght));
			nameValuePair.add(new BasicNameValuePair("scale", Float.toString(AnimActivity.scaleFactor)));

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

			Intent intent =
				new Intent(SaveAnmActivity.this, com.acceleratedio.pac_n_zoom.SelectImageActivity.class);

			startActivity(intent);
		}

		private String encodeJPG(Bitmap bmp, int qlty) {

			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, qlty, bao);
			byte[] ba = bao.toByteArray();
			return(Base64.encodeToString(ba, Base64.DEFAULT));
		}
	}
}


