package it.nunzio.locationcube;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

public class MenuActivity extends FragmentActivity implements
ActionBar.TabListener, GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
LocationListener, SensorEventListener {

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	//Vettore di frammenti
	Fragment[] vFragment = new Fragment[2];


	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


	//Definizione degli intervalli di aggiornamento della Location
	private static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	private static final int FASTEST_INTERVAL_IN_SECONDS = 2;
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;


	LocationRequest mLocationRequest;
	LocationClient mLocationClient;
	Location mCurrentLocation;


	SensorManager mSensorManager;
	Sensor mSensor;

	MyMapFragment MapFragment;
	RotatingCubeFragment CubeFragment;

	static int centered = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager
		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		//Setto il LocationRequest per gli aggiornamenti
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(
				LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

		//Creo un LocationClient
		mLocationClient = new LocationClient(this, this, this);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		//Setto i Fragment
		MapFragment = new MyMapFragment();
		CubeFragment = new RotatingCubeFragment();

		vFragment[0] = MapFragment;
		vFragment[1] = CubeFragment;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	//START Activity lifecycle callbacks

	@Override
	protected void onStart() {
		super.onStart();
		//Connetto il LocationClient
		mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		if (mLocationClient.isConnected()) {
			//Interrompo gli aggiornamenti della Location
			mLocationClient.removeLocationUpdates(this);
		}
		//Disconnetto il LocationClient
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//Interrompo gli aggiornamenti della Location
		mLocationClient.removeLocationUpdates(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Riprendo gli aggiornamenti della Location
		if(mLocationClient.isConnected())
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	//START TabListener Callbacks

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		if(tab.getPosition()==1){
			if(mSensorManager!=null)
				mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			if(mSensorManager!=null)
				mSensorManager.unregisterListener(this);
		}

	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	//START LocationClient and LocationRequest Callbacks

	@Override
	public void onLocationChanged(Location location) {
		mCurrentLocation = location;
		if(centered == 0){
			if(MapFragment.mapFragment.getMap().getMyLocation()!=null){
				MapFragment.CenterCamera();
				centered=1;
			}
		}
		if(mViewPager.getCurrentItem() == 0){
			(new GetAddressTask(this)).execute(mCurrentLocation);
		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(
						this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {

	}

	//START Sensor Callbacks

	@Override
	public void onAccuracyChanged(Sensor e, int arg1) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onSensorChanged(SensorEvent e) {
		// TODO Auto-generated method stub
		if(mViewPager.getCurrentItem() == 1){
			CubeFragment.onSensorChanged(e);
		}
	}

	//END Callbacks

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode =
				GooglePlayServicesUtil.
				isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			return true;
		} else {
			// Get the error code
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			// If Google Play services can provide an error dialog
			if (dialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment =
						new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(dialog);
				// Show the error dialog in the DialogFragment
				errorFragment.show(getSupportFragmentManager(),
						"Location Updates");
			}
			return false;
		}
	}

	private void showErrorDialog(int errorCode) {

		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
				errorCode,
				this,
				CONNECTION_FAILURE_RESOLUTION_REQUEST);

		if (errorDialog != null) {

			ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			errorFragment.setDialog(errorDialog);

			errorFragment.show(getSupportFragmentManager(), "Location Updates");
		}
	}


	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return vFragment[position];
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return "Mappa";
			case 1:
				return "Cubo";
			}
			return null;
		}
	}


	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;
		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}
		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}
		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}


	private class GetAddressTask extends
	AsyncTask<Location, Void, String> {
		Context mContext;

		public GetAddressTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected String doInBackground(Location... params) {
			Geocoder geocoder =
					new Geocoder(mContext, Locale.getDefault());
			
			Location loc = params[0];
			List<Address> addresses = null;
			try {
				addresses = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch (IOException e1) {
				Log.e("LocationSampleActivity",
						"IO Exception in getFromLocation()");
				e1.printStackTrace();
				return ("IO Exception trying to get address");
			} catch (IllegalArgumentException e2) {
				String errorString = "Illegal arguments " +
						Double.toString(loc.getLatitude()) +
						" , " +
						Double.toString(loc.getLongitude()) +
						" passed to address service";
				Log.e("LocationSampleActivity", errorString);
				e2.printStackTrace();
				return errorString;
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				String addressText = String.format(
						"%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ?
								address.getAddressLine(0) : "",
								address.getLocality(),
								address.getCountryName());
				return addressText;
			} else {
				return "No address found";
			}

		}

		@Override
		protected void onPostExecute(String address) {
			if(mViewPager.getCurrentItem() == 0){
				MapFragment.setTextAddress(address);
			}
		}

	}

}
