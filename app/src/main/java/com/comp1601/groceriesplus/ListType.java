package com.comp1601.groceriesplus;

public enum ListType {
    USER_CREATED {
        @Override
        int getIntValue() {
            return 0;
        };
    },
    GENERATED {
        @Override
        int getIntValue() {
            return 1;
        };
    },
    SUGGESTED {
        @Override
        int getIntValue() {
            return 2;
        };
    };

    abstract int getIntValue();

    public static ListType getType(int i) {
        if (i == 0) {
            return USER_CREATED;
        }
        else if (i == 1) {
            return GENERATED;
        }
        else {
            return SUGGESTED;
        }
    }
}
