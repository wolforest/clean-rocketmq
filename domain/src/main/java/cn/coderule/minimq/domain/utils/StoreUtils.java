package cn.coderule.minimq.domain.utils;

import java.text.NumberFormat;

public class StoreUtils {

    public static String offsetToFileName(long offset) {
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(20);
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(false);
        return nf.format(offset);
    }
}
