package core.viveP5;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;

import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.jogamp.opengl.util.GLBuffers;

import glm.mat._4.Mat4;
import glm.vec._2.i.Vec2i;
import one.util.streamex.IntStreamEx;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import vr.IVRCompositor_FnTable;
import vr.IVRSystem;
import vr.Texture_t;
import vr.TrackedDevicePose_t;
import vr.VR;
import vr.VRControllerState_t;
import vr.VREvent_t;

public class Vive {
	
	/**------------------------------------------------------------------------------------------
	 * This experimental library adds HTC Vive support for processing including controllers
	 * 
	 * Requires SteamVR to be installed on your computer
	 * 
	 * Thanks to the following repo's:
	 * 		GBarbieri's Jogl-hello-vr repo - https://github.com/java-opengl-labs/jogl-hello-vr
	 * 		Java OpenVR Binding - https://github.com/java-graphics/openvr
	 * 		Kougaku's OculusRiftP5 - https://github.com/kougaku/OculusRiftP5
	 * 
	 * @author James Pazzi
	 * @version 0.5.1
	 * 
	 */
	
	// TODO fix background ruining buffer
	// TODO implement modes for standing / seated
	// TODO pass velocity / angular velocity for HMD / controllers
	// TODO enclosure dimensions - as AABB?
	// TODO load single / more efficient instances of models
	
	public String steamVRPath = "C:/Program Files (x86)/Steam/steamapps/common/SteamVR";
	
	private int scale = 1000;
	private float r = 0, g = 0, b = 0;
	private boolean VRdraw = true;
	private int eyeL = 2, eyeR = 1;
	public ArrayList<Controller> controllers = new ArrayList<Controller>();
	
