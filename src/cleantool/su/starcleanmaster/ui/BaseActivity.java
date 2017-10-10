package cleantool.su.starcleanmaster.ui;

import android.app.Activity;
import android.view.MenuItem;

public abstract class BaseActivity extends Activity {

    protected abstract void initViews();

    protected abstract void initValues();

    protected abstract void initListeners();

    protected void setActionbar(boolean showBackBtn) {
        getActionBar().setDisplayHomeAsUpEnabled(showBackBtn);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

}