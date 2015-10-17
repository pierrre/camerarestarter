package camerarestarter.exlyo.com.camerarestarter;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.restart_camera_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				try {
					runRestartCameraShellCommand();
					showToastMessage(MainActivity.this, getString(R.string.camera_restared_successfully));
				} catch (Throwable t) {
					t.printStackTrace();
					showToastMessage(MainActivity.this, getString(R.string.camera_restart_failed, t.getMessage()));
				}
			}
		});
		findViewById(R.id.help_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				showMessageDialog(MainActivity.this, getString(R.string.help_message));
			}
		});
	}

	private void runRestartCameraShellCommand() throws Throwable {
		final Process p = Runtime.getRuntime().exec("su");
		final DataOutputStream os = new DataOutputStream(p.getOutputStream());
		final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		{
			os.writeBytes("ps|grep media" + "\n");
			String line = br.readLine();
			while (line != null) {
				if (line.startsWith("media") && line.endsWith("/system/bin/mediaserver")) {
					line = line.replaceAll("media[ ]*", "");
					final int pid = Integer.parseInt(line.substring(0, line.indexOf(" ")));
					os.writeBytes("kill " + pid + "\n");
					line = br.readLine();
					if (line == null) {
						throw new RuntimeException("FAIL");
					}
					return;
				}

				line = br.readLine();
			}
		}
		os.writeBytes("exit\n");
		os.flush();
	}

	private static void showToastMessage(@NonNull final Activity _activity, final String _messageText) {
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(_activity, _messageText, Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	private static void showMessageDialog(@NonNull final Activity _activity, final String _messageText) {
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
				builder.setMessage(_messageText);
				builder.setPositiveButton(R.string.ok, null);
				final AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
	}
}