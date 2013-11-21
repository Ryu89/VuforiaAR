package it.nunzio.locationcube;

import java.util.Timer;
import java.util.TimerTask;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class RotatingCubeFragment extends Fragment {
	
	static View root;
	static CubeView vCube;
	static TextView sensorData;
	
	static Timer timer;
	static float e1,e2,e3,previous_e1,previous_e2;
	static int initialized=0;
	float gravity[]=new float[3];

	public RotatingCubeFragment() {
		// Required empty public constructor
	}
	
	public RotatingCubeFragment newInstance(int title, String message)
	{
		RotatingCubeFragment fragment = new  RotatingCubeFragment();
	    Bundle bundle = new Bundle(2);
	    bundle.putInt("EXTRA_TITLE", title);
	    bundle.putString("EXTRA_MESSAGE", message);
	    fragment.setArguments(bundle);
	    return fragment ;
	}
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		timer = new Timer();
		timer.scheduleAtFixedRate(new UpdateFrame(), 33, 33);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		root = inflater.inflate(R.layout.fragment_rotating_cube, container,
				false);
		vCube = (CubeView)root.findViewById(R.id.cube_view);
		vCube.requestFocus();
		
		sensorData = (TextView)root.findViewById(R.id.sensor_data);
		sensorData.setText("Sensor Data:\nx:--\ny:--\nz:--");
		return root;
	}
	
	@Override
    public void onResume() {
        super.onResume();
        vCube.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        vCube.onPause();
    }
	
	public void onSensorChanged(SensorEvent e){
		final float alpha = 0.8f;

		gravity[0] = alpha * gravity[0] + (1 - alpha) * e.values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * e.values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * e.values[2];

		e1 = e.values[0] - gravity[0];
		e2 = e.values[1] - gravity[1];
		e3 = e.values[2] - gravity[2];
		
		sensorData.setText("Sensor Data:\nx:"+Float.toString(e1)+"\ny:"+Float.toString(e2)+"\nz:"+Float.toString(e3));
	}
	
	class UpdateFrame extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(vCube!=null && initialized==0){
			vCube.mRenderer.mAngleX += e1* vCube.TRACKBALL_SCALE_FACTOR;
			vCube.mRenderer.mAngleY += e2* vCube.TRACKBALL_SCALE_FACTOR;
			previous_e1=e1;
			previous_e2=e2;
			initialized=1;
			vCube.requestRender();}
			else {
				if(vCube!=null && ((previous_e1-e1)>0.1 || (previous_e1-e1)<-0.1)) {
					vCube.mRenderer.mAngleX += e1* vCube.TRACKBALL_SCALE_FACTOR;
				}
				if(vCube!=null && ((previous_e2-e2)>0.1 || (previous_e2-e2)<-0.1)) {
					vCube.mRenderer.mAngleY += e2* vCube.TRACKBALL_SCALE_FACTOR;
				} 
				previous_e1=e1;
				previous_e2=e2;
				if(vCube!=null)
					vCube.requestRender();
			}
		}
		
	}

}
