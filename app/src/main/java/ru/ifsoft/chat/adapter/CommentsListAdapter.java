package ru.ifsoft.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import ru.ifsoft.chat.ProfileActivity;
import ru.ifsoft.chat.R;
import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.constants.Constants;
import ru.ifsoft.chat.model.Comment;

public class CommentsListAdapter extends RecyclerView.Adapter<CommentsListAdapter.ViewHolder> implements Constants {

    private List<Comment> items = new ArrayList<>();

    private Context context;

    ImageLoader imageLoader = App.getInstance().getImageLoader();

    private OnItemMenuButtonClickListener onItemMenuButtonClickListener;

    public interface OnItemMenuButtonClickListener {

        void onItemClick(View view, Comment obj, int actionId, int position);
    }

    public void setOnMoreButtonClickListener(final OnItemMenuButtonClickListener onItemMenuButtonClickListener) {

        this.onItemMenuButtonClickListener = onItemMenuButtonClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircularImageView mItemAuthorPhoto, mItemAuthorIcon;
        public TextView mItemAuthor;
        public ImageView mItemAuthorOnlineIcon;
        public ImageView mItemMenuButton;
        public EmojiconTextView mItemDescription;
        public TextView mItemTimeAgo;

        public ViewHolder(View v) {

            super(v);

            mItemAuthorPhoto = (CircularImageView) v.findViewById(R.id.itemAuthorPhoto);
            mItemAuthorIcon = (CircularImageView) v.findViewById(R.id.itemAuthorIcon);

            mItemAuthor = (TextView) v.findViewById(R.id.itemAuthor);
            mItemAuthorOnlineIcon = (ImageView) v.findViewById(R.id.itemAuthorOnlineIcon);

            mItemDescription = (EmojiconTextView) v.findViewById(R.id.itemDescription);

            mItemMenuButton = (ImageView) v.findViewById(R.id.itemMenuButton);
            mItemTimeAgo = (TextView) v.findViewById(R.id.itemTimeAgo);
        }

    }

    public CommentsListAdapter(Context ctx, List<Comment> items) {

        this.context = ctx;
        this.items = items;

        if (imageLoader == null) {

            imageLoader = App.getInstance().getImageLoader();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_list_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final Comment p = items.get(position);

        holder.mItemAuthorIcon.setVisibility(View.GONE);
        holder.mItemAuthorOnlineIcon.setVisibility(View.GONE);

        holder.mItemAuthorPhoto.setVisibility(View.VISIBLE);

        holder.mItemAuthorPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("profileId", p.getFromUserId());
                context.startActivity(intent);
            }
        });

        if (p.getFromUserPhotoUrl().length() != 0) {

            imageLoader.get(p.getFromUserPhotoUrl(), ImageLoader.getImageListener(holder.mItemAuthorPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

        } else {

            holder.mItemAuthorPhoto.setVisibility(View.VISIBLE);
            holder.mItemAuthorPhoto.setImageResource(R.drawable.profile_default_photo);
        }

        holder.mItemAuthor.setVisibility(View.VISIBLE);
        holder.mItemAuthor.setText(p.getFromUserFullname());

        holder.mItemAuthor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("profileId", p.getFromUserId());
                context.startActivity(intent);
            }
        });

        if (p.getText().length() != 0) {

            holder.mItemDescription.setVisibility(View.VISIBLE);
            holder.mItemDescription.setText(p.getText().replaceAll("<br>", "\n"));

        } else {

            holder.mItemDescription.setVisibility(View.GONE);
        }

        holder.mItemTimeAgo.setVisibility(View.VISIBLE);

        String timeAgo;

        timeAgo = p.getTimeAgo();

        if (p.getReplyToUserId() != 0) {

            if (p.getReplyToUserFullname().length() != 0) {

                timeAgo = timeAgo + " " + context.getString(R.string.label_to) + " " + p.getReplyToUserFullname();

            } else {

                timeAgo = timeAgo + " " + context.getString(R.string.label_to) + " @" + p.getReplyToUserUsername();
            }
        }

        holder.mItemTimeAgo.setText(timeAgo);

        holder.mItemMenuButton.setVisibility(View.VISIBLE);
        holder.mItemMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                onItemMenuButtonClick(view, p, position);
            }
        });
    }

    private void onItemMenuButtonClick(final View view, final Comment comment, final int position){

        PopupMenu popupMenu = new PopupMenu(context, view);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                onItemMenuButtonClickListener.onItemClick(view, comment, item.getItemId(), position);

                return true;
            }
        });

        if (comment.getFromUserId() == App.getInstance().getId()) {

            // only delete option
            popupMenu.inflate(R.menu.menu_comment_popup_1);

        } else {

            // reply option
            popupMenu.inflate(R.menu.menu_comment_popup_3);
        }

        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}