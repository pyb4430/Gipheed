package com.example.taylor.gipheed.GifReading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by Taylor on 8/20/2016.
 */
public class GifReader {

    private static final String TAG = "GifReader";
    private static final int TRAILER_BYTE = 0x3B;
    private static final int IMAGE_DESCRIPTOR = 0x2C;
    private static final int EXTENSION_BYTE = 0x21;
    private static final int GRAPHIC_CONTROL_EXTEN = 0xF9;
    private static final int APPLICATION_EXTENSION = 0xFF;

    private byte[] byteBuffer = new byte[10];
    private int byteRead;
    private Context context;

    public GifReader(Context context) {
        this.context = context;
    }

    private int width;
    private int height;

    private boolean isGct;
    private int colorResBits;
    private int gctResBits;
    private int backColor;

    private int[] gct;

    private int[] activeColorTable;

    // counter
    private int frameCounter;

    //local frame data
    private int location_x = -1;
    private int location_y = -1;
    private int frameWidth = -1;
    private int frameHeight = -1;
    private boolean isLct = false;
    private boolean isInterlace = false;
    private int lctResBits = 0;
    private int[] lct;

    private Bitmap bm;
    private ArrayList<Bitmap> bms = new ArrayList<>();
    private Canvas canvas;

    private int clearCode;
    private int eoiCode;
    private int constrTableSize;
    private ArrayList<ArrayList<Integer>> constructionTable = new ArrayList<>();
    private int minCodeSize;
    private int currentCodeSize;
    private boolean firstCode;
    private int lastCode;

    // Animation vars
    private int repeatTimes;
    private int disposalMethod;
    private boolean transparencyFlag;
    private int delayTime;
    private int transparentColorIndex;

    ArrayList<GifFrame> gifFrames;

    FrameChangeListener frameChangeListener;

    public ArrayList<Bitmap> load(int gifResourceId, FrameChangeListener frameChangeListener) {
        this.frameChangeListener = frameChangeListener;
        InputStream is = context.getResources().openRawResource(gifResourceId);
        BufferedInputStream bis = new BufferedInputStream(is);

        gifFrames = new ArrayList<>();

        readHeader(bis);
        if(isGct) {
            gct = readColorTable(gctResBits, bis);
            activeColorTable = gct;
        }

        int byteCounter = 0;
        frameCounter = 0;
        try {
            byteRead = bis.read();
            Log.v(TAG, "top level byte read : " + Integer.toHexString(byteRead));
            while (byteRead != -1) {
                if(byteRead == EXTENSION_BYTE) {
                    Log.v(TAG, "extension reached");
                    readExtension(bis);
//                    if(frameCounter > 4) {
//                        break;
//                    }
                } else if(byteRead == IMAGE_DESCRIPTOR) {
                    frameCounter++;
                    Log.v(TAG, "frame count: "+ frameCounter + " byteCount " + byteCounter);
//                    break;
                    if (readDescriptorBlock(bis)) {
                        if(isLct) {
                            lct = readColorTable(lctResBits, bis);
                            activeColorTable = lct;
                        }
                        readImage(bis);
                        activeColorTable = gct;
//                        break;
                    }
                } else {
                    byteCounter++;
                }
                byteRead = bis.read();
                Log.v(TAG, "top level byte read : " + Integer.toHexString(byteRead));

            }
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
        }

//        animate();

        return bms;
    }

    private void readHeader(BufferedInputStream bis) {
        try {
            // Read the file identifier
            byte[] bytes = new byte[6];
            bis.read(bytes, 0, 6);
            String fileType = new String(bytes);
            if(fileType.equals("GIF89a")) {
                Log.v(TAG, "file type gif");
            }

            // Read GIF size
            bytes = new byte[4];
            bis.read(bytes, 0, 4);
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            width = bb.getShort(0);
            height = bb.getShort(2);
            Log.v(TAG, "gif image size: " + width + " " + height);

            // Read the global stacked byte
            byteRead = bis.read();
            isGct = byteRead >> 7 == 1;
            colorResBits = (byteRead & 0b01110000) >> 4;
            gctResBits = (byteRead & 0b00000111);
            Log.v(TAG, "gif fields: " + isGct + " " + colorResBits + " " + gctResBits + " " + byteRead);

            // Read the background color
            backColor = bis.read();
            bis.read();
            Log.v(TAG, "backColor " + backColor);
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
        }
    }

