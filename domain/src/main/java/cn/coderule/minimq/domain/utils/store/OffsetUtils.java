package cn.coderule.minimq.domain.utils.store;

import cn.coderule.common.util.lang.string.StringUtil;
import java.text.NumberFormat;

public class OffsetUtils {

    public static String offsetToFileName(long offset) {
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(20);
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(false);
        return nf.format(offset);
    }

    public static long fileNameToOffset(String fileName) {

        if (StringUtil.isBlank(fileName)) {
            return 0;
        }

        int length = fileName.length();
        if (length < 20) {
            return 0;
        }

        String numStr = fileName.substring(length - 20);
        return Long.parseLong(fileName);
    }
}
