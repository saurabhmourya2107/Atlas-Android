package com.layer.ui.util.imagecache;

import static junit.framework.Assert.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.layer.ui.util.imagecache.requesthandlers.MessagePartRequestHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.UUID;


public class PicassoImageCacheWrapperTest {

    PicassoImageCacheWrapper mPicassoImageCacheWrapper;

    @Mock
    MessagePartRequestHandler mMockMessagePartRequestHandler;
    @Mock
    Context mMockContext;
    @Mock
    ImageCacheWrapper.Callback mMockCallback;
    @Mock
    Picasso mMockPicasso;
    @Mock
    ArgumentCaptor<Target> mTargetArgumentCaptor;
    @Mock
    ArgumentCaptor<ImageCacheWrapper.Callback> mCallbackArgumentCaptor;
    @Mock
    com.squareup.picasso.RequestCreator mMockRequestCreator;
    @Mock
    Bitmap mMockBitmap;
    @Mock
    Target mMockTarget;
    @Mock
    BitmapWrapper mMockBitmapWrapper;
    @Mock
    Drawable mMockDrawable;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mPicassoImageCacheWrapper = spy(new PicassoImageCacheWrapper(mMockPicasso));
        when(mMockPicasso.load(anyString())).thenReturn(mMockRequestCreator);
        when(mMockRequestCreator.tag(any(UUID.class))).thenReturn(mMockRequestCreator);
        when(mMockRequestCreator.noPlaceholder()).thenReturn(mMockRequestCreator);
        when(mMockRequestCreator.noFade()).thenReturn(mMockRequestCreator);
        when(mMockRequestCreator.centerCrop()).thenReturn(mMockRequestCreator);
    }

    @Test
    public void testFetchBitmapSuccess() throws Exception {
        boolean isMultiTransform = false;
        BitmapWrapper bitmapWrapper = new BitmapWrapper("myUrl", 20,40, isMultiTransform);
        when(mMockRequestCreator.resize(bitmapWrapper.getWidth(), bitmapWrapper.getHeight())).thenReturn(mMockRequestCreator);
        when(mMockRequestCreator.transform(any(Transformation.class))).thenReturn(mMockRequestCreator);
        Target target = mPicassoImageCacheWrapper.createTarget(bitmapWrapper, mMockCallback);
        assertTrue(bitmapWrapper.getBitmap() == null);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Target target = (Target) args[0];
                target.onBitmapLoaded(mMockBitmap, Picasso.LoadedFrom.DISK);
                return null;
            }
        }).when(mMockRequestCreator).into(target);

        when(mPicassoImageCacheWrapper.createTarget(any(BitmapWrapper.class), any(
                ImageCacheWrapper.Callback.class)))
                .thenReturn(target);

        mPicassoImageCacheWrapper.fetchBitmap(bitmapWrapper, mMockCallback);
        assertTrue(bitmapWrapper.getBitmap() == mMockBitmap);
    }

    @Test
    public void testFetchBitmapFailure() throws Exception {
        boolean isMultiTransform = false;
        BitmapWrapper bitmapWrapper = new BitmapWrapper("myUrl", 20,40, isMultiTransform);
        when(mMockRequestCreator.resize(bitmapWrapper.getWidth(), bitmapWrapper.getHeight())).thenReturn(mMockRequestCreator);
        when(mMockRequestCreator.transform(any(Transformation.class))).thenReturn(mMockRequestCreator);
        Target target = mPicassoImageCacheWrapper.createTarget(bitmapWrapper, mMockCallback);
        assertTrue(bitmapWrapper.getBitmap() == null);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Target target = (Target) args[0];
                target.onBitmapFailed(mMockDrawable);
                return null;
            }
        }).when(mMockRequestCreator).into(target);

        when(mPicassoImageCacheWrapper.createTarget(any(BitmapWrapper.class), any(
                ImageCacheWrapper.Callback.class)))
                .thenReturn(target);

        mPicassoImageCacheWrapper.fetchBitmap(bitmapWrapper, mMockCallback);
        assertTrue(bitmapWrapper.getBitmap() == null);
    }
}