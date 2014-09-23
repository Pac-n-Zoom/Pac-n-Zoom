package com.acceleratedio.pac_n_zoom;

/**
 * Created by clive on 11-Jun-14.
 */

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.acceleratedio.pac_n_zoom.PickAnmActivity;
import com.acceleratedio.pac_n_zoom.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by clive on 03-Jun-14.
 * www.101apps.co.za
 */
public class ActivityTwo extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_two);

		int position = getIntent().getIntExtra("position", -1);

		ImageView imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setMaxHeight(600);
		imageView.setMaxWidth(600);

		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage("https://meme.svgvortec.com/Droid/db_rd.php?"
			+ PickAnmActivity.fil_nams[position].replace('/', '?') + ".svg"
			,imageView);
	}
}