    private void readExtension(BufferedInputStream bis) {
        try {
            byteRead = bis.read();

            if(byteRead == GRAPHIC_CONTROL_EXTEN) {
                Log.v(TAG, "Reading Graphic Control Extension");
                int subBlockLength = 0;
                subBlockLength = bis.read();
                Log.v(TAG, "GC extension, subBlockLength: " + subBlockLength);
                if(subBlockLength != 4) {
                    Log.v(TAG, "length of graphic control extension not 4");
                    // Throw exception?
                }

                // Read frame disposal method and transparency flag
                byteRead = bis.read();
                disposalMethod = (0b00011100 & byteRead) >> 2;
                transparencyFlag = (0b00000001 & byteRead) == 1;
                Log.v(TAG, "GC extension, disposal method: " + disposalMethod + ", transparency flag: " + transparencyFlag);

                // Read the index of the transparent color in the color table
                byte[] bytes = new byte[2];
                bis.read(bytes, 0, 2);
                transparentColorIndex = bis.read();
                Log.v(TAG, "GC extension, transparent color index: " + transparentColorIndex);

                // Read the delay before the next frame (100ths of seconds)
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                delayTime = bb.getShort();
                Log.v(TAG, "GC extension, inter-frame delay time: " + delayTime);

                // End of the extension
                subBlockLength = bis.read();
                Log.v(TAG, "GC extension, next subBlock should be 0: " + subBlockLength);
                if(subBlockLength != 0) {
                    Log.v(TAG, "length of graphic control extension not 4");
                    // Throw exception?
                }

            } else if (byteRead == APPLICATION_EXTENSION) {
                Log.v(TAG, "Reading Application Extension");
                int subBlockLength = bis.read();
                Log.v(TAG, "extension subBlockLength: " + subBlockLength);

                // Read the Application Version/Type
                String application = "";
                for(int i = 0; i < subBlockLength; i++) {
                    byteRead = bis.read();
                    application = application + (char)byteRead;
                }
                Log.v(TAG, "extension, Application Version: " + application);

                if(application.equals("NETSCAPE2.0")) {
                    subBlockLength = bis.read();
                    byteRead = bis.read();
                    Log.v(TAG, "extension, subBlockLength: " + subBlockLength);

                    // Get number of times GIF should repeat
                    byte[] bytes = new byte[2];
                    bis.read(bytes, 0, 2);
                    byteRead = bis.read();
                    ByteBuffer bb = ByteBuffer.wrap(bytes);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    repeatTimes = bb.getShort();
                    Log.v(TAG, "extension, repeat times: " + repeatTimes);
                    Log.v(TAG, "extension, next block should be 0: " + byteRead);

                } else {
                    // Read other extensions, no use for them yet
                    while(subBlockLength != 0) {
                        subBlockLength = bis.read();
                        String data = "";
                        for (int i = 0; i < subBlockLength; i++) {
                            byteRead = bis.read();
                            data = data + (char)byteRead;
                        }
                        if(application.equals("XMP DataXMP")) {
                            Log.v(TAG, "extension, XMP metadata: " + data);
                        } else {
                            Log.v(TAG, "extension, unknown block, data: " + data);
                        }
                    }
                }

            }
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
        }
    }

    private boolean readDescriptorBlock(BufferedInputStream bis) {
        try {
            // Get location of this block in the frame
            byte[] bytes = new byte[4];
            bis.read(bytes, 0, 4);
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            location_x = bb.getShort(0);
            location_y = bb.getShort(2);

            // Get the size of this block
            bis.read(bytes, 0, 4);
            bb.clear();
            bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            frameWidth = bb.getShort(0);
            frameHeight = bb.getShort(2);
//          if(location_x < 0 || location_x > width || location_y < 0 || location_y > height) {
//              return false;
//          }
            Log.v(TAG, "image location: " + location_x + " " + location_y + " " + frameWidth + " " + frameHeight);

            // Get local stacked byte
            byteRead = bis.read();
            isLct = (byteRead >> 7) == 1;
            isInterlace = ((byteRead & 0b01000000) >> 6) == 1;
            lctResBits = (byteRead & 0b00000111);
            Log.v(TAG, "lct present: " + (byteRead >> 7) +  " " + ((byteRead & 0b01000000) >> 6) + " " + ((byteRead & 0b00100000) >> 5) + " " + ((byteRead & 0b00000111)) + " " + byteRead);

            // Read descriptor block success
            return true;
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
            return false;
        }
    }

