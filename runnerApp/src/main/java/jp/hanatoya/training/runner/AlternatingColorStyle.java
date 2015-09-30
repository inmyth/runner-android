package jp.hanatoya.training.runner;

import android.graphics.Color;

import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.ValueDependentColor;

public class AlternatingColorStyle implements ValueDependentColor {

	static int orange = Color.parseColor("#dc4f03");
	static int yellow = Color.parseColor("#f6ab00");

	@Override
	public int get(GraphViewDataInterface data, int index) {
		if (index % 2 != 0) {
			return yellow;
		}
		return orange;
	}

}
