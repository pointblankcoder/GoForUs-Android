package info.goforus.goforus;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.goforus.goforus.event_results.LogoutFromApiResult;
import info.goforus.goforus.event_results.MessageMarkReadResult;
import info.goforus.goforus.event_results.MessagesFromApiResult;
import info.goforus.goforus.event_results.NewMessagesResult;
import info.goforus.goforus.jobs.LogoutJob;
import info.goforus.goforus.jobs.GoOnlineJob;
import info.goforus.goforus.managers.OrderModeManager;
import info.goforus.goforus.managers.QuickOrderManager;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;

public class NavigationActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    ActionBarDrawerToggle mDrawerToggle;
    InboxFragment inboxFragment;
    MapFragment mapFragment;
    MessagesFragment messagesFragment;
    JobsFragment jobsFragment;
    FragmentManager mFragmentManager;
    DialogPlus mTipDialog;
    OrderModeManager orderModeManager = OrderModeManager.getInstance();

    @Bind(R.id.exitModeFab) View exitModeFab;
    @Bind(R.id.quickOrderFab) FloatingActionButton mQuickOrderFab;
    @Bind(R.id.messageFab) FloatingActionButton mMessageFab;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawer;
    @Bind(R.id.nav_view) NavigationView mNavigationView;

    public NavigationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();
        mDrawer.addDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(this);


        mTipDialog = DialogPlus.newDialog(this).setHeader(R.layout.dialog_tips_main_header)
                               .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                               .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                               .setContentHolder(new ViewHolder(R.layout.dialog_tips_main_body))
                               .setGravity(Gravity.CENTER).setCancelable(true)
                               .setOnClickListener(new OnClickListener() {
                                   @Override
                                   public void onClick(DialogPlus dialog, View view) {
                                       AppCompatCheckBox checkBox = (AppCompatCheckBox) findViewById(R.id.doNotShowTips);
                                       if (view.equals(findViewById(R.id.dismissTipDialog))) {
                                           mTipDialog.dismiss();
                                       } else if (view.equals(checkBox)) {
                                           Account account = Account.currentAccount();
                                           account.showMapTips = !checkBox.isChecked();
                                           account.save();
                                       }
                                   }
                               }).create();

        if (Account.currentAccount().showMapTips) {
            mTipDialog.show();
            AppCompatCheckBox checkBox = (AppCompatCheckBox) mTipDialog
                    .findViewById(R.id.doNotShowTips);
            checkBox.setSelected(!Account.currentAccount().showMapTips);
        }

        updateMessageFAB();

        // only create fragments if they haven't been instantiated already
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mapFragment = (MapFragment) mFragmentManager.getFragment(savedInstanceState, "Map");

            inboxFragment = (InboxFragment) mFragmentManager
                    .getFragment(savedInstanceState, "Inbox");

            messagesFragment = (MessagesFragment) mFragmentManager
                    .getFragment(savedInstanceState, "Messages");

            if (Account.currentAccount().isPartner()) {
                jobsFragment = (JobsFragment) mFragmentManager
                        .getFragment(savedInstanceState, "Jobs");
            }
            if (savedInstanceState.getBoolean("showFABs")) {
                mQuickOrderFab.show();
                mMessageFab.show();
            } else {
                mQuickOrderFab.hide();
                mMessageFab.hide();
            }
        }
        if (mapFragment == null) mapFragment = new MapFragment();
        if (inboxFragment == null) inboxFragment = new InboxFragment();
        if (messagesFragment == null) messagesFragment = new MessagesFragment();

        if (Account.currentAccount().isPartner()) {
            if (jobsFragment == null) jobsFragment = new JobsFragment();
            mNavigationView.getMenu().findItem(R.id.nav_jobs).setVisible(true);

            if (savedInstanceState == null) showJobsFragment();
        } else {
            mNavigationView.getMenu().findItem(R.id.nav_jobs).setVisible(false);
            if (savedInstanceState == null) showMapFragment();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OrderModeManager.FIND_DROP_OFF_LOCATION || requestCode == OrderModeManager.FIND_PICKUP_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                if (requestCode == OrderModeManager.FIND_PICKUP_LOCATION) {
                    orderModeManager.addPickupPoint(place.getLatLng());
                }

                if (requestCode == OrderModeManager.FIND_DROP_OFF_LOCATION) {
                    orderModeManager.addDropOffPoint(place.getLatLng());
                }
            }
        }
        if (resultCode == RESULT_CANCELED) {
            orderModeManager.explainToUserAboutLongPress();
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @OnClick(R.id.messageFab)
    public void onMessageFabClick() {
        showInboxFragment();
    }

    @OnClick(R.id.quickOrderFab)
    public void onQuickOrderFabClick() {
        DialogPlus quickOrderDialog = DialogPlus.newDialog(this)
                                                .setContentHolder(new ViewHolder(R.layout.dialog_quick_order))
                                                .setGravity(Gravity.TOP).setCancelable(true)
                                                .setContentBackgroundResource(R.color.primary_material_dark_1)
                                                .setOnDismissListener(new OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogPlus dialog) {
                                                        orderModeManager.showTips();
                                                    }
                                                }).setOnClickListener(new QuickOrderManager(this))
                                                .create();
        quickOrderDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        FragmentManager sfm = getSupportFragmentManager();

        if (mapFragment.isAdded())
            sfm.putFragment(savedInstanceState, "Map", sfm.findFragmentByTag("Map"));
        if (inboxFragment.isAdded())
            sfm.putFragment(savedInstanceState, "Inbox", sfm.findFragmentByTag("Inbox"));
        if (messagesFragment.isAdded())
            sfm.putFragment(savedInstanceState, "Messages", sfm.findFragmentByTag("Messages"));
        if (jobsFragment != null && jobsFragment.isAdded())
            sfm.putFragment(savedInstanceState, "Jobs", sfm.findFragmentByTag("Jobs"));

        savedInstanceState
                .putBoolean("showFABs", (mMessageFab.isShown() || mQuickOrderFab.isShown()));

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (messagesFragment.isVisible()) {
                showInboxFragment();
            } else if (inboxFragment.isVisible()) {
                showMapFragment();
            } else if (mapFragment.isVisible()) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);

        TextView accountNameView = (TextView) findViewById(R.id.accountName);
        if (accountNameView != null) {
            accountNameView.setText(Account.currentAccount().name);
        }
        TextView accountEmailView = (TextView) findViewById(R.id.accountEmailAddress);
        if (accountEmailView != null) {
            accountEmailView.setText(Account.currentAccount().email);
        }

        MenuItem onlineSwitchActionItem = menu.findItem(R.id.action_switch);
        if (onlineSwitchActionItem != null) {

            if (Account.currentAccount().isPartner()) {
                onlineSwitchActionItem.setVisible(true);

                Switch onlineSwitch = (Switch) onlineSwitchActionItem.getActionView();
                onlineSwitch
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                mGoForUs.getJobManager()
                                        .addJobInBackground(new GoOnlineJob(isChecked));
                            }
                        });

                onlineSwitch.setChecked(Account.currentAccount().available && Account
                        .currentAccount().online);
            } else {
                onlineSwitchActionItem.setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                mGoForUs.getJobManager().addJobInBackground(new LogoutJob());
                break;
            case R.id.action_tips:
                mTipDialog.show();
                AppCompatCheckBox checkBox = (AppCompatCheckBox) mTipDialog
                        .findViewById(R.id.doNotShowTips);
                checkBox.setChecked(!Account.currentAccount().showMapTips);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            showMapFragment();
        } else if (id == R.id.nav_jobs) {
            showJobsFragment();
        } else if (id == R.id.nav_inbox) {
            showInboxFragment();
        } else if (id == R.id.nav_settings) {
            Snackbar.make(getWindow()
                    .getDecorView(), "Settings are not complete", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        item.setChecked(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* =================== Fragment Management =========== */

    public void showInboxFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();

        if (inboxFragment.isAdded()) {
            ft.show(inboxFragment);
        } else {
            ft.add(R.id.content, inboxFragment, "Inbox");
        }

        if (messagesFragment.isAdded()) ft.hide(messagesFragment);
        if (mapFragment.isAdded()) ft.hide(mapFragment);
        if (jobsFragment != null && jobsFragment.isAdded()) ft.hide(jobsFragment);
        mMessageFab.hide();
        mQuickOrderFab.hide();

        mNavigationView.setCheckedItem(R.id.nav_inbox);
        setTitle("Inbox");
        ft.commit();
    }

    public void showMessagesFragment(Conversation conversation) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        MessagesFragment.mConversation = conversation;

        if (messagesFragment.isAdded()) {
            ft.show(messagesFragment);
        } else {
            ft.add(R.id.content, messagesFragment, "Messages");
        }

        if (inboxFragment.isAdded()) ft.hide(inboxFragment);
        if (mapFragment.isAdded()) ft.hide(mapFragment);
        if (jobsFragment != null && jobsFragment.isAdded()) ft.hide(jobsFragment);
        mMessageFab.hide();
        mQuickOrderFab.hide();

        mNavigationView.setCheckedItem(R.id.nav_inbox);
        setTitle(getString(R.string.messages_fragment_title));
        ft.commit();
    }


    private void showMapFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();

        if (mapFragment.isAdded()) {
            ft.show(mapFragment);
        } else {
            ft.add(R.id.content, mapFragment, "Map");
        }
        if (inboxFragment.isAdded()) ft.hide(inboxFragment);
        if (messagesFragment.isAdded()) ft.hide(messagesFragment);
        if (jobsFragment != null && jobsFragment.isAdded()) ft.hide(jobsFragment);
        if (!mMessageFab.isShown()) mMessageFab.show();
        if (!mQuickOrderFab.isShown() && mapFragment.mapMode != MapFragment.ORDER_MODE)
            mQuickOrderFab.show();

        mNavigationView.setCheckedItem(R.id.nav_map);
        setTitle(getString(R.string.map_fragment_title));
        ft.commit();
    }

    private void showJobsFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();

        if (jobsFragment.isAdded()) {
            ft.show(jobsFragment);
        } else {
            ft.add(R.id.content, jobsFragment, "Jobs");
        }
        if (inboxFragment.isAdded()) ft.hide(inboxFragment);
        if (messagesFragment.isAdded()) ft.hide(messagesFragment);
        if (mapFragment.isAdded()) ft.hide(mapFragment);
        mMessageFab.show();
        mQuickOrderFab.hide();

        mNavigationView.setCheckedItem(R.id.nav_jobs);
        setTitle(getString(R.string.jobs_fragment_title));
        ft.commit();
    }

    int lastMessageCount = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyNewMessage(NewMessagesResult result) {
        lastMessageCount = result.getNewMessages().size();
        YoYo.with(Techniques.Shake).duration(2000).playOn(mMessageFab);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutConfirmed(LogoutFromApiResult response) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateMessageFAB() {
        int totalUnreadCount = Conversation.totalUnreadMessagesCount();
        if (totalUnreadCount > 0) {
            mMessageFab.setImageDrawable(ContextCompat
                    .getDrawable(this, R.drawable.ic_mail_white_24dp));
            YoYo.with(Techniques.Pulse).duration(1000).playOn(mMessageFab);
        } else {
            mMessageFab.setImageDrawable(ContextCompat
                    .getDrawable(this, R.drawable.ic_drafts_white_24dp));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessagesUpdate(MessagesFromApiResult result) {
        updateMessageFAB();
        int totalUnreadCount = Conversation.totalUnreadMessagesCount();
        if (result.getMessages().size() > 0 && totalUnreadCount > 0) Toast.makeText(this, String
                .format("You have %s new message%s", totalUnreadCount, totalUnreadCount == 1 ? "" : "s"), Toast.LENGTH_LONG)
                                                                          .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageRead(MessageMarkReadResult result) { updateMessageFAB(); }

    @Override
    public void onStart() { super.onStart(); }

    @Override
    public void onStop() { super.onStop(); }
}
