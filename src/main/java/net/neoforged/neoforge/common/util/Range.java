package net.neoforged.neoforge.common.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class Range<V extends Comparable<? super V>> implements Predicate<Object> {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private final Class<V> clazz;
    private final V min;
    private final V max;
    
    public Range(Class<V> clazz, V min, V max) {
        this.clazz = clazz;
        this.min = min;
        this.max = max;
    }
    
    public Class<? extends V> getClazz() {
        return clazz;
    }
    
    private boolean isNumber(Object other) {
        return Number.class.isAssignableFrom(clazz) && other instanceof Number;
    }
    
    @Override
    public boolean test(Object t) {
        if (isNumber(t)) {
            Number n = (Number) t;
            boolean result = ((Number) min).doubleValue() <= n.doubleValue() && n.doubleValue() <= ((Number) max).doubleValue();
            if (!result) {
                LOGGER.debug("Range value {} is not within its bounds {}-{}", n.doubleValue(), ((Number) min).doubleValue(), ((Number) max).doubleValue());            }
            return result;
        }
        if (!clazz.isInstance(t)) return false;
        V c = clazz.cast(t);
        
        boolean result = c.compareTo(min) >= 0 && c.compareTo(max) <= 0;
        if (!result) {
            LOGGER.debug("Range value {} is not within its bounds {}-{}", c, min, max);
        }
        return result;
    }
    
    public Object correct(Object value, Object def) {
        if (isNumber(value)) {
            Number n = (Number) value;
            return n.doubleValue() < ((Number) min).doubleValue() ? min : n.doubleValue() > ((Number) max).doubleValue() ? max : value;
        }
        if (!clazz.isInstance(value)) return def;
        V c = clazz.cast(value);
        return c.compareTo(min) < 0 ? min : c.compareTo(max) > 0 ? max : value;
    }
    
    @Override
    public String toString() {
        if (clazz == Integer.class) {
            if (max.equals(Integer.MAX_VALUE)) {
                return "> " + min;
            } else if (min.equals(Integer.MIN_VALUE)) {
                return "< " + max;
            }
        } // TODO add more special cases?
        return min + " ~ " + max;
    }
    
    public boolean overlaps(final Range<V> other) {
        if (clazz != other.clazz) return false;
        if (isNumber(min) && isNumber(other.min)) {
            return ((Number) min).doubleValue() <= ((Number) other.max).doubleValue() && ((Number) max).doubleValue() >= ((Number) other.min).doubleValue();
        }
        return min.compareTo(other.max) <= 0 && max.compareTo(other.min) >= 0;
    }
    
    public boolean contains(final V value) {
        if (isNumber(value)) {
            return ((Number) min).doubleValue() <= ((Number) value).doubleValue() && ((Number) max).doubleValue() >= ((Number) value).doubleValue();
        }
        return min.compareTo(value) <= 0 && max.compareTo(value) >= 0;
    }
    
    public Range<V> overlap(final Range<V> other) {
        if (!overlaps(other)) throw new IllegalArgumentException("Ranges do not overlap");
        if (clazz != other.clazz) throw new IllegalArgumentException("Ranges are not of the same type");
        if (isNumber(min) && isNumber(other.min)) {
            return new Range<>(clazz, most(min, other.min), least(max, other.max));
        }
        return new Range<>(clazz, most(min, other.min), least(max, other.max));
    }
    
    private V least(final V left, final V right) {
        return left.compareTo(right) < 0 ? left : right;
    }
    
    private V most(final V left, final V right) {
        return left.compareTo(right) > 0 ? left : right;
    }
    
    public V max() {
        return max;
    }
    
    public V min() {
        return min;
    }
}
