package org.twinone.irremote.ui;

import org.twinone.androidlib.AdMobBannerBuilder;
import org.twinone.androidlib.ShareManager;
import org.twinone.irremote.BuildConfig;
import org.twinone.irremote.R;
import org.twinone.irremote.components.Remote;
import org.twinone.irremote.ir.SignalCorrector;
import org.twinone.irremote.ir.io.HTCReceiver;
import org.twinone.irremote.ir.io.Receiver;
import org.twinone.irremote.ir.io.Transmitter;
import org.twinone.irremote.providers.common.CommonProviderActivity;
import org.twinone.irremote.providers.learn.LearnProviderActivity;
import org.twinone.irremote.ui.RenameRemoteDialog.OnRemoteRenamedListener;
import org.twinone.irremote.ui.SelectRemoteListView.OnRemoteSelectedListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements
		OnRemoteSelectedListener, OnRemoteRenamedListener {

	private static final String TAG = "MainActivity";

	public static final boolean SHOW_ADS = true;
	public static boolean DEBUG = BuildConfig.DEBUG && true;

	private NavFragment mNavFragment;

	private ViewGroup mAdViewContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!checkTransmitterAvailable() && !DEBUG) {
			showNotAvailableDialog();
		}

		SignalCorrector.setAffectedOnce(this);
		HTCReceiver.setReceiverAvailableOnce(this);

		setContentView(R.layout.activity_main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mNavFragment = (NavFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mNavFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		mNavFragment.setEdgeSizeDp(30);


	private boolean checkTransmitterAvailable() {
		final String key = "_has_ir_emitter";
		SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
		boolean available = false;
		if (sp.getBoolean(key, false)) {
			return true;
		}

		available = Transmitter.isTransmitterAvailable(this);
		sp.edit().putBoolean(key, true);
		return available;
	}

	private void showNotAvailableDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.dlg_na_tit);
		ab.setMessage(R.string.dlg_na_msg);
		ab.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		ab.setCancelable(false);
		ab.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		onRemotesChanged();
	}

	public void setRemote(String name) {
		Log.d("", "Set remote!!");
		RemoteFragment.showFor(this, name);
	}

	public String getRemoteName() {
		return mNavFragment.getSelectedRemoteName();
	}

	/**
	 * Updates the navigation fragment after a remote was selected / deleted /
	 * renamed
	 */
	public void updateRemoteLayout() {
		mNavFragment.update();
		setRemote(getRemoteName());
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		getActionBar().setTitle(title);
	}

	@Override
	public void onRemoteRenamed(String oldName, String newName) {
		// As we renamed this remote, it was selected before, so we need to
		// select it again
		Remote.setPersistedRemoteName(this, newName);
		mNavFragment.update();
	}

	@Override
	public void onRemoteSelected(int position, String remoteName) {
		Log.d("", "OnSelectedListener MainActivity");
		setRemote(remoteName);
	}

	@Override
	public void onAddRemoteSelected() {
		Intent i = new Intent(this, CommonProviderActivity.class);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.remote, menu);
		boolean hasRemote = getRemoteName() != null;
		boolean canReceive = Receiver.isAvailable(this);
		if (!hasRemote) {
			setTitle(R.string.app_name);
		}
		menu.findItem(R.id.menu_action_delete).setVisible(hasRemote);
		menu.findItem(R.id.menu_action_rename).setVisible(hasRemote);
		menu.findItem(R.id.menu_action_edit).setVisible(hasRemote);

		menu.findItem(R.id.menu_action_learn).setVisible(canReceive);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_action_delete:
			showDeleteRemoteDialog();
			return true;
		case R.id.menu_action_rename:
			RenameRemoteDialog d = RenameRemoteDialog
					.newInstance(getRemoteName());
			d.setOnRemoteRenamedListener(this);
			d.show(this);
			break;

		case R.id.menu_action_edit:
			Toast.makeText(this, "Edit will be available soon!",
					Toast.LENGTH_SHORT).show();
			break;

		case R.id.menu_action_learn:

			// Intent i = new Intent(this, EditRemoteActivity.class);
			// i.putExtra(EditRemoteActivity.EXTRA_REMOTE, getRemoteName());
			// startActivity(i);

			Intent learn = new Intent(this, LearnProviderActivity.class);
			startActivity(learn);
			break;
		case R.id.menu_action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			break;
		}
		return false;
	}

	private void showDeleteRemoteDialog() {
		final String remoteName = getRemoteName();
		if (remoteName == null)
			return;
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.delete_remote_title);
		ab.setMessage(getString(R.string.delete_remote_message, remoteName));
		ab.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Remote.remove(MainActivity.this, remoteName);
				onRemotesChanged();
			}
		});
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.show();
	}

	private void onRemotesChanged() {
		invalidateOptionsMenu();
		updateRemoteLayout();
		if (getRemoteName() == null) {
			mNavFragment.lockOpen(true);
			Log.d("", "Opened and locked!");
		} else {
			mNavFragment.unlock();
		}
	}

}
