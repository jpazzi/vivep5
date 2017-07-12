package core;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_MULTISAMPLE;

import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.nativewindow.util.Point;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;

import glm.mat._4.Mat4;
import glm.vec._2.i.Vec2i;
import glutil.BufferUtils;
import one.util.streamex.IntStreamEx;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.opengl.FrameBuffer;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;
import processing.opengl.PShader;
import vr.IVRCompositor_FnTable;
import vr.IVRSystem;
import vr.Texture_t;
import vr.TrackedDevicePose_t;
import vr.VR;
import vr.VRControllerState_t;
import vr.VREvent_t;

public class Vive {

	// TODO fix background ruining buffer
	// TODO check if the render sizes are correct

	private float r = 0, g = 0, b = 0;
	private boolean VRdraw = true;
	private final float z_near = 1;
	private final float z_far = 1000000000;
	public IVRSystem hmd;
	private IntBuffer errorBuffer = GLBuffers.newDirectIntBuffer(1);
	private PApplet parent;
	//private GL2ES2 gl;
	private PJOGL pgl;
	private GL4 gl4;
	private IVRCompositor_FnTable compositor;
	private Texture_t[] eyeTexture = { new Texture_t(), new Texture_t() };
	public Vec2i renderSize = new Vec2i();
	private Method VRdrawMethod;
	//private PShader barrel;
	private final float scaleFactor = 2.11f;
	public FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16);

	// TRACKED POSES
	@SuppressWarnings("unused")
	private String poseClasses;
	private TrackedDevicePose_t.ByReference trackedDevicePosesReference = new TrackedDevicePose_t.ByReference();
	public TrackedDevicePose_t[] trackedDevicePose = (TrackedDevicePose_t[]) trackedDevicePosesReference
			.toArray(VR.k_unMaxTrackedDeviceCount);
	public int trackedControllerCount = 0;
	public int trackedControllerCount_Last = -1;
	public int validPoseCount = 0;
	public int validPoseCount_Last = -1;
	public boolean[] rbShowTrackedDevice = new boolean[VR.k_unMaxTrackedDeviceCount];

	private char[] devClassChar = new char[VR.k_unMaxTrackedDeviceCount];

	public Mat4 hmdPose = new Mat4(), vp = new Mat4();
	public Mat4[] projection = new Mat4[VR.EVREye.Max];
	public Mat4[] eyePos = new Mat4[VR.EVREye.Max];
	public Mat4[] mat4DevicePose = new Mat4[VR.k_unMaxTrackedDeviceCount];
	public PGraphics fbL;
	public PGraphics fbR;
	PGL gl;
	
	public Vive(PApplet _parent) {
		parent = _parent;
		parent.frameRate(90);
		initHeadset();
		initCompositor();
		setupPGraphics();
		setupCameras();
		VRdrawMethod = getMethodRef(parent, "VRdraw", new Class[] { int.class });
		//barrel = parent.loadShader("barrel_frag.glsl");
		IntStreamEx.range(mat4DevicePose.length).forEach(mat -> mat4DevicePose[mat] = new Mat4());
		fbL = parent.createGraphics(renderSize.x, renderSize.y, parent.P3D);
		fbR = parent.createGraphics(renderSize.x, renderSize.y, parent.P3D);
	}

	// Initialization functions --------------------------------------

	// Initialize vive headset and SteamVR
	private void initHeadset() {
		hmd = VR.VR_Init(errorBuffer, VR.EVRApplicationType.VRApplication_Scene);
		if (errorBuffer.get(0) != VR.EVRInitError.VRInitError_None) {
			hmd = null;
			String s = "Unable to init VR runtime: " + VR.VR_GetVRInitErrorAsEnglishDescription(errorBuffer.get(0));
			throw new Error("VR_Init Failed, " + s);
		}
	}

	private boolean setupPGraphics() {
		if (hmd == null) {
			return false;
		}
		IntBuffer width = GLBuffers.newDirectIntBuffer(1), height = GLBuffers.newDirectIntBuffer(1);
		hmd.GetRecommendedRenderTargetSize.apply(width, height);

		renderSize.set(width.get(0), height.get(0));

		pgl = (PJOGL) parent.beginPGL();
		//gl = pgl.gl.getGL2ES2();

		BufferUtils.destroyDirectBuffer(width);
		BufferUtils.destroyDirectBuffer(height);
		parent.endPGL();
		return true;
	}

	private boolean initCompositor() {
		compositor = new IVRCompositor_FnTable(VR.VR_GetGenericInterface(VR.IVRCompositor_Version, errorBuffer));
		if (compositor == null || errorBuffer.get(0) != VR.EVRInitError.VRInitError_None) {
			System.err.println("Compositor initialization failed. See log file for details");
			return false;
		}
		return true;
	}

	// Initialization functions --------------------------------------

	// Draw functions ------------------------------------------------

	public PMatrix3D castMat4(Mat4 m, Mat4 pos, Mat4 eye) {
		PMatrix3D pm = new PMatrix3D();
		pm.m00 = m.m00;		pm.m01 = m.m10;		pm.m02 = m.m20;		pm.m03 = (m.m30 - (eye.m30))*1000;
		pm.m10 = m.m01;		pm.m11 = m.m11;		pm.m12 = m.m21;		pm.m13 = m.m31*1000;
		pm.m20 = m.m02;		pm.m21 = m.m12;		pm.m22 = m.m22;		pm.m23 = (m.m32 - (eye.m32))*1000;
		pm.m30 = m.m03;		pm.m31 = m.m13;		pm.m32 = m.m23;		pm.m33 = m.m33;
		System.out.println("pos");
		pos.print();
		System.out.println("m");
		m.print();
		pm.preApply(1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
		pm.scale(1, -1, 1);
		//pm.print();
		return pm;
	}
	
	public PMatrix3D castMat4Project(Mat4 m) {
		PMatrix3D pm = new PMatrix3D();
		pm.m00 = m.m00;		pm.m01 = m.m10;		pm.m02 = m.m20;		//pm.m03 = m.m30;
		pm.m10 = m.m01;		pm.m11 = m.m11;		pm.m12 = m.m21;		//pm.m13 = m.m31;
		pm.m20 = m.m02;		pm.m21 = m.m12;		pm.m22 = m.m22;		//pm.m23 = m.m32;
		pm.m30 = m.m03;		pm.m31 = m.m13;		pm.m32 = m.m23;		//pm.m33 = m.m33;

		//pm.preApply(1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
		//pm.scale(1, -1, 1);

		return pm;
	}

	public void draw() {
		handleInput();

		calcCurrentViewProjectionMatrix(0);
		//parent.resetMatrix();
		//parent.resetMatrix();
		//parent.applyMatrix(castMat4(hmdPose));
		
		
		//parent.applyMatrix(castMat4(projection[0]));
		//parent.perspective(parent.radians(100), (float)(renderSize.x * 1.0 / renderSize.y), z_near, z_far);
		// parent.applyMatrix(castMat4(mat4DevicePose[0]));

		
		
		//fb.setc
		
//		
		//System.out.println(fb.initialized);
		//parent.perspective(parent.radians(100), (float)(renderSize.x * 1.0 / renderSize.y), z_near, z_far);
//		pgl = (PJOGL) parent.beginPGL();
//		gl = pgl.gl.getGL2ES2();
//		gl4 = gl.getGL4();
//		gl4.glEnable(GL_MULTISAMPLE);
//		gl4.glViewport(0, 0, renderSize.x, renderSize.y);

//		gl4.glClearColor(r, g, b, 1.0f);
//		gl4.glClear(GL_COLOR_BUFFER_BIT);
//
//		gl4.glEnable(GL_DEPTH_TEST);
		

		for (int eye = 0; eye < VR.EVREye.Max; eye++) {

			parent.resetMatrix();
			//parent.applyMatrix(castMat4(hmdPose));
			//parent.applyMatrix(castMat4(eyePos[eye]));
			
			//parent.applyMatrix(castMat4(hmdPose, mat4DevicePose[VR.k_unTrackedDeviceIndex_Hmd], eyePos[eye]));
			//projection[eye].print();
			
			

			// calcCurrentViewProjectionMatrix(eye);
			//parent.applyMatrix(castMat4(eyePos[eye]));

			// parent.pushMatrix();
			// parent.strokeWeight(10);
			// parent.stroke(255);
			// parent.point(0, 0,0);
			//
			//
			// parent.stroke(0);
			// parent.strokeWeight(5);
			// parent.popMatrix();
			//fb.setCache(parent.IMAGE, 1);
//			if(eye == 1){
				
			 parent.g.beginDraw();
			 
			 gl = ((PGraphicsOpenGL)parent.g).beginPGL();
			 gl.clearColor(r, g, b, 1.0f);
			 gl.clear(GL_COLOR_BUFFER_BIT);
			 parent.applyMatrix(castMat4(hmdPose, mat4DevicePose[VR.k_unTrackedDeviceIndex_Hmd], eyePos[eye]));
			 //gl.viewport (0, 0, renderSize.x, renderSize.y);  
			 runVRdrawMethod(eye);
			 ((PGraphicsOpenGL)parent.g).endPGL();
			 parent.perspective(parent.radians(110), (float)(renderSize.x * 1.0 / renderSize.y), z_near, z_far);
			 
			 
			 parent.g.endDraw();
			 //parent.g.clear();
			 // right viewport - back view
//			 parent.g.beginDraw();
//			 parent.perspective(parent.radians(100), (float)(renderSize.x * 1.0 / renderSize.y), z_near, z_far);
//			 parent.camera(0,0,-600, 0,0,0, 0,1,0);
//			 runVRdrawMethod(eye);
//			 parent.g.endDraw();


//			}
			if(eye == 0){
				//runVRdrawMethod(eye);
//				fbL.beginDraw();
//				fbL.background(300);
//				fbL.image(parent.g.copy(), renderSize.x, renderSize.y);
//				fbL.endDraw();
				eyeTexture[eye].set(2, VR.EGraphicsAPIConvention.API_OpenGL, VR.EColorSpace.ColorSpace_Gamma);
				compositor.Submit.apply(eye, eyeTexture[eye], null, VR.EVRSubmitFlags.Submit_Default);
			}
			else{
				//runVRdrawMethod(eye);

				eyeTexture[eye].set(3, VR.EGraphicsAPIConvention.API_OpenGL, VR.EColorSpace.ColorSpace_Gamma);
				compositor.Submit.apply(eye, eyeTexture[eye], null, VR.EVRSubmitFlags.Submit_Default);
			}
			// set shader
			// set_shader(eye);
			// parent.shader(barrel);
			
			// parent.text(parent.frameRate,500, 500);
			// TODO store each pass to then be drawn to viewport
			//fb = gl4;
			

			
			
			// parent.resetShader();
			
		}
		//parent.g.endPGL();
		updateHMDMatrixPose();
	}

	public void SetBackground(float red, float green, float blue) {
		r = red / 255;
		g = green / 255;
		b = blue / 255;
	}

	// Draw functions ------------------------------------------------

	// Positional Tracking functions ---------------------------------

	private void updateHMDMatrixPose() {
		if (hmd == null) {
			return;
		}
		compositor.WaitGetPoses.apply(trackedDevicePosesReference, VR.k_unMaxTrackedDeviceCount, null, 0);
		//compositor.GetTrackingSpace
//		compositor.
		validPoseCount = 0;
		poseClasses = "";

		for (int device = 0; device < VR.k_unMaxTrackedDeviceCount; device++) {

			if (trackedDevicePose[device].bPoseIsValid == 1) {

				validPoseCount++;

				
				mat4DevicePose[device].set(trackedDevicePose[device].mDeviceToAbsoluteTracking);
				
				
				if (devClassChar[device] == 0) {

					switch (hmd.GetTrackedDeviceClass.apply(device)) {

					case VR.ETrackedDeviceClass.TrackedDeviceClass_Controller:
						devClassChar[device] = 'C';
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
					poseClasses += devClassChar[device];
				}
			}
		}
		if (trackedDevicePose[VR.k_unTrackedDeviceIndex_Hmd].bPoseIsValid == 1) {
			mat4DevicePose[VR.k_unTrackedDeviceIndex_Hmd].inverse(hmdPose);
		}
		//mat4DevicePose[VR.k_unTrackedDeviceIndex_Hmd].print();
	}

	private void calcCurrentViewProjectionMatrix(int eye) {
		projection[eye].mul(eyePos[eye], vp).mul(hmdPose).toDfb(matBuffer);
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
		float nearClip = 0.1f, farClip = 30.0f;
		return new Mat4(hmd.GetProjectionMatrix.apply(eye, nearClip, farClip, VR.EGraphicsAPIConvention.API_OpenGL));
	}

	private Mat4 getHmdMatrixPoseEye(int eye) {
		if (hmd == null) {
			return new Mat4();
		}
		return new Mat4(hmd.GetEyeToHeadTransform.apply(eye)).inverse();
	}

	// Positional Tracking functions ---------------------------------

	// Method initialization and use fucntion for VRdraw -------------

	// Method to run draw in MainApp
	private void runVRdrawMethod(int eye) {
		if (VRdraw) {
			try {
				VRdrawMethod.invoke(parent, new Object[] { (int) eye });
			} catch (Exception e) {
				System.out.println("VRdraw function does not exist in MainApp");
				VRdraw = false;
			}
		}
	}

	private Method getMethodRef(Object obj, String methodName, Class[] paraList) {
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
		for (int device = 0; device < VR.k_unMaxTrackedDeviceCount; device++) {

			VRControllerState_t state = new VRControllerState_t();

			if (hmd.GetControllerState.apply(device, state) != 0) {

				rbShowTrackedDevice[device] = state.ulButtonPressed == 0;

				// let's test haptic impulse too..
				if (state.ulButtonPressed != 0) {
					// apparently only axis ID 0 works, maximum duration value
					// is 3999
					hmd.TriggerHapticPulse.apply(device, 0, (short) 3999);
				}

			}
		}
		if (trackedControllerCount != trackedControllerCount_Last || validPoseCount != validPoseCount_Last) {

			validPoseCount_Last = validPoseCount;
			trackedControllerCount_Last = trackedControllerCount;

			System.out.println("PoseCount: " + validPoseCount + "(" + poseClasses + ")" + ", Controllers: "
					+ trackedControllerCount);
		}
	}

	public void processVREvent(VREvent_t event) {
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
