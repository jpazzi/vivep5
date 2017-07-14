package core;

import peasy.PeasyCam;
import processing.core.PApplet;


public class PGraphicsTest extends PApplet {

	static public void main(String[] args) {
		PApplet.main(PGraphicsTest.class.getName());
	}

	public void settings() {
		size(1080, 1200, P3D);
	}

	Vive vive;
	PeasyCam cam;
	boolean viveOn = true;

	float rotx, roty;

	public void setup() {
		cam = new PeasyCam(this, 1000);
		vive = new Vive(this);
	}

	public void draw() {
		if(viveOn){
			
			vive.draw();
		}
		else{
			vive.update();
			fill(0);
			sphere(50000);
			VRdraw(0);
		}
	}

	public void VRdraw(int eye) {
		//TODO write so eye information happens internally in vive
		
		// The scale is approximately real scale. The unit is a millimeter.
		// The default eye position is on the origin (0, 0, 0) in the scene.


		lights();
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
		if(key == ' '){
			viveOn = !viveOn;
			print(viveOn);
		}
	}

}
