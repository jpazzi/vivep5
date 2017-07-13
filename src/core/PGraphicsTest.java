package core;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

public class PGraphicsTest extends PApplet {

	static public void main(String[] args) {
		PApplet.main(PGraphicsTest.class.getName());
	}

	public void settings() {
		size(1080, 1200, P3D);
	}

	PImage imgL;
	PImage imgR;
	//PVector position;
	Vive vive;
	//private GL2ES2 gl;
	private PJOGL pgl;
	PeasyCam cam;
	PGL gl;
	float rotx, roty;

	public void setup() {
		//position = new PVector(0, 1200, 0); // 1200 mm from floor.
		//cam = new PeasyCam(this, 100);
		vive = new Vive(this);
	}

	public void draw() {
		//TODO TEST WITH PEASYCAM
		//TODO REMOVE DRAW FUNCTION ALL TOGETHER
		//background(50,50,50);
		//box(50);
		vive.draw();
//		 g.beginDraw();
//		 gl =  ((PGraphicsOpenGL)g).beginPGL();
//		 gl.viewport (0, 0, 500, 500);  
//		 ((PGraphicsOpenGL)g).endPGL();
//		 perspective(PI/6f, 1f, 10, 1000);
//		 camera(0,0,600, 0,0,0, 0,1,0);
//		 renderGeometry();
//		 g.endDraw();
//		 // right viewport - back view
//		 g.beginDraw();
//		 gl = ((PGraphicsOpenGL)g).beginPGL();
//		 gl.viewport(300, 0, 300, 300);  
//		 ((PGraphicsOpenGL)g).endPGL();
//		 perspective(PI/6f, 1f, 10, 1000);
//		 camera(0,0,-600, 0,0,0, 0,1,0);
//		 renderGeometry();
//		 g.endDraw();
		//VRdraw(0);
	}

	public void VRdraw(int eye) {
		//TODO write so eye information happens internally in vive
		
		// The scale is approximately real scale. The unit is a millimeter.
		// The default eye position is on the origin (0, 0, 0) in the scene.
		// The parameter "eye" gives LEFT or RIGHT.

		

		
//		background(50);
//		fill(255);
//		translate(position.x, position.y, position.z);

		// picture
//		pushMatrix();
//		//translate(0, -1500, -1800);
//		////GARBAGE NEEDED FOR EYE TRANSLATIONS?
//		//imageMode(CENTER);
//		scale(3.0f);
//		if (eye == LEFT) {
//			image(imgL, 0, 0);
//		} else if (eye == RIGHT) {
//			image(imgR, 0, 0);
//		}
//		popMatrix();
		// light
		//lights();
		text(frameRate,500, 500);
		stroke(255,0,0);
		strokeWeight(5);
		point(0, -50,0);
		// floor
		strokeWeight(1);
		stroke(0);
		pushMatrix();
		fill(0,50,50);
		rotateX(radians(90));
		translate(0, 0, 0);
		drawPlate(2000, 2000, 300);
		popMatrix();

		// wall
		pushMatrix();
		translate(0, -1500, -2000);
		drawPlate(2000, 2000, 300);
		popMatrix();

		// cube
		pushMatrix();
		translate(500, -500, -400);
		fill(50, 200, 50);
		rotateX(millis() / 1000.0f);
		rotateY(millis() / 900.0f);
		box(200);
		popMatrix();
	}

	void renderGeometry() {
		 lights();
		 rotateX(rotx);
		 rotateY(roty);
		 randomSeed(0);
		 for (int i=0; i<50; i++) {
		   float x = random(-100,100);
		   float y = random(-100,100);
		   float z = random(-100,100); 
		   pushMatrix();
		   translate(x,y,z);
		   fill(random(256), 255, 255);
		   box(i+1);
		   popMatrix();
		 }
		}
	
	void drawPlate(float w, float h, float grid_interval) {
		for (float x = -w / 2; x < w / 2; x += grid_interval) {
			line(x, -h / 2, 0, x, h / 2, 0);
		}
		for (float y = -h / 2; y < h / 2; y += grid_interval) {
			line(-w / 2, y, 0, w / 2, y, 0);
		}
		box(w, h, 0.5f);
	}

	public void keyPressed() {

		// Move
//		if (keyCode == LEFT) {
//			position.x += 500;
//		}
//		if (keyCode == RIGHT) {
//			position.x -= 500;
//		}
//		if (keyCode == UP) {
//			position.z += 500;
//		}
//		if (keyCode == DOWN) {
//			position.z -= 500;
//		}
	}

}
