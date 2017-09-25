package com.blueridgebinary.terra.utils;

import java.util.ArrayList;

// Class that holds an integer value and fires an onchange listener whenever its value is changed
public class ListenableBoolean {
        private Boolean mValue;
        private ArrayList<ChangeListener> mListeners;

        public ListenableBoolean(boolean someBoolean) {
                this.mValue = someBoolean;
                this.mListeners = new ArrayList<>();
        }

        public boolean getValue() {
            return mValue;
        }

        public void setValue(boolean someBoolean) {
            this.mValue = someBoolean;
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
