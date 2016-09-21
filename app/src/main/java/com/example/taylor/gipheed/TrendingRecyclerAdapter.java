package com.example.taylor.gipheed;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.taylor.gipheed.GifPlaying.GifPlayManager;
import com.example.taylor.gipheed.GifPlaying.GifPlayer;
import com.example.taylor.gipheed.Giphy.GiphyTrendRespModel;
import com.squareup.picasso.Picasso;

/**
 * Created by Taylor on 9/13/2016.
 */
public class TrendingRecyclerAdapter extends RecyclerView.Adapter<TrendingRecyclerAdapter.TrendingViewHolder> {
    private static final String TAG = "TrendingRecyclerAdepter";

    private Context context;
    private GiphyTrendRespModel giphyTrendRespModel;
    private GifPlayManager gifPlayManager;
    private ImageSelectedListener imageSelectedListener;

    private Utils.Sizer sizer;

    private String buttonText;

    private boolean isViewModeStream;

    public TrendingRecyclerAdapter(Context context, boolean isViewModeStream) {
        this(context, isViewModeStream, null);
    }

    public TrendingRecyclerAdapter(Context context, boolean isViewModeStream, String buttonText) {
        this.context = context;
        sizer = Utils.getSizer(context);
        this.isViewModeStream = isViewModeStream;
        this.buttonText = buttonText;
    }

    public void setData(GiphyTrendRespModel giphyTrendRespModel) {
        this.giphyTrendRespModel = giphyTrendRespModel;
        notifyDataSetChanged();
    }

    public void setisViewModeStream(boolean isViewModeStream) {
        this.isViewModeStream = isViewModeStream;
        notifyDataSetChanged();
    }

    public void setGifPlayManager(GifPlayManager gifPlayManager) {
        this.gifPlayManager = gifPlayManager;
    }

    public void setImageSelectedListener(ImageSelectedListener imageSelectedListener) {
        this.imageSelectedListener = imageSelectedListener;
    }

    @Override
    public int getItemCount() {
        if(giphyTrendRespModel != null) {
            return giphyTrendRespModel.data.length;
        } else {
            return 0;
        }
    }

    @Override
    public TrendingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TrendingViewHolder(new FrameLayout(context));
    }
