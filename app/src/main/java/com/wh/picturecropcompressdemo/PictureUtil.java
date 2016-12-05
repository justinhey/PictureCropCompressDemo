package com.wh.picturecropcompressdemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;


public class PictureUtil {
	
	private static String TAG = "PictureUtil";
	
	public static String compressImage(Bitmap srcBitmap, String targetPath, int reqWidth, int reqHeight, int quality)  {
		Log.i(TAG, "targetPath====" + targetPath);
	     
	     File outputFile = new File(targetPath);
	 	 try {
		 	 if (!outputFile.exists()) {
				 outputFile.getParentFile().mkdirs();
				 outputFile.createNewFile();
	    	 }else{
	    		 outputFile.delete();
	    	 }
		 	 
		 	 FileOutputStream out = new FileOutputStream(outputFile);
	         if(srcBitmap.compress(CompressFormat.JPEG, quality, out)){
			     out.close();  
			 }  
			 if(!srcBitmap.isRecycled()){  
				 srcBitmap.recycle();//记得释放资源，否则会内存溢出  
			 }  
	         return outputFile.getPath();
	         
	     } catch (Exception e){
	    	 e.printStackTrace();
	    	 return null;
	     } 
	 }
	
	 public static String compressImage(String srcPath, String targetPath, int reqWidth, int reqHeight, int quality)  {
		 Log.i(TAG, "srcPath====" + srcPath);
		 Log.i(TAG, "targetPath====" + targetPath);
	     
		 Bitmap bm = getSmallBitmap(srcPath, reqWidth, reqHeight);//获取一定尺寸的图片
	     
	     File outputFile = new File(targetPath);
	 	 try {
		 	 if (!outputFile.exists()) {
				 outputFile.getParentFile().mkdirs();
				 outputFile.createNewFile();
	    	 }else{
	    		 outputFile.delete();
	    	 }
		 	 
		 	 FileOutputStream fos = new FileOutputStream(outputFile);
	         if(bm.compress(CompressFormat.JPEG, quality, fos)){
			     fos.close();  
			 }  
			 if(!bm.isRecycled()){  
			     bm.recycle(); // 记得释放资源，否则可能会内存溢出  
			 }  
	         return outputFile.getPath();
	         
	     } catch (Exception e){
	    	 e.printStackTrace();
	    	 return null;
	     } 
	 }
	 
	 /**
	  * 根据路径获得图片信息并按比例压缩，返回bitmap
	  */
	public static Bitmap getSmallBitmap(Bitmap srcBitmap, int reqWidth, int reqHeight) {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		srcBitmap.compress(CompressFormat.JPEG, 95, bos);// 可以是CompressFormat.PNG

	     // 图片原始数据
	     byte[] byteArr = bos.toByteArray();
	     // 计算sampleSize
	     BitmapFactory.Options options = new BitmapFactory.Options();
	     options.inJustDecodeBounds = true;
	     // 调用方法后，option已经有图片宽高信息
	     BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length, options);
	     // 计算最相近缩放比例
	     options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	     options.inJustDecodeBounds = false;

	     return BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length, options);
	}	 
	 
	 /**
	  * 根据路径获得图片信息并按比例压缩，返回bitmap
	  */
	public static Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeight) {
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;//只解析图片边沿，获取宽高
	    BitmapFactory.decodeFile(filePath, options);
	    // 计算缩放比
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    // 完整解析图片返回bitmap
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(filePath, options);
	}
	
	/**
	 * 计算缩放比
	 * @param @param options
	 * @param @param reqWidth
	 * @param @param reqHeight
	 * @return int
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
           int reqWidth, int reqHeight) {
       final int height = options.outHeight;
       final int width = options.outWidth;
       int inSampleSize = 1;
       if (height > reqHeight || width > reqWidth) {
           final int heightRatio = Math.round((float) height / (float) reqHeight);
           final int widthRatio = Math.round((float) width / (float) reqWidth);
           inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
       }
       return inSampleSize;
   }
	
}
