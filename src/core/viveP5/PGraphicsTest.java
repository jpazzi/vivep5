package core.viveP5;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class PGraphicsTest extends PApplet {

	static public void main(String[] args) {
		PApplet.main(PGraphicsTest.class.getName());
	}

	public void settings() {
		size(1080, 1200, P3D);
	}

	Vive vive;
	PeasyCam cam;
	PShape bounds = new PShape();
	boolean viveOn = true;
	int voxel[][][];

	float rotx, roty;

	public void setup() {
		cam = new PeasyCam(this, 1000);
		vive = new Vive(this);
		vive.setBackground(255, 160, 122);
		bounds = createShape();
		bounds();
		//createVoxelGrid(200,200,200);
	}

	public void draw() {
		if (viveOn) {
			vive.draw();
		} else {
			vive.update();
			VRdraw();
		}
		// System.out.println(frameRate);
		
//		for(Controller c: vive.controllers){
//			if(c.getTrigger()){
//				//paint
//			}
//			if(c.getPad()){
//				//erase
//			}
//		}
//		
		
	}

	public void VRdraw() {
		for (int i = 0; i < 4; i++) {
			shape(bounds);
			bounds.rotateY(PI/2);
		}
		floorGrid();
		drawBoxes();
	}

	public void bounds() {
		
		
		//pushMatrix();
		//for (int i = 1; i < 5; i++) {
			
		pushMatrix();
		
			bounds.beginShape(PConstants.POLYGON);
			bounds.noStroke();
			bounds.fill(255, 160, 122);
			bounds.vertex(-5000,-5000,-5000);
			bounds.fill(255);
			bounds.vertex(-5000,0,-5000);
			bounds.fill(255);
			bounds.vertex(5000,0,-5000);
			bounds.fill(255, 160, 122);
			bounds.vertex(5000,-5000,-5000);
			bounds.endShape();
			popMatrix();
			bounds.scale(2);
			//shape(bounds);
		//}

		//popMatrix();
	}

	public void floorGrid() {
		stroke(120, 13);
		strokeWeight(2);
		for (int i = -5000; i < 5000; i += 1000) {
			line(i, 0, -5000, i, 0, 5000);
		}
		for (int i = -5000; i < 5000; i += 1000) {
			line(-5000, 0, i, 5000,0,i);
		}
	}

	public void keyPressed() {
		if (key == ' ') {
			viveOn = !viveOn;
			print(viveOn);
		}
	}

	void drawBoxes() {
		  for (int i = -1000; i < 1000; i+=200) {
		    for (int j = -1000; j < 1000; j += 200) {
		      for (int k = -2000; k < 0; k += 200) {
		        pushMatrix();
		        translate(i, k, j);
		        fill(255, 105, 170);
		        rotateX(millis() / 1000.0f);
		        rotateY(millis() / 900.0f);
		        box(50);
		        popMatrix();
		      }
		    }
		  }
		}
	
}