//


    @Override
    public void onViewDetachedFromWindow(TrendingViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
//        holder.flGifPlayer.removeAllViews();
    }

    @Override
    public void onViewRecycled(TrendingViewHolder holder) {
        //TODO: Only play the first fully visible or first visible gif
        gifPlayManager.removeGifPlayer(holder.getAdapterPosition());
        super.onViewRecycled(holder);
        holder.gifPlayer.stop();
    }

    public void setButtonText(String text) {
        this.buttonText = text;
    }

    @Override
    public void onBindViewHolder(final TrendingViewHolder holder, final int position) {
        Picasso.with(context).load(giphyTrendRespModel.data[position].images.fixed_height_still.url).into(holder.imageView);

        final float gifAspectRatio = (float) giphyTrendRespModel.data[position].images.fixed_height_still.height / (float) giphyTrendRespModel.data[position].images.fixed_height_still.width;

        if(isViewModeStream) {
            holder.imageView.setLayoutParams(new FrameLayout.LayoutParams(sizer.viewSize(120f), (int) ((float) sizer.viewSize(120f) * gifAspectRatio)));
            holder.flMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imageSelectedListener != null) {
                        imageSelectedListener.onGifPlayClick(holder.getAdapterPosition());
                    }
                }
            });
        } else {

            if(gifAspectRatio > (TrendingViewHolder.LIST_ITEM_HEIGHT/TrendingViewHolder.LIST_ITEM_WIDTH)) {
                holder.imageView.setLayoutParams(new FrameLayout.LayoutParams((int) ((float) sizer.viewSize(TrendingViewHolder.LIST_ITEM_HEIGHT) / gifAspectRatio), sizer.viewSize(TrendingViewHolder.LIST_ITEM_HEIGHT)));
                holder.gifPlayer.setLayoutParams(new FrameLayout.LayoutParams((int) ((float) sizer.viewSize(TrendingViewHolder.LIST_ITEM_HEIGHT) / gifAspectRatio), sizer.viewSize(TrendingViewHolder.LIST_ITEM_HEIGHT)));
            } else {
                holder.imageView.setLayoutParams(new FrameLayout.LayoutParams(sizer.viewSize(TrendingViewHolder.LIST_ITEM_WIDTH), (int) ((float) sizer.viewSize(TrendingViewHolder.LIST_ITEM_WIDTH) * gifAspectRatio), Gravity.CENTER));
                holder.gifPlayer.setLayoutParams(new FrameLayout.LayoutParams(sizer.viewSize(TrendingViewHolder.LIST_ITEM_WIDTH), (int) ((float) sizer.viewSize(TrendingViewHolder.LIST_ITEM_WIDTH) * gifAspectRatio), Gravity.CENTER));
            }

            holder.favoriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imageSelectedListener != null) {
                            imageSelectedListener.onImageSelected(giphyTrendRespModel.data[holder.getAdapterPosition()]);
                        }
                    }
            });

            holder.flMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imageSelectedListener != null) {
                        imageSelectedListener.onGifPlayClick(holder.getAdapterPosition());
                    }
                }
            });

            final ValueAnimator expandValAnimator = ValueAnimator.ofInt(sizer.viewSize(TrendingViewHolder.LIST_ITEM_WIDTH), sizer.viewSize(106));
            expandValAnimator.setDuration(400);
            expandValAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            expandValAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    holder.flGifPlayer.getLayoutParams().width = (int) animation.getAnimatedValue();
                    holder.flGifPlayer.getLayoutParams().height = (int) animation.getAnimatedValue();
                    if(gifAspectRatio > 1f) {
                        holder.gifPlayer.getLayoutParams().width = (int) ((float) (int) animation.getAnimatedValue() / gifAspectRatio);
                        holder.gifPlayer.getLayoutParams().height = (int) animation.getAnimatedValue();
                        holder.imageView.getLayoutParams().width = (int) ((float) (int) animation.getAnimatedValue() / gifAspectRatio);
                        holder.imageView.getLayoutParams().height = (int) animation.getAnimatedValue();
                    } else {
                        holder.gifPlayer.getLayoutParams().width = (int) animation.getAnimatedValue();
                        holder.gifPlayer.getLayoutParams().height = (int) ((float) (int) animation.getAnimatedValue() * gifAspectRatio);
                        holder.imageView.getLayoutParams().width = (int) animation.getAnimatedValue();
                        holder.imageView.getLayoutParams().height = (int) ((float) (int) animation.getAnimatedValue() * gifAspectRatio);
                    }
                    holder.flGifPlayer.requestLayout();
//                            holder.gifPlayer.requestLayout();
//                            holder.imageView.requestLayout();
                    holder.setIsRecyclable(false);
                }
            });

            final ValueAnimator btnMoveValAnimator = ValueAnimator.ofFloat(1f, 0f);
            btnMoveValAnimator.setDuration(300);
            btnMoveValAnimator.setInterpolator(new DecelerateInterpolator());
            btnMoveValAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    holder.favoriteBtn.setAlpha((float)animation.getAnimatedValue());
                    holder.favoriteBtn.requestLayout();
                }
            });
            btnMoveValAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if(holder.isExpanded) {
                        holder.favoriteBtn.setVisibility(View.GONE);
                    } else {
                        animation.setStartDelay(0);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            holder.flGifPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!holder.isExpanded) {
                        holder.isExpanded = true;
//                        holder.favoriteBtn.setVisibility(View.GONE);
                        btnMoveValAnimator.start();
                        expandValAnimator.start();
                        if(imageSelectedListener != null) {
                            imageSelectedListener.onGifPlayClick(holder.getAdapterPosition());
                        }
                    } else {
                        holder.isExpanded = false;
                        expandValAnimator.reverse();
                        holder.favoriteBtn.setVisibility(View.VISIBLE);
                        btnMoveValAnimator.setStartDelay(100);
                        btnMoveValAnimator.reverse();
                        holder.setIsRecyclable(true);
                    }
                }
            });
        }

//        final GifPlayer gifPlayer = new GifPlayer(context);
//        gifPlayer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        holder.flGifPlayer.addView(gifPlayer);


