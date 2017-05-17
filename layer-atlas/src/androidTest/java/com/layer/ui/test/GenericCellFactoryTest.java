package com.layer.ui.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

<<<<<<< HEAD:layer-atlas/src/androidTest/java/com/layer/atlas/test/GenericCellFactoryTest.java
import com.layer.atlas.mock.MockLayerClient;
import com.layer.atlas.mock.MockMessageImpl;
import com.layer.atlas.mock.MockMessagePart;
=======
import com.layer.ui.messagetypes.generic.GenericCellFactory;
import com.layer.ui.mock.MockLayerClient;
import com.layer.ui.mock.MockMessageImpl;
import com.layer.ui.mock.MockMessagePart;
>>>>>>> Rename Adapter:layer-atlas/src/androidTest/java/com/layer/ui/test/GenericCellFactoryTest.java
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.messagetypes.generic.GenericCellFactory;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GenericCellFactoryTest {
    private final static String MIME_TYPE = "text/plain";


    Identity mParticipant1, mParticipant2;

    private List<MessagePart> mTestMessageParts;
    private MessagePart mTextMessagePart;
    private Message mMessage;
    private LayerClient mLayerClient;

    @Before
    public void setUp() {
        mTestMessageParts = new ArrayList<>();
        mTextMessagePart = new MockMessagePart("Generic message".getBytes(), MIME_TYPE);
        mTestMessageParts.add(mTextMessagePart);
        mMessage = new MockMessageImpl(mTestMessageParts);
        mLayerClient = new MockLayerClient();
    }

    @Test
    public void testParseContent() {
        GenericCellFactory genericCellFactory = new GenericCellFactory();
        GenericCellFactory.ParsedContent parsedContent = genericCellFactory.parseContent(mLayerClient, mMessage);

        assertThat(parsedContent.getString().isEmpty(), is(false));
    }

    @Test
    public void testIsType() {
        GenericCellFactory genericCellFactory = new GenericCellFactory();

        assertThat(genericCellFactory.isType(mMessage), is(true));
    }

    @Test
    public void testIsBindable() {
        GenericCellFactory genericCellFactory = new GenericCellFactory();

        assertThat(genericCellFactory.isBindable(mMessage), is(true));
    }

    @Test
    public void testGetPreviewText() {
        GenericCellFactory genericCellFactory = new GenericCellFactory();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertThat(genericCellFactory.getPreviewText(context, mMessage).isEmpty(), is(false));
    }
}
