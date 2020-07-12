package ru.ifsoft.chat.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import ru.ifsoft.chat.R;
import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.constants.Constants;
import ru.ifsoft.chat.model.Image;

import static ru.ifsoft.chat.R.id.progressBar;


public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.MyViewHolder> {

    private List<Image> images;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout mStateIconContainer;

        public ImageView thumbnail, mStateIcon;
        public ImageView playImg;

        public ProgressBar mProgressBar;

        public MyViewHolder(View view) {

            super(view);

            mStateIconContainer = (RelativeLayout) view.findViewById(R.id.state_icon_container);

            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            playImg = (ImageView) view.findViewById(R.id.playImg);

            mProgressBar = (ProgressBar) view.findViewById(progressBar);

            mStateIcon = (ImageView) view.findViewById(R.id.state_icon);
        }
    }


    public GalleryListAdapter(Context context, List<Image> images) {

        mContext = context;
        this.images = images;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_thumbnail, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Image image = images.get(position);

        holder.mStateIconContainer.setVisibility(View.GONE);

        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.mProgressBar.setVisibility(View.VISIBLE);
        holder.playImg.setVisibility(View.GONE);
        holder.mStateIcon.setVisibility(View.GONE);

        final ImageView playImg = holder.playImg;
        final ProgressBar progressBar = holder.mProgressBar;

        if (image.getItemType() == Constants.GALLERY_ITEM_TYPE_VIDEO) {

            Glide.with(mContext)
                    .load(image.getImgUrl())
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            progressBar.setVisibility(View.GONE);
                            playImg.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                            progressBar.setVisibility(View.GONE);
                            playImg.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(holder.thumbnail);

        } else {

            Glide.with(mContext)
                    .load(image.getImgUrl())
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.thumbnail);
        }

        if (image.getFromUserId() == App.getInstance().getId()) {

            holder.mStateIconContainer.setVisibility(View.VISIBLE);
            holder.mStateIcon.setVisibility(View.VISIBLE);

            if (image.getModerateAt() == 0) {

                holder.mStateIcon.setImageResource(R.drawable.ic_wait_small);
                holder.mStateIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

            } else {

                holder.mStateIcon.setImageResource(R.drawable.ic_accept);
                holder.mStateIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @Override
    public int getItemCount() {

        return images.size();
    }

    public interface ClickListener {

        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GalleryListAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final GalleryListAdapter.ClickListener clickListener) {

            this.clickListener = clickListener;

            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (child != null && clickListener != null) {

                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());

            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {

                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}