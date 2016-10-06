package ru.megazlo.apnea;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Toast;

import org.androidannotations.annotations.*;

import ru.megazlo.apnea.entity.TableApnea;
import ru.megazlo.apnea.frag.*;
import ru.megazlo.apnea.receivers.ChangeFragmentReceiver;

@EActivity(R.layout.activity_main)
public class MainAct extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private final static String FRAGMENT_TAG = "CURRENT_FRAG_TAG";

	private TableListFragment tabList = new TableListFragment_();

	@ViewById(R.id.fab)
	public FloatingActionButton fab;
	@ViewById(R.id.toolbar)
	public Toolbar toolbar;
	@ViewById(R.id.drawer_layout)
	DrawerLayout drawer;
	@ViewById(R.id.nav_view)
	NavigationView navigationView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		setTheme(R.style.AppTheme_NoActionBar);
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@AfterViews
	protected void init() {
		setSupportActionBar(toolbar);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		navigationView.setNavigationItemSelectedListener(this);
		setFragment(tabList);
		registerReceiver(fragReceiver, new IntentFilter(ChangeFragmentReceiver.ACTION_FRAGMENT));
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(fragReceiver);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		final TableApnea tab = (TableApnea) getIntent().getSerializableExtra(ApneaForeService_.TABLE_RESTORE);
		if (tab != null) {
			Toast.makeText(this, "This future not implemented", Toast.LENGTH_SHORT).show();
		}
		if (getIntent().getBooleanExtra(ApneaForeService_.IS_ALERT_SERIES_END, false)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dlg_sess_complete).setIcon(R.mipmap.ic_ico).setCancelable(false).setPositiveButton(R.string.dlg_im_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			builder.create().show();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);// нужно! тк Intent в контексте старый
	}

	@Click(R.id.fab)
	public void clickFab(View view) {
		FabClickListener listener = (FabClickListener) getVisibleFragment();
		listener.clickByContext(view);
	}

	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
			return;
		}
		BackPressHandler handler = (BackPressHandler) getVisibleFragment();
		if (!handler.backPressed()) {//если нельзя перехватывать контекст
			return;
		}
		if (tabList != getVisibleFragment()) {
			setFragment(tabList);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();// Handle navigation view item clicks here.
		if (id == R.id.nav_settings) {
			setFragment(new SettingsFragment_());
		} else if (id == R.id.nav_info) {
			setFragment(new InfoFragment_().setResRawId(R.raw.info));
		} else if (id == R.id.nav_graphs) {
			dialogOxySoon();
			//setFragment();
		} else if (id == R.id.nav_record) {
			setFragment(new RecordFragment_());
		} else if (id == R.id.nav_tables) {
			setFragment(tabList);
		}

		drawer.closeDrawer(GravityCompat.START);
		ApneaBackupHelper.requestBackup(this);// бэкапим если надо
		return true;
	}

	private void dialogOxySoon() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setView(getLayoutInflater().inflate(R.layout.dialog_oxi, null)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		b.setTitle(R.string.coming_soon_oxy);
		b.create().show();
	}

	public Fragment getVisibleFragment() {
		return getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
	}

	public void setFragment(Fragment fragment) {
		FabClickListener listener = (FabClickListener) fragment;
		listener.modifyToContext(fab);
		getFragmentManager().beginTransaction().replace(R.id.main_content, fragment, FRAGMENT_TAG).commit();
	}

	private ChangeFragmentReceiver fragReceiver = new ChangeFragmentReceiver() {
		@Override
		public void onReceive(Context context, Intent i) {
			final String fragName = i.getStringExtra(ChangeFragmentReceiver.KEY_FRAG);
			if (ChangeFragmentReceiver.KEY_DETAIL.equals(fragName)) {
				TableDetailFragment_ fragment = new TableDetailFragment_();
				fragment.setTableApnea((TableApnea) i.getSerializableExtra(ChangeFragmentReceiver.KEY_TABLE));
				setFragment(fragment);
			} else if (ChangeFragmentReceiver.KEY_EDIT.equals(fragName)) {
				TableEditorFragment_ fragment = new TableEditorFragment_();
				setFragment(fragment);
			} else if (ChangeFragmentReceiver.KEY_LIST.equals(fragName)) {
				setFragment(tabList);
			}
		}
	};
}
