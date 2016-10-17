/*
 * Copyright 2016 Daniel Taylor Harrison
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This work contains code from and code modeled after Google Inc.'s Grafika project, specifically the
 * classes Texture2dProgram.java, EglCore.java, ContinuousCaptureActivity.java, EglSurfaceBase.java,
 * Drawable2d.java, and FullFrameRect.java. This work also contains code from and code modeled after
 * The Android Open Source Project's Compatibility Test Suite project (specifically the jb-mr2-release branch)
 * Any code from the Grafika project or the Compatibility Test Suite project that is included in this work
 * may have been modified from its original form by the author of this work.
 * The Grafika project contains the following copyright notice:
 * Copyright 2013 Google Inc. All rights reserved.
 * The Compatibility Test Suite project contains the the following copyright notice:
 * Copyright (C) 2013 The Android Open Source Project
 *
 */

package com.example.taylor.gipheed.GifPlaying;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import com.example.taylor.gipheed.Activities.GifDetailActivity;
import com.example.taylor.gipheed.Activities.GifDetailActivityV2;
import com.example.taylor.gipheed.OpenGL.GifEditingThread;
import com.example.taylor.gipheed.ThreadManager;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGL10;

import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;

/**
 * Created by Taylor on 9/14/2016.
 */
public class MovieDecoder {

    private static final String TAG = "MovieDecoder";

    // Seeking variables
    private MediaCodec vidDecoder;
    private MediaExtractor vidExtractor;
    private int frameCount;
    private long lastSampleTime;

    private float width;
    private float height;

    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    private volatile boolean stopPlaybackFlag = false;

    private DecodeCallback decodeCallback;

    private SeekRunnable seekRunnable;

    public MovieDecoder(DecodeCallback decodeCallback) {
        this.decodeCallback = decodeCallback;
    }

