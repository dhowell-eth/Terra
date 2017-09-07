package com.blueridgebinary.terra.utils;

import java.util.ArrayList;

// Class that holds an integer value and fires an onchange listener whenever its value is changed
public class ListenableInteger {
        private Integer mValue;
        private ArrayList<ChangeListener> mListeners;

        public ListenableInteger(int someInteger) {
                this.mValue = someInteger;
                this.mListeners = new ArrayList<>();
        }

        public int getValue() {
            return mValue;
        }

        public void setValue(int someInteger) {
            this.mValue = someInteger;
            if (mListeners != null) {
                for(ChangeListener listener : mListeners) {
                    listener.onChange();
                }
            }
        }

        public ArrayList<ChangeListener> getListeners() {
            return mListeners;
        }

        public void addListener(ChangeListener listener) {
            this.mListeners.add(listener);
        }

        public interface ChangeListener {
            void onChange();
        }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
