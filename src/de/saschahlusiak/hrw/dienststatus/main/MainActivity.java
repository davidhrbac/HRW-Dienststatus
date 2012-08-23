package de.saschahlusiak.hrw.dienststatus.main;

import de.saschahlusiak.hrw.dienststatus.R;
import de.saschahlusiak.hrw.dienststatus.dienstdetails.DetailFragment;
import de.saschahlusiak.hrw.dienststatus.dienste.DienststatusFragment;
import de.saschahlusiak.hrw.dienststatus.dienste.DienststatusFragment.OnNodeClicked;
import de.saschahlusiak.hrw.dienststatus.model.HRWNode;
import de.saschahlusiak.hrw.dienststatus.model.NewsItem;
import de.saschahlusiak.hrw.dienststatus.news.NewsItemFragment;
import de.saschahlusiak.hrw.dienststatus.news.NewsListFragment;
import de.saschahlusiak.hrw.dienststatus.news.NewsListFragment.OnNewsClicked;
import de.saschahlusiak.hrw.dienststatus.preferences.DienststatusPreferencesActivity;
import de.saschahlusiak.hrw.dienststatus.statistic.StatisticsFragment;
import de.saschahlusiak.hrw.dienststatus.statistic.StatisticsFragment.OnStatisticClicked;
import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends Activity implements OnNodeClicked, OnStatisticClicked, OnNewsClicked {
	static final String tag = MainActivity.class.getSimpleName();
	boolean isCreated = false;
	
	abstract class MyTabListener implements TabListener {
		Fragment f;
		String tag;

		MyTabListener(String tag) {
			this.tag = tag;

			f = getFragmentManager().findFragmentById(android.R.id.content);
            if (f != null && !f.isDetached() && f.getTag().equals(tag)) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(f);
                ft.commit();
            } else
            	f = null;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (!isCreated && f == null)
					return;
			
			if (f == null) {
				f = createFragment();
				ft.replace(android.R.id.content, f, tag);
			} else {
				ft.attach(f);
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (f == null)
				return;
			FragmentManager fm = getFragmentManager();
			if (fm.getBackStackEntryCount() > 0)
				fm.popBackStack(fm.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
			getActionBar().setDisplayHomeAsUpEnabled(false);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				getActionBar().setHomeButtonEnabled(false);
			}
			
			f = null;
		}
		
		abstract Fragment createFragment();
	}
	
	class DienststatusTabListener extends MyTabListener {
		boolean flat;
		public DienststatusTabListener(boolean flat) {
			super(flat ? "warning" : "all");
			this.flat = flat;
		}
		
		@Override
		Fragment createFragment() {
			return new DienststatusFragment(flat ? null : "all");
		}
	}
	
	class StatisticsTabListener extends MyTabListener {
		public StatisticsTabListener() {
			super("statistics");
		}

		@Override
		Fragment createFragment() {
			return new StatisticsFragment();
		}
	}

	class NewsTabListener extends MyTabListener {
		public NewsTabListener() {
			super("news");
		}

		@Override
		Fragment createFragment() {
			return new NewsListFragment();
		}
		
	}	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		getActionBar().setDisplayShowTitleEnabled(true);
//		getActionBar().setSubtitle("huhu");
		
		if (savedInstanceState == null)
			isCreated = true;
		
		getActionBar().addTab(getActionBar().newTab()
				.setText(R.string.tab_all)
				.setTabListener(new DienststatusTabListener(false)));
		getActionBar().addTab(getActionBar().newTab()
				.setText(R.string.tab_warnings)
				.setTabListener(new DienststatusTabListener(true)));
		getActionBar().addTab(getActionBar().newTab()
				.setText(R.string.tab_statistics)
				.setTabListener(new StatisticsTabListener()));
		getActionBar().addTab(getActionBar().newTab()
				.setText(R.string.news)
				.setTabListener(new NewsTabListener()));

		getActionBar().setDisplayHomeAsUpEnabled(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getActionBar().setHomeButtonEnabled(false);
		}
		isCreated = true;
		
		if (savedInstanceState != null) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
	}
	
	@Override
	protected void onStart() {
		setProgressBarIndeterminateVisibility(false);
		super.onStart();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.optionsmenu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			FragmentManager fragmentManager = getFragmentManager();			
			fragmentManager.popBackStack();
			break;
		case R.id.preferences:
			Intent intent = new Intent(this, DienststatusPreferencesActivity.class);
			startActivity(intent);
			break;
		
		default: return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onNodeDetails(DienststatusFragment fragment, HRWNode node) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(
				R.animator.fragment_slide_left_enter,
                R.animator.fragment_slide_left_exit /*,
                R.animator.fragment_slide_right_enter,
                R.animator.fragment_slide_right_exit */);
		fragmentTransaction.replace(android.R.id.content, new DetailFragment(node.id), fragment.getTag());
		fragmentTransaction.addToBackStack(node.getParentId());
		fragmentTransaction.commit();		
	}

	@Override
	public void onNodeClicked(DienststatusFragment fragment, HRWNode node) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(
				R.animator.fragment_slide_left_enter,
                R.animator.fragment_slide_left_exit /*,
                R.animator.fragment_slide_right_enter,
                R.animator.fragment_slide_right_exit */);
		fragmentTransaction.replace(android.R.id.content, new DienststatusFragment(node), fragment.getTag());
		Log.v(tag, "pushing new " + node.getParentId());
		fragmentTransaction.addToBackStack(node.getParentId());
		fragmentTransaction.commit();
	}

	@Override
	public void onStatisticClicked(StatisticsFragment fragment, int category) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(
				R.animator.fragment_slide_left_enter,
                R.animator.fragment_slide_left_exit /*,
                R.animator.fragment_slide_right_enter,
                R.animator.fragment_slide_right_exit */);
		fragmentTransaction.replace(android.R.id.content, new StatisticsFragment(category), fragment.getTag());
//		Log.v(tag, "pushing new " + node.getParentId());
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onNewsDetails(NewsListFragment fragment, NewsItem item) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(
				R.animator.fragment_slide_left_enter,
                R.animator.fragment_slide_left_exit /*,
                R.animator.fragment_slide_right_enter,
                R.animator.fragment_slide_right_exit */);
		fragmentTransaction.replace(android.R.id.content, new NewsItemFragment(item), fragment.getTag());
//		Log.v(tag, "pushing new " + node.getParentId());
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
}
