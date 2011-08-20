/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package org.selfkleptomaniac.ti.imageasresized;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.content.res.AssetManager;

@Kroll.module(name="Imageasresized", id="org.selfkleptomaniac.ti.imageasresized")
public class ImageasresizedModule extends KrollModule
{

  // Standard Debugging variables
  private static final String LCAT = "ImageasresizedModule";
  private static final boolean DBG = TiConfig.LOGD;

  // You can define constants with @Kroll.constant, for example:
  // @Kroll.constant public static final String EXTERNAL_NAME = value;
  
  public ImageasresizedModule(TiContext tiContext) {
    super(tiContext);
  }

  // Methods
  @Kroll.method
  public String example() {
    Log.d(LCAT, "example called");
    return "hello world";
  }
  
  // Properties
  @Kroll.getProperty
  public String getExampleProp() {
    Log.d(LCAT, "get example property");
    return "hello world";
  }
  
  
  @Kroll.setProperty
  public void setExampleProp(String value) {
    Log.d(LCAT, "set example property: " + value);
  }

  @Kroll.method
  public TiBlob cameraImageAsResized(TiBlob image, int width, int height){
    byte[] image_data = image.getBytes();

    try{
      BitmapFactory.Options opts = new BitmapFactory.Options();
      opts.inJustDecodeBounds = true;

      BitmapFactory.decodeByteArray(image_data, 0, image_data.length, opts);

      opts.inSampleSize = calcSampleSize(opts, width, height);

      opts.inJustDecodeBounds = false;

      Bitmap image_base = BitmapFactory.decodeByteArray(image_data, 0, image_data.length, opts);
      Matrix matrix = getScaleMatrix(opts.outWidth, opts.outHeight, image_base.getWidth(), image_base.getHeight());

      return returnBlob(opts, image_base, matrix, width, height);
    }catch(NullPointerException e){
      return null;
    }
  }

  @Kroll.method
  public TiBlob imageAsResized(int width, int height, String path, int rotate){
    Activity activity = getTiContext().getActivity();
    AssetManager as = activity.getResources().getAssets();

    String fpath = null;
    String save_path = null;

    if(path.startsWith("file://") || path.startsWith("content://")){
      fpath = path;
      File save_path_base = new File(path);
      save_path = "camera/" + save_path_base.getName();
    }else{
      fpath = "Resources/" + path;
      save_path = path;
    }

    String toFile = "/data/data/"+ TiApplication.getInstance().getPackageName() +"/app_appdata/" + save_path;

    try{
      // File must be copied to /data/data. you can't handle files under Resouces dir.
      // Who knows? Not me.
      InputStream is = as.open(fpath);
      copyFile(is, toFile);

      // Load image file data, not image file it self.
      BitmapFactory.Options opts = new BitmapFactory.Options();
      opts.inJustDecodeBounds = true;

      BitmapFactory.decodeFile(toFile, opts);
  
      try{
        // Load image
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = calcSampleSize(opts, width, height);
        Bitmap image_base = BitmapFactory.decodeFile(toFile, opts);

        // Calc scale.
        int w = image_base.getWidth();
        int h = image_base.getHeight();

        Matrix matrix = getScaleMatrix(opts.outWidth, opts.outHeight, w, h);

        if(rotate > 0){
          matrix.postRotate(rotate);
        }

        // Voila!
        return returnBlob(opts, image_base, matrix, w, h);
      }catch(NullPointerException e){
        Log.w(LCAT, "Bitmap IOException:" + e);
        return null;
      }
    }catch(IOException e){
      Log.w(LCAT, "Bitmap IOException:" + e);
      return null;
    }
  }
  
  // Copy from inputstream to file
  private static void copyFile(InputStream input, String dstFilePath) throws IOException{
    File dstFile = new File(dstFilePath);
   
    String parent_dir = dstFile.getParent();
    File dir = new File(parent_dir);
    dir.mkdirs();
   
    OutputStream output = null;
    output = new FileOutputStream(dstFile);
   
    int DEFAULT_BUFFER_SIZE = 1024 * 4;
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
    }
    input.close();
    output.close();
  }

  private Matrix getScaleMatrix(int orig_w, int orig_h, int w, int h){
    float scale = Math.min((float)orig_w/w, (float)orig_h/h);
    Matrix matrix = new Matrix();
    matrix.postScale(scale, scale);
    return matrix;
  }

  private TiBlob returnBlob(BitmapFactory.Options opts, Bitmap image_base, Matrix matrix, int w, int h)
    throws NullPointerException{
    Bitmap scaled_image = Bitmap.createBitmap(image_base, 0, 0, w, h, matrix, true);
    TiBlob blob = TiBlob.blobFromImage(getTiContext(), scaled_image);
    image_base.recycle();
    image_base = null;
    scaled_image.recycle();
    scaled_image = null;
    return blob;
  }

  private int calcSampleSize(BitmapFactory.Options opts, int width, int height){
    int scaleW = Math.max(1, opts.outWidth / width);
    int scaleH = Math.max(1, opts.outHeight / height);
    int sampleSize = Math.max(scaleW, scaleH);
    return sampleSize;
  }
}


