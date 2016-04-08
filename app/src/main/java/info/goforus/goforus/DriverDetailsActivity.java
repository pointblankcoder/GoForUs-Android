package info.goforus.goforus;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.goforus.goforus.managers.ContactDriverManager;
import info.goforus.goforus.models.drivers.Driver;
import info.goforus.goforus.models.drivers.Information;

public class DriverDetailsActivity extends BaseActivity implements ObservableScrollViewCallbacks {
    private static final String TAG = "DriverDetailsActivity";
    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;

    private final ContactDriverManager contactDriverManager = ContactDriverManager.getInstance();
    public static Information mDriver;
    @Bind(R.id.scroll) ObservableScrollView mScrollView;
    @Bind(R.id.driverBrief) View mBriefView;
    @Bind(R.id.background) View mBackgroundImageView;
    @Bind(R.id.fabContact) View mFabContact;
    @Bind(R.id.fabOrder) View mFabOrder;
    @Bind(R.id.ratingContainer) LinearLayout mRatingContainer;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.contentSpacer) View mContentSpacerView;
    @BindDrawable(R.drawable.ic_arrow_back_white_24dp) Drawable arrowBackDrawable;
    @BindDimen(R.dimen.flexible_space_image_height) int mFlexibleSpaceImageHeight;

    DialogPlus contactDialog;
    int mActionBarSize;
    boolean mFabIsShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_details);
        mDriver = Parcels.unwrap(getIntent().getParcelableExtra("Information"));
        setTitle(mDriver.email);

        ButterKnife.bind(this);

        // Toolbar
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            getSupportActionBar().setHomeAsUpIndicator(arrowBackDrawable);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mActionBarSize = getActionBarSize();

        setTitle(mDriver.email);

        mScrollView.setScrollViewCallbacks(this);
        mContentSpacerView.getLayoutParams().height = mFlexibleSpaceImageHeight;

        // Trigger init for views requiring scroll to position themselves
        ScrollUtils.addOnGlobalLayoutListener(mScrollView, new Runnable() {
            @Override
            public void run() {
                mScrollView.scrollTo(0, 1);
                mScrollView.scrollTo(0, 0);
            }
        });

        addRating();
    }

    @OnClick(R.id.fabContact)
    public void onContactClick() {
        contactDriverManager.setup(this, Driver.findByExternalId(mDriver.externalId));
        contactDriverManager.show();
    }

    @OnClick(R.id.fabOrder)
    public void onOrderClick() {
        Toast.makeText(this, "Order Fab Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        // Translate driverBrief
        float min = (mActionBarSize + 20);
        float max = mActionBarSize + (mBackgroundImageView.getHeight() / 2);
        float val = (-scrollY + (mActionBarSize / 2)) + (mBriefView.getHeight() / 2);
        float briefTranslationY = ScrollUtils.getFloat(val, min, max);
        ViewHelper.setTranslationY(mBriefView, briefTranslationY);


        // Translate Background
        float scrollViewY = findViewById(R.id.scrollContent).getY();
        float maxBackground = (mActionBarSize + 20) + mBriefView.getHeight() + 20;
        if ((scrollViewY - scrollY) > maxBackground) {
            ViewHelper.setTranslationY(mBackgroundImageView, -scrollY);
        } else {
            ViewHelper.setTranslationY(mBackgroundImageView, -(mBackgroundImageView
                    .getHeight() - maxBackground));
        }


        // Translate FABs
        ViewHelper.setTranslationY(mFabContact, ((mBriefView.getY() + mBriefView
                .getHeight() / 2) - (mFabContact.getHeight() / 2)));
        ViewHelper.setTranslationY(mFabOrder, mFabContact.getY());
        showFab();
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFabContact).cancel();
            ViewPropertyAnimator.animate(mFabOrder).cancel();
            ViewPropertyAnimator.animate(mFabContact).scaleX(1).scaleY(1).setDuration(200).start();
            ViewPropertyAnimator.animate(mFabOrder).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
        }
    }

    public void addRating() {
        for (int i = 0; i < mDriver.rating; i++) {
            ImageView star = new ImageView(this);
            star.setBackground(ContextCompat.getDrawable(this, R.drawable.star));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mRatingContainer
                    .getLayoutParams());
            star.setLayoutParams(params);
            mRatingContainer.addView(star);
        }
    }
}
