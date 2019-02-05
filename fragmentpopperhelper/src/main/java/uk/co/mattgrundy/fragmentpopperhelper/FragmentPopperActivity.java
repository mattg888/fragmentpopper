package uk.co.mattgrundy.fragmentpopperhelper;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import timber.log.Timber;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Matthew on 10/05/17.
 */
public abstract class FragmentPopperActivity
        extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener {

    protected boolean isTopLevelActivity;
    protected boolean isLastFragment;
    private boolean shouldExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onBackPressed() {

        // pop the last fragment
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate(
                    getLastStickyItem(), 0
            );

            return;
        }

        // if we're a sub-activity, pop to parent activity
        // otherwise, start a timer for double press back to exit
        if (shouldExit || !isTopLevelActivity) {
            finish();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            shouldExit = true;

            new Timer().schedule(new UpdateExitStatus(), 2000);
        }
    }

    protected void replaceFragment(Fragment newFragment) {
        replaceFragment(newFragment, false, true);
    }

    protected void replaceFragment(Fragment newFragment, boolean tabbed) {
        replaceFragment(newFragment, tabbed, true);
    }

    protected void replaceFragmentOverwrite(Fragment newFragment) {
        replaceFragment(newFragment, false, false);
    }

    protected void replaceFragment(Fragment newFragment, boolean tabbed, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (tabbed) {
            popToRoot();

            fragmentManager
                    .beginTransaction()
                    .replace(getFragmentContainerId(), newFragment)
                    .commit();

            isLastFragment = true;

        } else {

            if (!isLastFragment) {

                popToRoot();

                fragmentManager
                        .beginTransaction()
                        .add(getFragmentContainerId(), newFragment)
                        .commit();

                isLastFragment = true;
            } else {

                Timber.d("Adding fragment: " + newFragment.getClass().toString() + " with tag: "
                        + (addToBackStack ? "sticky" + fragmentManager.getBackStackEntryCount() : "none"));

                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.enter_from_right,
                                R.anim.exit_to_left,
                                R.anim.enter_from_left,
                                R.anim.exit_to_right
                        )
                        .replace(getFragmentContainerId(), newFragment)
                        .addToBackStack(addToBackStack ? "sticky" + fragmentManager.getBackStackEntryCount() : null)
                        .commitAllowingStateLoss();
            }
        }

        onVisibleFragmentChange(newFragment);
    }

    private void onVisibleFragmentChange(Fragment newFragment) {

        if (newFragment == null) {
            newFragment = getVisibleFragment();
        }

        onVisibleFragmentChanged(newFragment);
    }

    public void onVisibleFragmentChanged(Fragment newFragment) {
        // override point for checking visible fragment
    }

    @Override
    public void onBackStackChanged() {
        updateUpNavigation();
        onVisibleFragmentChange(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUpNavigation();
    }

    private void updateUpNavigation() {
        Timber.d("Update back stack: " + getSupportFragmentManager().getBackStackEntryCount());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(
                    (!isTopLevelActivity || getSupportFragmentManager().getBackStackEntryCount() > 0)
            );
        } else {
            Timber.d("No support action bar");
        }
    }

    protected void popToRoot() {
        getSupportFragmentManager().popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        );
    }

    public Fragment getVisibleFragment() {
        return getSupportFragmentManager().findFragmentById(getFragmentContainerId());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Timber.d("Selected a menu item");

        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                Timber.d("Popping to fragment: " + getLastStickyItem());

                getSupportFragmentManager().popBackStackImmediate(
                        getLastStickyItem(), 0
                );

                return true;
            } else if (!isTopLevelActivity) {
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private String getLastStickyItem() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            for (int i = getSupportFragmentManager().getBackStackEntryCount() - 2, j = 0; i >= j; i--) {
                FragmentManager.BackStackEntry backStackEntry = getSupportFragmentManager().getBackStackEntryAt(i);

                if (backStackEntry.getName() != null && backStackEntry.getName().startsWith("sticky")) {
                    return backStackEntry.getName();
                }
            }
        }

        return null;
    }

    class UpdateExitStatus extends TimerTask {
        public void run() {
            shouldExit = false;
        }
    }

    public abstract int getFragmentContainerId();
}
