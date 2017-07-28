package com.layer.ui.util;

import java.text.DateFormat;
import java.util.Date;

public interface LayerDateFormatter {
    String formatTimeDay(Date date);
    String formatTime(Date date, DateFormat timeFormat, DateFormat dateFormat);
}
