Pac-n-Zoom:
==========

This is an Android project that allows users to turn photos into 
animations.

This code is currently a work in progress. 

In this commit, the code will allow the user to select a picture. 
When the "Animate" is selected, the thumbnails from the cloud are 
displayed. The tags are loaded, but they are not used, yet. The user 
can type in a tag. The test case is "Bee" which is case sensitive. 
When "Send" is selected, only the thumbnails that have a "Bee" tag
are displayed.

In the next commit, the user will select a thumbnail. The original 
picture is displayed, and the animation that was represented by the 
selected thumbnail will play on the picture that was initially 
selected.


SVG Animation:
=============

SVG is a standard from W3C, and it is a form of XML. In Pac-n-Zoom
technology, the SVG is composed of a series of definitions that have
infinite recursion. Each object that is animated has its own name 
and instance as defined below. Both names and instances have a
sequential number. 

When ran on the Android, each instance of each object has its own 
view. Raster objects are images in a view. Vector objects are written 
to canvas that is assigned to view. 

The views (objects) are animated by a series of frames. A frame is 
sort of like an act in a play or a scene from a movie. A frame has a
defined length of time that is measured in seconds, but different 
frames can have different times. No two frames can occur 
simultaneously, but of course, you could have whatever you wanted 
(well, maybe some limits) in a single frame.

Any object can be translated (moved), scaled, or rotated. The actions
can occur separately or together.

Each SVG tag of interest is held in a java class (Roman Numeral). The 
following outline describes the structure of that data.

I. Viewport: The viewport has a tag name of "svg" and is a class with 
   the following variables (attributes).

	 A. id:
	 
			1. Purpose: The name of the animation
			
			2. Reserved: The name can not be "svg_hom_img"
			
			3. Example: "me_world_meme_1217.svg"

	 B. usrname: The id of person who created the animation.
			It is usually set to "null".
			
	 C. width: The width of the viewport in pixels
	 
	 D. height: The height of the viewport in pixels
	 
	 E. ordr: 
	 
			1. Purpose: The order of the objects that indicates 
				 which objects are on top of other objects. The 
				 higher array members are on top.
				 
			2. Syntax:
			 
				 a. First Letter: First letter of a string is 'g'
					
				 b. First Number: Number between the first 
						 letter and '_' is the object number.
						 
				 c. Second Number: The number after '_' is the
						 instance of the object.

				 d. Delimiter, the instances of objects are delimited with
				  	a ','.
				 
			3. Example: "g1_0,g2_0"
			
	 F. scale: A float that scales the entire animation. 
			This is read from the "g_scl" tag.
	 
	 G. translate: An array of two ints that translates the 
			entire animation. This is read form the "g_scl" tag.
	 
			1. Horizontal: The horizontal offset in pixels
			
			2. Vertical: The vertical offset in pixels
	 
	 H. rotate: An int that rotates the entire animation. 
			This is read form the "g_scl" tag.

II. Frame:

	 A. Structure: List of Frame classes
	 
	 B. Syntax: private List<Frame> frames = new ArrayList<Frame>(); 
			An example can be found at,
			http://stackoverflow.com/questions/1921181/java-arraylist-of-string-arrays.
	 
	 C. Class:
	 
			1. id:
			
				 a. syntax: "frm_" + frame_member
				 
				 b. example: "frm_0"
				 
			2. bgn: The time that the frame begins in seconds
			
			3. end: The time that the frame ends in seconds
			
			4. ordr: A list of strings
			
				 a. Purpose: Indicates which object instances are on top
				 
				 b. Example: "g1_0,g2_0"

			5. xfm_idx: This is an Integer[] that has a value set for 
				 object. If the value is -1, then no xfrm exists for the
	 			 object. Otherwise, the value is the index to Xfrm array.			 
			
III. Xfrm:

	 A. Structure: List of Xfrm classes
	 
	 B. Syntax: private List<Frame> frames = new ArrayList<Frame>(); 
	 
	 C. Default: Each object of each frame needs an Xfrm
			class to avoid default behavior. By default, if no
			class exists, then the values inherit the ending 
			values previous class of the same object. If no 
			previous class is available, the numbers are taken
			from the intial object definition is g_scl which 
			are stored in the symbol class for this object. If 
			no g_scl object definition is available, all 
			parameters assume their default value.
	 
	 D. Class: The scale data must be read an applied to 
			this data. 
	 
			1. id:
			
				 a. syntax: "anm_" + object_id + 'f' + frame_member. 
						The object_id syntax was given above in the 
						viewport order.
				 
				 b. example: "anm_g2_0_f0"
				 
			2. scl_bgn: Is the beginning scale. The default is 1.
			
			3. scl_end: Is the optional ending scale.
			
			4. mov_path:
			
				 a. Purpose: The movement of the object in this 
						frame in an syntax that is similar to SVG.
				 
				 b. Example: "105 105 L 33 104 L 13 109 L 2 121"
				 
			5. rot_bgn: rot_bgn is the beginning rotation in 
				 degrees. The default is 0.
			
			6. rot_end: rot_end is the optional ending rotation 
				 in degrees.

IV. symbol: Each object in the animation is defined by at
	 least 1 symbol class. A symbol class can hold other symbols.

	 A. Structure: List of symbol classes
	 
	 B. id: This is a string that contains the id of the 
			group who's parent is the symbol tag.
			
	 C. scale: A float that scales the entire animation. 
			This is read form the "g_scl" tag.
	 
	 D. translate: An array of two ints that translates the 
			entire animation. This is read form the "g_scl" tag.
	 
			1. Horizontal: The horizontal offset in pixels
			
			2. Vertical: The vertical offset in pixels
	 
	 E. rotate: An int that rotates the entire animation. 
			This is read form the "g_scl" tag.
			
	 F. raster: This contains the file path for a raster 
			image. If it is not a rater image, it should be set
			to NULL.
			
	 G. blob: This is a list of blob classes. At this time,
			each blob is a closed path that is filled with the
			color, but in the future, sprites will be 
			recursive. In other words, sprites could hold other
			sprites, common paths, or unique paths.
			
			1. Unique Paths:
	 
				 a. ID: This is the id of the blob that was read 
						from the file.
						
				 b. Tag: The SVG tag name is "path".
				 
				 c. Reference: The points are referenced to the
						upper left hand corner of the image.
						
				 d. Color: The color of the blob is given as an
						attribute of the "path" tag in CSS format.
			
			2. Common Paths:
	 
				 a. ID: This is the id of the blob that was read 
						from the file, but the id will have the 
						format pth_### where ### is the index of the
						common path ArrayList.
						
				 b. Tag: The SVG tag name is "use".
				 
				 c. Reference: The points are referenced to the
						reference point which is the most north (top) 
						and west (left) point of the blob.
						
				 d. Color: The color of the blob is given as an
						attribute of the "path" tag as and index to
						the colorTable ArrayList.
			
			3. Other Sprites: 
	 
				 a. ID: This is the id of the sprite that was 
						read from the file, but the id will have the 
						format sprt_### where ### is the index of the
						sprite ArrayList.
						
				 b. Tag: The SVG tag name is "use".
				 
				 c. Reference: The sprite is referenced to its 
						parent's reference point which is the most 
						north (top) and west (left) point of the 
						parent sprite. The offset from the reference
						point is given as an attribute in the use
						tag.
						
				 d. Color: The color is given in tag's decendants 
						that are paths.



