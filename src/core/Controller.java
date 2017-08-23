package core;

import glm.mat._4.Mat4;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import vr.VRControllerState_t;

public class Controller {

	// TODO save previous position to establish vel vector
	// TODO work out how to get angular velocity
	// TODO ability to be able to replace controllers with mesh
	// TODO draw controllers as curves

	PApplet pApplet;
	Vive parent;
	int id;
	int scale;
	boolean controllersInitialized = false;
	PVector pos = new PVector();
	PMatrix3D matrix = new PMatrix3D();

	// Meshes - its faster to load meshes separately for each controller rather
	// than apply
	// and reset matrices for each eye draw.
	public static PShape body;
	public static PShape buttonMesh;
	public static PShape l_grip;
	public static PShape r_grip;
	public static PShape sys_button;
	public static PShape trackpad;
	public static PShape trigger;

	// event values

	boolean padLeft = false;
	boolean padRight = false;
	boolean padUp = false;
	boolean padDown = false;
	public boolean grip = false;
	public boolean triggerClicked = false;
	public boolean padClicked = false;
	public float padX = 0;
	public float padY = 0;
	public boolean button = false;
	public float triggerVal = 0;
	int packet = 0;

	public Controller(PApplet _pApplet, Vive _parent, int _id, int _scale) {
		pApplet = _pApplet;
		id = _id;
		scale = _scale;
		parent = _parent;
		loadControllerObj(parent.steamVRPath);
	}

	public void processEvents(VRControllerState_t state) {
		if (parent.hmd.GetControllerState.apply(id, state) != 0) {
			if (state.unPacketNum > packet) {
				packet = state.unPacketNum;
				// how to get touch axis values
				padX = state.rAxis[0].x;
				padY = state.rAxis[0].y;
				
				//setup pad to work out left right up down
				if(padX > 0 || padY > 0){
					//-----
				}
				
				if(state.rAxis[1].x >0){
					if(state.rAxis[1].x ==1){
						triggerClicked = true;
					}
					else triggerClicked = false;
					triggerVal = state.rAxis[1].x;
				}
				else{
					triggerVal = 0;
				}
				if(state.ulButtonPressed != 0){
					System.out.println(state.ulButtonPressed);
					if(state.ulButtonPressed == 4 || state.ulButtonPressed == 4+4294967296l || state.ulButtonPressed == 4 + 2 || state.ulButtonPressed == 4 + 8589934592l
							|| state.ulButtonPressed == 4 + 8589934592l + 4294967296l || state.ulButtonPressed == 4 + 8589934592l + 2 || state.ulButtonPressed == 4 + 8589934592l + 4294967296l){
						grip = true;
					}
					else grip = false;
					if(state.ulButtonPressed == 4294967296l || state.ulButtonPressed == 4294967296l + 4 || state.ulButtonPressed == 4294967296l + 2 
							|| state.ulButtonPressed == 4294967296l + 2 + 8589934592l || state.ulButtonPressed == 4294967296l + 8589934592l || state.ulButtonPressed == 4294967296l + 4 + 8589934592l
							 || state.ulButtonPressed == 4294967296l + 4 + 8589934592l + 2){
						padClicked = true;
					}
					else padClicked = false;
					if(state.ulButtonPressed == 2 || state.ulButtonPressed == 4294967296l + 2 || state.ulButtonPressed == 2 + 4 || state.ulButtonPressed == 2 + 4 + 8589934592l
							|| state.ulButtonPressed == 2 + 8589934592l || state.ulButtonPressed == 2 + 4 + 4294967296l + 8589934592l || state.ulButtonPressed == 2 + 4 + 4294967296l){
						button = true;
					}
					else button = false;
				}
				else{
					grip = false;
					padClicked = false;
					button = false;
				}
			}
		}
	}

	private void updateMatrix(Mat4 m) {
		matrix.m00 = m.m00;		matrix.m01 = m.m10;		matrix.m02 = m.m20;		matrix.m03 = m.m30 * scale;
		matrix.m10 = -m.m01;	matrix.m11 = -m.m11;	matrix.m12 = -m.m21;	matrix.m13 = -m.m31 * scale;
		matrix.m20 = m.m02;		matrix.m21 = m.m12;		matrix.m22 = m.m22;		matrix.m23 = m.m32 * scale;
		pos.x = matrix.m03;		pos.y = matrix.m13;		pos.z = matrix.m23;
	}

	public void draw(int process) {
		if (process == 0) {
			// (ControllerRender.body).resetMatrix();
			// (ControllerRender.body).scale(scale);
			// updateMatrix(parent.mat4DevicePose[id]);
			// (ControllerRender.body).applyMatrix(matrix);
		}
		//draw here
	}

	private void loadControllerObj(String steamVRPath) {
		body = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/body.obj");
		buttonMesh = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/button.obj");
		l_grip = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/l_grip.obj");
		r_grip = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/r_grip.obj");
		sys_button = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/sys_button.obj");
		trackpad = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/trackpad.obj");
		trigger = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/trigger.obj");
	}
	
	// max duration is 3999
	public void hapticPulse(int milliseconds){
		parent.hmd.TriggerHapticPulse.apply(id, 0, (short) 0);
	}
	
	public void hapticPulse(){
		parent.hmd.TriggerHapticPulse.apply(id, 0, (short) 0);
	}
}
