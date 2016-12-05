package com.wh.picturecropcompressdemo;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

// As中不能用假png
public class MainActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private static final String TAG = "MainActivity";
	private static final int CAMERA = 0; // 跳到拍照界面的请求码
	private static final int ALBUM = 1; // 跳到相册界面的请求码
	private static final int CODE_CAMERA = 0;
	private static final int CODE_ALBUM = 1;
	private static final int CODE_CROP = 2; // 跳到裁剪界面的请求码
	private ImageView mPicture;
	private Context mContext;
	private Uri mPictureUri;
	private Dialog mPictureDialog;
	private View mMaskLayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext = this;

		initView();

		initListener();

	}

	private void initView() {
		mMaskLayer = findViewById(R.id.mask_layer);
		mPicture = (ImageView) findViewById(R.id.img_picture);
	}

	private void initListener() {
		mPicture.setOnClickListener(this);
		mMaskLayer.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_picture:
			initSelectDialog();
			break;

		case R.id.mask_layer:
			// 什么多不做，只是为了在上传图像的过程中让遮罩层把事件拦截下来，使别的按钮失效以避免误触
			break;

		default:
			break;
		}
	}

	private void initSelectDialog() {
		ListView listview = (ListView) getLayoutInflater().inflate(
				R.layout.dialog_list_view, null);
		listview.setOnItemClickListener(this);
		String[] options = getResources().getStringArray(R.array.picture_style);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
				R.layout.item_options, options);
		listview.setAdapter(adapter);
		mPictureDialog = DialogUtil.createDialog(mContext,
				R.string.select_head, listview);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case CAMERA:
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			String outCameraPhotoName = generateFileNameByTime();
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					mPictureUri = getUri(outCameraPhotoName));
			startActivityForResult(intent, CODE_CAMERA);
			break;

		case ALBUM:
			Intent intent2 = new Intent(
					Intent.ACTION_PICK,
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent2, CODE_ALBUM);
			break;

		default:
			break;
		}
		mPictureDialog.dismiss();
	}

	private String generateFileNameByTime() {
		return System.currentTimeMillis() + ".jpg";
	}

	private Uri getUri(String avatarName) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String sdPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			// imagesPath: /mnt/sdcard/myImages
			String imagesPath = sdPath + File.separator + "myImages";
			File imagesFile = new File(imagesPath);
			if (!imagesFile.exists()) {
				imagesFile.mkdirs();
			}
			//File newAvatarDirFile = imagesFile;
			String fileName = avatarName;
			return Uri.fromFile(new File(imagesPath, fileName));
		} else {
			Toast.makeText(mContext, "内存卡不存在", Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CODE_CAMERA) {

			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(mPictureUri, "image/*"); // 需要裁减的图片
			// 裁剪比例
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri); // 裁剪后的输出
			startActivityForResult(intent, CODE_CROP);

		} else if (requestCode == CODE_ALBUM) {

			Uri selectedImage = data.getData();
			Log.e(TAG, "图片Uri" + selectedImage.toString());

			String[] filePathColumns = { MediaStore.Images.Media.DATA };
			Cursor c = mContext.getContentResolver().query(selectedImage,
					filePathColumns, null, null, null);
			c.moveToFirst();
			int columnIndex = c.getColumnIndex(filePathColumns[0]);
			String picturePath = c.getString(columnIndex);// 取出图片路径
			Log.e(TAG, "图片路径" + picturePath);
			c.close();

			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(selectedImage, "image/*"); // 需要裁减的图片
			// 裁剪比例
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			String newAvatarName = generateFileNameByTime();
			mPictureUri = getUri(newAvatarName);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri); // 裁剪后的输出
			startActivityForResult(intent, CODE_CROP);

		} else if (requestCode == CODE_CROP) {

			String chosenAvatarPath = mPictureUri.getPath();
			Bitmap avatarBitmap = PictureUtil.getSmallBitmap(chosenAvatarPath,
					200, 200);
			if (avatarBitmap != null) {
				mPicture.setImageBitmap(avatarBitmap);
			}

			String newAvatarName = generateFileNameByTime();
			String newAvatarPath = getUri(newAvatarName).getPath();
			String[] params = new String[] { chosenAvatarPath, newAvatarPath };
			new CompressImageTask().execute(params);
		}
	}

	private class CompressImageTask extends AsyncTask<String, Void, Boolean> {

		private String srcPath;
		private String targetPath;

		@Override
		protected void onPreExecute() {
			setMaskLayerState(true);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			srcPath = params[0];
			targetPath = params[1];

			try {
				Thread.sleep(1000); // 延时效果
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			String compressImagePath = PictureUtil.compressImage(srcPath,
					targetPath, 200, 200, 100);
			return compressImagePath != null ? true : false;
		}

		@Override
		protected void onPostExecute(Boolean isSuccess) {
			setMaskLayerState(false);
			if (isSuccess) {
				Toast.makeText(mContext,
						getString(R.string.processing_success),
						Toast.LENGTH_SHORT).show();
				Log.i(TAG, "compressImagePath====" + targetPath);
				// TODO 是否要删除上传过的本地图片
			} else {
				Toast.makeText(mContext, getString(R.string.processing_failed),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void setMaskLayerState(boolean isVisible) {
		if (isVisible) {
			mMaskLayer.setVisibility(View.VISIBLE);
		} else {
			mMaskLayer.setVisibility(View.GONE);
		}
	}

}
