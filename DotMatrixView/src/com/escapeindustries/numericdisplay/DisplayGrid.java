package com.escapeindustries.numericdisplay;

import java.util.ArrayList;
import java.util.List;

import com.escapeindustries.numericdisplay.R;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class DisplayGrid implements Grid {
	private int columns;
	private int rows;
	private int paddingRowsTop;
	private int paddingColumnsLeft;
	private int paddingRowsBottom;
	private int paddingColumnsRight;
	private ViewGroup grid;
	private Context ctx;
	private int dotPadding;
	private int dotSize;

	private Digit[] digits;
	private Glyph[] glyphs;

	public DisplayGrid(Context ctx) {
		this.ctx = ctx;
		this.dotSize = (int) ctx.getResources().getDimension(R.dimen.dot_size);
		this.dotPadding = (int) ctx.getResources().getDimension(
				R.dimen.dot_padding);
	}

	@Override
	public int getColumns() {
		return columns;
	}

	@Override
	public void setColumns(int columns) {
		this.columns = columns;
	}

	@Override
	public int getRows() {
		return rows;
	}

	@Override
	public void setRows(int rows) {
		this.rows = rows;
	}

	@Override
	public int getPaddingRowsTop() {
		return paddingRowsTop;
	}

	@Override
	public int getPaddingColumnsLeft() {
		return paddingColumnsLeft;
	}

	@Override
	public int getPaddingRowsBottom() {
		return paddingRowsBottom;
	}

	@Override
	public int getPaddingColumnsRight() {
		return paddingColumnsRight;
	}

	@Override
	public void setPaddingDots(int top, int left, int bottom, int right) {
		this.paddingRowsTop = top;
		this.paddingColumnsLeft = left;
		this.paddingRowsBottom = bottom;
		this.paddingColumnsRight = right;
	}
	
	public void setFormat(String format) {
		GlyphFactory factory = new GlyphFactory(this);
		FormatStringParser parser = new FormatStringParser(factory);
		glyphs = parser.parse(format);
		digits = extractDigits(glyphs);
		int gridHeight = 0;
		int column = getPaddingColumnsLeft();
		for (Glyph glyph : glyphs) {
			glyph.setColumn(column);
			glyph.setRow(getPaddingRowsTop());
			column += glyph.getWidth();
			gridHeight = Math.max(gridHeight, glyph.getHeight());
		}
		setColumns(column);
		setRows(gridHeight);
		build();
	}

	public void setViewHolder(ViewGroup gridHolder) {
		gridHolder.addView(grid);
		for (Glyph glyph : glyphs) {
			glyph.draw();
		}
	}

	@Override
	public void setValue(String value) {
		DigitsParser parser = new DigitsParser();
		int[] values = parser.parse(value);
		int digitsOffset = digits.length - values.length;
		int valuesOffset = 0;
		if (digitsOffset < 0) {
			valuesOffset = digitsOffset * -1;
			digitsOffset = 0;
		}
		int limit = Math.min(digits.length, values.length);
		for (int i = 0; i < limit; i++) {
			digits[i + digitsOffset].setNumber(values[i + valuesOffset]);
		}
	}

	public void build() {
		grid = new LinearLayout(ctx);
		((LinearLayout) grid).setOrientation(LinearLayout.VERTICAL);
		setLayoutWrapContent(grid);
		for (int y = 0; y < rows + paddingRowsTop + paddingRowsBottom; y++) {
			grid.addView(buildRow());
		}
	}

	@Override
	public void changeDot(int x, int y, boolean on) {
		ImageView dot;
		dot = getDot(x, y);
		Animation anim = AnimationUtils.loadAnimation(ctx, on ? R.anim.appear
				: R.anim.vanish);
		anim.setAnimationListener(new DotAnimationListener(dot, on));
		dot.startAnimation(anim);
	}

	private ImageView getDot(int x, int y) {
		// TODO: this will crash if the column and row origin mean that
		// the dot is off the grid.
		Log.d("NumericDisplay", "getDot(" + x + " [max: " + getColumns()
				+ "], " + y + " [max: " + getRows() + "])");
		ViewGroup rowGroup = (ViewGroup) grid.getChildAt(y);
		ViewGroup dotStack = (ViewGroup) rowGroup.getChildAt(x);
		return (ImageView) dotStack.getChildAt(1);
	}

	private LinearLayout buildRow() {
		LinearLayout row = new LinearLayout(ctx);
		setLayoutWrapContent(row);
		row.setOrientation(LinearLayout.HORIZONTAL);
		for (int x = 0; x < columns + paddingColumnsLeft + paddingColumnsRight; x++) {
			row.addView(buildDotStack());
		}
		return row;
	}

	private FrameLayout buildDotStack() {
		FrameLayout frame = new FrameLayout(ctx);
		setLayoutWrapContent(frame);
		ImageView dot = new ImageView(ctx);
		setLayoutFromDimens(dot);
		dot.setImageResource(R.drawable.dot_dim);
		frame.addView(dot);
		dot = new ImageView(ctx);
		setLayoutFromDimens(dot);
		dot.setImageResource(R.drawable.dot_dim);
		frame.addView(dot);
		return frame;
	}

	private void setLayoutWrapContent(View view) {
		view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}

	private void setLayoutFromDimens(View view) {
		view.setLayoutParams(new LayoutParams(dotSize, dotSize));
		view.setPadding(dotPadding, dotPadding, dotPadding, dotPadding);
	}

	private Digit[] extractDigits(Glyph[] glyphs) {
		List<Digit> digits = new ArrayList<Digit>();
		for (int i = 0; i < glyphs.length; i++) {
			if (glyphs[i] instanceof Digit) {
				digits.add((Digit) glyphs[i]);
			}
		}
		return digits.toArray(new Digit[digits.size()]);
	}
}