package com.layer.ui.view;

import android.app.Activity;
import android.os.Bundle;

import com.layer.ui.avatar.IdentityNameFormatterImpl;
import com.layer.ui.mock.MockLayerClient;
import com.layer.sdk.LayerClient;
import com.layer.ui.R;
import com.layer.ui.avatar.AvatarView;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.imagecache.PicassoImageCacheWrapper;
import com.layer.ui.util.imagecache.requesthandlers.MessagePartRequestHandler;
import com.squareup.picasso.Picasso;

public class AvatarActivityTestView extends Activity {

    private AvatarView mAvatarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_test);
        LayerClient layerClient = new MockLayerClient();
        mAvatarView = (AvatarView) findViewById(R.id.test_avatar);
        Picasso picasso = new Picasso.Builder(this)
                .addRequestHandler(new MessagePartRequestHandler(layerClient))
                .build();
        ImageCacheWrapper imageCacheWrapper = new PicassoImageCacheWrapper(picasso);
        mAvatarView.init(new AvatarViewModelImpl(imageCacheWrapper), new IdentityNameFormatterImpl());
    }
}