//        ThreadManager.Run(new Runnable() {
//            @Override
//            public void run() {
//                holder.gifPlayer.init(giphyTrendRespModel.data[holder.getAdapterPosition()].images.original.mp4);
//            }
//        });

        if(gifPlayManager != null) {
            Log.v(TAG, "GifPlayer added to GifPlayManager");
            gifPlayManager.addGifPlayer(holder.getAdapterPosition(), holder.gifPlayer);
        } else {
            Log.v(TAG, "GifPlayManager null");
        }

//        holder.flMain.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(imageSelectedListener != null) {
//                    imageSelectedListener.onImageSelected(giphyTrendRespModel.data[holder.getAdapterPosition()]);
//                }
//            }
//        });

        // My version of loading the images async, Picasso is better
//        ThreadManager.Run(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    InputStream is = (new URL(giphyTrendRespModel.data[position].images.fixed_height_still.url)).openStream();
//
//                    final Bitmap bm = BitmapFactory.decodeStream(is);
//                    ThreadManager.RunUI(new Runnable() {
//                        @Override
//                        public void run() {
//                            holder.imageView.setImageBitmap(bm);
//                        }
//                    });
//                    is.close();
//                } catch (MalformedURLException e) {
//                    Log.v(TAG, e.getMessage());
//                } catch (IOException e) {
//                    Log.v(TAG, e.getMessage());
//                }
//            }
//        });

    }

    class TrendingViewHolder extends RecyclerView.ViewHolder {

        public static final float LIST_ITEM_HEIGHT = 42f;
        public static final float LIST_ITEM_WIDTH = 42f;

        FrameLayout flMain;
        ImageView imageView;
        GifPlayer gifPlayer;
        FrameLayout flGifPlayer;
        FrameLayout botBorder;

        Button favoriteBtn;

        boolean isExpanded = false;

        public TrendingViewHolder(View view) {
            super(view);
            flMain = (FrameLayout) view;
            flMain.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            flMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryHint));

            if(isViewModeStream) {
                initViewModeStream();
            } else {
                initViewModeList();
            }
        }

        private void initViewModeList() {
            flGifPlayer = new FrameLayout(context);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sizer.viewSize(LIST_ITEM_WIDTH), sizer.viewSize(LIST_ITEM_HEIGHT), Gravity.CENTER_VERTICAL|Gravity.LEFT);
            layoutParams.setMargins(sizer.viewSize(7), sizer.viewSize(4.9f), 0, sizer.viewSize(5));
            flGifPlayer.setLayoutParams(layoutParams);
            flMain.addView(flGifPlayer);

            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            flGifPlayer.addView(imageView);

            gifPlayer = new GifPlayer(context);
            gifPlayer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            flGifPlayer.addView(gifPlayer);

            favoriteBtn = new Button(context);
            layoutParams = new FrameLayout.LayoutParams(sizer.viewSize(28f), sizer.viewSize(12f), Gravity.CENTER_VERTICAL|Gravity.RIGHT);
            layoutParams.setMargins(0, 0, sizer.viewSize(7f), 0);
            favoriteBtn.setLayoutParams(layoutParams);
            favoriteBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
            if(buttonText == null || buttonText.length()<1) {
                favoriteBtn.setText("Favorite");
            } else {
                favoriteBtn.setText(buttonText);
            }
            flMain.addView(favoriteBtn);

            botBorder = new FrameLayout(context);
            botBorder.setLayoutParams(new FrameLayout.LayoutParams(sizer.viewSize(106f), sizer.viewSize(0.2f), Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM));
            botBorder.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            flMain.addView(botBorder);
        }

        private void initViewModeStream() {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            flMain.addView(imageView);

            flGifPlayer = new FrameLayout(context);
            flGifPlayer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            flMain.addView(flGifPlayer);

            gifPlayer = new GifPlayer(context);
            gifPlayer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            flMain.addView(gifPlayer);
        }
    }



    public interface ImageSelectedListener {
        void onImageSelected(GiphyTrendRespModel.Data imageDate);
        void onGifPlayClick(int position);
        void onGifDetailClick(int position);
    }
}