    boolean eoiReached = false;
    private void readImage(BufferedInputStream bis) {
        // Clear colors
        colors.clear();

        minCodeSize = 0;
        currentCodeSize = 0;

        int subBlockSize = 1;

        int code = 0;
        int bitsLeft;
        int bitsNeeded;
        int bytesRead;

        try {
            minCodeSize = bis.read()+1;
            currentCodeSize = minCodeSize;
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
        }
        Log.v(TAG, "lzw min code size: " + minCodeSize);

        constructionTable.clear();
        clearCode = (int)Math.pow(2, minCodeSize-1);
        eoiCode = clearCode + 1;
        constrTableSize = eoiCode;

        bitsNeeded = currentCodeSize;

        ArrayList<Integer> codes = new ArrayList<>();

        Log.v(TAG, "frameCount: " + frameCounter);
        while(subBlockSize > 0 && !eoiReached) {
            try {
                subBlockSize = bis.read();
            } catch (IOException e) {
                Log.v(TAG, e.getMessage());
            }
            Log.v(TAG, "number of bytes in subblock: " + subBlockSize + " frameCount: " + frameCounter);

            //add this in when ready
            bytesRead = 0;

            while(bytesRead < subBlockSize) {
                try {
                    byteRead = bis.read();
                } catch (IOException e) {
                    Log.v(TAG, e.getMessage());
                }

                bitsLeft = 8;
                bytesRead++;
                Log.v(TAG, "    byte #" + bytesRead + "/" + subBlockSize);
                Log.v(TAG, "    byteRead: " + Integer.toBinaryString(byteRead) + "  bitsNeeded: " + bitsNeeded + "  bits remaining: " + bitsLeft);


                while(bitsLeft > 0) {
                    if(bitsNeeded <= bitsLeft) {
                        // lop off the bits before the ones we want then shift our relevant bits to the lsb location. THEN shift them to the left by however many bits of the code
                        // we already have before finally or-ing that with the the existing state of the code
                        code = (((((byteRead  >> (8-bitsLeft)) << (bitsLeft - bitsNeeded)) & 0b11111111) >> (bitsLeft - bitsNeeded)) << (currentCodeSize-bitsNeeded)) | code;

                        addCode(code);
                        codes.add(code);

//                        Log.v(TAG, "da code: " + Integer.toBinaryString(code) + " " + bitsLeft + " " + bitsNeeded);
                        bitsLeft -= bitsNeeded;
                        bitsNeeded = currentCodeSize;
                        code = 0;
                        Log.v(TAG, "        took some bits, remaining: " + bitsLeft + ", now need: " + bitsNeeded);
                    } else {
                        Log.v(TAG, "        taking remaining bits, remaining: " + bitsLeft + ", need: " + bitsNeeded);
                        code = ((byteRead >> (8-bitsLeft)) << (currentCodeSize-bitsNeeded)) | code;
                        bitsNeeded -= bitsLeft;
                        bitsLeft = 0;
                        Log.v(TAG, "        took remaining bits, remaining: " + bitsLeft + ", still need: " + bitsNeeded);
                    }
                }

            }
//            for(int i : colors) {
//                Log.v(TAG, "color  " + i);
//            }
        }

        Integer[] codes1 = new Integer[codes.size()];
        gifFrames.add(new GifFrame(codes.toArray(codes1), activeColorTable, location_x, location_y, frameWidth, frameHeight, delayTime, disposalMethod));

//        bm = Bitmap.createBitmap(colors.toArray(), width, height, Bitmap.Config.ARGB_8888);

        Log.v(TAG, "color  length: " + colors.size());
        int[] colorArray = new int[width*height];
        for(int i = 0; i<colors.size(); i++) {
//                Log.v(TAG, "color  " + Integer.toHexString(i));
            colorArray[i] = colors.get(i);
        }
        if(frameCounter < 2) {
            bm = Bitmap.createBitmap(colorArray, width, height, Bitmap.Config.ARGB_8888);
            bm = bm.copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(bm);
            bms.add(bm.copy(Bitmap.Config.ARGB_8888, false));
        } else {
            Bitmap bm2 = Bitmap.createBitmap(colorArray, frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
            canvas.drawBitmap(bm2, location_x, location_y, null);
            bm2.recycle();
            bms.add(bm.copy(Bitmap.Config.ARGB_8888, false));
        }

//        try {
//            File file = new File(context.getCacheDir(), "gif_frame_" + frameCounter + ".png");
//            FileOutputStream os = new FileOutputStream(file);
//            bm.compress(Bitmap.CompressFormat.PNG, 0, os);
//            os.close();
//        } catch (Exception e) {
//            Log.v(TAG, e.getMessage());
//        }
//        LruCache<Integer, Bitmap> bitmapLruCache = new LruCache<>(Runtime.getRuntime().maxMemory()/4);
//        bitmapLruCache.put(frameCounter, bm);
        eoiReached = false;

//        eoiReached = true;


//        try {
//            byteRead = bis.read();
//        } catch (IOException e) {
//            Log.v(TAG, e.getMessage());
//        }
////        Log.v(TAG, "first byte: " + byteRead);
//        if(currentCodeSize > 8) {
//
//        }
//
//
//        try {
//            byteRead = bis.read();
//        } catch (IOException e) {
////            Log.v(TAG, e.getMessage());
//        }
//
//
////        Log.v(TAG, "second byte: " + byteRead);
//        while (byteRead != 0) {
//            for (int i = byteRead-1; i >= 0; i--) {
//                try {
//                    byteRead = bis.read();
//                    if(byteRead < gct.length && byteRead > 0) {
//                        bm.setPixel(col, row, gct[byteRead]);
//////                        Log.v(TAG, "color shit " +  gct[byteRead]);
//                    }
//                    col++;
//                    if(col >= width) {
//                        col = 0;
//                        row++;
//                        if(row >= height) {
//                            break;
//                        }
//                    }
//                } catch (IOException e) {
////                    Log.v(TAG, e.getMessage());
//                }
//            }
//        }
    }

    ArrayList<Integer> colors = new ArrayList<>();
    boolean resetCodeTable = false;
    // Add code to the colors list
    private void addCode(int code) {
        Log.v(TAG, "            colorCode: constrTableSize: " + constrTableSize + "    clearCode: " + clearCode + "    eoiCode: " + eoiCode);
        Log.v(TAG, "            colorCode: code: " + code);

        // Handle the code based on its presence in the color table, outside all tables, in the
        // construction table,or if its the clear code or eoi code.
        if(code < clearCode) {
            colors.add(activeColorTable[code]);

            // First actual code doesn't get added to the constructionTable. For subsequent codes in the
            // color table, add new entries in the constructionTable according to algorithm.
            if(firstCode) {
                firstCode = false;
            } else {
                if(lastCode < clearCode) {
                    ArrayList<Integer> codeToAdd = new ArrayList<Integer>();
                    codeToAdd.add(activeColorTable[lastCode]);
                    codeToAdd.add(activeColorTable[code]);
                    constructionTable.add(codeToAdd);
                } else {
                    ArrayList<Integer> codeToAdd = new ArrayList<Integer>(constructionTable.get(lastCode-eoiCode-1));
                    codeToAdd.add(activeColorTable[code]);
                    constructionTable.add(codeToAdd);
                }
                constrTableSize++;
            }
        } else if(code > constrTableSize) {

            // Add code to the colors and the constructionTable according to algorithm
            ArrayList<Integer> codeToAdd = new ArrayList<Integer>();
            if(lastCode < clearCode) {
                codeToAdd.add(activeColorTable[lastCode]);
                codeToAdd.add(activeColorTable[lastCode]);
            } else {
                codeToAdd.addAll(constructionTable.get(lastCode-eoiCode-1));
                codeToAdd.add(constructionTable.get(lastCode-eoiCode-1).get(0));
            }
            colors.addAll(codeToAdd);
            constructionTable.add(codeToAdd);
            constrTableSize++;

        } else if(code > eoiCode && eoiCode <= constrTableSize) {

            colors.addAll(constructionTable.get(code-eoiCode-1));
            if(lastCode < clearCode) {
                ArrayList<Integer> codeToAdd = new ArrayList<Integer>();
                codeToAdd.add(activeColorTable[lastCode]);
                codeToAdd.add(constructionTable.get(code-eoiCode-1).get(0));
                constructionTable.add(codeToAdd);
            } else {
                ArrayList<Integer> codeToAdd = new ArrayList<Integer>(constructionTable.get(lastCode-eoiCode-1));
                codeToAdd.add(constructionTable.get(code-eoiCode-1).get(0));
                constructionTable.add(codeToAdd);
            }
            constrTableSize++;

        } else if(code == clearCode) {
            // We have reached the max number of codes, reset the constructionTable (size is eoiCode),
            // and firstCode is coming
            constructionTable.clear();
            constrTableSize = eoiCode;
            firstCode = true;
        } else if(code == eoiCode) {
            eoiReached = true;
        }

        // Reset the code table
        if(resetCodeTable) {
            currentCodeSize = minCodeSize;
            resetCodeTable = false;
        }

        if(constrTableSize > (int)Math.pow(2, currentCodeSize)-2) {
            if(currentCodeSize < 12) {
                currentCodeSize++;
                Log.v(TAG, "            colorCode: increase the currentCodeSize to: " + currentCodeSize);
            } else {
//                currentCodeSize = minCodeSize;
                resetCodeTable = true;
                Log.v(TAG, "            colorCode: currentCodeSize reset to: " + currentCodeSize);
            }
        }
        lastCode = code;
        Log.v(TAG, "            colorCode: code read, constrTableSize " + constrTableSize + ", actual size: " + constructionTable.size());
    }

    private int[] readColorTable(int tableResBits, BufferedInputStream bis) {
        byte[] bytes = new byte[3];

        int[] colorTable = new int[(int)Math.pow(2, (tableResBits+1))];

        Log.v(TAG, "color table size: " + Math.pow(2, (tableResBits+1)));
        for(int i = 0; i < Math.pow(2, (tableResBits+1)); i++) {
            try {
                bis.read(bytes, 0, 3);
            } catch (IOException e) {
                Log.v(TAG, e.getMessage());
            }
            Log.v(TAG, "color read: " + Integer.toHexString((((bytes[0] << 16) | (bytes[1] << 8))| bytes[2]) | 0xff000000));
            if(transparencyFlag && i == transparentColorIndex) {
                colorTable[i] = 0x00000000;
            } else {
                colorTable[i] = ((((bytes[0] << 16) & 0x00ff0000) | ((bytes[1] << 8) & 0x0000ff00)) | (bytes[2] & 0x000000ff)) | 0xff000000;
            }
//            gct.add(i, new int[] {bytes[0], bytes[1], bytes[2]});
        }

        return colorTable;
    }

    private void animate() {
        frameCounter = 1;
        for(int i = 0; i < gifFrames.size(); i++) {
//            activeColorTable = frame.colorTable;
//
//            constructionTable.clear();
//            clearCode = (int)Math.pow(2, minCodeSize-1);
//            eoiCode = clearCode + 1;
//            constrTableSize = eoiCode;
//
//            for (Integer i : frame.codes) {
//                if(!eoiReached) {
//                    addCode(i);
//                } else {
//                    break;
//                }
//            }
//            eoiReached = false;
//
//            int[] colorArray = new int[width*height];
//            for(int i = 0; i<colors.size(); i++) {
    //                Log.v(TAG, "color  " + Integer.toHexString(i));
//                colorArray[i] = colors.get(i);
//            }
//            if(frameCounter < 2) {
//                bm = Bitmap.createBitmap(colorArray, width, height, Bitmap.Config.ARGB_8888);
//                bm = bm.copy(Bitmap.Config.ARGB_8888, true);
//                canvas = new Canvas(bm);
//            } else {
//                Bitmap bm2 = Bitmap.createBitmap(colorArray, frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
//                canvas.drawBitmap(bm2, location_x, location_y, null);
//                bm2.recycle();
//            }


            frameChangeListener.onFrameChange(bms.get(i));
            try {
                Thread.sleep(gifFrames.get(i).frameDelay * 10);
            } catch (Exception e) {

            }
//            break;
        }
    }

    public interface FrameChangeListener {
        void onFrameChange(Bitmap bitmap);
    }

    public class GifFrame {

        int[] colorTable;
        Integer[] codes;
        int left;
        int top;
        int width;
        int height;

        // animation stuff
        int disposalMethod;
        int frameDelay;

        public GifFrame(Integer[] codes, int[] colorTable, int left, int top, int width, int height, int frameDelay, int disposalMethod) {
            this.colorTable = colorTable;
            this.codes = codes;
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
            this.frameDelay = frameDelay;
            this.disposalMethod = disposalMethod;
        }


    }
}
