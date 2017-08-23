package core.viveP5;

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
		vive.SetBackground(50, 50, 50);
	}

	public void draw() {
		if(viveOn){
			vive.draw();
		}
		else{
			vive.update();
			VRdraw();
		}
		System.out.println(frameRate);
	}

	public void VRdraw() {
		// The scale is approximately real scale. The unit is a millimeter.

		// cube
		pushMatrix();
		translate(500, -500, -400);
		fill(50, 200, 50);
		rotateX(millis() / 1000.0f);
		rotateY(millis() / 900.0f);
		box(200);
		popMatrix();

	}

	public void keyPressed() {
		if(key == ' '){
			viveOn = !viveOn;
			print(viveOn);
		}
	}

}
