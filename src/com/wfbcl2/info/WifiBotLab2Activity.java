/*******************************************************************************
 * Copyright (c) 2012 LSIIT - Université de Strasbourg
 * Copyright (c) 2012 Erkan VALENTIN <erkan.valentin[at]unistra.fr>
 * http://www.senslab.info/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.wfbcl2.info;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WifiBotLab2Activity extends Activity implements OnClickListener, OnTouchListener, OnSeekBarChangeListener {


	private WifibotCmdSender wcs = null;
	private static String IP = "192.168.1.106";
	private static int PORT = 15020;
	private static int REFRESH_TIME = 100;
	private static int SPEED = 10;
	private Timer timer = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button btnForward = (Button) findViewById(R.id.btnForward);
		btnForward.setOnTouchListener(this);

		Button btnBackward = (Button) findViewById(R.id.btnBackward);
		btnBackward.setOnTouchListener(this);

		Button btnLeft = (Button) findViewById(R.id.btnLeft);
		btnLeft.setOnTouchListener(this);

		Button btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setOnTouchListener(this);

		Button btnRotate = (Button) findViewById(R.id.btnRotate);
		btnRotate.setOnTouchListener(this);

		ToggleButton btnConnected = (ToggleButton) findViewById(R.id.btnConnected);
		btnConnected.setOnClickListener(this);
	
		SeekBar sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
		sbSpeed.setOnSeekBarChangeListener(this);
		sbSpeed.setMax(240);
		sbSpeed.setProgress(SPEED);
		
		TextView tvState = (TextView) findViewById(R.id.tvState);
		tvState.setText("State: Disconnected");
		
		TextView tvSpeed = (TextView) findViewById(R.id.tvSpeed);
		tvSpeed.setText("Speed: " + SPEED);
		
		btnConnected.setChecked(false);
		btnForward.setEnabled(false);
		btnBackward.setEnabled(false);
		btnLeft.setEnabled(false);
		btnRight.setEnabled(false);
		btnRotate.setEnabled(false);
		sbSpeed.setEnabled(false);
		
	}
	

	@Override
	public boolean onTouch(View elem, MotionEvent event) {

		int action = event.getAction();

		if(elem.getId() == R.id.btnForward)
		{
			if (action == MotionEvent.ACTION_DOWN){
				wcs.forward(WifiBotLab2Activity.SPEED);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
		}

		if(elem.getId() == R.id.btnBackward)
		{
			if (action == MotionEvent.ACTION_DOWN){
				wcs.backward(WifiBotLab2Activity.SPEED);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
		}

		if(elem.getId() == R.id.btnLeft)
		{
			if (action == MotionEvent.ACTION_DOWN){
				wcs.direction(WifiBotLab2Activity.SPEED, true, true);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
		}

		if(elem.getId() == R.id.btnRight)
		{
			if (action == MotionEvent.ACTION_DOWN){
				wcs.direction(WifiBotLab2Activity.SPEED, false, true);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
		}

		if(elem.getId() == R.id.btnRotate)
		{
			if (action == MotionEvent.ACTION_DOWN){
				wcs.rotate(WifiBotLab2Activity.SPEED, true);
			}
			else if (action == MotionEvent.ACTION_UP){
				wcs.nothing();
			}
		}
		
		return false;
	}



	@Override
	public void onClick(View v) {
		
		Button btnForward = (Button) findViewById(R.id.btnForward);
		Button btnBackward = (Button) findViewById(R.id.btnBackward);
		Button btnLeft = (Button) findViewById(R.id.btnLeft);
		Button btnRight = (Button) findViewById(R.id.btnRight);
		Button btnRotate = (Button) findViewById(R.id.btnRotate);
		ToggleButton btnConnected = (ToggleButton) findViewById(R.id.btnConnected);
		TextView tvState = (TextView) findViewById(R.id.tvState);
		SeekBar sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);

		if(v.getId() == R.id.btnConnected){
			
			if(((ToggleButton) v).isChecked()) {
			
				
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				String ip = pref.getString("ip", WifiBotLab2Activity.IP);
				
				try {
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(ip, WifiBotLab2Activity.PORT), 1000);

					InputStream is = socket.getInputStream();
					DataInputStream dis = new DataInputStream(is);

					OutputStream out = socket.getOutputStream();
					DataOutputStream dos = new DataOutputStream(out);
					
					timer = new Timer();
					wcs = new WifibotCmdSender();
					wcs.configure(dos,dis);
					timer.scheduleAtFixedRate(wcs, 0, WifiBotLab2Activity.REFRESH_TIME);
					
					btnForward.setEnabled(true);
					btnBackward.setEnabled(true);
					btnLeft.setEnabled(true);
					btnRight.setEnabled(true);
					btnRotate.setEnabled(true);
					sbSpeed.setEnabled(true);
					tvState.setText("State: Connected");
					
				}
				catch (Exception e) {
					tvState.setText("State: " + e.getMessage());
					btnConnected.setChecked(false);
				}
			}
			else {
				btnForward.setEnabled(false);
				btnBackward.setEnabled(false);
				btnLeft.setEnabled(false);
				btnRight.setEnabled(false);
				btnRotate.setEnabled(false);
				sbSpeed.setEnabled(false);
				tvState.setText("State: Disconnected");
				if(timer != null)
					timer.cancel();
			}
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}   

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.iSettings:
			Intent i = new Intent(this, Preferences.class);
			startActivity(i);
			return true;
		case R.id.iAbout:
			try {
				PackageInfo manager = getPackageManager().getPackageInfo(getPackageName(), 0);
				Toast toast = Toast.makeText(
						this, this.getString(R.string.app_name) + " " + manager.versionName , 1000);
				toast.show();
			} catch (Exception e) {
				//
			}

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(timer != null)
			timer.cancel();
	}


	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		WifiBotLab2Activity.SPEED = seekBar.getProgress();
		TextView tvSpeed = (TextView) findViewById(R.id.tvSpeed);
		tvSpeed.setText("Speed: " + seekBar.getProgress());
		
	}
	
}