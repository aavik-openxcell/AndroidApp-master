package com.icanvass.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public abstract class NMDialog extends Dialog {

	private ViewGroup contentView;

	public NMDialog(Context context) {
		super(context);
	}

	public NMDialog(Context context, int theme) {
		super(context, theme);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		contentView = (ViewGroup) getWindow().getDecorView().findViewById(
				android.R.id.content);
		super.onCreate(savedInstanceState);
		initData();
		initRootView();
		View rootView = ((ViewGroup) contentView.getChildAt(0));
		initUIComponents(rootView);
		setListeners();
		loadData();
	}

	protected abstract void initData();

	protected abstract void initRootView();

	protected abstract void initUIComponents(View rootView);

	protected abstract void setListeners();

	protected abstract void loadData();

	public void setBackground(int color) {
		contentView.setBackgroundColor(color);
		contentView.invalidate();
	}

}
