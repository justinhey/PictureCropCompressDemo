package com.wh.picturecropcompressdemo;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DialogUtil {

	private static String parseParam(Context context, Object param) {
		if (param instanceof Integer) {
			return context.getString((Integer) param);
		} else if (param instanceof String) {
			return param.toString();
		}
		return null;
	}

	public static Dialog createDialog(Context context, Object titleText,
			Object contentText, final Object leftText, final Object rightText,
			final MyDialogListener listener, boolean force) {
		View view = LayoutInflater.from(context).inflate(R.layout.dialog, null);

		final Dialog dialog = new Dialog(context, R.style.MyDialog);
		dialog.show();
		dialog.setContentView(view);

		TextView title = (TextView) view.findViewById(R.id.dialog_title);
		title.setText(parseParam(context, titleText));

		TextView content = (TextView) view.findViewById(R.id.dialog_content);

		if (contentText instanceof View) {
			ViewGroup parent = (ViewGroup) content.getParent();
			parent.removeView(content);
			parent.addView((View) contentText);

		} else {
			content.setText(parseParam(context, contentText));
		}

		Button left = (Button) view.findViewById(R.id.left);
		if (leftText == null) {
			ViewGroup btnsLayout = (ViewGroup) left.getParent();
			ViewGroup parent = (ViewGroup) btnsLayout.getParent();
			parent.removeView(btnsLayout);

		} else {
			left.setText(parseParam(context, leftText));
			left.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					listener.onClick(dialog, (Button) v, leftText);
				}
			});

			Button right = (Button) view.findViewById(R.id.right);
			if (rightText == null) {
				ViewGroup parent = (ViewGroup) right.getParent();
				parent.removeView(right);
			} else {
				right.setText(parseParam(context, rightText));

				if (force) {
					right.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							listener.onClick(dialog, (Button) v, rightText);
						}
					});
				} else {
					right.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}
			}
		}
		return dialog;
	}

	public static Dialog createDialog(Context context, Object titleText,
			Object contentText, final Object leftText, final Object rightText,
			final MyDialogListener listener) {
		return createDialog(context, titleText, contentText, leftText,
				rightText, listener, false);
	}

	public static Dialog createDialog(Context context, Object titleText,
			Object contentText) {
		return createDialog(context, titleText, contentText, null, null, null,
				false);
	}

	public static Dialog createDialog(Context context, Object titleText,
			Object contentText, final Object btnText,
			final MyDialogListener listener) {
		return createDialog(context, titleText, contentText, btnText, null,
				listener, false);
	}

	public interface MyDialogListener {
		void onClick(Dialog dialog, Button btn, Object btnKey);
	}
}