	public IVRSystem hmd;
	private IntBuffer errorBuffer = GLBuffers.newDirectIntBuffer(1);
	private PApplet parent;
	private IVRCompositor_FnTable compositor;
	private Texture_t[] eyeTexture = { new Texture_t(), new Texture_t() };
	public Vec2i renderSize = new Vec2i();
	private Method VRdrawMethod;
	public boolean drawControllers = true;
	public FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16);

	// TRACKED POSES
	private TrackedDevicePose_t.ByReference trackedDevicePosesReference = new TrackedDevicePose_t.ByReference();
	public TrackedDevicePose_t[] trackedDevicePose = (TrackedDevicePose_t[]) trackedDevicePosesReference
			.toArray(VR.k_unMaxTrackedDeviceCount);
	private char[] devClassChar = new char[VR.k_unMaxTrackedDeviceCount];
	public Mat4 hmdPose = new Mat4(), vp = new Mat4();
	public Mat4[] projection = new Mat4[VR.EVREye.Max];
	public Mat4[] eyePos = new Mat4[VR.EVREye.Max];
	public Mat4[] mat4DevicePose = new Mat4[VR.k_unMaxTrackedDeviceCount];
	public Mat4[] multiplied = new Mat4[VR.EVREye.Max];
	private PGL gl;
	
	/**
	 * Creates Vive Class
	 * Pass PApplet to class
	 * @param _parent write "this"
	 */
	public Vive(PApplet _parent) {
		parent = _parent;
		parent.frameRate(90);
		initHeadset();
		initCompositor();
		setupCameras();
		VRdrawMethod = getMethodRef(parent, "VRdraw", null);
		IntStreamEx.range(mat4DevicePose.length).forEach(mat -> mat4DevicePose[mat] = new Mat4());

	}

	// Initialization functions --------------------------------------

	// Initialize vive headset and SteamVR
	/**
	 * Creates hmd and loads SteamVR
	 */
	private void initHeadset() {
		hmd = VR.VR_Init(errorBuffer, VR.EVRApplicationType.VRApplication_Scene);
		if (errorBuffer.get(0) != VR.EVRInitError.VRInitError_None) {
			hmd = null;
			String s = "Unable to init VR runtime: " + VR.VR_GetVRInitErrorAsEnglishDescription(errorBuffer.get(0));
			throw new Error("VR_Init Failed, " + s);
		}
	}
	
	/**
	 * Initializes Compositor (compositor sends frames to each eye)
	 */
	private boolean initCompositor() {
		compositor = new IVRCompositor_FnTable(VR.VR_GetGenericInterface(VR.IVRCompositor_Version, errorBuffer));
		if (compositor == null || errorBuffer.get(0) != VR.EVRInitError.VRInitError_None) {
			System.err.println("Compositor initialization failed.");
			return false;
		}
		return true;
	}

	// Initialization functions ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	
	private PMatrix3D castMat4Projecthmd(Mat4 m) {
		PMatrix3D pm = new PMatrix3D();
		pm.m00 = m.m00;		pm.m01 = m.m10;		pm.m02 = m.m20;		pm.m03 = m.m30 * scale;
		pm.m10 = m.m01;		pm.m11 = m.m11;		pm.m12 = m.m21;		pm.m13 = m.m31 * scale;
		pm.m20 = m.m02;		pm.m21 = m.m12;		pm.m22 = m.m22;		pm.m23 = m.m32 * scale;
		pm.m30 = m.m03;		pm.m31 = m.m13;		pm.m32 = m.m23;		pm.m33 = m.m33 * scale;
		pm.scale(1, -1, 1);
		return pm;
	}
	
	/**
	 * Set your unit scale as a multiplier of default Vive scale, which is metres
	 * 1000 = millimetres, 100 = centimetres, 1 = metres
	 * @param s scale (1000 by default)
	 */
	public void setScale(int s){
		scale = s;
	}
	
	/**
	 * If left eye is showing right eye or vice-versa run this command to swap them back to normal
	 */
	public void swapEyes(){
		int tempEye = eyeL;
		eyeL = eyeR;
		eyeR = tempEye;
	}
	
	/**
	 * Set your path to SteamVR directory here
	 * @param path path to SteamVR (usually "C:/Program Files (x86)/Steam/steamapps/common/SteamVR")
	 */
	public void setPath(String path){
		steamVRPath = path;
	}
	
	/**
	 * If eyes are render black or not at all experiment with changing the values here
	 * generally values betweem 1-4 work. right is usually before left (eg. right 2, left 3)
	 * 
	 * @param left integer between 1-4 usually
	 * @param right integer between 1-4 usually
	 */
	public void setEye(int left, int right){
		eyeL = left;
		eyeR = right;
	}

	/**
	 * If you want to update the vive without drawing to it, run this function, otherwise run draw.
	 */
	public void update(){
		parent.g.clear();
		handleInput();
		updateHMDMatrixPose();
	}
	
	// Draw functions ------------------------------------------------
	
	/**
	 * Updates Vive, draws a frame for each eye, and sends frames to headset
	 * Requires VRdraw function to be present in MainApp
	 */
	public void draw() {
		handleInput();

		for (int eye = 0; eye < VR.EVREye.Max; eye++) {
			calcCurrentViewProjectionMatrix(eye);

			parent.g.beginDraw();
			
			gl = ((PGraphicsOpenGL)parent.g).beginPGL();
			
			gl.clearColor(r, g, b, 1.0f);
			gl.clear(GL_COLOR_BUFFER_BIT);
			((PGraphicsOpenGL)parent.g).resetMatrix();
			((PGraphicsOpenGL)parent.g).setProjection(castMat4Projecthmd(vp));
			((PGraphicsOpenGL)parent.g).setSize(1680, 1512);
			
			// Draw here
			runVRdrawMethod();
			
			//draw controllers
			for(Controller c: controllers){
				c.draw(eye);
			}
			
			// End draw
			((PGraphicsOpenGL)parent.g).endPGL();
			parent.g.endDraw();
			
			// Send buffers to each eye
			if(eye == 0){
				eyeTexture[eye].set(eyeL, VR.EGraphicsAPIConvention.API_OpenGL, VR.EColorSpace.ColorSpace_Gamma);
				compositor.Submit.apply(eye, eyeTexture[eye], null, VR.EVRSubmitFlags.Submit_Default);
			}
			else{
				eyeTexture[eye].set(eyeR, VR.EGraphicsAPIConvention.API_OpenGL, VR.EColorSpace.ColorSpace_Gamma);
				compositor.Submit.apply(eye, eyeTexture[eye], null, VR.EVRSubmitFlags.Submit_Default);
			}
		}
		updateHMDMatrixPose();
	}

	/**
	 * Instead of using processings background command, use this to set your background colour
	 * Note: there is no possibility of background alpha / transparency value
	 * 
	 * @param red red value between 0 and 255
	 * @param green green value between 0 and 255
	 * @param blue blue value between 0 and 255
	 */
	public void setBackground(float red, float green, float blue) {
		r = red / 255;
		g = green / 255;
		b = blue / 255;
	}

	// Draw functions ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

	// Positional Tracking functions ---------------------------------
	
	private void updateHMDMatrixPose() {
		if (hmd == null) {
			return;
		}
		compositor.WaitGetPoses.apply(trackedDevicePosesReference, VR.k_unMaxTrackedDeviceCount, null, 0);

		for (int device = 0; device < VR.k_unMaxTrackedDeviceCount; device++) {

			if (trackedDevicePose[device].bPoseIsValid == 1) {
				
				mat4DevicePose[device].set(trackedDevicePose[device].mDeviceToAbsoluteTracking);
				
				if (devClassChar[device] == 0) {

					switch (hmd.GetTrackedDeviceClass.apply(device)) {

					case VR.ETrackedDeviceClass.TrackedDeviceClass_Controller:
						devClassChar[device] = 'C';
						controllers.add(new Controller(parent, this, device, scale));
						break;

					case VR.ETrackedDeviceClass.TrackedDeviceClass_HMD:
						devClassChar[device] = 'H';
						break;

					case VR.ETrackedDeviceClass.TrackedDeviceClass_Invalid:
						devClassChar[device] = 'I';
						break;

					case VR.ETrackedDeviceClass.TrackedDeviceClass_Other:
						devClassChar[device] = 'O';
						break;

					case VR.ETrackedDeviceClass.TrackedDeviceClass_TrackingReference:
						devClassChar[device] = 'T';
						break;

					default:
						devClassChar[device] = '?';
						break;
					}
				}
			}
		}
		if (trackedDevicePose[VR.k_unTrackedDeviceIndex_Hmd].bPoseIsValid == 1) {
			mat4DevicePose[VR.k_unTrackedDeviceIndex_Hmd].inverse(hmdPose);
		}
	}

	private void calcCurrentViewProjectionMatrix(int eye) {
		multiplied[eye] = projection[eye].mul(eyePos[eye], vp).mul(hmdPose);
	}

	private void setupCameras() {
		for (int eye = 0; eye < VR.EVREye.Max; eye++) {
			projection[eye] = getHmdMatrixProjection(eye);
			eyePos[eye] = getHmdMatrixPoseEye(eye);
		}
	}

	private Mat4 getHmdMatrixProjection(int eye) {
		if (hmd == null) {
			return new Mat4();
		}
		float nearClip = .1f, farClip = 3000.0f;
		return new Mat4(hmd.GetProjectionMatrix.apply(eye, nearClip, farClip, VR.EGraphicsAPIConvention.API_OpenGL));
	}

	private Mat4 getHmdMatrixPoseEye(int eye) {
		if (hmd == null) {
			return new Mat4();
		}
		return new Mat4(hmd.GetEyeToHeadTransform.apply(eye)).inverse();
	}

	// Method initialization and use fucntion for VRdraw -------------

	// Method to run draw in MainApp
	private void runVRdrawMethod() {
		if (VRdraw) {
			try {
				VRdrawMethod.invoke(parent);
			} catch (Exception e) {
				System.out.println("VRdraw function does not exist in MainApp");
				VRdraw = false;
			}
		}
	}

	private Method getMethodRef(Object obj, String methodName, @SuppressWarnings("rawtypes") Class[] paraList) {
		Method ret = null;
		if (VRdraw) {
			try {
				ret = obj.getClass().getMethod(methodName, paraList);
			} catch (Exception e) {
				System.out.println("VRdraw function does not exist in MainApp");
				VRdraw = false;
			}
		}
		return ret;
	}

	// Method initialization and use fucntion for VRdraw -------------

	private void handleInput() {
		// Process SteamVR events
		VREvent_t event = new VREvent_t();
		while (hmd.PollNextEvent.apply(event, event.size()) != 0) {
			processVREvent(event);
		}

		// Process SteamVR controller state
		for (Controller c: controllers) {
			VRControllerState_t state = new VRControllerState_t();
			c.processEvents(state);
		}
	}

	private void processVREvent(VREvent_t event) {
		switch (event.eventType) {
		case VR.EVREventType.VREvent_None:
			System.out.println("No event registered");
			break;
		case VR.EVREventType.VREvent_TrackedDeviceActivated:
			System.out.println("Device %u attached. Setting up render model.\n" + event.trackedDeviceIndex);
			break;

		case VR.EVREventType.VREvent_TrackedDeviceDeactivated:
			System.out.println("Device %u detached.\n" + event.trackedDeviceIndex);
			break;

		case VR.EVREventType.VREvent_TrackedDeviceUpdated:
			System.out.println("Device %u updated.\n" + event.trackedDeviceIndex);
			break;
		}
	}

}
