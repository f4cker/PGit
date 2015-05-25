package net.typeblog.git.activities;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import net.typeblog.git.R;
import net.typeblog.git.adapters.RepoAdapter;
import net.typeblog.git.dialogs.ToolbarDialog;
import net.typeblog.git.support.RepoManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.typeblog.git.BuildConfig.DEBUG;
import static net.typeblog.git.support.Utility.$;

public class HomeActivity extends ToolbarActivity implements AdapterView.OnItemClickListener
{
	private static final String TAG = HomeActivity.class.getSimpleName();
	
	private List<String> mRepos = new ArrayList<>();
	private List<String> mRepoNames = new ArrayList<>();
	private List<String> mRepoUrls = new ArrayList<>();
	private RepoAdapter mAdapter;
	private ListView mList;
	private View mAdd;

	@Override
	protected int getLayoutResource() {
		return R.layout.main;
	}

	@Override
	protected void onInitView() {
		//new AddRepoDialog().show();
		mList = $(this, R.id.repo_list);
		mAdd = $(this, R.id.fab);
		mAdapter = new RepoAdapter(mRepoNames, mRepoUrls);
		mList.setOnItemClickListener(this);
		mList.setAdapter(mAdapter);
		reload();
		
		mAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AddRepoDialog().show();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setClass(this, RepoActivity.class);
		i.putExtra("location", mRepos.get(pos));
		i.putExtra("name", mRepoNames.get(pos));
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.committer:
				new CommitterDialog().show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void reload() {
		mRepos.clear();
		mRepoNames.clear();
		mRepoUrls.clear();
		
		RepoManager manager = RepoManager.getInstance();
		
		String[] repos = manager.getRepoLocationList();
		mRepos.addAll(Arrays.asList(repos));
		
		for (String repo : mRepos) {
			
			if (DEBUG) {
				Log.d(TAG, "processing repo " + repo);
			}
			
			if (repo.lastIndexOf("/") == repo.length() - 1) {
				repo = repo.substring(0, repo.length());
			}
			
			mRepoNames.add(repo.substring(repo.lastIndexOf("/") + 1, repo.length()));
			mRepoUrls.add(manager.getUrl(repo));
		}
		
		mAdapter.notifyDataSetChanged();
	}
	
	private class AddRepoDialog extends ToolbarDialog {
		private EditText location, url, username, password;
		
		AddRepoDialog() {
			super(HomeActivity.this);
		}

		@Override
		protected int getLayoutResource() {
			return R.layout.add_repo;
		}

		@Override
		protected void onInitView() {
			setTitle(R.string.add_repo);
			
			location = $(this, R.id.local_path);
			url = $(this, R.id.clone_url);
			username = $(this, R.id.username);
			password = $(this, R.id.password);
		}

		@Override
		protected void onConfirm() {
			String location = this.location.getText().toString();
			
			String msg = RepoManager.checkRepo(location);
			if (msg != null) {
				Toast.makeText(HomeActivity.this, msg, Toast.LENGTH_SHORT).show();
			} else {
				String url = this.url.getText().toString();
				String username = this.username.getText().toString().trim();
				String password = this.password.getText().toString().trim();
				
				RepoManager.getInstance().addRepo(location, url, username, password);
				
				dismiss();
				reload();
			}
		}
	}
	
	private class CommitterDialog extends ToolbarDialog {
		EditText name, email;
		
		CommitterDialog() {
			super(HomeActivity.this);
		}
		
		@Override
		protected int getLayoutResource() {
			return R.layout.set_committer;
		}

		@Override
		protected void onInitView() {
			setTitle(R.string.committer);
			
			name = $(this, R.id.committer_name);
			email = $(this, R.id.committer_email);
			
			name.setText(RepoManager.getInstance().getCommitterName());
			email.setText(RepoManager.getInstance().getCommitterEmail());
		}

		@Override
		protected void onConfirm() {
			RepoManager.getInstance().setCommitterIdentity(name.getText().toString().trim(), email.getText().toString().trim());
			dismiss();
		}

		
	}
}
