package core.viveP5;

import glm.mat._4.Mat4;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import vr.VRControllerState_t;

public class Controller {

	/**------------------------------------------------------------------------------------------
	 * Controller class is created for each controller detected
	 * Upon creation this class loads controller meshes for each controller separately to avoid
	 * the massive overhead with applying / resetting matrix transformations to PShape.
	 * 
	 * Use the series of get functions to get the current state of each button / touch variable
	 * on the controllers
	 */
	
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
	// than apply and reset matrices for each eye draw.
	public  PShape body;
	public  PShape buttonMesh;
	public  PShape l_grip;
	public  PShape r_grip;
	public  PShape sys_button;
	public  PShape trackpad;
	public  PShape trigger;

	// event values
	boolean padLeft = false; //coming soon
	boolean padRight = false; //coming soon
	boolean padUp = false; //coming soon
	boolean padDown = false; //coming soon
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
	
	/**
	 * Returns true if grip is currently pressed
	 * @return boolean grip state
	 */
	public boolean getGrip(){
		return grip;
	}
	
	/**
	 * Returns true if trigger is currently completely pressed in
	 * @return boolean trigger button state
	 */
	public boolean getTrigger(){
		return triggerClicked;
	}
	
	/**
	 * Returns float value between 0,1 corresponding to amount of trigger depression
	 * 0 = not pressed
	 * 1= fully pressed
	 * @return float trigger state
	 */
	public float getTriggerValue(){
		return triggerVal;
	}
	
	/**
	 * Returns true if pad is currently pressed
	 * @return boolean pad state
	 */
	public boolean getPad(){
		return padClicked;
	}
	
	/**
	 * Returns float value between 0,1 corresponding to X-axis of touched pad
	 * 0 = Left
	 * 1 = Right
	 * @return float x-value
	 */
	public float getPadX(){
		return padX;
	}
	
	/**
	 * Returns float value between 0,1 corresponding to Y-axis of touched pad
	 * 0 = Down
	 * 1 = Up
	 * @return float y-value
	 */
	public float getPadY(){
		return padY;
	}
	
	/**
	 * Returns true if button above touchpad is currently pressed
	 * @return boolean button state
	 */
	public boolean getButton(){
		return button;
	}
	
	/**
	 * Returns PVector of current controller world position
	 * @return PVector position
	 */
	public PVector getPos(){
		return pos;
	}

	public void processEvents(VRControllerState_t state) {
		if (parent.hmd.GetControllerState.apply(id, state) != 0) {
			if (state.unPacketNum > packet) {
				packet = state.unPacketNum;
				padX = state.rAxis[0].x;
				padY = state.rAxis[0].y;
				
				//TODO setup pad to work out left right up down
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

	public void draw(int _process) {
		//TODO find out how to set matrix (override) rather than apply matrix (multiply - super slow) (requires reset - even slower)
		processPositions(_process);

		//draw here
		pApplet.shape(body);
		pApplet.shape(trigger);
		pApplet.shape(buttonMesh);
		pApplet.shape(l_grip);
		pApplet.shape(r_grip);
		pApplet.shape(sys_button);
		pApplet.shape(trackpad);
	}
	
	private void processPositions(int process){
		if(process == 0){
			body.resetMatrix();
			body.scale(scale);
			
			if (triggerVal > 0) {
				trigger.resetMatrix();
				trigger.translate(0, 0.016f,-.039f);
				trigger.rotate((float)Math.toRadians(triggerVal*17), -1, 0, 0);
				trigger.translate(0,- 0.016f,.039f);
				trigger.scale(scale);
			}
			else{
				trigger.resetMatrix();
				trigger.scale(scale);
			}
			
			buttonMesh.resetMatrix();
			buttonMesh.scale(scale);
			
			if(grip == true){
				l_grip.resetMatrix();
				l_grip.translate(.01f*.1f, .007f*.1f,0);
				l_grip.scale(scale);
				r_grip.resetMatrix();
				r_grip.translate(-.01f*.1f, .007f*.1f,0);
				r_grip.scale(scale);
			}
			else{
				l_grip.resetMatrix();
				l_grip.scale(scale);
				r_grip.resetMatrix();
				r_grip.scale(scale);
			}
			
			sys_button.resetMatrix();
			sys_button.scale(scale);
			
			if(padClicked){
				trackpad.resetMatrix();
				trackpad.translate(0, -.003f,0 );
				trackpad.scale(scale);
			}
			else{
				trackpad.resetMatrix();
				trackpad.scale(scale);
			}
			
			updateMatrix(parent.mat4DevicePose[id]);
			
			buttonMesh.applyMatrix(matrix);
			l_grip.applyMatrix(matrix);
			r_grip.applyMatrix(matrix);
			sys_button.applyMatrix(matrix);
			trackpad.applyMatrix(matrix);
			trigger.applyMatrix(matrix);
			body.applyMatrix(matrix);
		}
	}

	private void loadControllerObj(String steamVRPath) {
		try {
			body = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/body.obj");
			buttonMesh = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/button.obj");
			l_grip = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/l_grip.obj");
			r_grip = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/r_grip.obj");
			sys_button = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/sys_button.obj");
			trackpad = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/trackpad.obj");
			trigger = pApplet.loadShape(steamVRPath + "/resources/rendermodels/vr_controller_vive_1_5/trigger.obj");
		} catch(Exception e) {
			System.out.println("Could not find models in steamVRPath - use vive.setPath(string location) to set correct path");
		}
	}
	
	/**
	 * Triggers hapticPulse for corresponding controller
	 * Note: Maximum duration is 3999 milliseconds
	 * @param int milliseconds amount of time to pulse in milliseconds
	 */
	public void hapticPulse(int milliseconds){
		parent.hmd.TriggerHapticPulse.apply(id, 0, (short) 0);
	}
	
	/**
	 * Triggers hapticPulse for corresponding controller
	 */
	public void hapticPulse(){
		parent.hmd.TriggerHapticPulse.apply(id, 0, (short) 0);
	}
}
