package com.example.javanewwisebandit.goldentime_v1.Model;

public class TimeLineModel {
        public String text;
        public String time;
        public String content;
        public boolean isHead;
        public TimeLineModel(boolean isHead, String text, String time, String content) {
            this.isHead = isHead;
            this.text = text;
            this.time = time;
            this.content = content;
        }
}
