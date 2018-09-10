package gov.sag.cache.loaders.maindriver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.instrument.Instrumentation;

/**
 * Created by fabien.sanglier on 9/7/18.
 */
public class ObjectSizeFetcherAgent {
    private static final Logger logger = LoggerFactory.getLogger(ObjectSizeFetcherAgent.class);

    private static Instrumentation globalInstrumentation;

    public static void premain(String args, Instrumentation inst) {
        globalInstrumentation = inst;
    }

    public static long getObjectSize(final Object object) {
        if (globalInstrumentation == null) {
            throw new IllegalStateException("Agent not initialized.");
        }
        return globalInstrumentation.getObjectSize(object);
    }

    public static int serializedSizeOf(Object obj) {
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            objectOutputStream.close();

            return byteOutputStream.toByteArray().length;
        } catch (IOException e) {
            return -1;
        }
    }

    public static void printSize(String printMessage, String obj){
        long sizeHeap = 0L, sizeSerialized = 0L;

        if(obj != null){
            try {
                sizeHeap = ObjectSizeFetcherAgent.getObjectSize(obj);
            } catch (IllegalStateException ise){
                logger.info("Can't calculate size of object on heap", ise);
                sizeHeap = -1;
            }
            sizeSerialized = ObjectSizeFetcherAgent.serializedSizeOf(obj);
        }

        logger.info(((null != printMessage)?printMessage+":":"") + "Size on heap = {} bytes", sizeHeap);
        logger.info(((null != printMessage)?printMessage+":":"") + "Serialized size  = {} bytes", sizeSerialized);
    }
}