    public void getVideoSize(String videoUrl) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(videoUrl);
        String duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        width = Float.parseFloat(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        height = Float.parseFloat(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        Log.v(TAG, "duration: " + duration + " " + lastSampleTime);
        metadataRetriever.release();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void getVideoMetaData(String videoUrl) {
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(videoUrl);
            int videoTrack = getVideoTrack(mediaExtractor);
            mediaExtractor.selectTrack(videoTrack);
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(videoTrack);
            Log.v(TAG, "mediaFormat mimeType: " + mediaFormat.getString(MediaFormat.KEY_MIME));

            boolean dataRemaining = true;
            frameCount = 0;
            lastSampleTime = 0;
            while (dataRemaining) {
                dataRemaining = mediaExtractor.advance();
                if(mediaExtractor.getSampleTime() != -1) {
                    lastSampleTime = mediaExtractor.getSampleTime();
                    frameCount++;
                }
            }
            Log.v(TAG, "number of frames: " + frameCount + " " + lastSampleTime);
            mediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

//            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
//            metadataRetriever.setDataSource(videoUrl);
//            String duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            width = Float.parseFloat(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
//            height = Float.parseFloat(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
//
//            Log.v(TAG, "duration: " + duration + " " + lastSampleTime);
//            metadataRetriever.release();

            if(decodeCallback != null) {
                decodeCallback.metaDataRetrieved(mediaFormat.getInteger(MediaFormat.KEY_WIDTH), mediaFormat.getInteger(MediaFormat.KEY_HEIGHT), frameCount, lastSampleTime);
            }
        } catch(Exception e) {
            Log.v(TAG, e.getMessage());
        }
    }

    public void prepForSeeking(String videoUrl, Surface surface, GifEditingThread gifEditingThread) {
        getVideoMetaData(videoUrl);
        try {
            vidExtractor= new MediaExtractor();
            vidExtractor.setDataSource(videoUrl);
            int videoTrack = getVideoTrack(vidExtractor);
            vidExtractor.selectTrack(videoTrack);
            MediaFormat mediaFormat = vidExtractor.getTrackFormat(videoTrack);
            Log.v(TAG, "mediaFormat mimeType: " + mediaFormat.getString(MediaFormat.KEY_MIME));
            boolean dataRemaining = true;
            int frames = 0;
            while(dataRemaining) {
                dataRemaining = vidExtractor.advance();
                frames++;
            }
            Log.v(TAG, "number of frames: " + frames);
            vidExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

//            MediaCodecInfo format = selectCodec(mediaFormat.getString(MediaFormat.KEY_MIME));
//            Log.v(TAG, "createDecoderByType: " + format.getName());
            vidDecoder = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
            vidDecoder.configure(mediaFormat, surface, null, 0);
            vidDecoder.start();


            seekRunnable = new SeekRunnable(vidDecoder, vidExtractor, frameCount, lastSampleTime);
            seekRunnable.setGifEditingThread(gifEditingThread);
            ThreadManager.Run(seekRunnable);

        } catch (IOException e) {
            Log.e(TAG, "decode error: " + e.getMessage());
        }
    }

    private int lastFrameSought;
    public void goToFrame(int frameNumber) {
        Log.v(TAG, "go to frame: " + frameNumber);
        if(lastFrameSought < frameNumber) {
            getFrame(frameNumber, false);
        } else {
            stopPlaybackFlag = true;
            getFrame(frameNumber, true);
        }
        lastFrameSought = frameNumber;
    }

    private synchronized void getFrame(int frameNumber, boolean needSeek) {
        ByteBuffer[] inputBuffers = vidDecoder.getInputBuffers();

        // Calculate the target presentationTime
        long targetPresTime = (long) (((float)frameNumber / (float)frameCount)*(float)lastSampleTime);

        if(needSeek) {
            if(lastFrameSought < frameNumber) {
                return;
            }
            vidDecoder.flush();
            vidExtractor.seekTo(targetPresTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        } else {
            if(lastFrameSought > frameNumber) {
                return;
            }
        }

        stopPlaybackFlag = false;
        boolean inputDone = false;
        boolean outputDone = false;
        while(!stopPlaybackFlag && !outputDone) {
            Log.v(TAG, "seek decoding work loop");
//            if(!inputDone) {
                int inputBufferId = vidDecoder.dequeueInputBuffer(20000);
                if (inputBufferId >= 0) {
                    int chunkSize = vidExtractor.readSampleData(inputBuffers[inputBufferId], 0);

                    if (chunkSize >= 0) {
                        long presentationTime = vidExtractor.getSampleTime();

                        if(presentationTime >= targetPresTime) {
                            Log.v(TAG, "input done: " + presentationTime + " " + targetPresTime);
                            inputDone = true;
                        }
                        // fill inputBuffers[inputBufferId] with valid data
                        vidDecoder.queueInputBuffer(inputBufferId, 0, chunkSize, presentationTime, 0);
                    }
//                    else {
//                        // End of input
//                        Log.v(TAG, "queueing end of stream input buffer");
//                        vidDecoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                        inputDone = true;
//                    }
                    vidExtractor.advance();
                }
//            }


            int outputBufferId = vidDecoder.dequeueOutputBuffer(bufferInfo, 20000);
            Log.v(TAG, "outputBufferId: " + outputBufferId);

            if (outputBufferId >= 0) {
                if(bufferInfo.size != 0) {
                    if(bufferInfo.presentationTimeUs >= targetPresTime) {
                        outputDone = true;
                        Log.v(TAG, "rendering frame: " + bufferInfo.presentationTimeUs + " " + targetPresTime);
                    }
                    vidDecoder.releaseOutputBuffer(outputBufferId, outputDone);
                }

//                if((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM)!= 0) {
//                    vidExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//                    vidDecoder.flush();
//                    startTime = System.currentTimeMillis();
//                    inputDone = false;
//                    Log.v(TAG, "decoder flushed and extractor seeked to beginning");
////                    break;
//                }

            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                outputBuffers = codec.getOutputBuffers();
            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                MediaFormat format = vidDecoder.getOutputFormat();
            }
        }
        stopPlaybackFlag = false;
//        vidDecoder.flush();
    }

    public void releaseResources() {
        if(vidDecoder != null) {
            vidDecoder.stop();
            vidDecoder.release();
            vidDecoder = null;
        }
        if(vidExtractor != null) {
            vidExtractor.release();
            vidExtractor = null;
        }
    }

    public void decode(String videoUrl, Surface surface) {

        Log.v(TAG, "decode videoUrl: " + videoUrl);
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(videoUrl);
            int videoTrack = getVideoTrack(mediaExtractor);
            mediaExtractor.selectTrack(videoTrack);
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(videoTrack);
            Log.v(TAG, "mediaFormat mimeType: " + mediaFormat.getString(MediaFormat.KEY_MIME));
            boolean dataRemaining = true;
            int frames = 0;
            while(dataRemaining) {
                dataRemaining = mediaExtractor.advance();
                frames++;
            }
            Log.v(TAG, "number of frames: " + frames);
            mediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

//            MediaCodecInfo format = selectCodec(mediaFormat.getString(MediaFormat.KEY_MIME));
//            Log.v(TAG, "createDecoderByType: " + format.getName());
            MediaCodec vidDecoder = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
            vidDecoder.configure(mediaFormat, surface, null, 0);
            vidDecoder.start();

            playOnSurface(vidDecoder, mediaExtractor);

        } catch (IOException e) {
            Log.e(TAG, "decode error: " + e.getMessage());
        }
    }

    private int getVideoTrack(MediaExtractor mediaExtractor) {
        int trackCount = mediaExtractor.getTrackCount();
        for(int i = 0; i < trackCount; i++) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            if(format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for(int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if(!codecInfo.isEncoder()) {
                continue;
            }

            String[] supportedTypes = codecInfo.getSupportedTypes();
            for(String type : supportedTypes) {
                Log.v(TAG, "codec match for: " + type + "?");
                if(type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private synchronized void playOnSurface(MediaCodec vidDecoder, MediaExtractor extractor) {

        ByteBuffer[] inputBuffers = vidDecoder.getInputBuffers();
        long startTime = System.currentTimeMillis();

        boolean inputDone = false;
        while(!stopPlaybackFlag) {
            Log.v(TAG, "decoding work loop");

            if(!inputDone) {
                int inputBufferId = vidDecoder.dequeueInputBuffer(20000);
                if (inputBufferId >= 0) {
                    int chunkSize = extractor.readSampleData(inputBuffers[inputBufferId], 0);

                    if (chunkSize >= 0) {
                        long presentationTime = extractor.getSampleTime();

                        // fill inputBuffers[inputBufferId] with valid data
                        vidDecoder.queueInputBuffer(inputBufferId, 0, chunkSize, presentationTime, 0);
                    } else {
                        // End of input
                        Log.v(TAG, "queueing end of stream input buffer");
                        vidDecoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                    }
                    extractor.advance();
                }
            }
            

            int outputBufferId = vidDecoder.dequeueOutputBuffer(bufferInfo, 20000);
            Log.v(TAG, "bufferInfo.flags: " + bufferInfo.flags);
            if (outputBufferId >= 0) {
                if(bufferInfo.size != 0) {
                    while(bufferInfo.presentationTimeUs / 1000 > (System.currentTimeMillis() - startTime)) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    vidDecoder.releaseOutputBuffer(outputBufferId, true);
                    Log.v(TAG, "rendering frame");
                }

                if((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM)!= 0) {
                    extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    vidDecoder.flush();
                    startTime = System.currentTimeMillis();
                    inputDone = false;
                    Log.v(TAG, "decoder flushed and extractor seeked to beginning");
//                    break;
                }

            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                outputBuffers = codec.getOutputBuffers();
            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                MediaFormat format = vidDecoder.getOutputFormat();
            }
        }

        stopPlaybackFlag = false;
        vidDecoder.stop();
        vidDecoder.release();
        extractor.release();
    }

    public void setStopPlaybackFlag(boolean stopPlaybackFlag) {
        this.stopPlaybackFlag = stopPlaybackFlag;
    }

    public void goToFrame2(int frame) {
        seekRunnable.goToFrame(frame);
    }

    public void stopSeeking() {
        seekRunnable.stopSeeking();
    }

    public void onlyRenderTarget() {
        seekRunnable.onlyRenderTarget();
    }

    public interface DecodeCallback {
        void metaDataRetrieved(int width, int height, int numberOfFrames, long lastSampleTime);
    }


    private static class SeekRunnable implements Runnable {

        private MediaCodec vidDecoder;
        private MediaExtractor vidExtractor;
        private int frameCount;
        private long lastSampleTime = 0;

        private int targetFrame = 0;
        private int lastTargetFrame = 0;

        private boolean stopSeeking = false;
        private boolean shouldRender = false;

        private GifEditingThread gifEditingThread;

        public SeekRunnable(MediaCodec vidDecoder, MediaExtractor vidExtractor, int frameCount, long lastSampleTime) {
            this.vidDecoder = vidDecoder;
            this.vidExtractor = vidExtractor;
            this.frameCount = frameCount;
            this.lastSampleTime = lastSampleTime;
        }

        public void setGifEditingThread(GifEditingThread gifEditingThread) {
            this.gifEditingThread = gifEditingThread;
        }

        public synchronized void goToFrame(int targetFrame) {
            this.targetFrame = targetFrame;
        }

        public void stopSeeking() {
            this.stopSeeking = true;
        }

        public synchronized void onlyRenderTarget() {
            shouldRender = false;
        }

        @Override
        public void run() {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] inputBuffers = vidDecoder.getInputBuffers();
            long targetPresTime = 0;
            boolean outputDone = true;
            // We want all frames to render when seeking forward, but only the target frame should render when seeking backwards
            shouldRender = true;

            while(!stopSeeking) {
                if(targetFrame != lastTargetFrame) {
                    // Calculate the target presentationTime
                    targetPresTime = (long) (((float) targetFrame / (float) frameCount) * (float) lastSampleTime);

                    if (targetFrame < lastTargetFrame) {
                        vidDecoder.flush();
                        // dont think this is necessary
//                        inputBuffers = vidDecoder.getInputBuffers();
                        vidExtractor.seekTo(targetPresTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                        shouldRender = false;
                    } else {
                        shouldRender = true;
                    }

                    // We want to start reading frames
                    outputDone = false;

                    lastTargetFrame = targetFrame;
                }

                if(!outputDone) {
                    Log.v(TAG, "seek decoding work loop");
                    int inputBufferId = vidDecoder.dequeueInputBuffer(20000);
                    if (inputBufferId >= 0) {
                        int chunkSize = vidExtractor.readSampleData(inputBuffers[inputBufferId], 0);

                        if (chunkSize >= 0) {
                            long presentationTime = vidExtractor.getSampleTime();

                            if (presentationTime >= targetPresTime) {
                                Log.v(TAG, "input done: " + presentationTime + " " + targetPresTime);
                            }
                            // fill inputBuffers[inputBufferId] with valid data
                            vidDecoder.queueInputBuffer(inputBufferId, 0, chunkSize, presentationTime, 0);
                        }
                        vidExtractor.advance();
                    }

                    int outputBufferId = vidDecoder.dequeueOutputBuffer(bufferInfo, 20000);
                    Log.v(TAG, "outputBufferId: " + outputBufferId);

                    if (outputBufferId >= 0) {
                        if (bufferInfo.size != 0) {
                            if (bufferInfo.presentationTimeUs >= targetPresTime) {
                                outputDone = true;
                                shouldRender = true;
                                Log.v(TAG, "rendering frame: " + bufferInfo.presentationTimeUs + " " + targetPresTime);
                            }
                            vidDecoder.releaseOutputBuffer(outputBufferId, shouldRender);
                            if(shouldRender && gifEditingThread != null) {
                                gifEditingThread.updateFrame();
                            }
                        }
                    } else if (outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                      outputBuffers = codec.getOutputBuffers();
                    } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                      MediaFormat format = vidDecoder.getOutputFormat();
                    }
                }
            }
        }
    }
}
