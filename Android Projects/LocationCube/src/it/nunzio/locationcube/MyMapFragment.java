package it.nunzio.locationcube;

import java.util.Map;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MyMapFragment extends Fragment {

	static View root;
	static TextView tAddress;
	public static MFragment mapFragment;
	static MapView vMap;

	public MyMapFragment() {

	}

	public MyMapFragment newInstance(int title, String message)
	{
		MyMapFragment fragment = new MyMapFragment();
		Bundle bundle = new Bundle(2);
		bundle.putInt("EXTRA_TITLE", title);
		bundle.putString("EXTRA_MESSAGE", message);
		fragment.setArguments(bundle);
		return fragment ;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		root = inflater.inflate(R.layout.fragment_my_map, container, false);
		mapFragment = new MFragment();
		FragmentTransaction ts = getChildFragmentManager().beginTransaction();
		ts.replace(R.id.fragment1, mapFragment);
		ts.commit();
		tAddress = (TextView) root.findViewById(R.id.address);
		tAddress.setText("Current Address: --");
		return root;
	}

	public void CenterCamera(){
		GoogleMap map = mapFragment.getMap();
		if(map.getMyLocation()!=null){
			LatLng position = new LatLng(mapFragment.getMap().getMyLocation().getLatitude(), mapFragment.getMap().getMyLocation().getLongitude());
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16));
		}
	}

	public void setTextAddress(String address){
		tAddress = (TextView) root.findViewById(R.id.address);
		tAddress.setText("Current Address:\n" + address);
	}
	
	public static class MFragment extends SupportMapFragment{
		
		public MFragment() {
			
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			//GoogleMap map = getMap();
			if (this.getMap() != null) {
				this.getMap().setMyLocationEnabled(true);
				this.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
			}
		}
		
	}

}
