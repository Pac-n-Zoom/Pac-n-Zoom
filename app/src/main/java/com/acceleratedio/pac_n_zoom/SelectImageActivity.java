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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * This activity displays the gallery image picker. 
 * It displays the image that was picked.
 * 
 * @author ITCuties and Accelerated I/O
 *
 */
public class SelectImageActivity extends Activity implements OnClickListener {

	// Image loading result to pass to startActivityForResult method.
	private static int LOAD_IMAGE_RESULTS = 1;
	
	// GUI components
	private Button button;	// The button that selects the picture
	private Button btn_anm;	// The button that connects to the cloud 
	private ImageView image;// ImageView
  private RelativeLayout mainLayout; // Parent layout
	private static Context mContext;
  public static String orgFil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_image);
		button = (Button) findViewById(R.id.button); // Find references to the GUI objects
		image = (ImageView) findViewById(R.id.image);
		button.setOnClickListener(this); // Set button's onClick listener object. 
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Here we need to check if the activity that was triggers was the Image Gallery.
		// If it is the requestCode will match the LOAD_IMAGE_RESULTS value.
		// If the resultCode is RESULT_OK and there is some data we know that an image was picked.
		if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
			
			// Let's read picked image data - its URI
			Uri pickedImage = data.getData();
			
			// Let's read picked image path using content resolver
      String[] filePath = { MediaStore.Images.Media.DATA };
      Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
      cursor.moveToFirst();
      orgFil = cursor.getString(cursor.getColumnIndex(filePath[0]));
			final BitmapFactory.Options bmp_opt = new BitmapFactory.Options();
            
      // Now we need to set the GUI ImageView data with data read from the picked file.
      image.setImageBitmap(dcodRszdBmpFil(orgFil, bmp_opt));
            
      // At the end remember to close the cursor or you will end with the RuntimeException!
      cursor.close();

			//call the main layout from xml
			mainLayout = (RelativeLayout) findViewById(R.id.main_layout_id);

			//create a view to inflate the layout_item (the xml with the textView created before)
      View anm_btn_view = getLayoutInflater().inflate(R.layout.add_anm_btn, mainLayout, false);

      mainLayout.addView(anm_btn_view); //add the view to the main layout
			btn_anm = (Button) findViewById(R.id.btn_anm);
			btn_anm.setOnClickListener(this);
		}
	}

	public static Bitmap dcodRszdBmpFil(String orgFil, BitmapFactory.Options options)
	{
    options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(orgFil, options);
		options.inSampleSize = calInSampleSize(options);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(orgFil, options);
	}

	public static int calInSampleSize(BitmapFactory.Options options) {
		
		final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;
		int pxl_nmbr = height * width;

		while (pxl_nmbr > 500000) {
			pxl_nmbr /= 4;
			inSampleSize *= 2;			
		}

		return inSampleSize;
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {

			case R.id.button:

				// Create the Intent for Image Gallery.
				Intent i = new Intent(Intent.ACTION_PICK, 
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		
				// Start new activity with the LOAD_IMAGE_RESULTS to handle back the results when image is picked 
				// from the Image Gallery.
				startActivityForResult(i, LOAD_IMAGE_RESULTS);
				
				break;

			case R.id.btn_anm:

		    Intent intent = new Intent(SelectImageActivity.this, PickAnmActivity.class);
				intent.putExtra("requestString", " ");
				intent.setClassName("com.acceleratedio.pac_n_zoom", "com.acceleratedio.pac_n_zoom.PickAnmActivity");
		    startActivity(intent);
		}
	}
}
