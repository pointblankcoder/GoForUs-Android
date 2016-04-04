package info.goforus.goforus;

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
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.goforus.goforus.event_results.LogoutFromApiResult;
import info.goforus.goforus.event_results.NewMessagesResult;
import info.goforus.goforus.jobs.AttemptLogoutJob;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;

public class NavigationActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    ActionBarDrawerToggle mDrawerToggle;
    @Bind(R.id.messageFab) FloatingActionButton mMessageFab;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawer;
    InboxFragment inboxFragment;
    MapFragment mapFragment;
    MessagesFragment messagesFragment;
    FragmentManager mFragmentManager;

    DialogPlus mTipDialog;
    @Bind(R.id.nav_view) NavigationView mNavigationView;

    public NavigationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        if (mMessageFab != null) {
            mMessageFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInboxFragment();
                }
            });
            if (Conversation.totalUnreadMessagesCount() > 0) {
                mMessageFab.setImageDrawable(ContextCompat
                        .getDrawable(this, R.drawable.ic_mail_white_24dp));
            }
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();
        mDrawer.addDrawerListener(mDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(this);


        mTipDialog = DialogPlus.newDialog(this).setHeader(R.layout.dialog_tips_header)
                               .setContentHolder(new ViewHolder(R.layout.dialog_tips_body))
                               .setFooter(R.layout.dialog_tips_footer).setGravity(Gravity.CENTER)
                               .setCancelable(true)
                               .setOnClickListener(new OnClickListener() {
                                   @Override
                                   public void onClick(DialogPlus dialog, View view) {
                                       AppCompatCheckBox checkBox = (AppCompatCheckBox) findViewById(R.id.doNotShowTips);
                                       if(view.equals(findViewById(R.id.dismissTipDialog))){
                                           mTipDialog.dismiss();
                                       } else if (view.equals(checkBox)){
                                           Account account = Account.currentAccount();
                                           account.showTips = !checkBox.isChecked();
                                           account.save();
                                       }
                                   }
                               }).create();

        if (Account.currentAccount().showTips) {
            mTipDialog.show();
            AppCompatCheckBox checkBox = (AppCompatCheckBox) mTipDialog.findViewById(R.id.doNotShowTips);
            checkBox.setSelected(!Account.currentAccount().showTips);
        }




        // only create fragments if they haven't been instantiated already
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mapFragment = (MapFragment) mFragmentManager.getFragment(savedInstanceState, "Map");
            inboxFragment = (InboxFragment) mFragmentManager
                    .getFragment(savedInstanceState, "Inbox");
            messagesFragment = (MessagesFragment) mFragmentManager
                    .getFragment(savedInstanceState, "Messages");
            if (savedInstanceState.getBoolean("mMessageFabShown")) {
                mMessageFab.show();
            } else {
                mMessageFab.hide();
            }
        }
        if (mapFragment == null) mapFragment = new MapFragment();
        if (inboxFragment == null) inboxFragment = new InboxFragment();
        if (messagesFragment == null) messagesFragment = new MessagesFragment();
        if (savedInstanceState == null) showMapFragment();
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

        savedInstanceState.putBoolean("mMessageFabShown", mMessageFab.isShown());

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                mApplication.getJobManager().addJobInBackground(new AttemptLogoutJob());
                break;
            case R.id.action_tips:
                mTipDialog.show();
                AppCompatCheckBox checkBox = (AppCompatCheckBox) mTipDialog.findViewById(R.id.doNotShowTips);
                checkBox.setChecked(!Account.currentAccount().showTips);
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
        if (messagesFragment.isAdded()) {
            ft.hide(messagesFragment);
        }
        if (mapFragment.isAdded()) {
            ft.hide(mapFragment);
        }

        if (mMessageFab.isShown()) {
            mMessageFab.hide();
        }

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

        if (inboxFragment.isAdded()) {
            ft.hide(inboxFragment);
        }
        if (mapFragment.isAdded()) {
            ft.hide(mapFragment);
        }

        if (mMessageFab.isShown()) {
            mMessageFab.hide();
        }

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
        if (inboxFragment.isAdded()) {
            ft.hide(inboxFragment);
        }
        if (messagesFragment.isAdded()) {
            ft.hide(messagesFragment);
        }

        if (!mMessageFab.isShown()) {
            mMessageFab.show();
        }

        mNavigationView.setCheckedItem(R.id.nav_map);
        setTitle(getString(R.string.map_fragment_title));
        ft.commit();
    }


    /* =================== Api Callbacks ================= */ int lastMessageCount = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyNewMessage(NewMessagesResult result) {
        lastMessageCount = result.getNewMessages().size();
        Toast.makeText(NavigationActivity.this, String
                .format("You have %s new messages.", lastMessageCount), Toast.LENGTH_LONG).show();
        YoYo.with(Techniques.Shake).duration(2000).playOn(mMessageFab);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutConfirmed(LogoutFromApiResult response) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
