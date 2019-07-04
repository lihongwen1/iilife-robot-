package com.ilife.iliferobot.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.adapter.X_seriesAdapter;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.Utils;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;

/**
 * Created by chengjiaping on 2018/8/9.
 */
//DONE
public class SelectActivity_x extends BackBaseActivity {
    final String TAG = SelectActivity_x.class.getSimpleName();
    public static final String KEY_SUBDOMAIN = "key_subdomain";
    public static final String KEEY_SUBDOMAIN_ID = "key_subdomain_id";
    Context context;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    X_seriesAdapter adapter;
    String[] subdomains;
    long[] subdomainIds;
    @BindView(R.id.tv_top_title)
    TextView tvTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_select_x;
    }

    @Override
    public void initView() {
        context = this;
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        if (Utils.isIlife()) {
            subdomains = new String[]{/*Constants.subdomain_x900, */Constants.subdomain_x800, Constants.subdomain_x787, Constants.subdomain_x785};
            subdomainIds = new long[]{/*Constants.subdomainId_x900,*/ Constants.subdomainId_x800, Constants.subdomainId_x787, Constants.subdomainId_x785};
        } else {
            subdomains=new String[]{Constants.subdomain_a9s,Constants.subdomain_a8s};
            subdomainIds=new long[]{Constants.subdomaiId_a9s,Constants.subdomaiId_a8s};
        }

        adapter = new X_seriesAdapter(context);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SpaceItemDecoration(Utils.dip2px(this, 6)));
        adapter.setOnItemClickListener(position -> {
            SpUtils.saveString(context, KEY_SUBDOMAIN, subdomains[position]);
            SpUtils.saveLong(context, KEEY_SUBDOMAIN_ID, subdomainIds[position]);
            Intent i = new Intent(context, FirstApActivity.class);
            startActivity(i);
        });
        tvTitle.setText(R.string.select_x_aty_add);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.IS_FIRST_AP = true;
    }

    class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        int mSpace;

        /**
         * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
         * the number of pixels that the item view should be inset by, similar to padding or margin.
         * The default implementation sets the bounds of outRect to 0 and returns.
         * <p>
         * <p>
         * If this ItemDecoration does not affect the positioning of item views, it should set
         * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
         * before returning.
         * <p>
         * <p>
         * If you need to access Adapter for additional data, you can call
         * {@link RecyclerView#getChildAdapterPosition(View)} to get the adapter position of the
         * View.
         *
         * @param outRect Rect to receive the output.
         * @param view    The child view to decorate
         * @param parent  RecyclerView this ItemDecoration is decorating
         * @param state   The current state of RecyclerView.
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.left = mSpace;
            outRect.right = mSpace;
            outRect.bottom = mSpace;
            outRect.top = mSpace;
            if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1) {
                outRect.top = 0;
            } else {
                outRect.top = mSpace;

            }


        }

        SpaceItemDecoration(int space) {
            this.mSpace = space;
        }
    }
}
