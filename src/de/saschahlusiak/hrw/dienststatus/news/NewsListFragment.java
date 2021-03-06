package de.saschahlusiak.hrw.dienststatus.news;


import java.util.ArrayList;
import de.saschahlusiak.hrw.dienststatus.R;
import de.saschahlusiak.hrw.dienststatus.model.NewsItem;
import de.saschahlusiak.hrw.dienststatus.model.NewsProvider;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NewsListFragment extends ListFragment implements OnItemClickListener {
	NewsAdapter adapter;
	ListView list;
	
	private static final String WEBSITE = "http://www.hs-weingarten.de/web/rechenzentrum/aktuelles";
	private static final String tag = NewsListFragment.class.getSimpleName();

	public interface OnNewsClicked {
		public void onNewsDetails(NewsListFragment fragment, NewsItem item);
	}
	
	OnNewsClicked mListener;
	Menu optionsMenu;
	View mRefreshIndeterminateProgressView;
	
	public class RefreshTask extends AsyncTask<Void, NewsItem, String> implements NewsProvider.OnNewNewsItem {
		ArrayList<NewsItem> mynodes = new ArrayList<NewsItem>();
		
		@Override
		protected void onPreExecute() {
			setProgressActionView(true);
			adapter.setNodes(mynodes);
			adapter.notifyDataSetChanged();
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Void... args) {
			return NewsProvider.fetchNews(getActivity(), this);
		}
		
		@Override
		protected void onProgressUpdate(NewsItem... values) {
			mynodes.add(values[0]);
			adapter.notifyDataSetChanged();
			setProgressActionView(false);
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onCancelled() {
			if (getActivity() != null)
				Toast.makeText(getActivity(), getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
			setProgressActionView(false);
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && getActivity() != null)
				Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
			setProgressActionView(false);
			super.onPostExecute(result);
		}

		@Override
		public void onNewNewsItem(NewsItem item) {
			publishProgress(item);
		}
	}
	
	RefreshTask task = null;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter =  new NewsAdapter(getActivity());
		setListAdapter(adapter);
		setHasOptionsMenu(true);
		
		if (savedInstanceState != null) {
		}		

	}
	
	@Override
	public void onStart() {
		task = new RefreshTask();
		task.execute();
		super.onStart();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.mListener = (OnNewsClicked) getActivity();

		getListView().setOnItemClickListener(this);
	//	registerForContextMenu(getListView());
		
		getActivity().getActionBar().setTitle(R.string.news);
		getActivity().getActionBar().setSubtitle(null);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getActivity().getActionBar().setHomeButtonEnabled(false);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
		NewsItem news;

		news = (NewsItem) adapter.getItem(position);
		mListener.onNewsDetails(this, news);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.optionsmenu_newslist, menu);
		optionsMenu = menu;
		if (task != null && task.getStatus() == Status.RUNNING)
			setProgressActionView(true);
		
		ShareActionProvider share = (ShareActionProvider) menu.findItem(R.id.menu_item_share).getActionProvider();
		
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, WEBSITE);		
		share.setShareIntent(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.refresh) {
			if (task != null) {
				task.cancel(false);
				try {
					task.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			task = new RefreshTask();
			task.execute();
			return true;
		}
		if (item.getItemId() == R.id.gotowebsite) {
			Intent intent = new Intent(
					"android.intent.action.VIEW",
					Uri.parse(WEBSITE));
			startActivity(intent);
			return true;
		}
		if (item.getItemId() == R.id.sendemail) {
			try {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.rz_service_email) });
				intent.putExtra(Intent.EXTRA_SUBJECT, "Frage an das Rechenzentrum");
				intent.putExtra(Intent.EXTRA_TEXT, "Siehe: " + WEBSITE);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	void setProgressActionView(boolean refreshing) {
		if (optionsMenu == null)
			return;
        final MenuItem refreshItem = optionsMenu.findItem(R.id.refresh);
        if (refreshItem != null) {
            if (refreshing) {
                if (mRefreshIndeterminateProgressView == null) {
                    LayoutInflater inflater = (LayoutInflater)
                            getActivity().getSystemService(
                                    Context.LAYOUT_INFLATER_SERVICE);
                    mRefreshIndeterminateProgressView = inflater.inflate(
                            R.layout.actionbar_indeterminate_progress, null);
                }

                refreshItem.setActionView(mRefreshIndeterminateProgressView);
            } else {
                refreshItem.setActionView(null);
            }
        }
	}	
}
