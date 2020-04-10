package com.example.awesomecurriculum.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.awesomecurriculum.R;


public class GlideUtils {

    public static void loadChatImage(final Context mContext, String imgUrl, final ImageView imageView) {


        final RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.default_img_failed)// 正在加载中的图片
                .error(R.mipmap.default_img_failed); // 加载失败的图片

        Glide.with(mContext)
                .load(imgUrl) // 图片地址
                .apply(options)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        ImageSize imageSize = BitmapUtil.getImageSize(((BitmapDrawable) resource).getBitmap());
                        RelativeLayout.LayoutParams imageLP = (RelativeLayout.LayoutParams) (imageView.getLayoutParams());
                        imageLP.width = imageSize.getWidth();
                        imageLP.height = imageSize.getHeight();
                        imageView.setLayoutParams(imageLP);

                        Glide.with(mContext)
                                .load(resource)
                                .apply(options) // 参数
                                .into(imageView);
                    }
                });
    }

    public static void loadHeadImage(final Context mContext, String imgUrl, final ImageView imageView) {


        final RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.default_img_failed)// 正在加载中的图片
                .error(R.mipmap.default_img_failed); // 加载失败的图片

        Glide.with(mContext)
                .load(imgUrl) // 图片地址
                .apply(options)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        ImageSize imageSize = BitmapUtil.getImageSize(((BitmapDrawable) resource).getBitmap());
                        RelativeLayout.LayoutParams imageLP = (RelativeLayout.LayoutParams) (imageView.getLayoutParams());
                        imageLP.width = 80;
                        imageLP.height = 80;
                        imageView.setLayoutParams(imageLP);

                        Glide.with(mContext)
                                .load(resource)
                                .apply(options) // 参数
                                .into(imageView);
                    }
                });
    }


}
