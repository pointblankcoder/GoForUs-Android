package info.goforus.goforus;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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

import info.goforus.goforus.models.driver.Information;

public class DriverDetailsActivity extends BaseActivity implements ObservableScrollViewCallbacks {
    private static final String TAG = "DriverDetailsActivity";
    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;

    public static Information mDriver;
    private ObservableScrollView mScrollView;
    private int mActionBarSize;
    private boolean mFabIsShown;
    private View mBriefView;
    private View mBackgroundImageView;
    private View mFab;
    private LinearLayout mRatingContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_details);
        mDriver = getIntent().getParcelableExtra("Driver");


        // Toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mActionBarSize = getActionBarSize();

        mBriefView = findViewById(R.id.driverBrief);
        mBackgroundImageView = findViewById(R.id.background);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        mRatingContainer = (LinearLayout) findViewById(R.id.ratingContainer);
        mScrollView.setScrollViewCallbacks(this);


        int mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        View mContentSpacerView = findViewById(R.id.contentSpacer);
        mContentSpacerView.getLayoutParams().height = mFlexibleSpaceImageHeight;

        setTitle(null);

        // FAB
        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DriverDetailsActivity.this, "FAB is clicked", Toast.LENGTH_SHORT).show();
            }
        });

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
            ViewHelper.setTranslationY(mBackgroundImageView, -(mBackgroundImageView.getHeight() - maxBackground));
        }

        // Translate OrderFab
        ViewHelper.setTranslationY(mFab, ((mBriefView.getY() + mBriefView.getHeight()) - (mFab.getHeight() / 2)));

        // Translate Rating Bar (Based on Driver Brief positioning)
        ViewHelper.setTranslationY(mRatingContainer, ((mBriefView.getY()) - (mRatingContainer.getHeight() / 2)));


        showFab();
    }

    public void addRating() {
        for (int i = 0; i < mDriver.rating; i++ ){
            ImageView star = new ImageView(this);
            star.setBackground(ContextCompat.getDrawable(this, R.drawable.star));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mRatingContainer.getLayoutParams());
            star.setLayoutParams(params);
            mRatingContainer.addView(star);
        }
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
        }
    }
}
