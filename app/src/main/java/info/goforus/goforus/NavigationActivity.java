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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.goforus.goforus.event_results.LogoutFromApiResult;
import info.goforus.goforus.event_results.MessageMarkReadResult;
import info.goforus.goforus.event_results.MessagesFromApiResult;
import info.goforus.goforus.event_results.NewMessagesResult;
import info.goforus.goforus.jobs.AttemptLogoutJob;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.drivers.Driver;

public class NavigationActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int FIND_PICKUP_LOCATION = 0;
    private static final int FIND_DROPOFF_LOCATION = 1;

    ActionBarDrawerToggle mDrawerToggle;
    InboxFragment inboxFragment;
    MapFragment mapFragment;
    MessagesFragment messagesFragment;
    FragmentManager mFragmentManager;
    DialogPlus mTipDialog;
    DriversOnMapManager driversOnMapManager = DriversOnMapManager.getInstance();

    @Bind(R.id.exitModeFab) View exitModeFab;
    @Bind(R.id.quickOrderFab) FloatingActionButton mQuickOrderFab;
    @Bind(R.id.messageFab) FloatingActionButton mMessageFab;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawer;
    @Bind(R.id.nav_view) NavigationView mNavigationView;
    @Bind(R.id.quickLocationSelection) LinearLayout quickLocationSelection;
    @Bind(R.id.findDropOff) View findDropOff;
    @Bind(R.id.removeDropOff) View removeDropOff;
    @Bind(R.id.findPickup) View findPickup;
    @Bind(R.id.removePickup) View removePickup;
    @Bind(R.id.complete) View complete;

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
        if (savedInstanceState == null) showMapFragment();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FIND_DROPOFF_LOCATION || requestCode == FIND_PICKUP_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                if (requestCode == FIND_PICKUP_LOCATION) {
                    Marker marker = mapFragment.mMap
                            .addMarker(new MarkerOptions().position(place.getLatLng())
                                                          .title("Pickup Point").draggable(true)
                                                          .icon(BitmapDescriptorFactory
                                                                  .fromResource(R.drawable.ic_nature_black_36dp)));
                    mapFragment.pickupPoints.add(marker);
                    CameraUpdate cameraUpdate = CameraUpdateFactory
                            .newLatLngZoom(marker.getPosition(), 15);
                    mapFragment.mMap.animateCamera(cameraUpdate, 1, null);
                    mapFragment.dropPinEffect(marker);

                    Toast.makeText(this, "Pickup point added", Toast.LENGTH_SHORT).show();

                    findPickup.setVisibility(View.GONE);
                    removePickup.setVisibility(View.VISIBLE);
                }

                if (requestCode == FIND_DROPOFF_LOCATION) {
                    Marker marker = mapFragment.mMap
                            .addMarker(new MarkerOptions().position(place.getLatLng())
                                                          .title("Dropoff Point").draggable(true)
                                                          .icon(BitmapDescriptorFactory
                                                                  .fromResource(R.drawable.ic_person_pin_circle_black_36dp)));
                    mapFragment.dropOffPoints.add(marker);
                    CameraUpdate cameraUpdate = CameraUpdateFactory
                            .newLatLngZoom(marker.getPosition(), 15);
                    mapFragment.mMap.animateCamera(cameraUpdate, 1, null);
                    mapFragment.dropPinEffect(marker);

                    Toast.makeText(this, "Dropoff point added", Toast.LENGTH_SHORT).show();

                    findDropOff.setVisibility(View.GONE);
                    removeDropOff.setVisibility(View.VISIBLE);
                }

                if (mapFragment.pickupPoints.size() == 1 && mapFragment.dropOffPoints.size() == 1) {
                    View complete = findViewById(R.id.complete);
                    if (complete != null) complete.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @OnClick(R.id.exitModeFab)
    public void onExitModeClick() {
        exitModeFab.setVisibility(View.GONE);
        driversOnMapManager.setSelectedDriver(null);
        driversOnMapManager.hideAllDrivers(false);
        driversOnMapManager.unblockIndicators();
        for (Driver d : driversOnMapManager.getCurrentlyDisplayedDrivers()) {
            d.marker.remove();
        }
        for (Driver d : driversOnMapManager.getCurrentlyDisplayedDrivers()) {
            d.addToMap(mapFragment.mMap);
        }

        for (Marker m : mapFragment.pickupPoints) {
            m.remove();
        }
        for (Marker m : mapFragment.dropOffPoints) {
            m.remove();
        }

        mapFragment.pickupPoints = new ArrayList<>();
        mapFragment.dropOffPoints = new ArrayList<>();

        quickLocationSelection.setVisibility(View.GONE);

        Toast.makeText(this, "You have cancelled your order", Toast.LENGTH_LONG).show();

        mapFragment.switchMapMode(MapFragment.BROWSE_MODE);
    }

    @OnClick(R.id.complete)
    public void onCompleteClick() {
        quickLocationSelection.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
    }

    @OnClick(R.id.removePickup)
    public void onRemovePickupClick() {
        findPickup.setVisibility(View.VISIBLE);
        removePickup.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
        for (Marker m : mapFragment.pickupPoints)
            m.remove();
        mapFragment.pickupPoints = new ArrayList<>();
    }

    @OnClick(R.id.findPickup)
    public void onFindPickupClick() {
        openLocationFinder(FIND_PICKUP_LOCATION);
    }


    @OnClick(R.id.removeDropOff)
    public void onRemoveDropOffClick() {
        findDropOff.setVisibility(View.VISIBLE);
        removeDropOff.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
        for (Marker m : mapFragment.dropOffPoints)
            m.remove();
        mapFragment.dropOffPoints = new ArrayList<>();
    }

    @OnClick(R.id.findDropOff)
    public void onFindDropOffClick() {
        openLocationFinder(FIND_DROPOFF_LOCATION);
    }

    public void openLocationFinder(int requestId) {
        try {
            PlaceAutocomplete.IntentBuilder builder = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY);
            builder.setBoundsBias(toBounds(Account.currentAccount().location(), 5_000));

            Intent intent = builder.build(this);
            startActivityForResult(intent, requestId);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
        }
    }

    public void showQuickLocationSelectionDialog() {
        quickLocationSelection.setVisibility(View.VISIBLE);
    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
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
                                                .setOnClickListener(new QuickOrderHandler(this))
                                                .setOnDismissListener(new OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogPlus dialog) {
                                                        /* WTF? WHY IS THIS HERE.. Well now, looks like the library (DialogPlus) is kinda buggy with multiple
                                                         dialogs being shown on the same ui rendering cycle, causing the internals to say it's showing the newly
                                                          created dialog even though is not showing or has not even being created for that matter. A way around this is to listen to the on dismiss callback
                                                          not the best way around it but the only way to ensure that the animation does not interfere with wanting make a new dialog.
                                                           */
                                                        if (mapFragment.mapMode == MapFragment.ORDER_MODE) {
                                                        }
                                                    }
                                                }).create();
        quickOrderDialog.show();
    }

    @Override
    public void onDestroy() {
        mGoForUs.ServicesManager.cancelConversationsUpdateAlarm();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoForUs.ServicesManager.scheduleConversationsUpdateAlarm();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoForUs.ServicesManager.cancelConversationsUpdateAlarm();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                mGoForUs.getJobManager().addJobInBackground(new AttemptLogoutJob());
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
    public void onMessageRead(MessageMarkReadResult result) {
        updateMessageFAB();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
