package com.trading.roulette.api.utils;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.annotation.Tag;

public final class TagUtil {
    private TagUtil() {
    }

    public static boolean isMarked(Class<? extends IStrategy> clazz, Tag tag) {
        Strategy annotation = clazz.getAnnotation(Strategy.class);
        if (annotation == null) {
            return false;
        }
        Tag[] tags = annotation.tags();
        if (tags == null) {
            return false;
        }
        for (Tag item : tags) {
            if (item.equals(tag)) {
                return true;
            }
        }
        return false;
    }
}
