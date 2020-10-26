package com.naofi.compiler.binding.symbols;

import java.util.Arrays;

public enum VariableType {
    BYTE("B") {
        @Override
        public boolean fits(long value) {
            return inRange(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
    },
    SHORT("S") {
        @Override
        public boolean fits(long value) {
            return inRange(value, Short.MIN_VALUE, Short.MAX_VALUE);
        }
    },
    INT("I") {
        @Override
        public boolean fits(long value) {
            return inRange(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
    },
    LONG("J") {
        @Override
        public boolean fits(long value) {
            return inRange(value, Long.MIN_VALUE, Long.MAX_VALUE);
        }
    },
    CHAR("C") {
        @Override
        public boolean fits(long value) {
            return inRange(value, Character.MIN_VALUE, Character.MAX_VALUE);
        }
    },
    FLOAT("F") {
        @Override
        public boolean fits(long value) {
            return true;
        }
    },
    DOUBLE("D") {
        @Override
        public boolean fits(long value) {
            return true;
        }
    },
    UNDEFINED("V") {
        @Override
        public boolean fits(long value) {
            return true;
        }
    };

    public static VariableType of(String typeName) {
        return valueOf(typeName.toUpperCase());
    }

    public static VariableType fitsRange(long val) {
        return Arrays.stream(values())
                .filter(t -> t.fits(val))
                .findFirst()
                .orElse(UNDEFINED);
    }

    public static VariableType max(VariableType a, VariableType b) {
        return (a.ordinal() > b.ordinal()) ? a : b;
    }

    private final String descriptor;

    VariableType(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public boolean isAssignableFrom(VariableType other) {
        return ordinal() >= other.ordinal();
    }

    /**
     * Check if value is in type range
     */
    public abstract boolean fits(long value);

    boolean inRange(long val, long from, long to) {
        return (val >= from) && (val <= to);
    }
}
