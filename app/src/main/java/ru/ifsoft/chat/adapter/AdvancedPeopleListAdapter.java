package ru.ifsoft.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import ru.ifsoft.chat.R;
import ru.ifsoft.chat.app.App;
import ru.ifsoft.chat.model.Profile;

public class AdvancedPeopleListAdapter extends RecyclerView.Adapter<AdvancedPeopleListAdapter.MyViewHolder> {

	private Context mContext;
	private List<Profile> itemList;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, Profile obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

	public class MyViewHolder extends RecyclerView.ViewHolder {

		public TextView mTitle, mSubtitle;
		public CircularImageView mCirclePhotoImage, mOnlineImage, mVerifiedImage, mProImage, mGenderImage;

        public ImageView mSquarePhotoImage;

		public MaterialRippleLayout mParent;
		public ProgressBar mProgressBar;

		public MyViewHolder(View view) {

			super(view);

			mParent = (MaterialRippleLayout) view.findViewById(R.id.parent);

			if (App.getInstance().getCircleItems()) {

                mCirclePhotoImage = (CircularImageView) view.findViewById(R.id.photo_image);

            } else {

                mSquarePhotoImage = (ImageView) view.findViewById(R.id.photo_image);
            }

			mOnlineImage = (CircularImageView) view.findViewById(R.id.online_image);
			mVerifiedImage = (CircularImageView) view.findViewById(R.id.verified_image);
			mProImage = (CircularImageView) view.findViewById(R.id.pro_image);
			mGenderImage = (CircularImageView) view.findViewById(R.id.gender_image);
			mTitle = (TextView) view.findViewById(R.id.title_label);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle_label);
			mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		}
	}


	public AdvancedPeopleListAdapter(Context mContext, List<Profile> itemList) {

		this.mContext = mContext;
		this.itemList = itemList;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;

        if (App.getInstance().getCircleItems()) {

            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_thumbnail_circle, parent, false);

        } else {

            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_thumbnail_square, parent, false);
        }


		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {

		final Profile item = itemList.get(position);

        holder.mSubtitle.setVisibility(View.GONE);

        holder.mOnlineImage.setVisibility(View.GONE);
        holder.mVerifiedImage.setVisibility(View.GONE);
        holder.mProImage.setVisibility(View.GONE);
		holder.mGenderImage.setVisibility(View.GONE);

		holder.mProgressBar.setVisibility(View.VISIBLE);

        if (App.getInstance().getCircleItems()) {

            holder.mCirclePhotoImage.setVisibility(View.VISIBLE);

        } else {

            holder.mSquarePhotoImage.setVisibility(View.VISIBLE);
        }

		if (item.getNormalPhotoUrl() != null && item.getNormalPhotoUrl().length() > 0) {

            final ImageView img;

            if (App.getInstance().getCircleItems()) {

                img = holder.mCirclePhotoImage;

            } else {

                img = holder.mSquarePhotoImage;
            }

			final ProgressBar progressView = holder.mProgressBar;

			Glide.with(mContext)
					.load(item.getNormalPhotoUrl())
					.listener(new RequestListener<String, GlideDrawable>() {
						@Override
						public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

							progressView.setVisibility(View.GONE);
							img.setImageResource(R.drawable.profile_default_photo);
							img.setVisibility(View.VISIBLE);

							if (item.getSex() < 3) {

								holder.mGenderImage.setVisibility(View.VISIBLE);
							}

							return false;
						}

						@Override
						public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

							progressView.setVisibility(View.GONE);
							img.setVisibility(View.VISIBLE);

							if (item.getSex() < 3) {

								holder.mGenderImage.setVisibility(View.VISIBLE);
							}

							return false;
						}
					})
					.into(img);

		} else {

            holder.mProgressBar.setVisibility(View.GONE);

			if (item.getSex() < 3) {

				holder.mGenderImage.setVisibility(View.VISIBLE);
			}

            if (App.getInstance().getCircleItems()) {

                holder.mCirclePhotoImage.setVisibility(View.VISIBLE);
                holder.mCirclePhotoImage.setImageResource(R.drawable.profile_default_photo);

            } else {

                holder.mSquarePhotoImage.setVisibility(View.VISIBLE);
                holder.mSquarePhotoImage.setImageResource(R.drawable.profile_default_photo);
            }
		}

		if (item.getAge() != 0) {

			holder.mTitle.setText(item.getFullname() + ", " + Integer.toString(item.getAge()));

		} else {

			holder.mTitle.setText(item.getFullname());
		}

		if (item.getSex() < 3) {

			switch (item.getSex()) {

				case 0: {

					holder.mGenderImage.setImageResource(R.drawable.ic_gender_male);

					break;
				}

				case 1: {

					holder.mGenderImage.setImageResource(R.drawable.ic_gender_female);

					break;
				}

				default: {

					holder.mGenderImage.setImageResource(R.drawable.ic_gender_secret);

					break;
				}
			}
		}

		if (item.getDistance() > 0.0) {

		    holder.mSubtitle.setVisibility(View.VISIBLE);
			holder.mSubtitle.setText(String.format("%.1f km", item.getDistance()));

		} else {

		    // For guests

		    if (item.getLastVisit().length() > 0) {

                holder.mSubtitle.setVisibility(View.VISIBLE);
                holder.mSubtitle.setText(item.getLastVisit());

            } else {

                holder.mSubtitle.setVisibility(View.GONE);
            }
		}

		if (item.isOnline()) {

			holder.mOnlineImage.setVisibility(View.VISIBLE);

		} else {

			holder.mOnlineImage.setVisibility(View.GONE);
		}

		if (item.isVerify()) {

			holder.mVerifiedImage.setVisibility(View.VISIBLE);

		} else {

			holder.mVerifiedImage.setVisibility(View.GONE);
		}

		if (item.isProMode()) {

			holder.mProImage.setVisibility(View.VISIBLE);

		} else {

			holder.mProImage.setVisibility(View.GONE);
		}

		holder.mParent.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				if (mOnItemClickListener != null) {

					mOnItemClickListener.onItemClick(view, item, position);
				}
			}
		});
	}

	@Override
	public int getItemCount() {

		return itemList.size();
	}
}